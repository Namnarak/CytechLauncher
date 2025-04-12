package com.movtery.zalithlauncher.bridge;

import android.content.Context;

public final class ZLBridge {
    //Game
    public static native void initializeGameExitHook();
    public static native void setupExitMethod(Context context);

    //Launch
    public static native void setLdLibraryPath(String ldLibraryPath);
    public static native boolean dlopen(String libPath);

    //Render
    public static native void setupBridgeWindow(Object surface);
    public static native void releaseBridgeWindow();
    public static native void moveWindow(int xOffset, int yOffset);
    public static native int[] renderAWTScreenFrame();

    //Input
    public static native void sendInputData(int type, int i1, int i2, int i3, int i4);
    public static native void clipboardReceived(String data, String mimeTypeSub);

    //Callback
    public static native int getCurrentFPS();

    //Utils
    public static native int chdir(String path);

    static {
        System.loadLibrary("exithook");
        System.loadLibrary("pojavexec");
        System.loadLibrary("pojavexec_awt");
    }
}
