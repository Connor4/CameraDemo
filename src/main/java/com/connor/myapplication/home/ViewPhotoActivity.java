package com.connor.myapplication.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.connor.myapplication.R;
import com.connor.myapplication.widget.CameraUtil;

import java.io.File;
import java.io.IOException;

public class ViewPhotoActivity extends BaseActivity
{
    private String mPicPath;
    private ImageView mThumbBackIv;
    private ImageView mThumbSaveIv;
    private ImageView mThumbPicIv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnail);
        mPicPath = getIntent().getStringExtra("picPath");
        initView();
        ShowPic();
    }

    private void ShowPic()
    {
        Bitmap bitmap = CameraUtil.getRotatedBitmap(mPicPath);
        mThumbPicIv.setImageBitmap(bitmap);
    }

    private void initView()
    {
        mThumbBackIv = (ImageView) findViewById(R.id.thumb_back);
        mThumbSaveIv = (ImageView) findViewById(R.id.thumb_save);
        mThumbPicIv = (ImageView) findViewById(R.id.thumb_pic);
        mThumbBackIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewPhotoActivity.this.finish();

            }
        });

        mThumbSaveIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewPhotoActivity.this.finish();
            }
        });
    }


}
