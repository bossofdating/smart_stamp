package com.kt.SmartStamp.fragment;

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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.activity.ConnectActivity;
import com.kt.SmartStamp.activity.ConnectAdminActivity;
import com.kt.SmartStamp.data.AppVariables;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.BleService;
import com.kt.SmartStamp.service.GpsTracker;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FragmentMainAdmin extends Fragment implements HTTP_RESULT_LISTENER, View.OnClickListener {
	public static HTTP_ASYNC_REQUEST httpAsyncRequest;
	public static SessionManager sessionManager;
	private JSONService jsonService;
	public static Context mContext;

	ProgressDialog progressDialog;

	public Button stampButton;
	public ImageView imageviewBattery;
	public TextView textviewBattery;

	private long mLastClickTime;
	private static final long MIN_BTN_CLICK_INTERVAL = 3000;
	private static final long MIN_OPEN_INTERVAL = 8000;
	private static final long MIN_CLOSE_INTERVAL = 10000;

	private static GpsTracker gpsTracker;

	public BluetoothAdapter mBluetoothAdapter;
	private BleService mBleService = null;
	public boolean isService = false;
	private static Intent gattServiceIntent = null;
	private boolean mIsBound;

	final static int BT_REQUEST_ENABLE = 100;
	public String stampStatus = "";
	public String buttonType = "";
	private String mac = "";

	@Override
	public void onAttach(Context context) {
		super.onAttach( context );
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View FragmentView = inflater.inflate(R.layout.fragment_main_admin, container, false);
		httpAsyncRequest = new HTTP_ASYNC_REQUEST(getActivity(), COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
		sessionManager = new SessionManager(getActivity());
		jsonService = new JSONService();
		mContext = getActivity();

		stampButton = FragmentView.findViewById(R.id.stamp_button);
		imageviewBattery = FragmentView.findViewById(R.id.imageview_battery);
		textviewBattery = FragmentView.findViewById(R.id.textview_battery);

		stampButton.setOnClickListener(this);

		displayLayoutDefault();

		return FragmentView;
	}

	@Override
	public void onStart() { super.onStart();	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		AppVariables.device = null;
		if (mBleService != null) {
			setUnbindService();
		}
	}
	@Override
	public void onDetach() {
		super.onDetach();

		if ("open".equals(stampStatus)) {
			stampStatus = "close";
			requestHttpDataStamp();
		}

		AppVariables.device = null;
		if (mBleService != null) {
			setUnbindService();
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); }

	/**************************************** 레이아웃 출력 *******************************************/
	private void displayLayoutDefault() {
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

		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		switch(view.getId()) {
			case R.id.stamp_button :
				if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					showDialogForLocationServiceSetting();
				} else if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
					Toast.makeText(getContext(),"블루투스가 활성화 되어 있지 않습니다",Toast.LENGTH_SHORT).show();
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
										stampButton.setText("인장 열기");
										stampButton.setBackgroundResource(R.drawable.btn_rounded_primary_fill);
									}
								},
								MIN_CLOSE_INTERVAL
						);
					} else {
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
												stampButton.setText("인장 닫기");
												stampButton.setBackgroundResource(R.drawable.btn_rounded_red_fill);
											}
										},
										MIN_OPEN_INTERVAL
								);
							} else if ("close".equals(buttonType)) {
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
												}
											},
											MIN_CLOSE_INTERVAL
									);
								}
							}
						} else {
							Intent IntentInstance = new Intent(getActivity(), ConnectAdminActivity.class);
							startActivityForResult(IntentInstance, 2);
						}
					}
				}
				break;
		}
	}

	/************************************ 액티비티 실행 결과 수신 ************************************/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			switch(requestCode) {
				case BT_REQUEST_ENABLE:
					if (resultCode == getActivity().RESULT_OK) {
					} else if (resultCode == getActivity().RESULT_CANCELED) {
						Toast.makeText(getContext(), "블루투스 연결 후 이용이 가능합니다.", Toast.LENGTH_LONG).show();
					}
					break;
				case 2:
					if (AppVariables.isRunServiceMainView) {
						mac = data.getStringExtra("mac");
						setStartService();
					}
			}
		} catch( Exception e ) {
			if(BuildConfig.DEBUG) e.printStackTrace();
		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	/************************************* HTTP  데이터요청 ******************************************/
	private void requestHttpDataStamp() {
		gpsTracker = new GpsTracker(getActivity());
		double latitude = gpsTracker.getLatitude();
		double longitude = gpsTracker.getLongitude();

		httpAsyncRequest.AddHeaderData("cont_idx", "0");
		httpAsyncRequest.AddHeaderData("stamp_idx", mac);
		httpAsyncRequest.AddHeaderData("status", stampStatus);
		httpAsyncRequest.AddHeaderData("addr", getCurrentAddress(latitude, longitude));
		httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_STAMP, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 8);
	}

	/*************************************** Http 요청결과 수신 ***************************************/
	@Override
	public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
		if(Success) {
			if(RequestCode == 1) {}//
			if(RequestCode == 2) {}//
		} else Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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

	// GPS 사용
	public static void showDialogForLocationServiceSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogStyle);
		builder.setTitle("위치 정보");
		builder.setMessage("인장을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 사용하시겠습니까?");
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

	/************************************** 블루투스 관련 ****************************************/
	private void setStartService() {
		gattServiceIntent = new Intent(getActivity(), BleService.class);
		mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		mContext.startService(gattServiceIntent);

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mServiceMessageReceiver, new IntentFilter(AppVariables.EXTRA_SERVICE_DATA));

		mIsBound = true;
	}
	private void setUnbindService() {
		if(mIsBound){
			mContext.unbindService(mServiceConnection);
			mIsBound=false;
		}
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mServiceMessageReceiver);
		mContext.stopService(gattServiceIntent);
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
				stampButton.setText("인장 열기");
				stampButton.setBackgroundResource(R.drawable.btn_rounded_primary_fill);
			} else if ("disconnect".equals(action)) {
				isService = false;
				stampButton.setText("인장 연결");
				stampButton.setBackgroundResource(R.drawable.btn_rounded_gray_fill);
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
				}
				textviewBattery.setVisibility(View.VISIBLE);
				textviewBattery.setText(Integer.toString(battery) + "%");
			}

			mBleService.bExeThread = true;
		}
	};

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
