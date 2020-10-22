package com.kt.SmartStamp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.kt.SmartStamp.adapter.GridViewAdapterDocReady;
import com.kt.SmartStamp.customview.Dialog_Common;
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
import java.util.Arrays;

public class DetailReadyActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER, AdapterView.OnItemClickListener {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private ArrayList<ServerDataPhoto> SELECTED_PROFILE_PHOTO_LIST;
    private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 사진 선택 다이얼로그 가변 적용 보관 변수
    private String CURRENT_SELECTING_PROFILE_PHOTO_PATH;										// 선택중인 사진의 경로 (사진 업로드, 수정 상태에서만 일시적으로 사용됨)

    private static final long MIN_CLICK_INTERVAL = 1000;
    private long mLastClickTime;
    private int offset = 0;

    public static GridViewAdapterDocReady GVADoc;
    private GridView gridViewDoc;

    private LinearLayout linearLayoutDoc;
    private LinearLayout dashboardNestedscrollview;
    public TextView contNameTextView;
    public TextView contStateTextView;
    public TextView contDateTextView;
    public TextView contDetailTextView;
    public Button docRegButton;
    public Button docCompleteButton;
    public ImageView backImageview;

    private String contIdx;
    private String modifyDocBefIdx;
    private int dposition;
    private String modifyFl = "n";
    public static int pageNum;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;

    public ArrayList<ServerDataDoc> ArrayListDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ready);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();

        SELECTED_PROFILE_PHOTO_LIST = new ArrayList<>();

        gridViewDoc = findViewById(R.id.GridView_Doc);
        gridViewDoc.setOnItemClickListener(this);
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
        docCompleteButton = findViewById(R.id.doc_complete_button);
        backImageview = findViewById(R.id.back_imageview);

        docRegButton.setOnClickListener(this);
        docCompleteButton.setOnClickListener(this);
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
                modifyFl = "n";
                DisplayDialog_Doc();
                break;
            case R.id.doc_complete_button :
                DisplayDialog_Doc_Com();
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
                modifyDocBefIdx = view.getTag().toString();
                DisplayDialog_Doc_Act();
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
    // 문서 수정 -4
    private void requestHttpDataDocModify(String ImagePath) {
        String ConvertedFilePath = ActivityImageSelector.GetDefaultProcessingBitmapImage(this, ImagePath, ActivityImageSelector.IMAGE_TYPE_NORMAL);
        String ServerFileName = HTTP_DEFINE.GetUploadImageFileName( COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE, Integer.parseInt(sessionManager.getMemIdx()), ".jpg" );
        if( ConvertedFilePath.length() > 0 ) {
            httpAsyncRequest.SetUseProgress( true );
            httpAsyncRequest.UploadPostImage(String.format(HTTP_DEFINE.HTTP_URL_DOC_MOD, modifyDocBefIdx, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 4, ConvertedFilePath, "picture", ServerFileName);
        }
    }
    // 문서 삭제 -5
    private void requestHttpDataDocDelete() {
        httpAsyncRequest.AddHeaderData("doc_bef_idx", modifyDocBefIdx);
        httpAsyncRequest.RequestHttpPatchData(String.format(HTTP_DEFINE.HTTP_URL_DOC_DEL, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 5);
    }
    // 문서 등록 완료 -6
    private void requestHttpDataDocComplete() {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("com_state", "r");
        httpAsyncRequest.RequestHttpPatchData(String.format(HTTP_DEFINE.HTTP_URL_CONT_COMPLETE, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 6);
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
            if(RequestCode == 6) parseJsonDocCom(ResultData);		// 문서 등록 완료 - 6
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
        if (jsonData != null) {
            jsonService.CreateJSONArray(jsonData);
            if (pageNum == 0) ArrayListDoc.clear();

            while(jsonService.SetNextNode()) {
                ArrayListDoc.add(jsonService.GetClass(ServerDataDoc.class));
            }

            if (pageNum == 0) {
                GVADoc = new GridViewAdapterDocReady(this, ArrayListDoc, layerWidth);
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
    // 문서 등록 완료 - 6
    private void parseJsonDocCom(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
            Toast.makeText(this, "문서 등록이 완료됐습니다.", Toast.LENGTH_SHORT).show();
            finish();
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

    /**************************************** 액티비티 출력 *******************************************/
    private void DisplayActivity_SelectPhoto(int SelectedType, int requestCode) {
        Intent IntentInstance = new Intent(this, ActivityImageSelector.class);
        IntentInstance.putExtra(ActivityImageSelector.INTENT_DATA_SELECTED_TYPE, SelectedType);
        startActivityForResult(IntentInstance, requestCode);
    }

    /**************************** 사진 등록 다이얼로그 선택 분기 실행 *****************************/
    private void BranchExecutionPhotoDialog(int DialogItemIndex) {
        String [] ProfilePhotoBasisItemList = getResources().getStringArray(R.array.photo_select);
        if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get(DialogItemIndex).equals( ProfilePhotoBasisItemList[0] ) ) {		// 앨범
            DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_GALLERY, 1);
        }
        else if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[1])) {	// 카메라
            DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_CAMERA, 1);
        }
    }

    /**************************** 사진 액션 다이얼로그 선택 분기 실행 *****************************/
    private void ActionPhotoDialog(int DialogItemIndex ) {
        String [] ProfilePhotoBasisItemList = getResources().getStringArray(R.array.photo_act);
        if (SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get(DialogItemIndex).equals( ProfilePhotoBasisItemList[0])) {		        // 확대보기
            if(ArrayListDoc.size() > 0) {
                ArrayList<String> ArrayList_MultiView_ProfileImage = new ArrayList<>();
                ArrayList_MultiView_ProfileImage.add(ArrayListDoc.get(dposition).bef_picture);
                Intent IntentInstance = new Intent(this, Activity_ImageViewer_Multi.class);
                IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_IMAGE_PATH_LIST, ArrayList_MultiView_ProfileImage);
                IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_ITEM_POSITION, 0);
                startActivity(IntentInstance);
            }
        } else if (SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[1])) {	    // 수정
            modifyFl = "y";
            DisplayDialog_Doc();
        } else if (SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[2])) {    	// 삭제
            DisplayDialog_Doc_Del();
        }
    }

    /*************************************** 다이얼로그 출력 ******************************************/
    private void DisplayDialog_Doc() {
        Dialog_Common DialogBuilder = new Dialog_Common(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        DialogBuilder.setTitle("선택");
        SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList( getResources().getStringArray(R.array.photo_select)));
        DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BranchExecutionPhotoDialog(which);
                dialog.dismiss();
            }
        });
        DialogBuilder.setOnNegativeButtonClickListener("취소", null);
        DialogBuilder.show();
    }
    private void DisplayDialog_Doc_Complete() {
        Dialog_Common DialogBuilder = new Dialog_Common(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        DialogBuilder.setTitle("선택");
        SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList( getResources().getStringArray(R.array.photo_act)));
        DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActionPhotoDialog(which);
                dialog.dismiss();
            }
        });
        DialogBuilder.setOnNegativeButtonClickListener("취소", null);
        DialogBuilder.show();
    }
    private void DisplayDialog_Doc_Act() {
        Dialog_Common DialogBuilder = new Dialog_Common(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        DialogBuilder.setTitle("선택");
        SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList( getResources().getStringArray(R.array.photo_act)));
        DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActionPhotoDialog(which);
                dialog.dismiss();
            }
        });
        DialogBuilder.setOnNegativeButtonClickListener("취소", null);
        DialogBuilder.show();
    }
    private void DisplayDialog_Doc_Com() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle("등록 완료 확인");
        builder.setMessage("문서 등록을 완료하시겠습니까? 문서 등록 완료 후에는 수정이 불가합니다.");
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
    private void DisplayDialog_Doc_Del() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle("문서 삭제 확인");
        builder.setMessage("문서를 삭제하시겠습니까? 삭제된 문서는 복구가 불가합니다.");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestHttpDataDocDelete();
            }
        });
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
        TextView textView = theAlertDialog.findViewById(android.R.id.message);
        textView.setTextSize(15.0f);
    }
}
