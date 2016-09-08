package com.connor.myapplication.home;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.connor.myapplication.R;
import com.connor.myapplication.widget.CameraUtil;

/**
 * Created by meitu on 2016/5/10.
 */
public class PreActivity extends BaseActivity
{

    private ImageView takePictureIv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);
        //魅族的需要先获取权限
        CameraUtil.checkPermission(Manifest.permission.CAMERA);

        takePictureIv = (ImageView) findViewById(R.id.pre_iv);
        takePictureIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(PreActivity.this, TakePhotoActivity.class));
            }
        });


    }
}
