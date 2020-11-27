package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.RecyclerViewAdapterStampLocation;
import com.kt.SmartStamp.data.ServerDataStamp;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;

public class DetailHistoryActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER, LIST_ITEM_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;
    private int offset = 0;

    public static RecyclerViewAdapterStampLocation RVAStamp;

    private NestedScrollView historyListNestedscrollview;
    private RecyclerView recyclerViewStampHistory;
    public TextView contNameTextView;
    public TextView contStateTextView;
    public TextView contDateTextView;
    public TextView contDetailTextView;
    public ImageView backImageview;
    private TextView nodataTextview;

    private String contIdx;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;

    public ArrayList<ServerDataStamp> arrayListStampLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_detail_history);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        historyListNestedscrollview = findViewById(R.id.history_list_nestedscrollview);
        recyclerViewStampHistory = findViewById(R.id.stamp_history_recyclerview);
        nodataTextview = findViewById(R.id.nodata_textview);
        contNameTextView = findViewById(R.id.cont_name_textview);
        contStateTextView = findViewById(R.id.cont_state_textview);
        contDateTextView = findViewById(R.id.cont_date_textview);
        contDetailTextView = findViewById(R.id.cont_detail_textview);
        backImageview = findViewById(R.id.back_imageview);

        backImageview.setOnClickListener(this);

        historyListNestedscrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    offset++;
                    requestHttpDataStampLocation(offset);
                }
            }
        });

        // 인덴트 데이터 수집
        Intent intent = getIntent();
        contIdx = intent.getStringExtra("cont_idx");

        arrayListStampLocation = new ArrayList<>();

        requestHttpDataContDetail();
    }

    @Override
    public void onItemClick(Object CallerObject, int clickType, int position) {	}
    @Override
    public void onReachedLastItem(Object callerObject) {}

    /**************************************** 레이아웃 출력 *******************************************/
    private void displayLayoutDefault(int offset) {
        requestHttpDataStampLocation(offset);
    }

    /************************************** 클릭 이벤트 핸들러 ****************************************/
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
            case R.id.back_imageview :
                finish();
                break;
        }
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 계약 상세 - 1
    private void requestHttpDataContDetail() {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_CONT_DETAIL, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
    }
    // 인장 위치 리스트 - 2
    private void requestHttpDataStampLocation(int offset) {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("offset", Integer.toString(offset));
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_STAMP_LOCATION, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
        if(Success) {
            if(RequestCode == 1) parseJsonContDetail(ResultData);	    // 계약 상세 - 1
            if(RequestCode == 2) parseJsonStampLocation(ResultData);	// 인장 위치 리스트 - 2
        } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 계약 상세 - 1
    private void parseJsonContDetail(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            contNameTextView.setText(jsonService.GetString("cont_name", null));
            contDateTextView.setText("반출 기간 : " + jsonService.GetString("appr_st_dt", null)
                + " ~ " + jsonService.GetString("appr_ed_dt", null)
                + "\n인장 반출자 : " + jsonService.GetString("mem_idx", null)
                + "\n인장 MAC : " + jsonService.GetString("stamp_idx", null));
            contDetailTextView.setText(jsonService.GetString("cont_detail", null));

            String comState = jsonService.GetString("com_state", null);
            if ("n".equals(comState)) {
                contStateTextView.setText("등록 대기");
                contStateTextView.setTextColor(getResources().getColor(R.color.commonTextGrayDeep));
            } else if ("r".equals(comState)) {
                contStateTextView.setText("날인 대기");
                contStateTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            } else if ("y".equals(comState)) {
                contStateTextView.setText("날인 완료");
                contStateTextView.setTextColor(getResources().getColor(R.color.colorBlue));
            }

            displayLayoutDefault(offset);
        }
    }
    // 인장 위치 리스트 - 2
    private void parseJsonStampLocation(String jsonData) {
        jsonService.CreateJSONArray(jsonData);

        if(jsonService != null && jsonService.GetArrayLength() > 0) {
            while(jsonService.SetNextNode()) {
                ServerDataStamp serverDataStamp = jsonService.GetClass(ServerDataStamp.class);
                arrayListStampLocation.add(serverDataStamp);
            }

            if (offset == 0) {
                nodataTextview.setVisibility(View.GONE);
                recyclerViewStampHistory.setVisibility(View.VISIBLE);
            }

            RVAStamp = new RecyclerViewAdapterStampLocation(this, arrayListStampLocation, this);
            RVAStamp.notifyDataSetChanged();
            recyclerViewStampHistory.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewStampHistory.setAdapter(RVAStamp);
        } else {
            if (offset == 0) {
                recyclerViewStampHistory.setVisibility(View.GONE);
                nodataTextview.setVisibility(View.VISIBLE);
            }
            offset--;
        }
    }
}
