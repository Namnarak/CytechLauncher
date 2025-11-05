//
// Created by maks on 15.01.2025.
//

#include <jni.h>
#include <unistd.h>
#include <stdbool.h>
#include <bytehook.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdlib.h>
#include "stdio_is.h"

static _Atomic bool exit_tripped = false;

typedef void (*custom_exit_func)(int);
typedef void (*custom_abort_func)(void);
typedef void (*custom__exit_func)(int);

static void handle_exit(int code, const char* reason) {
    if(exit_tripped) {
        __android_log_print(ANDROID_LOG_INFO, "exit_hook", "Recursive exit detected (%s), code=%d", reason, code);
        return;
    }
    exit_tripped = true;
    __android_log_print(ANDROID_LOG_INFO, "exit_hook", "Exiting via %s, code=%d", reason, code);
    nominal_exit(code, false);
}

// Hook exit()
static void custom_exit(int code) {
    handle_exit(code, "exit");
    BYTEHOOK_CALL_PREV(custom_exit, custom_exit_func, code);
    BYTEHOOK_POP_STACK();
}

// Hook _exit()
static void custom__exit(int code) {
    handle_exit(code, "_exit");
    BYTEHOOK_CALL_PREV(custom__exit, custom__exit_func, code);
    BYTEHOOK_POP_STACK();
}

// Hook abort()
static void custom_abort() {
    handle_exit(1, "abort");
    BYTEHOOK_CALL_PREV(custom_abort, custom_abort_func);
    BYTEHOOK_POP_STACK();
}

// fallback for atexit
static void custom_atexit() {
    if(exit_tripped) {
        __android_log_print(ANDROID_LOG_INFO, "exit_hook", "Recursive atexit detected, ignoring");
        return;
    }
    handle_exit(0, "atexit fallback");
}

static bool init_exit_hook() {
    void* bytehook_handle = dlopen("libbytehook.so", RTLD_NOW);
    if(bytehook_handle == NULL) {
        goto dlerror;
    }

    bytehook_stub_t (*bytehook_hook_all_p)(const char *, const char *, void *, bytehook_hooked_t, void *);
    int (*bytehook_init_p)(int, bool);

    bytehook_hook_all_p = dlsym(bytehook_handle, "bytehook_hook_all");
    bytehook_init_p = dlsym(bytehook_handle, "bytehook_init");

    if(bytehook_hook_all_p == NULL || bytehook_init_p == NULL) {
        goto dlerror;
    }

    int bhook_status = bytehook_init_p(BYTEHOOK_MODE_AUTOMATIC, false);
    if(bhook_status == BYTEHOOK_STATUS_CODE_OK) {
        bytehook_hook_all_p(NULL, "exit", &custom_exit, NULL, NULL);
        bytehook_hook_all_p(NULL, "_exit", &custom__exit, NULL, NULL);
        bytehook_hook_all_p(NULL, "abort", &custom_abort, NULL, NULL);
        __android_log_print(ANDROID_LOG_INFO, "exit_hook", "Successfully initialized exit/_exit/abort hooks");
        return true;
    } else {
        __android_log_print(ANDROID_LOG_WARN, "exit_hook", "bytehook_init failed (%i), using fallback", bhook_status);
        dlclose(bytehook_handle);
        return false;
    }

    dlerror:
    if(bytehook_handle != NULL) dlclose(bytehook_handle);
    __android_log_print(ANDROID_LOG_ERROR, "exit_hook", "Failed to load hook library: %s", dlerror());
    return false;
}

JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_bridge_ZLBridge_initializeGameExitHook(JNIEnv *env, jclass clazz) {
    bool hookReady = init_exit_hook();
    if(!hookReady){
        // If we can't hook, register atexit(). This won't report a proper error code,
        // but it will prevent a SIGSEGV or a SIGABRT from the depths of Dalvik that happens
        // on exit().
        __android_log_print(ANDROID_LOG_WARN, "exit_hook", "Using atexit fallback hook");
        atexit(custom_atexit);
    }
}