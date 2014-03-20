#include <jni.h>
#include <getInfo.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <sys/ioctl.h>
#include <string.h>
#include <android/log.h>
#include <stdlib.h>
#include <unistd.h>
#include "hardware/hardware.h"
#include "hardware/sensors.h"

#define LOGE(Message) __android_log_print(ANDROID_LOG_ERROR,"lib_rkinfo",Message)
#define LOGI(Message) __android_log_print(ANDROID_LOG_INFO,"lib_rkinfo",Message)

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

#define BOARD_ID_IOCTL_BASE 'b'
#define BOARD_ID_IOCTL_READ_VENDOR_DATA 		_IOR(BOARD_ID_IOCTL_BASE, 0x63,	char[DEVICE_NUM_TYPES])
#define BID_DATA_LENGTH (DEVICE_NUM_TYPES-DEVICE_TYPE_SUM)

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
	char bid[96]; //32-127
	char Reserved[323]; //128-450
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

struct bid_data {
	char bid[BID_DATA_LENGTH];
	int flag;
};
static idb_data sysIdbdata;
static int g_sys_fd = -1;
static int sNandFd = -1;
static int sCtrlFd = -1;

static char g_bid_no_area[BID_DATA_LENGTH] = { 0x00, 0x00, 0x00, 0x30, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x02, 0x02, 0x02, 0x0b, 0x01, 0x0b, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x02, 0x07, 0x00, 0x01, 0x01, 0x00, 0x03, 0x01, 0x00,
		0x00, 0x01, 0x01, 0x00, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00 };
static struct bid_data g_bid[AREA_ID_NUMS];


void rknand_print_hex_data(char *s, uint32 * buf, uint32 len) {
	uint32 i, j, count;
	for (i = 0; i < len; i += 4) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%x %x %x %x",
				buf[i], buf[i + 1], buf[i + 2], buf[i + 3]);
	}
}
#if 0
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
		close(sys_fd);
		return -1;
	}
	close(sys_fd);
	return 0;
}
#endif

int rknand_close_device(void) {
	if (sNandFd >= 0) {
		close (sNandFd);
		sNandFd = -1;
	}

	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s\n", __FUNCTION__);
	return 0;
}

int rknand_open_device(void) {
	int ret = 0;
	int i = 0;
	if (sNandFd < 0) {
		sNandFd = open("/dev/rknand_sys_storage", O_RDWR, 0);
		if (sNandFd < 0) {
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
					"%s:line=%d,error=%s\n", __FUNCTION__, __LINE__,
					strerror(errno));
			return -1;
		}
	}

	sysData.tag = SN_SECTOR_OP_TAG;
	sysData.len = RKNAND_SYS_STORGAE_DATA_LEN;
	ret = ioctl(sNandFd, RKNAND_GET_SN_SECTOR, &sysData);
	for (i = 0; i < 16; i += 4) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%x %x %x %x",
				sysData.data[i], sysData.data[i + 1], sysData.data[i + 2],
				sysData.data[i + 3]);
	}
	if (ret) {
		LOGE("get sn error\n");
		rknand_close_device();
		return -1;
	}

	memcpy(&sysIdbdata, sysData.data, sizeof(sysData.data));

	sysVendorData.tag = VENDOR_SECTOR_OP_TAG;
	sysVendorData.len = RKNAND_SYS_STORGAE_DATA_LEN - 8;

	ret = ioctl(sNandFd, RKNAND_GET_VENDOR_SECTOR0, &sysVendorData);
	rknand_print_hex_data("vendor_sector load:", (uint32*) sysVendorData.data,
			64);
	if (ret) {
		LOGE("get vendor_sector error\n");
		rknand_close_device();
		return -1;
	}

//	rknand_close_device();
	__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s\n", __FUNCTION__);
	return 0;
}

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

int board_id_data_init(void) {
	int i = 0, j = 0;

	for (i = 0; i < AREA_ID_NUMS; i++) {
		for (j = 0; j < BID_DATA_LENGTH; j++)
			g_bid[i].bid[j] = g_bid_no_area[j];
	}

	for (i = 0; i < AREA_ID_NUMS; i++) {
		g_bid[i].bid[DEVICE_TYPE_AREA - DEVICE_TYPE_SUM] += i;
		g_bid[i].bid[DEVICE_TYPE_SUM -DEVICE_TYPE_SUM + 3] += i;
	}

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
	close(sCtrlFd);
	return result;

	error:
	close(sCtrlFd);
	strcpy(id, "123456789");
	return result;
}

int board_id_data_set(struct bid_data *bid) {
	board_id_open_device();
	rknand_open_device();
	int result = 0;

	memcpy(&sysVendorData.data, bid->bid, BID_DATA_LENGTH);
	result = ioctl(sNandFd, RKNAND_STORE_VENDOR_SECTOR0, &sysVendorData);
	if (result) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
						"save vendor_sector error %s\\n",strerror(errno));
		return -1;
	}
	rknand_close_device();
	return result;
}


/******************************************************************
*jstring ----->char*
********************************************************************/
char* js2c(JNIEnv* env, jstring jstr)
{
   char* rtn = NULL;
   jclass clsstring = env->FindClass("java/lang/String");
   jstring strencode = env->NewStringUTF("utf-8");
   jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
   jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
   jsize alen = env->GetArrayLength(barr);
   jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
   if (alen > 0)
   {
     rtn = (char*)malloc(alen + 1);
     memcpy(rtn, ba, alen);
     rtn[alen] = 0;
   }
   env->ReleaseByteArrayElements(barr, ba, 0);
   return rtn;
}

void valueToChar(char* src, char* des, int len) {
	if (src == NULL || des == NULL || len == 0) {
		return;
	}
	int i = 0;
	for (i = 0; i < len + 1; i++) {
		sprintf(&des[i * 2], "%x%x", src[i] >> 4, src[i] & 0x0f);
	}
	des[len << 1] = '\0';
}

void charToValue(char* src, char* des, int len) {
	if (src == NULL || des == NULL || len == 0) {
			return;
	}
	int i = 0;
	for(i = 0; i < len/2; i++) {
		 sscanf(&src[i*2],"%2x",&des[i]);
	}
	des[len/2] = '\0';
}

JNIEXPORT void JNICALL Java_com_OOBDeviceTest_helper_NativeManger_init(JNIEnv *,
		jobject) {
	//rknand_sys_storage_test_sn();
	//rknand_sys_storage_vendor_sector_load();
	rknand_open_device();
	board_id_data_init();
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
			"id: %x %x %x %x %x %x %x %x", sysVendorData.data[0],
			sysVendorData.data[1], sysVendorData.data[2], sysVendorData.data[3],
			sysVendorData.data[4], sysVendorData.data[5], sysVendorData.data[6],
			sysVendorData.data[7]);

	for (i = 0; i < length + 1; i++) {
		sprintf(&buf[i * 2], "%x%x", sysVendorData.data[i] >> 4,
				sysVendorData.data[i] & 0x0f);
	}
	buf[length << 1] = '\0';

	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getMAC(
		JNIEnv *env, jobject obj) {
	char buf[6 << 1 + 1];
//	int i = 0;
//	for (i=0; i<7; i++) {
//			sprintf(&buf[i*2],"%x%x",sysIdbdata.Mac_Data[i]>>4, sysIdbdata.Mac_Data[i]&0x0f);
//		}
//	buf[6<<1]='\0';
	valueToChar(sysIdbdata.BlueTooth, buf, 6);
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getIMEI(
		JNIEnv *env, jobject obj) {
	//char buf[15 << 1 + 1];
	//valueToChar(sysIdbdata.IMEI_Data, buf, 15);

	char buf[16];
	memcpy(buf, sysIdbdata.IMEI_Data, sizeof(sysIdbdata.IMEI_Data));
	buf[15] = '\0';
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getUID(
		JNIEnv *env, jobject obj) {
//	char buf[30 << 1 + 1];
//	valueToChar(sysIdbdata.UID_Data, buf, 30);

	/*char buf[31];
	memcpy(buf, sysIdbdata.UID_Data, sizeof(sysIdbdata.UID_Data));
	buf[30] = '\0';
	return env->NewStringUTF(buf);*/
	
	char buf[37];
	memset(buf,'\0',37);
	for(int i=12,m=0;i<28;i++,m+=2){
		if(i==16){
			buf[m++]='-';
		}

		if(i==18){
			buf[m++]='-';
		}
		if(i==20){
			buf[m++]='-';
		}
		if(i==22){
			buf[m++]='-';
		}

		sprintf(&buf[m], "%02x", sysIdbdata.UID_Data[i]);
		
	}
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_android_settings_DeviceInfoSettings_getUIDBySetting(
		JNIEnv *env, jobject obj) {
	rknand_open_device();
	char buf[37];
	memset(buf,'\0',37);
	for(int i=12,m=0;i<28;i++,m+=2){
		if(i==16){
			buf[m++]='-';
		}

		if(i==18){
			buf[m++]='-';
		}
		if(i==20){
			buf[m++]='-';
		}
		if(i==22){
			buf[m++]='-';
		}

		sprintf(&buf[m], "%02x", sysIdbdata.UID_Data[i]);
		
	}
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getBT(
		JNIEnv *env, jobject obj) {
	char buf[6 << 1 + 1];
	valueToChar(sysIdbdata.BlueTooth, buf, 6);
	return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_android_settings_DeviceInfoSettings_getBoardIdBySetting(
		JNIEnv *env, jobject obj) {
	rknand_open_device();
	int length = DEVICE_NUM_TYPES - 32;
		char buf[length << 1 + 1];
		int i = 0;
		//memcpy(&sysVendorData, sysData.data, sizeof(sysData.data));

		LOGE("NEW VERSION_2\n");
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
				"id: %x %x %x %x %x %x %x %x", sysVendorData.data[0],
				sysVendorData.data[1], sysVendorData.data[2], sysVendorData.data[3],
				sysVendorData.data[4], sysVendorData.data[5], sysVendorData.data[6],
				sysVendorData.data[7]);

		for (i = 0; i < length + 1; i++) {
			sprintf(&buf[i * 2], "%x%x", sysVendorData.data[i] >> 4,
					sysVendorData.data[i] & 0x0f);
		}
		buf[length << 1] = '\0';

		return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_com_cghs_stresstest_test_BoardidSwitchTest_getBoardId
  (JNIEnv *env, jobject obj ) {
	rknand_open_device();
		int length = DEVICE_NUM_TYPES - 32;
			char buf[length << 1 + 1];
			int i = 0;
			//memcpy(&sysVendorData, sysData.data, sizeof(sysData.data));

			LOGE("NEW VERSION_2\n");
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
					"id: %x %x %x %x %x %x %x %x", sysVendorData.data[0],
					sysVendorData.data[1], sysVendorData.data[2], sysVendorData.data[3],
					sysVendorData.data[4], sysVendorData.data[5], sysVendorData.data[6],
					sysVendorData.data[7]);

			for (i = 0; i < length + 1; i++) {
				sprintf(&buf[i * 2], "%x%x", sysVendorData.data[i] >> 4,
						sysVendorData.data[i] & 0x0f);
			}
			buf[length << 1] = '\0';

			return env->NewStringUTF(buf);

}

JNIEXPORT void JNICALL Java_com_cghs_stresstest_test_BoardidSwitchTest_setBoardId
  (JNIEnv *env, jobject obj, jstring bidStr) {
	struct bid_data tempBid;
	char *bidchar = js2c(env,bidStr);
	charToValue(bidchar, tempBid.bid, 90);

	for (int i = 0; i < 45; i = i + 9) {
		__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo",
					"---------set id: %x %x %x %x %x %x %x %x %x", tempBid.bid[i],
					tempBid.bid[i+1], tempBid.bid[i+2], tempBid.bid[i+3],
					tempBid.bid[i+4], tempBid.bid[i+5], tempBid.bid[i+6],
					tempBid.bid[i+7], tempBid.bid[i+8]);
	}

	free(bidchar);
	bidchar = NULL;
	board_id_data_set(&tempBid);

}

JNIEXPORT int JNICALL Java_com_cghs_stresstest_test_BoardidSwitchTest_setBoardIdByAreaid
(JNIEnv *env, jobject obj, jint areaid) {
	board_id_data_init();
	int result = board_id_data_set(&(g_bid[areaid]));
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

JNIEXPORT jstring JNICALL Java_com_OOBDeviceTest_helper_NativeManger_getLcdId(
		JNIEnv *env, jobject obj) {
	char src[4];
	char buf[4 << 1 + 1];
	lcd_id_get(src);
	valueToChar(src, buf, 4);
	return env->NewStringUTF(buf);
}
// =========================================================================

//==============================G-sensor=========================================
static int gx, gy, gz;

int rknand_sys_storage_vendor_sector_store_for_gSensor(void) {
	uint32 i;
		int ret;
		RKNAND_SYS_STORGAE sysData;

		int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
		if (sys_fd < 0) {
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "%s:rknand_sys_storage open fail:%s\n", __func__,
					strerror(errno));
			return -1;
		}
		sysData.tag = VENDOR_SECTOR_OP_TAG;
		sysData.len = RKNAND_SYS_STORGAE_DATA_LEN - 8;

		sysData.data[0] = (gx > 0) ? (gx & 0x7f) : (-gx & 0x7f | 0x80);
		sysData.data[1] = (gy > 0) ? (gy & 0x7f) : (-gy & 0x7f | 0x80);
		sysData.data[2] = (gz > 0) ? (gz & 0x7f) : (-gz & 0x7f | 0x80);
		rknand_print_hex_data("vendor_sector save:", (uint32*) sysData.data, 32);
		ret = ioctl(sys_fd, RKNAND_STORE_VENDOR_SECTOR1, &sysData);
		if (ret){
			__android_log_print(ANDROID_LOG_ERROR, "lib_rkinfo", "save vendor_sector error:%s\n", strerror(errno));
			close(sys_fd);
			return -1;
		}

		close(sys_fd);
		return 0;
}

int rknand_sys_storage_vendor_sector_load_for_gSensor(int data[3]) {
	uint32 i;
	int ret;
	RKNAND_SYS_STORGAE sysData;

	int sys_fd = open("/dev/rknand_sys_storage", O_RDWR, 0);
	if (sys_fd < 0) {
		//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "rknand_sys_storage open fail\n");
		LOGE("rknand_sys_storage open fail\n");
		return -1;
	}

	sysData.tag = VENDOR_SECTOR_OP_TAG;
	sysData.len = RKNAND_SYS_STORGAE_DATA_LEN - 8;

	ret = ioctl(sys_fd, RKNAND_GET_VENDOR_SECTOR1, &sysData);
	rknand_print_hex_data("vendor_sector load:", (uint32*) sysData.data, 32);
	if (ret) {
		//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "get vendor_sector error\n");
		LOGE("get vendor_sector error\n");
		close(sys_fd);
		return -1;
	}

	close(sys_fd);

	data[0] =
			(sysData.data[0] & 0x80) ?
					-(sysData.data[0] & 0x7f) : (sysData.data[0] & 0x7f);
	data[1] =
			(sysData.data[1] & 0x80) ?
					-(sysData.data[1] & 0x7f) : (sysData.data[1] & 0x7f);
	data[2] =
			(sysData.data[2] & 0x80) ?
					-(sysData.data[2] & 0x7f) : (sysData.data[2] & 0x7f);

	if (!data[0] && !data[1] && !data[2])
		return 0;
	else
		return 1;
}


JNIEXPORT jint JNICALL Java_com_OOBDeviceTest_helper_NativeManger_gSensorStore(JNIEnv * env,
		jclass c, jint x, jint y, jint z) {
	gx = x;
	gy = y;
	gz = z;
	__android_log_print(ANDROID_LOG_INFO, "lib_rkinfo",
			"Java_com_rk_sensorutile_jniUtil_store:gx=%d,gy=%d,gz=%d\n", gx, gy,
			gz);
	rknand_sys_storage_vendor_sector_store_for_gSensor();
	return 0;
}


JNIEXPORT jintArray JNICALL Java_com_OOBDeviceTest_helper_NativeManger_gSensorLoad(JNIEnv * env,
		jclass c) {
	//jint* body = (*env)->GetIntArrayElements(env, array,0);
		int data[3] = { 0, 0, 0 };
		rknand_sys_storage_vendor_sector_load_for_gSensor(data);

		int size = 3;
		jintArray result;
		result = env->NewIntArray(size);
		if (result == NULL) {
			return NULL; /* out of memory error thrown */
		}
		//int i;
		// fill a temp structure to use to populate the java int array
		/*	jint fill[3];
		 fill[0]=;
		 fill[1]=;
		 fill[2]=;*/
		__android_log_print(ANDROID_LOG_INFO, "lib_rkinfo",
				"V1.0 Java_com_rk_sensorutile_jniUtil_load:%d %d %d\n", data[0], data[1],data[2]);

		// move from the temp structure to the java structure
		env->SetIntArrayRegion(result, 0, size, data);
		return result;
}


/*
 * Class:     com_rk_sensorutile_jniUtil
 * Method:    cabiration
 * Signature: ([I)I
 */
JNIEXPORT jint JNICALL Java_com_OOBDeviceTest_helper_NativeManger_gSensorCalbiration(JNIEnv * env,
                jclass c, jintArray array) {
        LOGI("Java_com_rk_sensorutile_jniUtil_cabiration");
        /*sQueue = sensors_create_queue();
         SensorEventQueue* queue = new SensorEventQueue(sQueue);
         queue->disableSensor(0);
         free(queue);
         sensors_destroy_queue(sQueue);*/
//        struct sensors_poll_device_t* mSensorDevice;
//        struct sensors_module_t* mSensorModule;
//        int err = hw_get_module(SENSORS_HARDWARE_MODULE_ID,
//                        (hw_module_t const**) &mSensorModule);
//        err = sensors_open(&mSensorModule->common, &mSensorDevice);
//	if (mSensorDevice == NULL) {
//
//	    LOGI("mSensorDevice is null!!!");
//	    return -1;
//	}
//        sensor_t const* list;
//        int count = mSensorModule->get_sensors_list(mSensorModule, &list);
//        mSensorDevice->activate(mSensorDevice, list[0].handle, 0);
        return 0;

}


// =========================================================================

