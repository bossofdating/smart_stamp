package com.kt.SmartStamp.data;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.kt.SmartStamp.R;

import java.io.ByteArrayOutputStream;

public class AppVariables {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    public static long sendHpDate = System.currentTimeMillis() ;

    public static boolean reConnect = false;

    public static boolean bConnect = false;

    public static String User_Phone_Number = "";
    public static String User_Name ="";
    public static String User_Email ="";
    public static String User_Team ="";
    public static String Mac_Adress ="";
    public static String User_Hp_Token="";
    public static String User_Idx ="";
    public static String User_Permission ="N";
    public static String User_co_idx = "";

    public static BluetoothDevice device = null;

    public static int Config_Time =0;
    public static int Config_Battery_amount = 0;
    public static int Config_Mode = 0;
    public static int Config_Halmet_TIme=0;
    public static int Config_Halmet_Battery_amount =0;
    public static int Config_Delay_Time =0;
    public static int Config_Send_Mode =0;

    public static boolean isRunServiceMainView = false;

    public static int iBatteryAmmount = 0;
    public static String sApplyMode = "0";

    public static final String EXTRA_SERVICE_DATA = "com.kt.SmartStamp.SERVICE_EXTRA_DATA";
    public static final String EXTRA_SERVICE_BATTERY_INFO = "com.kt.SmartStamp.SERVICE_EXTRA_DATA.BATTERY.";
    public static final String EXTRA_SERVICE_APPLY_INFO = "com.kt.SmartStamp.SERVICE_EXTRA_DATA.APPLY.";
    public static final String EXTRA_SERVICE_EMERGENCY_OFF= "com.kt.SmartStamp.SERVICE_EXTRA_DATA.EMERGENCY.OFF.";
    public static final String EXTRA_SERVICE_STOP= "com.kt.SmartStamp.SERVICE_EXTRA_DATA.STOP";

    public static Activity activitySet;

    public static Boolean IsShowInfoDialog = false;

    public static  SoundPool soundPoolEmer = null, soundPoolNormal = null;
    public static  int emerAlarm = -1, normalAlarm = -1;
    public static int iBatteryAmmountFlag = -1;

}
