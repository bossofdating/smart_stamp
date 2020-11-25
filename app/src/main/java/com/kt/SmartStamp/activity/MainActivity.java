package com.kt.SmartStamp.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.fragment.FragmentMainCompleteList;
import com.kt.SmartStamp.fragment.FragmentMainDashboard;
import com.kt.SmartStamp.fragment.FragmentMainList;
import com.kt.SmartStamp.fragment.FragmentMainAdmin;
import com.kt.SmartStamp.service.SessionManager;

public class MainActivity extends AppCompatActivity {
    public static final int FRAGMENT_DASHBOARD = 0;
    public static final int FRAGMENT_LIST = 1;
    public static final int FRAGMENT_COMPLETE_LIST = 2;
    public static final int FRAGMENT_ADMIN = 3;

    private long PREVIOUS_BACK_KEY_PRESSED_TIME;

    public static Context mContext;

    public BottomNavigationView navView;

    private FragmentManager fragmentManager;
    private Fragment fragment;

    private TextView titleTextView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    displayFragment(FRAGMENT_DASHBOARD);
                    return true;
                case R.id.navigation_list:
                    displayFragment(FRAGMENT_LIST);
                    return true;
                case R.id.navigation_complete_list:
                    displayFragment(FRAGMENT_COMPLETE_LIST);
                    return true;
                case R.id.navigation_admin:
                    displayFragment(FRAGMENT_ADMIN);
                    return true;
            }
            return false;
        }
    };

    /******************************************* OnCreate *********************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.activity_main);

        mContext = this;

        titleTextView = findViewById(R.id.title_textview);

        fragmentManager = getSupportFragmentManager();
        displayFragment(FRAGMENT_DASHBOARD);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.getAdminFl() != null && "y".equalsIgnoreCase(sessionManager.getAdminFl())) {
            navView = findViewById(R.id.nav_view_admin);
        } else {
            navView = findViewById(R.id.nav_view);
        }

        navView.setVisibility(View.VISIBLE);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /******************************************* OnDestroy ********************************************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    /*************************************** 프래그먼트 출력 ******************************************/
    public void displayFragment(int FragmentIndex) {
        switch(FragmentIndex) {
            case FRAGMENT_DASHBOARD :
                titleTextView.setText("등록 대기");
                fragment = new FragmentMainDashboard();
                break;
            case FRAGMENT_LIST :
                titleTextView.setText("날인 대기");
                fragment = new FragmentMainList();
                break;
            case FRAGMENT_COMPLETE_LIST :
                titleTextView.setText("날인 완료");
                fragment = new FragmentMainCompleteList();
                break;
            case FRAGMENT_ADMIN :
                titleTextView.setText("관리자");
                fragment = new FragmentMainAdmin();
                break;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_base_linearLayout, fragment).commitNowAllowingStateLoss();
    }

    /*************************************** onBackPressed ***************************************/
    @Override
    public void onBackPressed() {
        if( SystemClock.elapsedRealtime() - PREVIOUS_BACK_KEY_PRESSED_TIME > 1000 ) {
            PREVIOUS_BACK_KEY_PRESSED_TIME = SystemClock.elapsedRealtime();
            Toast.makeText( this, "뒤로 가기 키를 두 번 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT ).show();
        } else super.onBackPressed();
    }
}