package com.connor.myapplication.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.connor.myapplication.R;
import com.connor.myapplication.widget.CameraPreview;
import com.connor.myapplication.widget.CameraUtil;
import com.connor.myapplication.widget.Constant;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 4:3布局
 */
public class FourtoThreeActivity extends BaseActivity implements View.OnClickListener, View
        .OnTouchListener, CameraPreview
        .OnFocusFinishCallback, SensorEventListener
{ //界面成员变量
    private Button mTakePhotoIV;
    private ImageView mBackIV;
    private ImageView mThumbPicIV;
    private ImageView mFlashSwitchIV;
    private Button mSwitchCamIV;
    private Button mSwitchLayout;
    private SeekBar mZoomSB;
    private FrameLayout mPreviewLayout;

    private static final String TAG = "FourtoThreeActivity";//Logcat使用
    private Context context = FourtoThreeActivity.this;
    private static final int MEDIA_TYPE_IMAGE = 1;//拍照的意思
    private static final String path = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_PICTURES) + "/CameraDemo/";//照片保存的路径

    private boolean haveFrontCam;//是否有前置摄像头
    private boolean CamPosition;//用true代表后和false代表前
    private static boolean haveFlash = false;//是否打开闪光灯
    private int frontCamInd;//前置摄像头编号
    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度
    private int degree;//手机旋转角度

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.Parameters FlashStatusParameters;
    private SensorManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourtothree);
        initContent();
        OpenCamera();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (CameraUtil.checkCameraHardware(this) && mCamera == null)
        {
            mPreviewLayout.removeAllViews();
            //根据当前摄像头位置获取对应的摄像头权限
            if (CamPosition)
            {
                mCamera = CameraUtil.getCameraInstance();
                if (haveFlash)
                {
                    FlashStatusParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(FlashStatusParameters);
                }
                CamPosition = true;
            } else
            {
                frontCamInd = CameraUtil.findFrontCamera();
                mCamera = CameraUtil.getFrontCameraInstance(frontCamInd);
                CamPosition = false;
            }
            UpdateCameraStatus();
            getThumbPic();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        release();
        sm.unregisterListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        release();
        sm.unregisterListener(this);
    }

    /**
     * 初始化控件
     */
    private void initContent()
    {
        //获取屏幕宽度去设置预览界面
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        //外围布局
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.fourtothree_mainlayout);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        //顶部布局
        View headView = LayoutInflater.from(this).inflate(R.layout.activity_head, null);
        mBackIV = (ImageView) headView.findViewById(R.id.back);
        mSwitchCamIV = (Button) headView.findViewById(R.id.head_title);
        mFlashSwitchIV = (ImageView) headView.findViewById(R.id.head_iv);
        mBackIV.setOnClickListener(this);
        mSwitchCamIV.setOnClickListener(this);
        mFlashSwitchIV.setOnClickListener(this);
        mainLayout.addView(headView);

        //预览界面布局
        View PreviewView = LayoutInflater.from(this).inflate(R.layout.activity_framelayout, null);
        RelativeLayout cameraPreview = (RelativeLayout) PreviewView.findViewById(R.id
                .frame_mainlayout);
        RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(screenWidth,
                (int) (screenWidth * 1.3));
        cameraPreview.setLayoutParams(previewParams);

        mPreviewLayout = new FrameLayout(FourtoThreeActivity.this);
        FrameLayout.LayoutParams preview = new FrameLayout.LayoutParams(screenWidth, (int)
                (screenWidth * 1.3));
        mPreviewLayout.setLayoutParams(preview);
        cameraPreview.addView(mPreviewLayout);
        mainLayout.addView(PreviewView);


        //底部布局
        View BottomView = LayoutInflater.from(this).inflate(R.layout.activity_f2t_bottom, null);
        mTakePhotoIV = (Button) BottomView.findViewById(R.id.fourtothree_capture);
        mSwitchLayout = (Button) BottomView.findViewById(R.id.fourtothree_switchlayout);
        mThumbPicIV = (ImageView) BottomView.findViewById(R.id.fourtothree_thumbPic);
        mTakePhotoIV.setOnClickListener(this);
        mSwitchLayout.setOnClickListener(this);
        mThumbPicIV.setOnClickListener(this);
        mainLayout.addView(BottomView);

        //获取缩略图
        new Handler().post(new Runnable()
        {
            @Override
            public void run()
            {
                getThumbPic();
            }
        });

        //检查是否有前置摄像头,并且获取其编号
        frontCamInd = CameraUtil.findFrontCamera();
        if (frontCamInd == -1)
        {
            haveFrontCam = false;
        } else
        {
            haveFrontCam = true;
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.head_title:
                SwitchCam();
                break;
            case R.id.fourtothree_capture:
                if (mCamera != null)
                {
                    SetPhotoDegree(degree);

                    try
                    {
                        mCamera.takePicture(null, null, mPicture);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } else
                {
                    Toast.makeText(context, "手机不支持相机", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.back:
                FourtoThreeActivity.this.finish();
                break;
            case R.id.head_iv:
                if (CamPosition)
                    setFlashStatus();
                break;
            case R.id.fourtothree_switchlayout:
                startActivity(new Intent(FourtoThreeActivity.this, TakePhotoActivity.class));
                release();
                this.finish();
                break;
            default:
                break;
        }
    }


    /**
     * 打开摄像头
     */
    private void OpenCamera()
    {
        //判断手机是否支持相机并且设置摄像头是否可用
        if (CameraUtil.checkCameraHardware(context))
        {
            mCamera = CameraUtil.getCameraInstance();
            CamPosition = true;
            if (mCamera != null)
            {
                UpdateCameraStatus();
            }
        } else
        {
            Toast.makeText(context, "无法打开相机", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 释放摄像头
     */
    private void release()
    {
        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
        sm.unregisterListener(this);
    }

    /**
     * 更新摄像头设置
     */
    private void UpdateCameraStatus()
    {
        FlashStatusParameters = mCamera.getParameters();
        mPreview = new CameraPreview(context, mCamera, Constant.PreviewType.FOUR_TO_THREE);
        mPreview.setScreenPix(screenWidth, screenHeight);
        mPreview.setFocusable(true);
        mPreviewLayout.addView(mPreview);//添加相机预览界面到布局中
        mPreviewLayout.setOnTouchListener(this);//设置点击监听器
        mPreview.setOnFocusFinishListener(this);//设置对焦回调接口
        //设置传感器，保存相片方向用
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    /**
     * 获取缩略图
     */
    private void getThumbPic()
    {
        Bitmap bitmap;
        File imgPath = new File(path);
        CameraUtil.makeDir(imgPath);
        File[] list = imgPath.listFiles();
        if (list.length != 0)
        {
            bitmap = CameraUtil.getRotatedBitmap(list[list.length - 1].getAbsoluteFile().toString
                    ());
            mThumbPicIV.setImageBitmap(bitmap);
        }
    }

    /**
     * 调转摄像头
     */
    private void SwitchCam()
    {
        mPreviewLayout.removeAllViews();
        release();
        if (!CamPosition)
        {
            mCamera = CameraUtil.getCameraInstance();
            CamPosition = true;
            UpdateCameraStatus();
        } else
        {
            if (haveFrontCam)
            {
                mCamera = CameraUtil.getFrontCameraInstance(frontCamInd);
                CamPosition = false;
                UpdateCameraStatus();
            } else
            {
                Toast.makeText(context, "手机不支持前置摄像头", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 设置闪光灯图标和保存其状态
     */
    private void setFlashStatus()
    {
        if (haveFlash == false)
        {
            FlashStatusParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(FlashStatusParameters);

            mFlashSwitchIV.setImageResource(R.drawable.flashlight_on);
            haveFlash = true;
        } else
        {
            FlashStatusParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(FlashStatusParameters);

            mFlashSwitchIV.setImageResource(R.drawable.flashlight_off);
            haveFlash = false;
        }
    }


    /**
     * 拍照回调函数
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            try
            {
                mCamera.stopPreview();
            } catch (Exception e)
            {
            }
            File pictureFile = CameraUtil.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null)
            {
                return;
            }

            try
            {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Intent intent = new Intent(FourtoThreeActivity.this, ViewPhotoActivity.class);
                intent.putExtra("picPath", pictureFile.getAbsolutePath());
                startActivity(intent);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (mPreview != null && mCamera != null)
        {
            mPreview.focusOnTouch(event);
        }
        return true;
    }

    /**
     * 对焦回调，取消对焦框
     *
     * @param success 回调参数
     */
    @Override
    public void CallForFocusFrame(boolean success)
    {
        if (success)
        {
        }
    }

    /**
     * 根据角度设置相片保存角度
     */
    public void SetPhotoDegree(int degree)
    {
        Camera.Parameters p = mCamera.getParameters();
        if (degree < 45 || degree > 315)
        {
            if (CamPosition)
            {
                p.setRotation(90);
            } else
            {
                p.setRotation(270);
            }

        } else if (degree > 135 && degree < 225)
        {
            if (CamPosition)
            {
                p.setRotation(270);
            } else
            {
                p.setRotation(90);
            }
        } else if (degree > 45 && degree < 135)
        {
            if (CamPosition)
            {
                p.setRotation(0);
            } else
            {
                p.setRotation(0);
            }
        } else if (degree > 225 && degree < 315)
        {
            if (CamPosition)
            {
                p.setRotation(180);
            } else
            {
                p.setRotation(180);
            }
        }
        mCamera.setParameters(p);
    }


    /**
     * 使用传感器获取手机角度，用于保存相片的方向
     */
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        double rotation = 0;
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType())
        {
            return;
        }

        float[] values = event.values;
        float ax = values[0];
        float ay = values[1];

        double g = Math.sqrt(ax * ax + ay * ay);
        double cos = ay / g;
        if (cos > 1)
        {
            cos = 1;
        } else if (cos < -1)
        {
            cos = -1;
        }
        double rad = Math.acos(cos);
        if (ax < 0)
        {
            rad = 2 * Math.PI - rad;
        }

        int uiRot = getWindowManager().getDefaultDisplay().getRotation();
        double uiRad = Math.PI / 2 * uiRot;
        rad -= uiRad;
        rotation = rad;
        degree = (int) (180 * rotation / Math.PI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
