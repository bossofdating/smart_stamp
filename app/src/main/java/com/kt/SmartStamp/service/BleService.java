package com.kt.SmartStamp.service;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kt.SmartStamp.activity.DetailListActivity;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.ble.Profile;
import com.kt.SmartStamp.data.AppVariables;
import com.kt.SmartStamp.utility.SoundManager;

import java.util.concurrent.TimeUnit;


public class BleService extends Service {
    public static BleService instance;

    private static final int SYSTEM_CLOCK_TIME = 0;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private int mConnectionState = STATE_DISCONNECTED;
    public BluetoothGatt mBluetoothGatt;
    public BluetoothGattCharacteristic write_char;
    public BluetoothGattCharacteristic battery_char;

    public Thread mThread;
    public boolean bExeThread = true;

    private final IBinder mBinder = new LocalBinder();

    private final static String ACTION_GATT_CONNECTED = "com.kt.SmartStamp.ACTION_GATT_CONNECTED,";
    private final static String ACTION_GATT_DISCONNECTED = "com.kt.SmartStamp.ACTION_GATT_DISCONNECTED,";
    private final static String ACTION_GATT_SERVICES_DISCOVERED = "com.kt.SmartStamp.ACTION_GATT_SERVICES_DISCOVERED,";
    private final static String ACTION_DATA_AVAILABLE = "com.kt.SmartStamp.ACTION_DATA_AVAILABLE,";

    private static final int NOTI_ID = 1653422;

    private void myLog(String msg){
        Log.i("BleService ******>> ",msg);
    }


    public static boolean isService = false;
    /************************************************************************************************************************
     * 시스템
     *************************************************************************************************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        // BlueTooth Filter
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);                //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);             //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND);                         //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);           //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);          //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mBluetoothStateReceiver, stateFilter);

        instance = this;

        // Bluetooth Manager & Adapter
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                myLog("Unable to initialize BluetoothManager.");
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            myLog("Unable to obtain a BluetoothAdapter.");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(AppVariables.device != null) {
                    mBluetoothGatt = AppVariables.device.connectGatt(BleService.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                }
            } else {
                if(AppVariables.device != null) {
                    mBluetoothGatt = AppVariables.device.connectGatt(BleService.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                }
            }
        }
        isService = true;
        bExeThread = true;
        startScanThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLog("onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        myLog("stopService");
        return super.stopService(name);
    }

    //anders
    @Override
    public void onDestroy() {
        super.onDestroy();

        mThread.interrupt();

        if( mBluetoothGatt != null) {
            if( mBluetoothGatt.connect()) {
                mBluetoothGatt.disconnect();
            }
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if( mBluetoothAdapter !=null) {
            mBluetoothAdapter = null;
            mBluetoothManager = null;
        }

        instance = null;
        AppVariables.device = null;

        this.unregisterReceiver(mBluetoothStateReceiver);
        myLog("onDestroy");
    }

    /************************************************************************************************************************
     * 통신
     *************************************************************************************************************************/

    private void startScanThread(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    if (Thread.currentThread().isInterrupted()) break;
                    try {
                        //anders
                        Thread.sleep(1000 * 5 ); // 1 minute
                        if( bExeThread) {
                            if (mConnectionState == STATE_DISCONNECTED) {
                                if (mBluetoothAdapter != null) {
                                    if (!mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.startDiscovery();
                                    }
                                }
                            }
                        }
                    } catch(Exception e) {
                        myLog("Thread Error : "+e.toString());
                    }
                }

            }
        });
        mThread.start();
    }

    public BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bExeThread=false;
            final String action = intent.getAction();

            BluetoothDevice receiverDevice;
            String addressDevice ="";
            String nameDevice = "";

            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF: myLog("STATE_OFF:" +nameDevice); return;
                        case BluetoothAdapter.STATE_TURNING_OFF: myLog("STATE_TURNING_OFF:" +nameDevice);
                        case BluetoothAdapter.STATE_ON: myLog("STATE_ON:" +nameDevice); break;
                        case BluetoothAdapter.STATE_TURNING_ON: myLog("STATE_TURNING_ON:" +nameDevice); break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                    myLog("ACTION_ACL_CONNECTED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    myLog("ACTION_BOND_STATE_CHANGED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                    myLog("ACTION_ACL_DISCONNECTED:" +nameDevice);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: //블루투스 기기 검색 시작
                    myLog("ACTION_DISCOVERY_STARTED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_FOUND:  //블루투스 기기 검색 됨, 블루투스 기기가 근처에서 검색될 때마다 수행됨
                    receiverDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    addressDevice = receiverDevice.getAddress();
                    nameDevice = receiverDevice.getName();

                    myLog("ACTION_FOUND:" +nameDevice + "   " + addressDevice);

                    if( AppVariables.Mac_Adress.equals(addressDevice)){
                        AppVariables.device = receiverDevice;
                        if(AppVariables.device != null && mConnectionState == STATE_DISCONNECTED) {
                            mBluetoothAdapter.cancelDiscovery();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mBluetoothGatt = AppVariables.device.connectGatt(BleService.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                            }
                            else {
                                mBluetoothGatt = AppVariables.device.connectGatt(BleService.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                            }
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    myLog("ACTION_PAIRING_REQUEST:" +nameDevice);
                    break;
            }
            bExeThread=true;
        }
    };

    public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                AppVariables.bConnect = true;
                gatt.discoverServices();
                SoundManager.getInstance(getApplicationContext()).play(R.raw.connect, 1f);
                Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
                intent.putExtra("action", "connect");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                myLog("Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                AppVariables.bConnect = false;
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                SoundManager.getInstance(getApplicationContext()).play(R.raw.disconnect, 1f);
                Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
                intent.putExtra("action", "disconnect");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                myLog("Disconnected from GATT server.");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                myLog("-->Discover sucess!!");
                prepareRead(gatt);
            } else {
                myLog("onServicesDiscovered -->received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,  BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().equals(Profile.CHARACTERISTIC_BATTERY)) {
                    byte[] readByte = characteristic.getValue();
                    int battery_data = readByte[0];

                    Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
                    intent.putExtra("battery", String.format("%d", battery_data));
                    intent.putExtra("action", "battery");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                myLog("-->onCharacteristicRead" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.getUuid().equals(Profile.CHARACTERISTIC_BATTERY)) {
                byte[] readByte = characteristic.getValue();
                int battery_data = readByte[0];

                Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
                intent.putExtra("battery", String.format("%d", battery_data));
                intent.putExtra("action", "battery");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            myLog("-->onCharacteristicChanged" + characteristic.getUuid().toString());
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            myLog("-->onCharacteristicWrite::" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            myLog(" DescriptorRead-->" + descriptor.getUuid().toString()+"::" + Integer.toString(status));
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            myLog("-->onDescriptorWrite::" + descriptor.getUuid().toString()+"::" + Integer.toString(status));
        }
    };
    private void prepareRead(BluetoothGatt gatt) {
        // 송신
        write_char = gatt.getService(Profile.UUID_SERVICE).getCharacteristic(Profile.CHARACTERISTIC_WRITE);
        gatt.setCharacteristicNotification(write_char, true);

        // 배터리
        battery_char = gatt.getService(Profile.BATTERY_SERVICE).getCharacteristic(Profile.CHARACTERISTIC_BATTERY);
        gatt.setCharacteristicNotification(battery_char, true);
        gatt.readCharacteristic(battery_char);
    }

    private boolean writeData(int index, byte[] data) {
        boolean bResult = false;
        try {
            BluetoothGattCharacteristic mWrite = null;
            BluetoothGatt mGatt = null;

            if (index == 1) {
                mWrite = write_char;
                mGatt = mBluetoothGatt;
            }

            byte[] sendData;
            sendData = null;
            boolean result = false;

            if (null != mGatt && mGatt.connect()) {
                if (mWrite == null) {
                    myLog("Write Data : Write gatt characteristic is null");
                } else {
                    int dataLen = data.length;

                    String sendDataG = "";
                    for (int i=0; i<data.length; i++) {
                        sendDataG += String.format("%02x", data[i]);
                    }
                    myLog("Write Data : " + sendDataG);
                    sendData = data;

                    try {
                        Thread.sleep(10);
                        mWrite.setValue(sendData);
                        bResult = mBluetoothGatt.writeCharacteristic(mWrite);
                    } catch (Exception e){e.printStackTrace();}
                    result = true;
                }
            } else {
                myLog("Write Data : Bluetooth gatt is not connected");
            }
        }
        catch (Exception e) {
            myLog("Write Data Exception : "+ e.toString());
            e.printStackTrace();
        }

        return bResult;
    }

    public void sendOpen() {
        byte[] command = {(byte) 0x37};
        writeData(1, command);
    }

    public void sendClose() {
        byte[] command = {(byte) 0x38};
        writeData(1, command);
    }

    public void sendOff() {
        byte[] command = {(byte) 0x50};
        writeData(1, command);
    }

    public void getBattery() {
        byte[] command = {(byte) 0x41};
        writeData(1, command);
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    private PendingIntent getIntent() {
        Intent intent = new Intent(this, DetailListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        return contentIntent;
    }

    public void initPrepare() {
        if (null != mBluetoothGatt && null != mBluetoothGatt.getService(Profile.UUID_SERVICE))
            prepareRead(mBluetoothGatt);
    }

    /*private void broadcastUpdate(String action) {

    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        bExeThread = false;
        try {
            byte[] readByte = characteristic.getValue();

            if(characteristic.getUuid().equals(Profile.CHARACTERISTIC_NOTI)) {
                int battery_data = readByte[0]  ; //s & 0xff;
                AppVariables.iBatteryAmmount = Integer.parseInt(String.format("%d", battery_data));
                Log.d("STRIP==>", "베터리 정보 들어옴 :" + Integer.toString(AppVariables.iBatteryAmmount));
                Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
                intent.putExtra("battery", String.format("%d", battery_data));
                intent.putExtra("action", "battery");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
            bExeThread=true;
        }
        catch(Exception e)
        {
            bExeThread=true;
            myLog("STRIP UI Exception==>"+e.toString());
            e.printStackTrace();
        }
    }*/

}
