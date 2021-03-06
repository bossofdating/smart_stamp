package com.kt.SmartStamp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.AES256Util;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    private SessionManager sessionManager;
    private JSONService jsonService;
    private PackageInfo packageInfo;
    private AES256Util aES256Util;

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;

    private Button loginButton;
    private EditText idEditText;
    private EditText pwEditText;
    private TextView textviewAppversion;
    private String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_login);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }

        requestHttpDataGetKey(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        idEditText = findViewById(R.id.id_edittext);
        pwEditText = findViewById(R.id.pw_edittext);
        loginButton = findViewById(R.id.login_button);
        textviewAppversion = findViewById(R.id.textview_appversion);

        loginButton.setOnClickListener(this);

        if (getVersionInfo(this) != null)
            textviewAppversion.setText("Version " + getVersionInfo(this));
    }

    /*************************************** 클릭 이벤트 핸들러 ***************************************/
    @Override
    public void onClick(View view) {
        // 중복 클릭 방지
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        if(elapsedTime<=MIN_CLICK_INTERVAL){
            return;
        }
        mLastClickTime=currentClickTime;

        switch(view.getId()) {
            case R.id.login_button :
                String id = idEditText.getText().toString();
                String pw = pwEditText.getText().toString();

                try {
                    aES256Util = new AES256Util(key);
                    pw = aES256Util.encrypt(pw);
                } catch (UnsupportedEncodingException e){
                } catch (GeneralSecurityException e){}

                if (id == null || "".equals(id)) {
                    Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (pw == null || "".equals(pw)) {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    requestHttpDataLogin(id, pw, packageInfo.versionName, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                }

                break;
        }
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 키 - 0
    public static void requestHttpDataGetKey(String deviceId) {
        httpAsyncRequest.AddHeaderData("device_id", deviceId);
        httpAsyncRequest.RequestHttpPostData(HTTP_DEFINE.HTTP_URL_KEY_INFO, null, 0);
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 로그인 - 1
    public static void requestHttpDataLogin(String id, String pw, String versionName, String deviceId) {
        httpAsyncRequest.AddHeaderData("device_id", deviceId);
        httpAsyncRequest.AddHeaderData("id", id);
        httpAsyncRequest.AddHeaderData("pw", pw);
        httpAsyncRequest.AddHeaderData("fcm_key", "test");
        httpAsyncRequest.AddHeaderData("app_version", versionName);
        httpAsyncRequest.RequestHttpPostData(HTTP_DEFINE.HTTP_URL_LOGIN, null, 1);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean success, String resultData, Bitmap resultBitmap, int requestCode, int httpResponseCode, Object passThroughData) {
        if(success) {
            if(requestCode == 0) parseJsonGetKey(resultData); // 키 - 0
            if(requestCode == 1) parseJsonLogin(resultData); // 로그인 - 1
        } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 키 - 0
    private void parseJsonGetKey(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            key = jsonService.GetString( "key", null );
        }
    }

    // 로그인 - 1
    private void parseJsonLogin(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            String loginFlag = jsonService.GetString( "Status", null );
            String failedMessage = jsonService.GetString( "Message", null );
            String authKey = jsonService.GetString( "auth_key", null );
            String memIdx = jsonService.GetString( "mem_idx", null );
            String adminFl = jsonService.GetString( "admin_fl", null );
            String apprFl = jsonService.GetString( "appr_fl", null );
            String agreeFl = jsonService.GetString( "agree_fl", null );
            String memName = jsonService.GetString( "name", null );
            String utName = jsonService.GetString( "ut_name", null );

            sessionManager.setAuthKey(authKey);
            sessionManager.setMemIdx(memIdx);
            sessionManager.setAdminFl(adminFl);
            sessionManager.setApprFl(apprFl);
            sessionManager.setMemName(memName);
            sessionManager.setUtName(utName);

            if (loginFlag != null && "Failed".equals(loginFlag)) Toast.makeText(this, failedMessage, Toast.LENGTH_SHORT).show();
            else if(loginFlag != null && "Exception".equals(loginFlag)) Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            else {
                finish();
                if ("y".equals(agreeFl)) startActivity(new Intent(LoginActivity.this, MainActivity.class));
                else if ("n".equals(agreeFl)) startActivity(new Intent(LoginActivity.this, TermsActivity.class));
            }
        }
    }

    /**************************************** 버전 *******************************************/
    public String getVersionInfo(Context context){
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch(PackageManager.NameNotFoundException e) { }
        return version;
    }

}
