package com.kt.SmartStamp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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
import com.kt.SmartStamp.data.AppVariables;
import com.kt.SmartStamp.data.ServerDataDoc;
import com.kt.SmartStamp.data.ServerDataPhoto;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.BleService;
import com.kt.SmartStamp.service.GpsTracker;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailListActivity extends AppCompatActivity implements View.OnClickListener, HTTP_RESULT_LISTENER, AdapterView.OnItemClickListener {
    public static HTTP_ASYNC_REQUEST httpAsyncRequest;
    public static SessionManager sessionManager;
    private JSONService jsonService;

    private ArrayList<ServerDataPhoto> SELECTED_PROFILE_PHOTO_LIST;
    private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 사진 선택 다이얼로그 가변 적용 보관 변수
    private String CURRENT_SELECTING_PROFILE_PHOTO_PATH;										// 선택중인 사진의 경로 (사진 업로드, 수정 상태에서만 일시적으로 사용됨)

    ProgressDialog progressDialog;

    private static final long MIN_CLICK_INTERVAL = 1000;
    private static final long MIN_BTN_CLICK_INTERVAL = 3000;
    private static final long MIN_OPEN_INTERVAL = 8000;
    private static final long MIN_CLOSE_INTERVAL = 10000;
    private long mLastClickTime;
    private int offset = 0;
    private int doc_after_cnt = 0;
    private int doc_before_cnt = 0;

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
    public ImageView imageviewBattery;
    public TextView textviewBattery;

    private String contIdx;
    public String stampStatus = "";
    public String buttonType = "";
    private String mac = "";
    private int dposition;
    public static int pageNum;
    int layerWidth = 0;
    boolean lastItemVisibleFlag = false;
    private String modifyFl = "n";
    private String modifyDocBefIdx;

    public ArrayList<ServerDataDoc> ArrayListDoc;
    public static Context mContext;
    private static GpsTracker gpsTracker;

    final static int BT_REQUEST_ENABLE = 100;
    private static Intent gattServiceIntent = null;
    private boolean mIsBound;
    private BleService mBleService = null;
    public boolean isService = false;
    public BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_detail_list);
        httpAsyncRequest = new HTTP_ASYNC_REQUEST(this, COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
        sessionManager = new SessionManager(this);
        jsonService = new JSONService();
        mContext = this;

        AppVariables.isRunServiceMainView = false;
        progressDialog = new ProgressDialog(DetailListActivity.this);

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
        imageviewBattery = findViewById(R.id.imageview_battery);
        textviewBattery = findViewById(R.id.textview_battery);

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

    /*************************************** onDestroy ***************************************/
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ("open".equals(stampStatus)) {
            stampStatus = "close";
            requestHttpDataStamp();
        }

        AppVariables.device = null;
        if (mBleService != null) {
            setUnbindService();
        }
    }

    /*************************************** onBackPressed ***************************************/
    @Override
    public void onBackPressed() {
        if ("open".equals(stampStatus)) {
            stampStatus = "close";
            mBleService.sendClose();
            mBleService.sendOff();
            requestHttpDataStamp();
            loading();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            loadingEnd();
                            finish();
                        }
                    },
                    MIN_CLOSE_INTERVAL
            );
        } else {
            finish();
        }
    }

    public void loading() {
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        if ("open".equals(stampStatus)) {
            progressDialog.setMessage("인장 여는중...");
        } else if ("close".equals(stampStatus)) {
            progressDialog.setMessage("인장 닫는중...");
        } else {
            progressDialog.setMessage("잠시만 기다려 주세요");
        }
        progressDialog.show();
    }

    public void loadingEnd() {
        progressDialog.dismiss();
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
        if(elapsedTime<=MIN_BTN_CLICK_INTERVAL){
            return;
        }
        mLastClickTime=currentClickTime;

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        switch(view.getId()) {
            case R.id.back_imageview :
                if ("open".equals(stampStatus)) {
                    stampStatus = "close";
                    mBleService.sendClose();
                    mBleService.sendOff();
                    requestHttpDataStamp();
                    loading();
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    loadingEnd();
                                    finish();
                                }
                            },
                            MIN_CLOSE_INTERVAL
                    );
                } else {
                    finish();
                }
                break;
            case R.id.start_sign :
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialogForLocationServiceSetting("start_sign");
                } else if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(),"블루투스가 활성화 되어 있지 않습니다",Toast.LENGTH_SHORT).show();
                    Intent intentBthEnable = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentBthEnable,BT_REQUEST_ENABLE);
                } else {
                    buttonType = "open";
                    if ("open".equals(stampStatus)) {
                        stampStatus = "close";
                        mBleService.sendClose();
                        requestHttpDataStamp();
                        loading();
                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        loadingEnd();
                                        startSign.setText("인장 열기");
                                    }
                                },
                                MIN_CLOSE_INTERVAL
                        );
                    } else {
                        requestHttpDataStampCheck();
                    }
                }
                break;
            case R.id.doc_complete_button :
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialogForLocationServiceSetting("doc_complete_button");
                } else if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(),"블루투스가 활성화 되어 있지 않습니다",Toast.LENGTH_SHORT).show();
                    Intent intentBthEnable = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentBthEnable,BT_REQUEST_ENABLE);
                } else {
                    buttonType = "close";
                    if (doc_after_cnt == doc_before_cnt) {
                        requestHttpDataStampCheck();
                    } else Toast.makeText(this, "모든 날인문서를 등록해야 날인 완료가 가능합니다.", Toast.LENGTH_SHORT).show();
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
    // 인장 사용 가능 체크 - 7
    private void requestHttpDataStampCheck() {
        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_STAMP_CHECK, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 7);
    }
    // 인장 사용 - 8
    private void requestHttpDataStamp() {
        gpsTracker = new GpsTracker(this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        httpAsyncRequest.AddHeaderData("cont_idx", contIdx);
        httpAsyncRequest.AddHeaderData("stamp_idx", mac);
        httpAsyncRequest.AddHeaderData("status", stampStatus);
        httpAsyncRequest.AddHeaderData("addr", getCurrentAddress(latitude, longitude));
        httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_STAMP, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 8);
    }

    /*************************************** Http 요청결과 수신 ***************************************/
    @Override
    public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
        if (Success) {
            if (RequestCode == 1) parseJsonContDetail(ResultData);	    // 계약 상세 - 1
            if (RequestCode == 2) parseJsonDocList(ResultData);	    // 문서 리스트 - 2
            if (RequestCode == 3) parseJsonDocReg(ResultData);		    // 문서 등록 - 3
            if (RequestCode == 4) parseJsonDocMod(ResultData);		    // 문서 수정 - 4
            if (RequestCode == 5) parseJsonDocDel(ResultData);		    // 문서 삭제 - 5
            if (RequestCode == 6) parseJsonDocCom(ResultData);		    // 날인 완료 - 6
            if (RequestCode == 7) parseJsonStampCheck(ResultData);  	// 인장 사용 가능 체크- 7
            if (RequestCode == 8) parseJsonStamp(ResultData);		    // 인장 사용 - 8
        } else Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    /**************************************** JsonData 변환 *******************************************/
    // 계약 상세 - 1
    private void parseJsonContDetail(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if (jsonService != null) {
            contNameTextView.setText(jsonService.GetString("cont_name", null));
            contDateTextView.setText("반출 기간 : " + jsonService.GetString("appr_st_dt", null)
                    + " ~ " + jsonService.GetString("appr_ed_dt", null));
            contDetailTextView.setText(jsonService.GetString("cont_detail", null));

            doc_after_cnt = Integer.parseInt(jsonService.GetString("doc_after_cnt", null));
            doc_before_cnt = Integer.parseInt(jsonService.GetString("doc_before_cnt", null));
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

        if (jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 등록됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 문서 수정 - 4
    private void parseJsonDocMod(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if (jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 수정됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 문서 삭제 - 5
    private void parseJsonDocDel(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if (jsonService != null) {
            pageNum = 0;
            requestHttpDataContDetail();

            Toast.makeText(this, "문서가 삭제됐습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 날인 완료 - 6
    private void parseJsonDocCom(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if (jsonService != null) {
            Toast.makeText(this, "날인이 완료됐습니다.", Toast.LENGTH_SHORT).show();
            if ("open".equals(stampStatus)) {
                stampStatus = "close";
                mBleService.sendClose();
                mBleService.sendOff();
                requestHttpDataStamp();
                loading();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                loadingEnd();
                                finish();
                            }
                        },
                        MIN_CLOSE_INTERVAL
                );
            } else {
                finish();
            }

            ((MainActivity)MainActivity.mContext).displayFragment(2);
            ((MainActivity)MainActivity.mContext).navView.setSelectedItemId(R.id.navigation_complete_list);
        }
    }
    // 인장 사용 가능 체크 - 7
    private void parseJsonStampCheck(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if (jsonService != null) {
            mac = jsonService.GetString("stamp_idx", null);

            if (isService) {
                if ("open".equals(buttonType)) {
                    stampStatus = "open";
                    mBleService.sendOpen();
                    requestHttpDataStamp();
                    loading();
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    loadingEnd();
                                    startSign.setText("인장 닫기");
                                }
                            },
                            MIN_OPEN_INTERVAL
                    );
                } else if ("close".equals(buttonType)) {
                    DisplayDialog_Doc_Com();
                }
            } else {
                Intent IntentInstance = new Intent(this, ConnectActivity.class);
                IntentInstance.putExtra("mac", mac);
                startActivityForResult(IntentInstance, 2);
            }
        } else {
            Toast.makeText(this, "등록이 안된 인장입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 인장 사용 - 8
    private void parseJsonStamp(String jsonData) {
        jsonService.CreateJSONObject(jsonData);

        if(jsonService != null) {
        }
    }

    private void setStartService() {
        gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(gattServiceIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceMessageReceiver, new IntentFilter(AppVariables.EXTRA_SERVICE_DATA));

        mIsBound = true;
    }
    private void setUnbindService() {
        if(mIsBound){
            unbindService(mServiceConnection);
            mIsBound=false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceMessageReceiver);
        stopService(gattServiceIntent);
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.LocalBinder mBle = (BleService.LocalBinder)service;
            mBleService  = mBle.getService();
            mBleService.initPrepare();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };
    private BroadcastReceiver mServiceMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBleService.bExeThread = false;

            String action = intent.getStringExtra("action");
            if ("connect".equals(action)) {
                isService = true;
                startSign.setText("인장 열기");
                startSign.setBackgroundResource(R.drawable.btn_rounded_primary_fill);
            } else if ("disconnect".equals(action)) {
                isService = false;
                startSign.setText("인장 연결");
                startSign.setBackgroundResource(R.drawable.btn_rounded_gray_fill);
                imageviewBattery.setImageResource(R.drawable.not_connect);
                textviewBattery.setVisibility(View.GONE);
            } else if ("battery".equals(action)) {
                int battery = Integer.parseInt(intent.getStringExtra("battery"));
                if (battery > 75) {
                    imageviewBattery.setImageResource(R.drawable.ic_100per);
                } else if (battery > 50) {
                    imageviewBattery.setImageResource(R.drawable.ic_75per);
                } else if (battery > 25) {
                    imageviewBattery.setImageResource(R.drawable.ic_50per);
                } else {
                    imageviewBattery.setImageResource(R.drawable.ic_25per);
                    if (battery <= 10) DisplayDialog_Low_Battery();
                }

                textviewBattery.setVisibility(View.VISIBLE);
                textviewBattery.setText(Integer.toString(battery) + "%");
            }

            mBleService.bExeThread = true;
        }
    };

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
                        CropImageBuilder.setAllowRotation(true);
                        CropImageBuilder.setAllowFlipping(false);
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
                case BT_REQUEST_ENABLE:
                    if (resultCode == RESULT_OK) {
                    } else if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(getApplicationContext(), "블루투스 연결 후 이용이 가능합니다.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    if (AppVariables.isRunServiceMainView) {
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        progressDialog.setMessage("연결중...");

                        setStartService();

                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                },
                                2000
                        );
                    }
            }
        } catch( Exception e ) {}
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

    private void DisplayDialog_Low_Battery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle("배터리 부족 안내");
        builder.setMessage("배터리가 부족합니다. 배터리를 교체해주세요.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            builder.setMessage("인장을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 사용하시겠습니까?");
        } else if ("doc_complete_button".equals(type)) {
            builder.setMessage("날인을 완료하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 사용하시겠습니까?");
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

    public static String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            //Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString();
    }

}
