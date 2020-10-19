package com.kt.SmartStamp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.GridViewAdapterDoc;
import com.kt.SmartStamp.customview.Dialog_Common;
import com.kt.SmartStamp.data.ServerDataDoc;
import com.kt.SmartStamp.data.ServerDataPhoto;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;
import com.kt.SmartStamp.utility.RequiredPermissionsManager;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DetailReadyActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private ArrayList<ServerDataPhoto> SELECTED_PROFILE_PHOTO_LIST;
    private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 프로필 사진 선택 다이얼로그 가변 적용 보관 변수
    private String CURRENT_SELECTING_PROFILE_PHOTO_PATH;										// 선택중인 프로필 사진의 경로 (사진 업로드, 수정 상태에서만 일시적으로 사용됨)

    private static final long MIN_CLICK_INTERVAL = 500;
    private long mLastClickTime;
    private int offset = 0;

    public static GridViewAdapterDoc GVADoc;
    private GridView gridViewDoc;

    private LinearLayout linearLayoutDoc;
    private LinearLayout dashboardNestedscrollview;
    public TextView contNameTextView;
    public TextView contStateTextView;
    public TextView contDateTextView;
    public TextView contDetailTextView;
    public Button docRegButton;
    public ImageView backImageview;

    private String contIdx;
    public static int pageNum;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;

    public static ArrayList<ServerDataDoc> ArrayListDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ready);
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
        docRegButton = findViewById(R.id.doc_reg_button);
        backImageview = findViewById(R.id.back_imageview);

        docRegButton.setOnClickListener(this);
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
            case R.id.doc_reg_button :
                DisplayDialog_ProfilePhoto();
                break;
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
        httpAsyncRequest.AddHeaderData("order", "b");
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_DOC_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
    }
    // 문서 등록 -3
    private void requestHttpDataDocReg(String ImagePath) {
        String ConvertedFilePath = ActivityImageSelector.GetDefaultProcessingBitmapImage(this, ImagePath, ActivityImageSelector.IMAGE_TYPE_NORMAL);
        String ServerFileName = HTTP_DEFINE.GetUploadImageFileName( COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE, Integer.parseInt(sessionManager.getMemIdx()), ".jpg" );
        if( ConvertedFilePath.length() > 0 ) {
            httpAsyncRequest.SetUseProgress( true );
            httpAsyncRequest.UploadPostImage(String.format(HTTP_DEFINE.HTTP_URL_DOC_REG, contIdx, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 3, ConvertedFilePath, "picture", ServerFileName);
        }
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
        if(Success) {
            if(RequestCode == 1) parseJsonContDetail(ResultData);	// 계약 상세 - 1
            if(RequestCode == 2) parseJsonDocList(ResultData);		// 문서 리스트 - 2
            if(RequestCode == 3) parseJsonDocReg(ResultData);		// 문서 등록 - 3
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

            int doc_before_cnt = Integer.parseInt(jsonService.GetString("doc_before_cnt", "0"));
            if (doc_before_cnt > 0) {
                contStateTextView.setText("등록중 (" + jsonService.GetString("doc_before_cnt", null) + ")");
                contStateTextView.setTextColor(this.getResources().getColor(R.color.colorAccent));
            } else contStateTextView.setText("등록 대기");

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
                GVADoc = new GridViewAdapterDoc(this, ArrayListDoc, layerWidth);
                gridViewDoc.setAdapter(GVADoc);
            }

            GVADoc.notifyDataSetChanged();

            linearLayoutDoc.setVisibility(View.VISIBLE);
        } else {
            if (pageNum == 0) {
                linearLayoutDoc.setVisibility(View.GONE);
            } else pageNum--;
        }
    }
    // 문서 등록 - 3
    private void parseJsonDocReg(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 등록됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /************************************ 액티비티 실행 결과 수신 ************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch(requestCode) {
                case 1 :
                    if(resultCode == Activity.RESULT_OK) {
                        CURRENT_SELECTING_PROFILE_PHOTO_PATH = data.getStringExtra( ActivityImageSelector.INTENT_DATA_SELECTED_IMAGE_PATH );
                        CropImage.ActivityBuilder CropImageBuilder = CropImage.activity( Uri.fromFile( new File( CURRENT_SELECTING_PROFILE_PHOTO_PATH ) ) );
                        CropImageBuilder.setAspectRatio(3, 4);
                        CropImageBuilder.setAllowRotation( false );
                        CropImageBuilder.setAllowFlipping( false );
                        CropImageBuilder.setCropMenuCropButtonIcon(R.drawable.common_icon_done_whte);
                        CropImageBuilder.start(this);
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE :
                    CropImage.ActivityResult CropResult = CropImage.getActivityResult( data );
                    if(resultCode == Activity.RESULT_OK) {
                        CURRENT_SELECTING_PROFILE_PHOTO_PATH = CropResult.getUri().getPath();
                                //data.getStringExtra(ActivityImageSelector.INTENT_DATA_SELECTED_IMAGE_PATH);
                        requestHttpDataDocReg(CURRENT_SELECTING_PROFILE_PHOTO_PATH);
                    }
                    break;
            }
        } catch( Exception e ) {
            if(BuildConfig.DEBUG) e.printStackTrace();
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    /**************************************** 액티비티 출력 *******************************************/
    private void DisplayActivity_SelectPhoto(int SelectedType ) {
        Intent IntentInstance = new Intent(this, ActivityImageSelector.class);
        IntentInstance.putExtra(ActivityImageSelector.INTENT_DATA_SELECTED_TYPE, SelectedType);
        startActivityForResult(IntentInstance, 1);
    }

    /**************************** 프로필 사진 다이얼로그 선택 분기 실행 *****************************/
    private void BranchExecutionProfilePhotoDialog(int DialogItemIndex ) {
        String [] ProfilePhotoBasisItemList = getResources().getStringArray(R.array.photo_select);
        if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get(DialogItemIndex).equals( ProfilePhotoBasisItemList[0] ) ) {		// 앨범
            DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_GALLERY);
        }
        else if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[1])) {	// 카메라
            DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_CAMERA);
        }
    }

    /*************************************** 다이얼로그 출력 ******************************************/
    private void DisplayDialog_ProfilePhoto() {
        Dialog_Common DialogBuilder = new Dialog_Common(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        DialogBuilder.setTitle("선택");
        SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList( getResources().getStringArray(R.array.photo_select)));
        DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BranchExecutionProfilePhotoDialog(which);
                dialog.dismiss();
            }
        });
        DialogBuilder.setOnNegativeButtonClickListener("취소", null);
        DialogBuilder.show();
    }
}
