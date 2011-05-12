LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../jpeg
LOCAL_STATIC_LIBRARIES := jpeg

#LOCAL_MODULE := jnigraphics-prebuild
#LOCAL_SRC_FILES := libjnigraphics.so
#include $(PREBUILT_SHARED_LIBRARY)


LOCAL_MODULE    := libbitmap
LOCAL_SRC_FILES := bitmap.c
LOCAL_LDLIBS    := -lm -llog -ljnigraphics 

include $(BUILD_SHARED_LIBRARY)

