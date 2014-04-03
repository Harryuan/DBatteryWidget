#ifeq ($(strip $(MMI_DUAL_BATTERY_SETTING)), yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_JAVA_LIBRARIES := mediatek-framework 

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files) 

LOCAL_PACKAGE_NAME := DualBatterySetting

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)
  
#endif
