package com.connor.myapplication.home;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity
{
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //设置占据整个屏幕
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //保持屏幕常亮
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager
                .ON_AFTER_RELEASE, "BaseActivity");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mWakeLock.acquire();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mWakeLock.release();
    }

}
