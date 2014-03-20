LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := OOBDeviceTest

LOCAL_JNI_SHARED_LIBRARIES := librkinfoOOB
LOCAL_JAVA_LIBRARIES := javax.obex

LOCAL_STATIC_JAVA_LIBRARIES += user_mode
LOCAL_STATIC_JAVA_LIBRARIES += ftp4j-1.7.2
LOCAL_STATIC_JAVA_LIBRARIES += jcifs-1.3.16
LOCAL_REQUIRED_MODULES := librkinfoOOB

LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

include $(CLEAR_VARS) 

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := user_mode:user_mode.jar \
			ftp4j-1.7.2:/libs/ftp4j-1.7.2.jar \
			jcifs-1.3.16:/libs/jcifs-1.3.16.jar

include $(BUILD_MULTI_PREBUILT)
include $(LOCAL_PATH)/libs/armeabi/Android.mk
