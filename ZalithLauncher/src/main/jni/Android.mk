LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := awt_headless
include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(LOCAL_PATH)/awt_xawt
include $(CLEAR_VARS)
LOCAL_MODULE := awt_xawt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SHARED_LIBRARIES := awt_headless
LOCAL_SRC_FILES := xawt_fake.c
include $(BUILD_SHARED_LIBRARY)