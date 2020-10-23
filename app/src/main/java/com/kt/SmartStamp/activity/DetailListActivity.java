package com.kt.SmartStamp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.GridViewAdapterDocList;
import com.kt.SmartStamp.data.ServerDataDoc;
import com.kt.SmartStamp.data.ServerDataPhoto;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;

public class DetailListActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER, AdapterView.OnItemClickListener {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private ArrayList<ServerDataPhoto> SELECTED_PROFILE_PHOTO_LIST;
    private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 사진 선택 다이얼로그 가변 적용 보관 변수
    private String CURRENT_SELECTING_PROFILE_PHOTO_PATH;										// 선택중인 사진의 경로 (사진 업로드, 수정 상태에서만 일시적으로 사용됨)

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;
    private int offset = 0;
    private int doc_after_cnt = 0;

    public static GridViewAdapterDocList GVADoc;
    private GridView gridViewDoc;

    private LinearLayout linearLayoutDoc;
    private LinearLayout dashboardNestedscrollview;
    public TextView contNameTextView;
    public TextView contStateTextView;
    public TextView contDateTextView;
    public TextView contDetailTextView;
    public ImageView backImageview;
    public Button docCompleteButton;
    public Button startSign;

    private String contIdx;
    private int dposition;
    public static int pageNum;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;
    private String modifyFl = "n";
    private String modifyDocBefIdx;

    public ArrayList<ServerDataDoc> ArrayListDoc;

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();
        mContext = this;

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
        docCompleteButton = findViewById(R.id.doc_complete_button);
        startSign = findViewById(R.id.start_sign);
        backImageview = findViewById(R.id.back_imageview);

        docCompleteButton.setOnClickListener(this);
        startSign.setOnClickListener(this);
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

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        switch(view.getId()) {
            case R.id.back_imageview :
                finish();
                break;
            case R.id.start_sign :
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialogForLocationServiceSetting("start_sign");
                } else {
                    requestHttpDataStamp("test:1234:5678", "open", "서울시 강동구");
                }
                break;
            case R.id.doc_complete_button :
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialogForLocationServiceSetting("doc_complete_button");
                } else {
                    if (doc_after_cnt > 0) DisplayDialog_Doc_Com();
                    else Toast.makeText(this, "등록된 날인 문서가 없어서 날인을 완료할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
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
        httpAsyncRequest.AddHeaderData("order", "r");
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_DOC_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
    }
    // 문서 등록 -3
    private void requestHttpDataDocReg(String ImagePath) {
        String ConvertedFilePath = ActivityImageSelector.GetDefaultProcessingBitmapImage(this, ImagePath, ActivityImageSelector.IMAGE_TYPE_NORMAL);
        String ServerFileName = HTTP_DEFINE.GetUploadImageFileName( COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE, Integer.parseInt(sessionManager.getMemIdx()), ".jpg" );
        if( ConvertedFilePath.length() > 0 ) {
            httpAsyncRequest.SetUseProgress( true );
            httpAsyncRequest.UploadPostImage(String.format(HTTP_DEFINE.HTTP_URL_DOC_REG_AFT, contIdx, ArrayListDoc.get(Integer.parseInt(modifyDocBefIdx)).doc_bef_idx, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 3, ConvertedFilePath, "picture", ServerFileName);
        }
    }
    // 문서 수정 -4
    private void requestHttpDataDocModify(String ImagePath) {
        String ConvertedFilePath = ActivityImageSelector.GetDefaultProcessingBitmapImage(this, ImagePath, ActivityImageSelector.IMAGE_TYPE_NORMAL);
        String ServerFileName = HTTP_DEFINE.GetUploadImageFileName( COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE, Integer.parseInt(sessionManager.getMemIdx()), ".jpg" );
        if( ConvertedFilePath.length() > 0 ) {
            httpAsyncRequest.SetUseProgress( true );
            httpAsyncRequest.UploadPostImage(String.format(HTTP_DEFINE.HTTP_URL_DOC_MOD_AFT, ArrayListDoc.get(Integer.parseInt(modifyDocBefIdx)).doc_aft_idx, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 4, ConvertedFilePath, "picture", ServerFileName);
        }
    }
    // 문서 삭제 -5
    public void requestHttpDataDocDelete(String modifyDocBefIdxParam) {
        httpAsyncRequest.AddHeaderData("doc_aft_idx", ArrayListDoc.get(Integer.parseInt(modifyDocBefIdxParam)).doc_aft_idx);
        httpAsyncRequest.RequestHttpPatchData(String.format(HTTP_DEFINE.HTTP_URL_DOC_DEL_AFT, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 5);
    }
    // 날인 완료 -6
    private void requestHttpDataDocComplete() {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("com_state", "y");
        httpAsyncRequest.RequestHttpPatchData(String.format(HTTP_DEFINE.HTTP_URL_CONT_COMPLETE, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 6);
    }
    // 인장 사용 - 7
    private void requestHttpDataStamp(String stampIdx, String status, String addr) {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("stamp_idx", stampIdx);
        httpAsyncRequest.AddHeaderData("status", status);
        httpAsyncRequest.AddHeaderData("addr", addr);
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_STAMP, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 7);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
        if(Success) {
            if(RequestCode == 1) parseJsonContDetail(ResultData);	// 계약 상세 - 1
            if(RequestCode == 2) parseJsonDocList(ResultData);		// 문서 리스트 - 2
            if(RequestCode == 3) parseJsonDocReg(ResultData);		// 문서 등록 - 3
            if(RequestCode == 4) parseJsonDocMod(ResultData);		// 문서 수정 - 4
            if(RequestCode == 5) parseJsonDocDel(ResultData);		// 문서 삭제 - 5
            if(RequestCode == 6) parseJsonDocCom(ResultData);		// 날인 완료 - 6
            if(RequestCode == 7) parseJsonStamp(ResultData);		// 인장 사용 - 7
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

            doc_after_cnt = Integer.parseInt(jsonService.GetString("doc_after_cnt", null));
            if (doc_after_cnt > 0) {
                contStateTextView.setText("날인중 (" + jsonService.GetString("doc_after_cnt", null) + "/"
                        + jsonService.GetString("doc_before_cnt", null) +  ")");
                contStateTextView.setTextColor(this.getResources().getColor(R.color.colorAccent));
            } else {
                contStateTextView.setText("날인 대기 (" + jsonService.GetString("doc_after_cnt", null) + "/"
                        + jsonService.GetString("doc_before_cnt", null) +  ")");
                contStateTextView.setTextColor(this.getResources().getColor(R.color.colorBlue));
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
                GVADoc = new GridViewAdapterDocList(this, ArrayListDoc, layerWidth);
                gridViewDoc.setAdapter(GVADoc);
            }

            GVADoc.notifyDataSetChanged();
        } else {
            if (pageNum == 0) {
            } else pageNum--;
        }

        linearLayoutDoc.setVisibility(View.VISIBLE);
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
    // 문서 수정 - 4
    private void parseJsonDocMod(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 수정됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 문서 삭제 - 5
    private void parseJsonDocDel(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 삭제됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 날인 완료 - 6
    private void parseJsonDocCom(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            requestHttpDataStamp("test:1234:5678", "close", "서울시 강동구");
            Toast.makeText(this, "날인이 완료됐습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    // 인장 사용 - 7
    private void parseJsonStamp(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
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
                        if ("n".equals(modifyFl)) requestHttpDataDocReg(CURRENT_SELECTING_PROFILE_PHOTO_PATH);
                        else requestHttpDataDocModify(CURRENT_SELECTING_PROFILE_PHOTO_PATH);
                    }
                    break;
            }
        } catch( Exception e ) {
            if(BuildConfig.DEBUG) e.printStackTrace();
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    /**************************** 확대보기 *****************************/
    private void ActionPhotoDialog() {
        if (ArrayListDoc.size() > 0) {
            ArrayList<String> ArrayList_MultiView_ProfileImage = new ArrayList<>();
            ArrayList_MultiView_ProfileImage.add(ArrayListDoc.get(dposition).aft_picture);
            Intent IntentInstance = new Intent(this, Activity_ImageViewer_Multi.class);
            IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_IMAGE_PATH_LIST, ArrayList_MultiView_ProfileImage);
            IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_ITEM_POSITION, 0);
            startActivity(IntentInstance);
        }
    }

    /**************************************** 액티비티 출력 *******************************************/
    public void DisplayActivity_SelectPhoto(int SelectedType, int requestCode, String modifyDocBefIdxparam, String modifyFlparam) {
        modifyDocBefIdx = modifyDocBefIdxparam;
        modifyFl = modifyFlparam;
        Intent IntentInstance = new Intent(this, ActivityImageSelector.class);
        IntentInstance.putExtra(ActivityImageSelector.INTENT_DATA_SELECTED_TYPE, SelectedType);
        startActivityForResult(IntentInstance, requestCode);
    }

    private void DisplayDialog_Doc_Com() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle("날인 완료 확인");
        builder.setMessage("날인을 완료하시겠습니까?\n날인 완료 후에는 수정이 불가합니다.");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestHttpDataDocComplete();
            }
        });
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
        TextView textView = theAlertDialog.findViewById(android.R.id.message);
        textView.setTextSize(15.0f);
    }
    // GPS 사용
    public static void showDialogForLocationServiceSetting(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogStyle);
        builder.setTitle("위치 정보");
        if ("start_sign".equals(type)) {
            builder.setMessage("도장을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 On하시겠습니까?");
        } else if ("doc_complete_button".equals(type)) {
            builder.setMessage("날인을 완료하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 On하시겠습니까?");
        }
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(callGPSSettingIntent);
            }
        });
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
        TextView textView = theAlertDialog.findViewById(android.R.id.message);
        textView.setTextSize(15.0f);
    }
}
