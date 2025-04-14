package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import android.os.Build
import android.system.ErrnoException
import android.system.Os
import android.util.ArrayMap
import android.util.Log
import androidx.annotation.CallSuper
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.ErrorActivity
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_X86
import com.movtery.zalithlauncher.utils.device.Architecture.is64BitsDevice
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.getDisplayFriendlyRes
import com.movtery.zalithlauncher.utils.string.StringUtils
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import com.oracle.dalvik.VMLauncher
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.util.TimeZone

abstract class Launcher {
    private var dirNameHomeJre: String = "lib"
    private var ldLibraryPath: String = ""
    private var jvmLibraryPath: String = ""

    abstract suspend fun launch()
    abstract fun chdir(): String

    protected fun launchJvm(
        activity: Activity,
        runtime: Runtime,
        jvmArgs: List<String>,
        userArgs: String
    ) {
        val runtimeHome = RuntimesManager.getRuntimeHome(runtime.name).absolutePath
        relocateLibPath(runtime, runtimeHome)
        initLdLibraryPath(runtimeHome)
        setEnv(runtimeHome, runtime)
        initJavaRuntime(runtimeHome)
        initSoundEngine()
        launchJavaVM(activity, runtimeHome, jvmArgs, userArgs)
    }

    private fun launchJavaVM(
        activity: Activity,
        runtimeHome: String,
        jvmArgs: List<String>,
        userArgs: String
    ) {
        val args = getJavaArgs(runtimeHome, userArgs).toMutableList()
        progressFinalUserArgs(args)

        args.addAll(jvmArgs)

        val iterator = args.iterator()
        while (iterator.hasNext()) {
            val arg = iterator.next()
            if (arg.startsWith("--accessToken") && iterator.hasNext()) {
                iterator.next()
                continue
            }
            Logger.appendToLog("JVMArg: $arg")
        }

        ZLBridge.setupExitMethod(activity.application)
        ZLBridge.initializeGameExitHook()
        ZLBridge.chdir(chdir())

        args.add(0, "java") //argv[0] is the program name according to C standard.

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        Logger.appendToLog("Java Exit code: $exitCode")
        if (exitCode != 0) {
            ErrorActivity.showExitMessage(activity, exitCode, false)
        }
    }

    protected open fun progressFinalUserArgs(args: MutableList<String>) {
        args.purgeArg("-Xms")
        args.purgeArg("-Xmx")
        args.purgeArg("-d32")
        args.purgeArg("-d64")
        args.purgeArg("-Xint")
        args.purgeArg("-XX:+UseTransparentHugePages")
        args.purgeArg("-XX:+UseLargePagesInMetaspace")
        args.purgeArg("-XX:+UseLargePages")
        args.purgeArg("-Dorg.lwjgl.opengl.libname")
        // Don't let the user specify a custom Freetype library (as the user is unlikely to specify a version compiled for Android)
        args.purgeArg("-Dorg.lwjgl.freetype.libname")
        // Overridden by us to specify the exact number of cores that the android system has
        args.purgeArg("-XX:ActiveProcessorCount")

        args.add("-javaagent:${LibPath.MIO_LIB_PATCHER.absolutePath}")

        //Add automatically generated args
        args.add("-Xms1024M") //TODO 内存
        args.add("-Xmx1024M") //TODO 内存

        // Force LWJGL to use the Freetype library intended for it, instead of using the one
        // that we ship with Java (since it may be older than what's needed)
        args.add("-Dorg.lwjgl.freetype.libname=${PathManager.DIR_NATIVE_LIB}/libfreetype.so")

        // Some phones are not using the right number of cores, fix that
        args.add("-XX:ActiveProcessorCount=${java.lang.Runtime.getRuntime().availableProcessors()}")
    }

    private fun MutableList<String>.purgeArg(argStart: String) {
        removeIf { arg: String -> arg.startsWith(argStart) }
    }

    private fun relocateLibPath(runtime: Runtime, jreHome: String) {
        var jreArchitecture = runtime.arch
        if (Architecture.archAsInt(jreArchitecture) == ARCH_X86) {
            jreArchitecture = "i386/i486/i586"
        }

        for (arch in jreArchitecture.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val f = File(jreHome, "lib/$arch")
            if (f.exists() && f.isDirectory) {
                dirNameHomeJre = "lib/$arch"
            }
        }

        val libName = if (is64BitsDevice) "lib64" else "lib"
        val ldLibraryPath = java.lang.StringBuilder()
        //TODO FFMPEG插件
//        if (FFmpegPlugin.isAvailable) {
//            ldLibraryPath.append(FFmpegPlugin.libraryPath).append(":")
//        }
        val customRenderer = RendererPluginManager.selectedRendererPlugin
        if (customRenderer != null) {
            ldLibraryPath.append(customRenderer.path).append(":")
        }
        ldLibraryPath.append(jreHome)
            .append("/").append(dirNameHomeJre)
            .append("/jli:").append(jreHome).append("/").append(dirNameHomeJre)
            .append(":")
        ldLibraryPath.append("/system/").append(libName).append(":")
            .append("/vendor/").append(libName).append(":")
            .append("/vendor/").append(libName).append("/hw:")
            .append(PathManager.DIR_NATIVE_LIB)
        this.ldLibraryPath = ldLibraryPath.toString()
    }

    private fun initLdLibraryPath(jreHome: String) {
        val serverFile = File(jreHome).child(dirNameHomeJre, "server", "libjvm.so")
        jvmLibraryPath = "$jreHome/$dirNameHomeJre/" + (if (serverFile.exists()) "server" else "client")
        Log.d("DynamicLoader", "Base ldLibraryPath: $ldLibraryPath")
        Log.d("DynamicLoader", "Internal ldLibraryPath: $jvmLibraryPath:$ldLibraryPath")
        ZLBridge.setLdLibraryPath("$jvmLibraryPath:$ldLibraryPath")
    }

    protected fun findInLdLibPath(libName: String): String {
        val path = Os.getenv("LD_LIBRARY_PATH") ?: run {
            try {
                if (ldLibraryPath.isNotEmpty()) {
                    Os.setenv("LD_LIBRARY_PATH", ldLibraryPath, true)
                }
            } catch (e: ErrnoException) {
                Log.e("Launcher", StringUtils.throwableToString(e))
            }
            ldLibraryPath
        }
        return path.split(":").find { libPath ->
            val file = File(libPath, libName)
            file.exists() && file.isFile
        }?.let {
            File(it, libName).absolutePath
        } ?: libName
    }

    private fun locateLibs(path: File): List<File> {
        val children = path.listFiles() ?: return emptyList()
        return children.flatMap { file ->
            when {
                file.isFile && file.name.endsWith(".so") -> listOf(file)
                file.isDirectory -> locateLibs(file)
                else -> emptyList()
            }
        }
    }

    private fun setEnv(jreHome: String, runtime: Runtime) {
        val envMap = initEnv(jreHome, runtime)
        envMap.forEach { (key, value) ->
            Logger.appendToLog("Added env: $key = $value")
            runCatching {
                Os.setenv(key, value, true)
            }.onFailure {
                Log.e("Launcher", it.getMessageOrToString())
            }
        }
    }

    @CallSuper
    protected open fun initEnv(jreHome: String, runtime: Runtime): MutableMap<String, String> {
        val envMap: MutableMap<String, String> = ArrayMap()
        setJavaEnv(envMap = { envMap }, jreHome = jreHome)
        return envMap
    }

    private fun setJavaEnv(envMap: () -> MutableMap<String, String>, jreHome: String) {
        envMap().let { map ->
            map["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
//            map["DRIVER_PATH"] = //TODO 驱动器
            map["JAVA_HOME"] = jreHome
            map["HOME"] = PathManager.DIR_FILES_EXTERNAL.absolutePath
            map["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
            map["LD_LIBRARY_PATH"] = ldLibraryPath
            map["PATH"] = "$jreHome/bin:${Os.getenv("PATH")}"
            map["AWTSTUB_WIDTH"] = (CallbackBridge.windowWidth.takeIf { it > 0 } ?: CallbackBridge.physicalWidth).toString()
            map["AWTSTUB_HEIGHT"] = (CallbackBridge.windowHeight.takeIf { it > 0 } ?: CallbackBridge.physicalHeight).toString()

            //TODO 更多设置
            //        if (AllSettings.getDumpShaders().getValue())
            //            envMap.put("LIBGL_VGPU_DUMP", "1");
            //        if (AllSettings.getZinkPreferSystemDriver().getValue())
            //            envMap.put("POJAV_ZINK_PREFER_SYSTEM_DRIVER", "1");
            //        if (AllSettings.getVsyncInZink().getValue())
            //            envMap.put("POJAV_VSYNC_IN_ZINK", "1");
            //        if (AllSettings.getBigCoreAffinity().getValue())
            //            envMap.put("POJAV_BIG_CORE_AFFINITY", "1");
            //        if (FFmpegPlugin.isAvailable)
            //            envMap.put("POJAV_FFMPEG_PATH", FFmpegPlugin.executablePath);
        }
    }

    private fun initJavaRuntime(jreHome: String) {
        ZLBridge.dlopen(findInLdLibPath("libjli.so"))
        if (!ZLBridge.dlopen("libjvm.so")) {
            Log.w("DynamicLoader", "Failed to load with no path, trying with full path")
            ZLBridge.dlopen("$jvmLibraryPath/libjvm.so")
        }
        ZLBridge.dlopen(findInLdLibPath("libverify.so"))
        ZLBridge.dlopen(findInLdLibPath("libjava.so"))
        ZLBridge.dlopen(findInLdLibPath("libnet.so"))
        ZLBridge.dlopen(findInLdLibPath("libnio.so"))
        ZLBridge.dlopen(findInLdLibPath("libawt.so"))
        ZLBridge.dlopen(findInLdLibPath("libawt_headless.so"))
        ZLBridge.dlopen(findInLdLibPath("libfreetype.so"))
        ZLBridge.dlopen(findInLdLibPath("libfontmanager.so"))
        locateLibs(File(jreHome, dirNameHomeJre)).forEach { file ->
            ZLBridge.dlopen(file.absolutePath)
        }
    }

    @CallSuper
    protected open fun initSoundEngine() {
        ZLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
    }

    companion object {
        fun redirectAndPrintJRELog() {
            Log.v("jrelog", "Log starts here")
            Thread(object : Runnable {
                var failTime: Int = 0
                var logcatPb: ProcessBuilder? = null
                override fun run() {
                    try {
                        if (logcatPb == null) {
                            logcatPb = ProcessBuilder().command("logcat", "-v", "brief", "-s", "jrelog:I", "LIBGL:I", "NativeInput").redirectErrorStream(true)
                        }

                        Log.i("jrelog-logcat", "Clearing logcat")
                        ProcessBuilder().command("logcat", "-c").redirectErrorStream(true).start()
                        Log.i("jrelog-logcat", "Starting logcat")
                        val p = logcatPb!!.start()

                        val buf = ByteArray(1024)
                        var len: Int
                        while ((p.inputStream.read(buf).also { len = it }) != -1) {
                            val currStr = String(buf, 0, len)
                            Logger.appendToLog(currStr)
                        }

                        if (p.waitFor() != 0) {
                            Log.e("jrelog-logcat", "Logcat exited with code " + p.exitValue())
                            failTime++
                            Log.i(
                                "jrelog-logcat",
                                (if (failTime <= 10) "Restarting logcat" else "Too many restart fails") + " (attempt " + failTime + "/10"
                            )
                            if (failTime <= 10) {
                                run()
                            } else {
                                Logger.appendToLog("ERROR: Unable to get more Logging.")
                            }
                        }
                    } catch (e: Throwable) {
                        Log.e("jrelog-logcat", "Exception on logging thread", e)
                        Logger.appendToLog("Exception on logging thread:\n${Log.getStackTraceString(e)}")
                    }
                }
            }).start()
            Log.i("jrelog-logcat", "Logcat thread started")
        }

        /**
         * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/98947f2/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/JREUtils.java#L345-L401)
         */
        fun getJavaArgs(runtimeHome: String, userArgumentsString: String): List<String> {
            val userArguments = parseJavaArguments(userArgumentsString).toMutableList()
            val resolvFile = File(PathManager.DIR_FILES_PRIVATE.parent, "resolv.conf").absolutePath

            val overridableArguments = listOf(
                "-Djava.home=$runtimeHome",
                "-Djava.io.tmpdir=${PathManager.DIR_CACHE.absolutePath}",
                "-Djna.boot.library.path=${PathManager.DIR_NATIVE_LIB}",
                "-Duser.home=${GamePathManager.currentPath}",
                "-Duser.language=${System.getProperty("user.language")}",
                "-Dos.name=Linux",
                "-Dos.version=Android-${Build.VERSION.RELEASE}",
                "-Dpojav.path.minecraft=${getGameHome()}",
                "-Dpojav.path.private.account=${PathManager.DIR_ACCOUNT}",
                "-Duser.timezone=${TimeZone.getDefault().id}",
                "-Dorg.lwjgl.vulkan.libname=libvulkan.so",
                "-Dglfwstub.windowWidth=${
                    getDisplayFriendlyRes(
                        ZLApplication.DISPLAY_METRICS.widthPixels,
                        1f //TODO 缩放
                    )
                }",
                "-Dglfwstub.windowHeight=${
                    getDisplayFriendlyRes(
                        ZLApplication.DISPLAY_METRICS.heightPixels,
                        1f //TODO 缩放
                    )
                }",
                "-Dglfwstub.initEgl=false",
                "-Dext.net.resolvPath=$resolvFile",
                "-Dlog4j2.formatMsgNoLookups=true",
                "-Dnet.minecraft.clientmodname=${InfoDistributor.LAUNCHER_NAME}",
                "-Dfml.earlyprogresswindow=false",
                "-Dloader.disable_forked_guis=true",
                "-Djdk.lang.Process.launchMechanism=FORK",
                "-Dsodium.checks.issue2561=false"
            )

            val additionalArguments = overridableArguments.filter { arg ->
                val stripped = arg.substringBefore('=')
                val overridden = userArguments.any { it.startsWith(stripped) }
                if (overridden) {
                    Log.i("ArgProcessor", "Arg skipped: $arg")
                }
                !overridden
            }

            userArguments += additionalArguments
            return userArguments
        }

        /**
         * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/98947f2/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/JREUtils.java#L411-L456)
         */
        fun parseJavaArguments(args: String): List<String> {
            val parsedArguments = mutableListOf<String>()
            var cleanedArgs = args.trim().replace(" ", "")
            val separators = listOf("-XX:-", "-XX:+", "-XX:", "--", "-D", "-X", "-javaagent:", "-verbose")

            for (prefix in separators) {
                while (true) {
                    val start = cleanedArgs.indexOf(prefix)
                    if (start == -1) break

                    val end = separators
                        .mapNotNull { sep ->
                            val i = cleanedArgs.indexOf(sep, start + prefix.length)
                            if (i != -1) i else null
                        }
                        .minOrNull() ?: cleanedArgs.length

                    val parsedSubstring = cleanedArgs.substring(start, end)
                    cleanedArgs = cleanedArgs.replace(parsedSubstring, "")

                    if (parsedSubstring.indexOf('=') == parsedSubstring.lastIndexOf('=')) {
                        val last = parsedArguments.lastOrNull()
                        if (last != null && (last.endsWith(',') || parsedSubstring.contains(','))) {
                            parsedArguments[parsedArguments.lastIndex] = last + parsedSubstring
                        } else {
                            parsedArguments.add(parsedSubstring)
                        }
                    } else {
                        Log.w("JAVA ARGS PARSER", "Removed improper arguments: $parsedSubstring")
                    }
                }
            }

            return parsedArguments
        }

        fun getCacioJavaArgs(
            isJava8: Boolean,
            widthPixels: Int,
            heightPixels: Int
        ): List<String> {
            val argsList: MutableList<String> = ArrayList()

            // Caciocavallo config AWT-enabled version
            argsList.add("-Djava.awt.headless=false")
            argsList.add("-Dcacio.managed.screensize=" + (widthPixels * 0.8).toInt() + "x" + (heightPixels * 0.8).toInt())
            argsList.add("-Dcacio.font.fontmanager=sun.awt.X11FontManager")
            argsList.add("-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler")
            argsList.add("-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel")
            if (isJava8) {
                argsList.add("-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit")
                argsList.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment")
            } else {
                argsList.add("-Dawt.toolkit=com.github.caciocavallosilano.cacio.ctc.CTCToolkit")
                argsList.add("-Djava.awt.graphicsenv=com.github.caciocavallosilano.cacio.ctc.CTCGraphicsEnvironment")
                argsList.add("-Djava.system.class.loader=com.github.caciocavallosilano.cacio.ctc.CTCPreloadClassLoader")

                argsList.add("--add-exports=java.desktop/java.awt=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.awt.event=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED")
                argsList.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED")
                argsList.add("--add-exports=java.base/sun.security.action=ALL-UNNAMED")
                argsList.add("--add-opens=java.base/java.util=ALL-UNNAMED")
                argsList.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
                argsList.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED")
                argsList.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED")
                argsList.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED")

                // Opens the java.net package to Arc DNS injector on Java 9+
                argsList.add("--add-opens=java.base/java.net=ALL-UNNAMED")
            }

            val cacioClassPath = StringBuilder()
            cacioClassPath.append("-Xbootclasspath/").append(if (isJava8) "p" else "a")
            val cacioFiles = if (isJava8) LibPath.CACIO_8 else LibPath.CACIO_17
            cacioFiles.listFiles()?.onEach {
                if (it.name.endsWith(".jar")) cacioClassPath.append(":").append(it.absolutePath)
            }

            argsList.add(cacioClassPath.toString())

            return argsList
        }
    }
}