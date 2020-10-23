package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.GridViewAdapterDocComplete;
import com.kt.SmartStamp.data.ServerDataDoc;
import com.kt.SmartStamp.data.ServerDataPhoto;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;

public class DetailCompleteActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER, AdapterView.OnItemClickListener {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private ArrayList<ServerDataPhoto> SELECTED_PROFILE_PHOTO_LIST;
    private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 사진 선택 다이얼로그 가변 적용 보관 변수
    private String CURRENT_SELECTING_PROFILE_PHOTO_PATH;										// 선택중인 사진의 경로 (사진 업로드, 수정 상태에서만 일시적으로 사용됨)

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;
    private int offset = 0;

    public static GridViewAdapterDocComplete GVADoc;
    private GridView gridViewDoc;

    private LinearLayout linearLayoutDoc;
    private LinearLayout dashboardNestedscrollview;
    public TextView contNameTextView;
    public TextView contStateTextView;
    public TextView contDateTextView;
    public TextView contDetailTextView;
    public ImageView backImageview;

    private String contIdx;
    private int dposition;
    public static int pageNum;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;

    public ArrayList<ServerDataDoc> ArrayListDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_complete);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        SELECTED_PROFILE_PHOTO_LIST = new ArrayList<>();

        gridViewDoc = findViewById(R.id.GridView_Doc);
        gridViewDoc.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                    // 중복 스크롤인 경우
                    long currentClickTime= SystemClock.uptimeMillis();
                    long elapsedTime=currentClickTime-mLastClickTime;
                    if(elapsedTime<=MIN_CLICK_INTERVAL){
                        return;
                    }
                    mLastClickTime=currentClickTime;
                    pageNum++;
                    requestHttpDataDocList(pageNum);
                }
            }
        });

        linearLayoutDoc = findViewById(R.id.LinearLayout_Doc);
        dashboardNestedscrollview = findViewById(R.id.dashboard_nestedscrollview);
        contNameTextView = findViewById(R.id.cont_name_textview);
        contStateTextView = findViewById(R.id.cont_state_textview);
        contDateTextView = findViewById(R.id.cont_date_textview);
        contDetailTextView = findViewById(R.id.cont_detail_textview);
        backImageview = findViewById(R.id.back_imageview);

        backImageview.setOnClickListener(this);

        // 인덴트 데이터 수집
        Intent intent = getIntent();
        contIdx = intent.getStringExtra("cont_idx");

        ArrayListDoc = new ArrayList<>();
        pageNum = 0;

        requestHttpDataContDetail();
    }

    /**************************************** 레이아웃 출력 *******************************************/
    private void displayLayoutDefault(int offset) {
        dashboardNestedscrollview.setVisibility(View.VISIBLE);
        ViewTreeObserver viewTreeObserver = dashboardNestedscrollview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                layerWidth = dashboardNestedscrollview.getWidth();
            }
        });
        requestHttpDataDocList(offset);
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

    /************************************* ListView 이벤트 핸들러 *************************************/
    @Override
    public void onItemClick(AdapterView<?> Parent, View view, final int position, long id ) {
        // 중복 클릭인 경우
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        if(elapsedTime<=MIN_CLICK_INTERVAL){
            return;
        }
        mLastClickTime=currentClickTime;
        switch(view.getId()) {
            case R.id.LinearLayout_Doc :
                dposition = position;
                ActionPhotoDialog();
        }
    }

    /************************************* HTTP  데이터요청 ******************************************/
    // 계약 상세 - 1
    private void requestHttpDataContDetail() {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_CONT_DETAIL, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
    }
    // 문서 리스트 - 2
    private void requestHttpDataDocList(int offset) {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("offset", Integer.toString(offset));
        httpAsyncRequest.AddHeaderData("order", "y");
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
            contNameTextView.setText(jsonService.GetString("cont_name", null));
            contDateTextView.setText("반출 기간 : " + jsonService.GetString("appr_st_dt", null)
                    + " ~ " + jsonService.GetString("appr_st_dt", null));
            contDetailTextView.setText(jsonService.GetString("cont_detail", null));

            int doc_after_cnt = Integer.parseInt(jsonService.GetString("doc_after_cnt", "0"));
            if (doc_after_cnt > 0) {
                contStateTextView.setText("문서 (" + jsonService.GetString("doc_after_cnt", null) + ")");
                contStateTextView.setTextColor(this.getResources().getColor(R.color.colorAccent));
            }

            displayLayoutDefault(offset);
        }
    }
    // 문서 리스트 - 2
    private void parseJsonDocList(String jsonData) {
        if (jsonData != null && !"".equals(jsonData) && !"[]".equals(jsonData) && !"{}".equals(jsonData)) {
            jsonService.CreateJSONArray(jsonData);
            if (pageNum == 0) ArrayListDoc.clear();

            while(jsonService.SetNextNode()) {
                ArrayListDoc.add(jsonService.GetClass(ServerDataDoc.class));
            }

            if (pageNum == 0) {
                GVADoc = new GridViewAdapterDocComplete(this, ArrayListDoc, layerWidth);
                gridViewDoc.setAdapter(GVADoc);
            }

            GVADoc.notifyDataSetChanged();
        } else {
            if (pageNum == 0) {
            } else pageNum--;
        }

        linearLayoutDoc.setVisibility(View.VISIBLE);
    }

    /**************************** 확대보기 *****************************/
    private void ActionPhotoDialog() {
        if(ArrayListDoc.size() > 0) {
            ArrayList<String> ArrayList_MultiView_ProfileImage = new ArrayList<>();
            ArrayList_MultiView_ProfileImage.add(ArrayListDoc.get(dposition).aft_picture);
            Intent IntentInstance = new Intent(this, Activity_ImageViewer_Multi.class);
            IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_IMAGE_PATH_LIST, ArrayList_MultiView_ProfileImage);
            IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_ITEM_POSITION, 0);
            startActivity(IntentInstance);
        }
    }
}
