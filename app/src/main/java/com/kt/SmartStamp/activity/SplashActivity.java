package com.kt.SmartStamp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.TextView;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.utility.RequiredPermissionsManager;


public class SplashActivity extends Activity {
    private RequiredPermissionsManager requiredPermissionsManager;
    private String[] requirePermissionList = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    };
    private String[] deniedPermissionList;

    private Handler mWaitHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );

        requiredPermissionsManager = new RequiredPermissionsManager(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setContentView(R.layout.activity_splash);

        mWaitHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!requiredPermissionsManager.CheckPermissions(requirePermissionList)){
                        requiredPermissionsManager.RequestPermissions(requirePermissionList, 1);
                    } else {
                        finish();
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }

                } catch (Exception ignored) {
                }
            }
        }, 2000);
    }

    /********************************* 권한요청 결과 이벤트 핸들러 ***********************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        requiredPermissionsManager.onRequestPermissionsResult(permissions, grantResults);
        deniedPermissionList = requiredPermissionsManager.GetDeniedPermissionList();
        if( deniedPermissionList != null && deniedPermissionList.length == 0 ) {
            finish();
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else DisplayDialog_Permission_Guide();
    }

    // 권한 동의 안내 다이얼로그
    private void DisplayDialog_Permission_Guide() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle("안내");
        builder.setMessage("필수 권한에 동의해야 이용 가능합니다.");
        builder.setCancelable(false);
        builder.setNegativeButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                finishAndRemoveTask();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requiredPermissionsManager.RequestPermissions(requirePermissionList, 1);
            }
        });
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
        TextView textView = theAlertDialog.findViewById(android.R.id.message);
        textView.setTextSize(15.0f);
    }

}
