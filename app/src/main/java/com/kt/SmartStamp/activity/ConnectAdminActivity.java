package com.kt.SmartStamp.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.data.AppVariables;

import java.util.List;

public class ConnectAdminActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mScanning;
    private Handler mHandler;

    private ScanCallback mScanCallback = null;
    private BleScanListner mBleScanListner;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private String[] permisionlist = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}; //내 앱에 부여할 퍼미션
    private final int PERMISSION_REQ_CODE = 225;

    private TextView txtInfo = null;
    private String deviceName = "";
    public static final int ANIMATION_DELAY_TIME = 500;
    private Animation buttonAnimation;
    public String mac = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_connect);

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button_animation);

        Button btnGoToMain = (Button)findViewById(R.id.btnGoToMain);

        final ImageView btnReScan = (ImageView)findViewById(R.id.btnReScan);

        txtInfo = (TextView)findViewById(R.id.txtInfo);
        txtInfo.setText("인장을 찾을 수 없습니다.");

        Intent IntentInstance = getIntent();

        if(IsPermision() == true) {
            mHandler = new Handler();
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "Bluetooth 기기를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        btnGoToMain.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if (checkDeviceName()) {
                    stopScan();
                    AppVariables.isRunServiceMainView = true;
                    Intent intent = new Intent();
                    intent.putExtra("mac", mac);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        btnReScan.setOnClickListener(new ImageView.OnClickListener(){
            @Override
            public void onClick(View view){
                view.startAnimation(buttonAnimation);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(mScanning){
                            scanLeDevice(false);
                        }
                        scanLeDevice(true);
                    }
                }, ANIMATION_DELAY_TIME < 0 ? 0 : ANIMATION_DELAY_TIME );
            }
        });

        scanLeDevice(true);
    }

    private boolean checkDeviceName(){
        boolean bResult = false;
        if(AppVariables.Device_Name.isEmpty() && ! deviceName.isEmpty()){
            AppVariables.Device_Name = deviceName;
        }
        if(!AppVariables.Device_Name.equals(deviceName) && !deviceName.isEmpty()){
            AppVariables.Device_Name = deviceName;
        }

        Log.i("---------->",AppVariables.Device_Name);

        if(AppVariables.device !=null) bResult = true;
        return bResult;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            startScan();
        } else {
            mScanning = false;
            stopScan();
        }
    }
    public boolean startScan() {
        if(mBluetoothAdapter == null) return false;

        if(mBluetoothAdapter.isEnabled() == false)
            mBluetoothAdapter.enable();

        try {
            if(mScanCallback == null) makeCallBakc();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            else
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void stopScan() {
        if(mBluetoothAdapter == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        else
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private void makeCallBakc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    scanProcess(result.getDevice());
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }
                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    scanProcess(bluetoothDevice);
                }
            };
        }
    }

    private void scanProcess(BluetoothDevice device) {
        if(device == null) return;
        String deviceName= device.getName();
        if(deviceName == null) return;

        if (deviceName.trim().equals("SMART STAMP")) {
            AppVariables.device = device;
            mac = device.getAddress();
            txtInfo.setText(mac);
            stopScan();
        }

        if(mBleScanListner != null)
            mBleScanListner.scanResult(device);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            };

    private boolean IsPermision()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            for(String permision : permisionlist)
            {
                int permisionState = checkSelfPermission(permision);
                if(permisionState == -1)
                {
                    requestPermissions(permisionlist, PERMISSION_REQ_CODE); //권한 요청
                    return false;
                }
            }
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {

                }else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(getApplicationContext(), "bluetooth를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            try {
                boolean bPemision = true;

                for (int i = 0; i < permisionlist.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        bPemision = false;
                        break;
                    }
                }

                if(bPemision == true) {}
                else Toast.makeText(this, "권한요청이 정상적으로 이루어 지지않아 앱을 종료 합니다.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {}
        }
    }

    public void setScanListner(BleScanListner bleScanListner) {
        mBleScanListner = bleScanListner;
    }

    public interface BleScanListner {
        public void scanResult(BluetoothDevice device);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mScanning) scanLeDevice(false);
        if( mBleScanListner !=null) mBleScanListner = null;
        if (mBluetoothAdapter != null) mBluetoothAdapter = null;
    }

}

