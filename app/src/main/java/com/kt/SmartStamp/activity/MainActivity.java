package com.kt.SmartStamp.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.fragment.FragmentMainCompleteList;
import com.kt.SmartStamp.fragment.FragmentMainDashboard;
import com.kt.SmartStamp.fragment.FragmentMainList;

public class MainActivity extends AppCompatActivity {
    public static final int FRAGMENT_DASHBOARD = 0;
    public static final int FRAGMENT_LIST = 1;
    public static final int FRAGMENT_COMPLETE_LIST = 2;
    public static final int FRAGMENT_SETTING = 3;

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
                case R.id.navigation_settings:
                    displayFragment(FRAGMENT_SETTING);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleTextView = findViewById(R.id.title_textview);

        fragmentManager = getSupportFragmentManager();
        displayFragment(FRAGMENT_DASHBOARD);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /*************************************** 프래그먼트 출력 ******************************************/
    public void displayFragment(int FragmentIndex) {
        switch(FragmentIndex) {
            case FRAGMENT_DASHBOARD :
                titleTextView.setText("대시보드");
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
            case FRAGMENT_SETTING :
                titleTextView.setText("설정");
                break;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_base_linearLayout, fragment).commitNowAllowingStateLoss();
    }

}
