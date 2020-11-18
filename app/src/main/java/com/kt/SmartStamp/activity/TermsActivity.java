package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.data.ServerDataTerms;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;


public class TermsActivity extends AppCompatActivity implements HTTP_RESULT_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    private SessionManager sessionManager;
    private JSONService jsonService;
    private ArrayList<ServerDataTerms> termsArrayList;

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;

    private CheckBox chkAgree01;
    private CheckBox chkAgree02;
    private TextView txtAgree01;
    private TextView txtAgree02;
    private Button btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_agree);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this,COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();
        termsArrayList = new ArrayList<>();

        chkAgree01 = findViewById(R.id.chkAgree01);
        chkAgree02 = findViewById(R.id.chkAgree02);
        txtAgree01 = findViewById(R.id.txtAgree01);
        txtAgree02 = findViewById(R.id.txtAgree02);
        btnAgree = findViewById(R.id.btnAgree);

        displayLayout();
    }

    private void displayLayout() {
        requestHttpDataTermsList();
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 약관 목록
    public void requestHttpDataTermsList() {
        httpAsyncRequest.RequestHttpGetData(HTTP_DEFINE.HTTP_URL_TERMS_LIST, null, 1);
    }
    // 약관 동의
    public void requestHttpDataTermsAgree() {
        httpAsyncRequest.RequestHttpGetData(String.format(HTTP_DEFINE.HTTP_URL_TERMS_AGREE, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean success, String resultData, Bitmap resultBitmap, int requestCode, int httpResponseCode, Object passThroughData) {
        if(success) {
            if(requestCode == 1) parseJsonTermsList(resultData);    // 약관 목록
            if(requestCode == 2) parseJsonTermsAgree(resultData);   // 약관 동의
        }
        else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 약관 목록 - 1
    private void parseJsonTermsList(String jsonData) {
        jsonService.CreateJSONArray(jsonData);

        if(jsonService != null && jsonService.GetArrayLength() > 0) {
            while(jsonService.SetNextNode()) {
                ServerDataTerms serverDataTerms = jsonService.GetClass(ServerDataTerms.class);
                termsArrayList.add(serverDataTerms);
            }

            chkAgree01.setText(termsArrayList.get(0).terms_title);
            chkAgree02.setText(termsArrayList.get(1).terms_title);
            txtAgree01.setText(termsArrayList.get(0).terms_text);
            txtAgree02.setText(termsArrayList.get(1).terms_text);
        }

        txtAgree01.setMovementMethod(new ScrollingMovementMethod());
        txtAgree02.setMovementMethod(new ScrollingMovementMethod());

        btnAgree.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                // 중복 클릭 방지
                long currentClickTime= SystemClock.uptimeMillis();
                long elapsedTime=currentClickTime-mLastClickTime;
                if(elapsedTime<=MIN_CLICK_INTERVAL){
                    return;
                }
                mLastClickTime=currentClickTime;

                if(!chkAgree01.isChecked() || !chkAgree02.isChecked()){
                    Toast.makeText(TermsActivity.this, "모든 약관에 동의해주세요", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    requestHttpDataTermsAgree();
                }
            }
        });
    }

    // 약관 동의 - 2
    private void parseJsonTermsAgree(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            String status = jsonService.GetString( "Status", null );
            if ("OK".equals(status)) {
                finish();
                startActivity(new Intent(TermsActivity.this, MainActivity.class));
            } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

}

