package com.example.administrator.mymusic2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.example.administrator.mymusic2.R;

/**
 * Created by Administrator on 2018/4/12.
 */

public class LockScreenActivity extends Activity{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.activity_lockscreen);

        //使解锁屏幕显示在锁屏之上
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        Log.d("LockTag","Lock");
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
