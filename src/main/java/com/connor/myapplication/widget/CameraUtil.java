package com.connor.myapplication.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by meitu on 2016/5/13.
 */
public class CameraUtil
{
    private static final String TAG = "CameraUtil";
    //手机屏幕比率
    private static float mScreenRatio;
    //手机预览最佳大小
    private static Camera.Size mPictureSize;
    //手机保存大小
    private static Camera.Size mPreviewSize;
    //进行降序排序
    private static Comparator<Camera.Size> comparator = new Comparator<Camera.Size>()
    {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs)
        {
            if (lhs.width == rhs.width)
            {
                return 0;
            }
            if (lhs.width > rhs.width)
            {
                return -1;
            } else
            {
                return 1;
            }
        }
    };

    //由于获取的屏幕宽高位置跟getSupport()获取的位置不一样，所以调换一下
    public static void getScreenRatio(int screenWidth, int screenHeight)
    {
        mScreenRatio = (float) screenHeight / (float) screenWidth;
    }


    /**
     * 获取previewsize
     */
    public static Camera.Size findBestPreviewSize(List<Camera.Size> sizes, Constant.PreviewType
            type)
    {
        float ratio;
        float value = 0;
        Camera.Size optimalSize = null;
        Collections.sort(sizes, comparator);//对list降序排序
        switch (type)
        {
            case FULL_SCREEN://对比每个尺寸的绝对值，选取与屏幕相差最小的
                //额外先取第一个对来给接下来的比
                optimalSize = sizes.get(sizes.size() - 1);
                ratio = (float) optimalSize.width / (float) optimalSize.height;
                float lastvalue = Math.abs(ratio - mScreenRatio);

                for (Camera.Size s : sizes)
                {
                    ratio = (float) s.width / (float) s.height;
                    value = Math.abs(ratio - mScreenRatio);
                    if (lastvalue > value)
                    {
                        optimalSize = s;
                        lastvalue = value;
                    }
                }
                break;
            case FOUR_TO_THREE:
                for (Camera.Size s : sizes)
                {
                    ratio = (float) s.width / (float) s.height;
                    if (Math.abs(ratio - 1.33) < 0.01)
                    {
                        optimalSize = s;
                        break;
                    }
                }
                if (optimalSize == null)
                {
                    optimalSize = sizes.get(0);
                }
                break;
            case ONE_TO_ONE:
                break;
            default:
                break;
        }

        return optimalSize;
    }

    /**
     * 选取和预览界面大小相差最小的尺寸
     */
    public static Camera.Size findBestPictureSize(List<Camera.Size> sizes, int width, int height,
                                                  Constant.PreviewType type)
    {
        Camera.Size optimalSize = null;
        switch (type)
        {
            case FULL_SCREEN:
                for (Camera.Size size : sizes)
                {
                    if ((size.width <= width && size.height <= height) || (size.height <= width
                            && size
                            .width <= height))
                    {
                        if (optimalSize == null)
                        {
                            optimalSize = size;
                        } else
                        {
                            int resultArea = optimalSize.width * optimalSize.height;
                            int newArea = size.width * size.height;

                            if (newArea > resultArea)
                            {
                                optimalSize = size;
                            }
                        }
                    }
                }
                break;
            case FOUR_TO_THREE:
                float ratio;
                int i = 0;//由于比较赶，没写读取相片时，对大相片的处理，先直接取小一点的不处理了
                Collections.sort(sizes, comparator);//对list降序排序
                for (Camera.Size s : sizes)
                {
                    ratio = (float) s.width / (float) s.height;
                    if (Math.abs(ratio - 1.33) < 0.01 && i < 3)
                    {
                        optimalSize = s;
                        i++;
                    }
                }
                if (optimalSize == null)
                {
                    optimalSize = sizes.get(0);
                }
                break;
            case ONE_TO_ONE:
                break;
            default:
                break;
        }
        return optimalSize;
    }

    /**
     * 寻找前置摄像头的编号
     *
     * @return 前置摄像头下标
     */
    public static int findFrontCamera()
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for (int num = 0; num < count; num++)
        {
            Camera.getCameraInfo(num, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                return num;
            }
        }
        return -1;
    }

    /**
     * 检查设备是否有摄像头
     */
    public static boolean checkCameraHardware(Context context)
    {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 获取前置摄像头实例
     *
     * @param CameraIndex 前置摄像头下标
     * @return
     */
    public static Camera getFrontCameraInstance(int CameraIndex)
    {
        Camera c = null;
        try
        {
            c = Camera.open(CameraIndex);
        } catch (Exception e)
        {
            Log.e(TAG, "无法打开前置摄像头");
        }
        return c;
    }

    /**
     * 获取后摄像头实例
     */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try
        {
            c = Camera.open();
        } catch (Exception e)
        {
            Log.e(TAG, "无法打开摄像头");
        }
        return c;
    }

    /**
     * 检查是否获取了权限。操作一遍需要获取的。
     *
     * @param permission 需要检查的权限
     * @return 是否获取
     */
    public static boolean checkPermission(String permission)
    {
        switch (permission)
        {
            case Manifest.permission.CAMERA:
                try
                {
                    Camera c = Camera.open();
                    c.release();
                    return true;
                } catch (RuntimeException e)
                {
                    Log.e(TAG, "DON'T HAVE CAMERA PERMISSION");
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * 建立目录，用于保存照片
     *
     * @param f 需要建立的路径
     */
        public static void makeDir(File f)
    {
        if (!f.exists())
        {
            if (!f.mkdirs())
            {
                return;
            }
        }
    }

    /**
     * 保存相片到目录
     *
     * @param type 这里只有一个type
     * @return 生成的照片名加路径
     */
    public static File getOutputMediaFile(int type)
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "CameraDemo");

        makeDir(mediaStorageDir);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else
        {
            return null;
        }
        return mediaFile;
    }

    /**
     * getOrientation以及getRotatedBitmap专门给三星旋转
     *
     * @param Path 图片路径
     */
    public static Bitmap getRotatedBitmap(String Path)
    {
        int degree;
        Bitmap bitmap = null;
        Bitmap newBitmap = null;
        try
        {
            degree = getOrientation(Path);
            if (degree != 0)
            {
                bitmap = BitmapFactory.decodeFile(Path);
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                        .getHeight(), matrix, true);
                bitmap.recycle();
            } else
            {
                newBitmap = BitmapFactory.decodeFile(Path);
            }
        } catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        return newBitmap;
    }

    /**
     * getOrientation以及getRotatedBitmap专门给三星旋转
     */
    public static int getOrientation(String path)
    {
        ExifInterface exif = null;
        try
        {
            exif = new ExifInterface(path);
        } catch (IOException e)
        {
            e.printStackTrace();
            exif = null;
        }

        int degree = 0;
        if (exif != null)
        {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        }
        return degree;
    }
}
