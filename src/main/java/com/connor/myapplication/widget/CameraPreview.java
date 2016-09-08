package com.connor.myapplication.widget;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.connor.myapplication.widget.CameraUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.connor.myapplication.widget.Constant.PreviewType.FULL_SCREEN;

/**
 * Camera预览界面
 */
public class CameraPreview extends GLSurfaceView implements SurfaceHolder.Callback, Camera
        .AutoFocusCallback
{
    private static final String TAG = "CameraPreview";
    private int screenWidth;
    private int screenHeight;
    private Constant.PreviewType UsingType;

    private OnFocusFinishCallback mFocusFinishListener;//回调监听器
    private SurfaceHolder mHolder;
    private Camera mCamera;


    public CameraPreview(Context context, Camera camera, Constant.PreviewType type)
    {
        super(context);
        mCamera = camera;
        UsingType = type;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mCamera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (mHolder.getSurface() == null)
        {
            return;
        }
        mCamera.stopPreview();
        setSurfaceView();
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }

    /**
     * 获取屏幕大小设置预览大小
     *
     * @param width  获取到的屏幕宽
     * @param height 高
     */
    public void setScreenPix(int width, int height)
    {
        screenWidth = width;
        screenHeight = height;
    }

    /**
     * 手动对焦区域设置
     */
    public void focusOnTouch(MotionEvent event)
    {
        if (mCamera != null)
        {
            int area_size = 200;
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            //屏幕坐标换算成相机坐标
            float touchX = (event.getRawY() / screenHeight) * 2000 - 1000;
            float touchY = 1000 - (event.getRawX() / screenWidth) * 2000;
            //生成对焦区域
            int left = clamp((int) touchX - area_size / 2, -1000, 1000);
            int right = clamp(left + area_size, -1000, 1000);
            int top = clamp((int) touchY - area_size / 2, -1000, 1000);
            int bottom = clamp(top + area_size, -1000, 1000);
            Rect rect = new Rect(left, top, right, bottom);

            if (parameters.getMaxNumFocusAreas() > 0)
            {
                List<Camera.Area> areaList = new ArrayList<Camera.Area>();
                areaList.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(areaList);
            }
            if (parameters.getMaxNumMeteringAreas() > 0)
            {
                List<Camera.Area> metertingList = new ArrayList<Camera.Area>();
                metertingList.add(new Camera.Area(rect, 1000));
                parameters.setMeteringAreas(metertingList);
            }
            mCamera.setParameters(parameters);
            mCamera.autoFocus(this);
        }
    }

    private int clamp(int x, int min, int max)
    {
        if (x > max)
        {
            return max;
        }
        if (x < min)
        {
            return min;
        }
        return x;
    }

    /**
     * 设置预览界面以及照片保存精细度
     */
    private void setSurfaceView()
    {
        try
        {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mCamera.setDisplayOrientation(90);

        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size mPictureSize;
        Camera.Size mPreviewSize;
        //设置相片预览精细度和保存精细度
        CameraUtil.getScreenRatio(screenWidth, screenHeight);
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        mPreviewSize = CameraUtil.findBestPreviewSize(previewSizes, UsingType);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mPictureSize = CameraUtil.findBestPictureSize(pictureSizes, screenWidth,
                screenHeight, UsingType);
        parameters.setPictureSize(mPictureSize.width, mPictureSize.height);
        //判断手机是否支持自动对焦
        List<String> supportFocusMode = parameters.getSupportedFocusModes();
        if (supportFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(parameters);
    }


    /**
     * 系统对焦之后传递是否对焦成功
     *
     * @param success 系统对焦是否成功
     * @param camera
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera)
    {
        mFocusFinishListener.CallForFocusFrame(success);
    }

    /**
     * 对焦结束回调接口
     */
    public interface OnFocusFinishCallback
    {
        void CallForFocusFrame(boolean success);
    }

    /**
     * 设置对焦回调监听器
     */
    public void setOnFocusFinishListener(OnFocusFinishCallback listener)
    {
        this.mFocusFinishListener = listener;
    }
}
