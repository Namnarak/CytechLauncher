plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlinx-serialization")
    id("stringfog")
}
apply(plugin = "stringfog")

val zalithPackageName = "com.movtery.zalithlauncher"
val launcherAPPName = project.findProperty("launcher_app_name") as? String ?: error("The \"launcher_app_name\" property is not set in gradle.properties.")
val launcherName = project.findProperty("launcher_name") as? String ?: error("The \"launcher_name\" property is not set in gradle.properties.")
val generatedZalithDir = file("$buildDir/generated/source/zalith/java")

fun getKeyFromLocal(envKey: String, fileName: String? = null): String {
    val key = System.getenv(envKey)
    return key ?: fileName?.let {
        val file = File(rootDir, fileName)
        if (file.canRead() && file.isFile) file.readText() else null
    } ?: run {
        logger.warn("BUILD: $envKey not set; related features may throw exceptions.")
        ""
    }
}

configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    fogPackages = arrayOf("$zalithPackageName.info")
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

android {
    namespace = zalithPackageName
    compileSdk = 35

    signingConfigs {
        create("releaseBuild") {
            storeFile = file("zalith_launcher.jks")
            storePassword = getKeyFromLocal("STORE_PASSWORD", ".store_password.txt")
            keyAlias = "movtery_zalith"
            keyPassword = getKeyFromLocal("KEY_PASSWORD", ".key_password.txt")
        }
    }

    defaultConfig {
        applicationId = zalithPackageName
        applicationIdSuffix = ".v2"
        minSdk = 26
        targetSdk = 35
        versionCode = 190000
        versionName = "2.0.0"
        manifestPlaceholders["launcher_name"] = launcherAPPName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releaseBuild")
        }
    }

    sourceSets["main"].java.srcDirs(generatedZalithDir)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

fun generateJavaClass(
    sourceOutputDir: File,
    packageName: String,
    className: String,
    importList: List<String> = emptyList(),
    constantList: List<String>
) {
    val outputDir = File(sourceOutputDir, packageName.replace(".", "/"))
    outputDir.mkdirs()
    val javaFile = File(outputDir, "$className.java")
    val imports = importList.takeIf { it.isNotEmpty() }?.joinToString("\n") { import ->
        "import $import;"
    }
    javaFile.writeText(
        """
        |/**
        | * Automatically generated file. DO NOT MODIFY
        | */
        |package $packageName;
        |${imports?.let { "\n$imports\n" }}
        |public class $className {
        |${constantList.joinToString("\n") { "\t$it" }}
        |}
        """.trimMargin()
    )
    println("Generated Java file: ${javaFile.absolutePath}")
}

tasks.register("generateInfoDistributor") {
    doLast {
        fun String.toStatement(type: String = "String", variable: String) = "public static final $type $variable = $this;"

        val importList = listOf("com.movtery.zalithlauncher.utils.CryptoManager")
        val constantList = listOf(
            "\"${getKeyFromLocal("OAUTH_CLIENT_ID", ".oauth_client_id.txt")}\"".toStatement(variable = "OAUTH_CLIENT_ID"),
            "\"${getKeyFromLocal("CRYPTO_KEY", ".crypto_key.txt")}\"".toStatement(variable = "CRYPTO_KEY"),
            "\"$launcherAPPName\"".toStatement(variable = "LAUNCHER_NAME")
        )
        generateJavaClass(generatedZalithDir, "$zalithPackageName.info", "InfoDistributor", importList, constantList)
    }
}

tasks.named("preBuild") {
    dependsOn("generateInfoDistributor")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    //Utils
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    //Safe
    implementation(libs.stringfog.xor)
}