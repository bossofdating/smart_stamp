package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    private SessionManager sessionManager;
    private JSONService jsonService;
    private PackageInfo packageInfo;

    private static final long MIN_CLICK_INTERVAL = 500;
    private long mLastClickTime;

    private Button loginButton;
    private EditText idEditText;
    private EditText pwEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            if( BuildConfig.DEBUG ) e.printStackTrace();
        }

        idEditText = findViewById(R.id.id_edittext);
        pwEditText = findViewById(R.id.pw_edittext);
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        // 자동 로그인 (개발완료 후 제거)
        requestHttpDataLogin("admin", "admin", "1.0");
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

                if (id == null || "".equals(id)) {
                    Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (pw == null || "".equals(pw)) {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    requestHttpDataLogin(id, pw, packageInfo.versionName);
                }

                break;
        }
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 로그인 - 1
    public static void requestHttpDataLogin(String id, String pw, String versionName) {
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
            if(requestCode == 1) parseJsonLogin(resultData); // 로그인 - 1
        } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 로그인 - 1
    private void parseJsonLogin(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            String loginFlag = jsonService.GetString( "Status", null );
            String authKey = jsonService.GetString( "auth_key", null );
            String memIdx = jsonService.GetString( "mem_idx", null );
            String agreeFl = jsonService.GetString( "agree_fl", null );
            String memName = jsonService.GetString( "name", null );
            String utName = jsonService.GetString( "ut_name", null );

            sessionManager.setAuthKey(authKey);
            sessionManager.setMemIdx(memIdx);
            sessionManager.setMemName(memName);
            sessionManager.setUtName(utName);

            if(loginFlag != null && "Failed".equals(loginFlag)) Toast.makeText(this, "잘못된 로그인 정보입니다", Toast.LENGTH_SHORT).show();
            else if(loginFlag != null && "Exception".equals(loginFlag)) Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            else {
                finish();
                if ("y".equals(agreeFl)) startActivity(new Intent(LoginActivity.this, MainActivity.class));
                else if ("n".equals(agreeFl)) startActivity(new Intent(LoginActivity.this, TermsActivity.class));
            }
        }
    }

}
