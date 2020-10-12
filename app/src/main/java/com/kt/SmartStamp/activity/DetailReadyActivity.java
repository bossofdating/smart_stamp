package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;

public class DetailReadyActivity extends AppCompatActivity implements HTTP_RESULT_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    public static final int ANIMATION_DELAY_TIME = 100;
    private static final long MIN_CLICK_INTERVAL = 500;
    private long mLastClickTime;
    private int offset = 0;

    public TextView contNameTextview;
    public TextView contStateTextview;
    public TextView contDateTextview;
    public TextView contDetailTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ready);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offset = 0;
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        contNameTextview = findViewById(R.id.cont_name_textview);
        contStateTextview = findViewById(R.id.cont_state_textview);
        contDateTextview = findViewById(R.id.cont_date_textview);
        contDetailTextview = findViewById(R.id.cont_detail_textview);

        // 인덴트 데이터 수집
        Intent intent = getIntent();

        requestHttpDataContDetail(intent.getStringExtra("cont_idx"));
    }

    /**************************************** 레이아웃 출력 *******************************************/
    private void displayLayoutDefault() {
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 계약 상세 - 1
    public static void requestHttpDataContDetail(String contIdx) {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_CONT_DETAIL, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
    }
    // 문서 리스트 - 2
    public static void requestHttpDataDocList(String contIdx, int offset) {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("offset", Integer.toString(offset));
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_DOC_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
        if(Success) {
            if(RequestCode == 1) parseJsonContDetail(ResultData);	// 계약 상세 - 1
            if(RequestCode == 2) parseJsonDocList(ResultData);		// 문서 리스트 - 2
        } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 계약 상세 - 1
    private void parseJsonContDetail(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            contNameTextview.setText(jsonService.GetString("cont_name", null));
            contDateTextview.setText("반출 기간 : " + jsonService.GetString("appr_st_dt", null)
                    + " ~ " + jsonService.GetString("appr_st_dt", null));
            contDetailTextview.setText(jsonService.GetString("cont_detail", null));

            int doc_after_cnt = Integer.parseInt(jsonService.GetString("doc_after_cnt", "0"));
            if (doc_after_cnt > 0) {
                contStateTextview.setText("등록중 (" + jsonService.GetString("doc_after_cnt", null) + ")");
                contStateTextview.setTextColor(this.getResources().getColor(R.color.colorAccent));
            } else contStateTextview.setText("등록 대기");

            requestHttpDataDocList(jsonService.GetString("cont_idx", null), offset);
        }
    }
    // 문서 리스트 - 2
    private void parseJsonDocList(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
        }
    }

}
