package com.movtery.zalithlauncher.game.input;

import dalvik.annotation.optimization.CriticalNative;

/**
 * <a href="https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/CriticalNativeTest.java">From PojavLauncher</a>
 */
public class CriticalNativeTest {
    @CriticalNative
    public static native void testCriticalNative(int arg0, int arg1);
    public static void invokeTest() {
        testCriticalNative(0, 0);
    }
}
