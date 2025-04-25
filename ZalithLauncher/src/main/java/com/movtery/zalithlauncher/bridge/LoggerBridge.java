package com.movtery.zalithlauncher.bridge;

/**
 * Singleton class made to log on one file
 * The singleton part can be removed but will require more implementation from the end-dev
 * <a href="https://github.com/PojavLauncherTeam/PojavLauncher/blob/f1cb9e6/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Logger.java">Modified from PojavLauncher</a>
 */
public final class LoggerBridge {
    /** Reset the log file, effectively erasing any previous logs */
    public static native void start(String filePath);

    /** Print the text to the log file if not censored */
    public static native void append(String log);

    /** Link a log listener to the logger */
    public static native void setListener(EventLogListener listener);

    /** Small listener for anything listening to the log */
    public interface EventLogListener {
        void onEventLogged(String text);
    }

    public static void appendTitle(String title) {
        String logText = "==================== " + title + " ====================";
        append(logText);
    }
}
