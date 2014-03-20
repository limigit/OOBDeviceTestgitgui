package com.OOBDeviceTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.os.storage.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;

/*
 * @auther CGHS
 * 
 * @date 2011-11-13
 */

public class UsbHostTestActivity extends Activity {
    // Create by CGHS @ Rockchip
    private static final String TAG = UsbHostTestActivity.class.getSimpleName();
    private static final String TEST_STRING = "Rockchip UsbHostTest File";
    private static final String MOUNT_SUCCESS = "mount success!";
    private static final String MOUNT_UNKNOWN = "mount unknown!";
    private static final File HOST_STORAGE_DIRECTORY = new File(FirstRun.usb_path);
    private static final int R_PASS = 1;
    private static final int R_FAIL = 2;
    private StringBuilder sBuilder;
    private boolean mIsSuccess = false;
    private static StorageManager mStorageManager = null;
    public String SUCCESS;
    public String FAIL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(1152);
        setContentView(R.layout.usbhosttest);

        ControlButtonUtil.initControlButtonView(this);
        ControlButtonUtil.Show();

        SUCCESS = getString(R.string.success);
        FAIL = getString(R.string.fail);
		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
    }

    @Override
    public void onResume() {
        super.onResume();
        mStorageManager.registerListener(mStorageListener);
        if (testDevDevice()) {
            testHostStorageState();
            testReadAndWrite();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }

    public static File getHostStorageDirectory() {
        return HOST_STORAGE_DIRECTORY;
    }

    public static String getHostStorageState() {
        try {
            return mStorageManager.getVolumeState(FirstRun.usb_path);
        } catch (Exception rex) {
            rex.printStackTrace();
            return Environment.MEDIA_REMOVED;
        }
    }

    public boolean testDevDevice() {
        TextView textView = (TextView) findViewById(R.id.linuxSupport);
        StringBuilder sBuilder = new StringBuilder();
        Process process;
        String temp;
        Runtime runtime = Runtime.getRuntime();

        try {
            process = runtime.exec("/system/bin/ls /dev/block/");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((temp = reader.readLine()) != null) {
                if (temp.startsWith("sda") && !temp.equals("sda")) {
                    textView.setText(getString(R.string.HardwareSupport) + temp);
                    process.destroy();
                    reader.close();
                    return true;
                }
            }
            textView.setText(getString(R.string.HardwareNoSupport));
            process.destroy();
            reader.close();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public void testHostStorageState() {
        TextView textView = (TextView) findViewById(R.id.hostStorageState);
        textView.setText(getString(R.string.HostStorageState)
                + getHostStorageState());
    }

    public void testReadAndWrite() {
        sBuilder = new StringBuilder();
        if (dotestReadAndWrite()) {
            sBuilder.append(getString(R.string.UsbHostTestTitle) + SUCCESS);
            mHandler.sendEmptyMessageDelayed(R_PASS, 3000);
            mIsSuccess = true;
        } else {
            sBuilder.append(getString(R.string.UsbHostTestTitle) + FAIL);
            mHandler.sendEmptyMessageDelayed(R_FAIL, 3000);
        }

        TextView textView = (TextView) findViewById(R.id.Writable);
        textView.setText(sBuilder.toString());

    }

    private boolean dotestReadAndWrite() {
        String directoryName = getHostStorageDirectory().toString() + "/test";

        File directory = new File(directoryName);
        if (!directory.isDirectory()) { // Create Test Dir
            if (!directory.mkdirs()) {
                sBuilder.append(getString(R.string.MakeDir) + FAIL).append("\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.MakeDir) + SUCCESS).append(
                        "\n");
            }
        }
        File f = new File(directoryName, "UsbHostTest.txt");
        try {
            // Remove stale file if any
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) { // Create Test File
                sBuilder.append(getString(R.string.CreateFile) + FAIL).append(
                        "\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.CreateFile) + SUCCESS).append(
                        "\n");

                doWriteFile(f.getAbsoluteFile().toString());

                if (doReadFile(f.getAbsoluteFile().toString()).equals(
                        TEST_STRING)) {
                    sBuilder.append(getString(R.string.Compare)).append(SUCCESS).append(
                            "\n");
                } else {
                    sBuilder.append(getString(R.string.Compare)).append(FAIL).append(
                            "\n");
                    return false;
                }
            }

            sBuilder.append(getString(R.string.FileDel)).append(
                    (f.delete() ? SUCCESS : FAIL)).append("\n");
            sBuilder.append(getString(R.string.DirDel)).append(
                    (directory.delete() ? SUCCESS : FAIL)).append("\n");
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "isWritable : false (IOException)!");
            return false;
        }
    }

    public void doWriteFile(String filename) {
        try {
            sBuilder.append(getString(R.string.WriteData)).append("\n");
            OutputStreamWriter osw = new OutputStreamWriter(
                                                            new FileOutputStream(
                                                                                 filename));
            osw.write(TEST_STRING, 0, TEST_STRING.length());
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String doReadFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(
                                                   new InputStreamReader(
                                                                         new FileInputStream(
                                                                                             filename)));
            String data = null;
            StringBuilder temp = new StringBuilder();
            sBuilder.append(getString(R.string.ReadData)).append("\n");
            while ((data = br.readLine()) != null) {
                temp.append(data);
            }
            br.close();
            Log.e(TAG, "Readfile " + temp.toString());
            return temp.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
        	if (path.equals(FirstRun.usb_path) && newState.equals(Environment.MEDIA_MOUNTED)) {
                if (testDevDevice()) {
                    testHostStorageState();
                    testReadAndWrite();
                }
        	}
        }
    };

    public void TestResult(int result) {
        if (result == R_PASS) {
            ((Button) findViewById(R.id.btn_Pass)).performClick();
        } else if (result == R_FAIL) {
            ((Button) findViewById(R.id.btn_Fail)).performClick();
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case R_PASS:
                    TestResult(R_PASS);
                    break;
                case R_FAIL:
                    TestResult(R_FAIL);
                    break;
            }
        };
    };

}
