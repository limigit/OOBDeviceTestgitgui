#include <jni.h>
#include <getInfo.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <sys/ioctl.h>
#include <string.h>
#include <android/log.h>

#define LOGE(Message) __android_log_write(ANDROID_LOG_ERROR,"lib_rkinfo",Message)

typedef unsigned short uint16;
typedef unsigned long uint32;
typedef unsigned char uint8;

#define RKNAND_DIASBLE_SECURE_BOOT _IOW('d', 127, unsigned int)
#define RKNAND_ENASBLE_SECURE_BOOT _IOW('d', 126, unsigned int)
#define RKNAND_GET_SN_SECTOR       _IOW('d', 3, unsigned int)

#define RKNAND_GET_VENDOR_SECTOR0       _IOW('v', 16, unsigned int)
#define RKNAND_STORE_VENDOR_SECTOR0     _IOW('v', 17, unsigned int)

#define RKNAND_GET_VENDOR_SECTOR1       _IOW('v', 18, unsigned int)
#define RKNAND_STORE_VENDOR_SECTOR1     _IOW('v', 19, unsigned int)

#define DRM_KEY_OP_TAG              0x4B4D5244 // "DRMK" 
#define SN_SECTOR_OP_TAG            0x41444E53 // "SNDA"
#define DIASBLE_SECURE_BOOT_OP_TAG  0x42534444 // "DDSB"
#define ENASBLE_SECURE_BOOT_OP_TAG  0x42534E45 // "ENSB"
#define VENDOR_SECTOR_OP_TAG        0x444E4556 // "VEND"
#define RKNAND_SYS_STORGAE_DATA_LEN 512

typedef struct tagRKNAND_SYS_STORGAE {
	uint32 tag;
	uint32 len;
	uint8 data[RKNAND_SYS_STORGAE_DATA_LEN];
} RKNAND_SYS_STORGAE;

typedef struct tagSN_SECTOR_INFO {
	uint32 snSectag; // "SNDA" 0x41444E53
	uint32 snSecLen; // 512
	uint16 snLen; // 0:no sn , 0~30,sn len
	uint8 snData[30]; // sn data
	uint32 reserved2[(0x200 - 0x20) / 4];
} SN_SECTOR_INFO, *pSN_SECTOR_INFO;

struct idb_data {
	uint16_t SN_Size; //0-1
	char SN[30]; //2-31
	char bid[97]; //32-128
	char Reserved[322]; //129-450
	char IMEI_Size; //451
	char IMEI_Data[15]; //452-466
	char UID_Size; //467
	char UID_Data[30]; //468-497
	char BT_Size; //498
	char BlueTooth[6]; //499-504
	char Mac_Size; //505
	char Mac_Data[6]; //506-511
};

static RKNAND_SYS_STORGAE sysData;
static RKNAND_SYS_STORGAE sysVendorData;

static idb_data sysIdbdata;
void valueToChar(char* src, char* des, int len);

void rknand_print_hex_data(char *s, uint32 * buf, uint32 len) {
	uint32 i, j, count;
	for (i = 0; i < len; i += 4) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%x %x %x %x",
				buf[i], buf[i + 1], buf[i + 2], buf[i + 3]);
	}
}

int rknand_sys_storage_test_sn(void) {
	uint32 i;
	int ret;
	//RKNAND_SYS_STORGAE sysData;
	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}
	//sn
	sysData.tag = SN_SECTOR_OP_TAG;
	sysData.len = RKNAND_SYS_STORGAE_DATA_LEN;
	ret = ioctl(sys_fd, RKNAND_GET_SN_SECTOR, &sysData);
//	rknand_print_hex_data("sndata:",(uint32*)sysData.data,16);
	for (i = 0; i < 16; i += 4) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%x %x %x %x",
				sysData.data[i], sysData.data[i + 1], sysData.data[i + 2],
				sysData.data[i + 3]);
	}
	if (ret) {
		LOGE("get sn error\n");
		return -1;
	}

	memcpy(&sysIdbdata, sysData.data, sizeof(sysData.data));
	close(sys_fd);
	return 0;
}

int rknand_sys_storage_secure_boot_disable(void) {
	uint32 i;
	int ret;
	//RKNAND_SYS_STORGAE sysData;

	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}
	sysData.tag = DIASBLE_SECURE_BOOT_OP_TAG;
	sysData.len = RKNAND_SYS_STORGAE_DATA_LEN;

	ret = ioctl(sys_fd, RKNAND_DIASBLE_SECURE_BOOT, &sysData);
	if (ret) {
		LOGE("disable secure boot error\n");
		return -1;
	}
	close(sys_fd);
	return 0;
}

int rknand_sys_storage_secure_boot_enable(void) {
	uint32 i;
	int ret;
	// RKNAND_SYS_STORGAE sysData;

	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}
	sysData.tag = ENASBLE_SECURE_BOOT_OP_TAG;
	sysData.len = RKNAND_SYS_STORGAE_DATA_LEN;

	ret = ioctl(sys_fd, RKNAND_ENASBLE_SECURE_BOOT, &sysData);
	if (ret) {
		LOGE("enable secure boot error\n");
		return -1;
	}
	return 0;
}

int rknand_sys_storage_vendor_sector_load(void) {
	uint32 i;
	int ret;
	// RKNAND_SYS_STORGAE sysVendorData;

	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}

	sysVendorData.tag = VENDOR_SECTOR_OP_TAG;
	sysVendorData.len = RKNAND_SYS_STORGAE_DATA_LEN - 8;

	ret = ioctl(sys_fd, RKNAND_GET_VENDOR_SECTOR0, &sysVendorData);
	rknand_print_hex_data("vendor_sector load:", (uint32*)sysVendorData.data, 64);
	if (ret) {
		LOGE("get vendor_sector error\n");
		return -1;
	}
	memcpy(&sysIdbdata, sysData.data, sizeof(sysData.data));
	close(sys_fd);
	return 0;
}

int rknand_sys_storage_vendor_sector_store(void) {
	uint32 i;
	int ret;
	//RKNAND_SYS_STORGAE sysVendorData;

	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}
	sysVendorData.tag = VENDOR_SECTOR_OP_TAG;
	sysVendorData.len = RKNAND_SYS_STORGAE_DATA_LEN - 8;
	for (i = 0; i < 126; i++) {
		sysVendorData.data[i] = i;
	}
	//rknand_print_hex_data("vendor_sector save:",(uint32*)sysData.data,32);
	ret = ioctl(sys_fd, RKNAND_STORE_VENDOR_SECTOR0, &sysVendorData);
	if (ret) {
		LOGE("save vendor_sector error\n");
		return -1;
	}
	return 0;
}

static int sCtrlFd = -1;

enum type_devices {
	DEVICE_TYPE_NULL = 0x0,

	DEVICE_TYPE_SUM = 0x20, 
	DEVICE_TYPE_AREA = 0x24, //
	DEVICE_TYPE_OPERATOR = 0x25,
	DEVICE_TYPE_OPERATOR2 = 0x26,
	DEVICE_TYPE_RESERVE = 0x27,
	DEVICE_TYPE_STATUS = 0x28,

	DEVICE_TYPE_TP = 0x29, //one byte size
	DEVICE_TYPE_LCD,
	DEVICE_TYPE_KEY,
	DEVICE_TYPE_CODEC,
	DEVICE_TYPE_WIFI,
	DEVICE_TYPE_BT,
	DEVICE_TYPE_GPS,
	DEVICE_TYPE_FM,
	DEVICE_TYPE_MODEM,
	DEVICE_TYPE_DDR,
	DEVICE_TYPE_FLASH,
	DEVICE_TYPE_HDMI,
	DEVICE_TYPE_BATTERY,
	DEVICE_TYPE_CHARGE,
	DEVICE_TYPE_BACKLIGHT,
	DEVICE_TYPE_HEADSET,
	DEVICE_TYPE_MICPHONE,
	DEVICE_TYPE_SPEAKER,
	DEVICE_TYPE_VIBRATOR,
	DEVICE_TYPE_TV,
	DEVICE_TYPE_ECHIP, //30
	DEVICE_TYPE_HUB,
	DEVICE_TYPE_TPAD,

	DEVICE_TYPE_PMIC,
	DEVICE_TYPE_REGULATOR,
	DEVICE_TYPE_RTC,
	DEVICE_TYPE_CAMERA_FRONT,
	DEVICE_TYPE_CAMERA_BACK, //35
	DEVICE_TYPE_ANGLE,
	DEVICE_TYPE_ACCEL,
	DEVICE_TYPE_COMPASS,
	DEVICE_TYPE_GYRO,
	DEVICE_TYPE_LIGHT,
	DEVICE_TYPE_PROXIMITY,
	DEVICE_TYPE_TEMPERATURE,
	DEVICE_TYPE_PRESSURE,

	DEVICE_NUM_TYPES,
};

#define BOARD_ID_IOCTL_BASE 'b'

#define BOARD_ID_IOCTL_READ_VENDOR_DATA 		_IOR(BOARD_ID_IOCTL_BASE, 0x63,	char[DEVICE_NUM_TYPES])

int board_id_open_device(void) {
	if (sCtrlFd < 0) {
		sCtrlFd = open("/dev/board_id_misc", O_RDWR);
		if (sCtrlFd < 0) {
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
					"%s:line=%d,error=%s\n", __FUNCTION__, __LINE__,
					strerror(errno));
			return -1;
		}
	}

	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s\n", __FUNCTION__);
	return 0;
}

int board_id_close_device(void) {
	if (sCtrlFd >= 0) {
		close(sCtrlFd);
		sCtrlFd = -1;
	}

	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s\n", __FUNCTION__);
	return 0;
}

int board_id_get(char *id) {
	board_id_open_device();
	int result = 0;
	int ioctl_cmd_temp = BOARD_ID_IOCTL_READ_VENDOR_DATA;

	if (!id) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s:id is null\n",
				__FUNCTION__);
		goto error;
	}

	if (0 > ioctl(sCtrlFd, ioctl_cmd_temp, id)) {
//		printf("%s:line=%d,error=%s\n", __FUNCTION__, __LINE__,
//				strerror(errno));
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
				"%s:line=%d,error=%s", __FUNCTION__, __LINE__, strerror(errno));
		result = -1;
		goto error;
	}
	return result;

	error: strcpy(id, "123456789");
	return result;
}


//===================LCD========================
#define EDID_IOCTL_MAGIC 'd'
#define EDID_IOCTL_GET_ALL_DATA _IOR(EDID_IOCTL_MAGIC, 1, int *)
#define EDID_IOCTL_GET_VID_PID _IOR(EDID_IOCTL_MAGIC, 2, int *)

int open_lcd_device() {
	int lcd_fd = -1;
	lcd_fd = open("/dev/edid_misc", O_RDWR);
			if (lcd_fd < 0) {
				__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
						"%s:line=%d,error=%s\n", __FUNCTION__, __LINE__,
						strerror(errno));
				return -1;
			}
	return lcd_fd;
}

int lcd_id_get(char* id) {
	int lcd_fd = open_lcd_device();
	int result = 0;
	int ioctl_cmd_temp = EDID_IOCTL_GET_VID_PID;

		if (!id) {
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s:id is null\n",
					__FUNCTION__);
			goto error;
		}

		if (0 > ioctl(lcd_fd, ioctl_cmd_temp, id)) {
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
					"%s:line=%d,error=%s", __FUNCTION__, __LINE__, strerror(errno));
			result = -1;
			goto error;
		}
		return result;

		error: strcpy(id, "123456789");
		return result;
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getLcdId
  (JNIEnv *env, jobject obj) {
	char src[4];
	char buf[4 <<1 + 1];
	lcd_id_get(src);
	valueToChar(src, buf, 4);
	return env->NewStringUTF(buf);
}



JNIEXPORT void JNICALL Java_com_OOBDeviceTest_helper_NativeManger_init(JNIEnv *,
		jobject) {
	rknand_sys_storage_test_sn();
	rknand_sys_storage_vendor_sector_load();
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getSN(
		JNIEnv *env, jobject obj) {
	//return sysIdbdata.SN;
	return env->NewStringUTF(sysIdbdata.SN);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getBoardId(
		JNIEnv *env, jobject obj) {
	int length = DEVICE_NUM_TYPES - 32;
	char buf[length << 1 + 1];
	int i = 0;
	//memcpy(&sysVendorData, sysData.data, sizeof(sysData.data));

	LOGE("NEW VERSION_2\n");
	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
			"id: %x %x %x %x %x %x %x %x", sysVendorData.data[0], sysVendorData.data[1],
			sysVendorData.data[2], sysVendorData.data[3], sysVendorData.data[4], sysVendorData.data[5],
			sysVendorData.data[6], sysVendorData.data[7]);

	for (i=0; i<length+1; i++) {
		sprintf(&buf[i*2],"%x%x",sysVendorData.data[i]>>4, sysVendorData.data[i]&0x0f);
	}
	buf[length<<1]='\0';

	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_android_settings_deviceinfo_Status_getBoardIdBySetting(
		JNIEnv *env, jobject obj) {
	rknand_sys_storage_vendor_sector_load();

	int length = DEVICE_NUM_TYPES - 32;
	char buf[length << 1 + 1];
	int i = 0;

	LOGE("NEW VERSION_2\n");
	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
			"id: %x %x %x %x %x %x %x %x", sysVendorData.data[0], sysVendorData.data[1],
			sysVendorData.data[2], sysVendorData.data[3], sysVendorData.data[4], sysVendorData.data[5],
			sysVendorData.data[6], sysVendorData.data[7]);

	for (i=0; i<length+1; i++) {
		sprintf(&buf[i*2],"%x%x",sysVendorData.data[i]>>4, sysVendorData.data[i]&0x0f);
	}
	buf[length<<1]='\0';

	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getMAC
  (JNIEnv *env, jobject obj) {
	char buf[6 << 1 + 1];
//	int i = 0;
//	for (i=0; i<7; i++) {
//			sprintf(&buf[i*2],"%x%x",sysIdbdata.Mac_Data[i]>>4, sysIdbdata.Mac_Data[i]&0x0f);
//		}
//	buf[6<<1]='\0';
	valueToChar(sysIdbdata.BlueTooth, buf, 6);
	return env->NewStringUTF(buf);
}


JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getIMEI
  (JNIEnv *env, jobject obj) {
	//char buf[15 << 1 + 1];
	//valueToChar(sysIdbdata.IMEI_Data, buf, 15);

	char buf[16];
	memcpy(buf, sysIdbdata.IMEI_Data, sizeof(sysIdbdata.IMEI_Data));
	buf[15] = '\0';
	return env->NewStringUTF(buf);
}


JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getUID
  (JNIEnv *env, jobject obj) {
//	char buf[30 << 1 + 1];
//	valueToChar(sysIdbdata.UID_Data, buf, 30);

	char buf[31];
	memcpy(buf, sysIdbdata.UID_Data, sizeof(sysIdbdata.UID_Data));
	buf[30] = '\0';
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getBT
  (JNIEnv *env, jobject obj) {
	char buf[6 << 1 + 1];
	valueToChar(sysIdbdata.BlueTooth, buf, 6);
	return env->NewStringUTF(buf);
}

void valueToChar(char* src, char* des, int len) {
	if (src == NULL || des == NULL || len == 0) {
		return ;
	}
	int i = 0;
	for (i=0; i<len+1; i++) {
		sprintf(&des[i*2],"%x%x",src[i]>>4, src[i]&0x0f);
	}
	des[len<<1] ='\0';
}

