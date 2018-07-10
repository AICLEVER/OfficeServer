package com.intellstone.officeserver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.intellstone.officeserver.SendThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**----------------------------------------------------------------------------------------------
 * 사진촬영 App 개발 시 intent 를 사용하는 방식은 제어가 어려우며 어떤 Activity 가 선택될 지
 * 미리 알수가 없어서 예외 발생 시 대용이 쉽지 않다.
 * 정교한 제어가 필요하다면 Camera 클래스를 사용해야 함.
 * <참고> 안드로이드 앱에서 카메라를 촬영하고 싶다면 Camera API 또는 Camera2 API 를 사용할 수 있다
 * Camera API 는 모든 안드로이드 버전에서 사용할 수 있어서 호환성이 좋지만, 정교한 기능이 부족함.
 * Camera2 API는 안드로이드 5.0 이상에서만 지원되므로 호환성이 좋지 않지만 강력하고 정교하다.
 * 이 프로그램은 Camera API 를 사용한다.
 * Created by HUH MOONKI on 2018-04-07.
 *---------------------------------------------------------------------------------------------*/
@SuppressWarnings("deprecation")

public class CapturePreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Activity  mActivity;
    private ImageView mImageView;

    //private Camera mCamera;
    public static Camera mCamera; //MKHUH 2018-04-06.
    private int    mCameraId = 0; // 후면 카메라
    private int    mCount;
    Context context;

    public CapturePreview(Activity activity, SurfaceView surfaceView, ImageView imageView) {

        mActivity = activity;

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        mImageView = imageView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open(mCameraId);
            setCameraDisplayOrientation(mCamera);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this); // 교재에서 누락된 한 줄을 추가합니다.
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
    }

    private int getCameraDisplayOrientation() {

        int degress = 0;
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        return (cameraInfo.orientation - degress + 360) % 360;
    }

    private void setCameraDisplayOrientation(Camera camera) {
        int orientation = getCameraDisplayOrientation();
        camera.setDisplayOrientation(orientation);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if(++mCount == 10) {
            mCount = 0;

            Camera.Parameters parameters = camera.getParameters();

            int width       = parameters.getPreviewSize().width;
            int heigth      = parameters.getPreviewSize().height;
            int format      = parameters.getPreviewFormat();
            int orientation = getCameraDisplayOrientation();

            YuvImage image = new YuvImage(data, format, width, heigth, null);
            Rect     rect  = new Rect(0, 0, width, heigth);
            Bitmap bitmap  = rotateBitmap(image, orientation, rect);

            mImageView.setImageBitmap(bitmap);
            sendBitmapThroughNetwork(bitmap);
        }
    }

    private Bitmap rotateBitmap(YuvImage yuvImage, int orientation, Rect rect) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(rect, 100, os);

        byte[] bytes = os.toByteArray();

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**--------------------------------------------------------------------------------------------
     *  비트맵을 전송하는 메서드
     *-------------------------------------------------------------------------------------------*/
    private void sendBitmapThroughNetwork(Bitmap bitmap) {

        // SendThread 핸들러가 아직 준비되지 않았다면 리턴하는 부분
        if(com.intellstone.officeserver.SendThread.mHandler == null) return;

        // 네트워크 전송 지연이 생기면 이전에 SendThread의 메세지 큐에 넣은 메세지가 아직 남아 있을 수 있다.
        // 앱의 특성상 모든 데이터를 반드시 보내야 하는 것이 아니기 때문에 기존 메세지를 삭제하고 새로운 메세지를 넣는다.
        if(com.intellstone.officeserver.SendThread.mHandler.hasMessages(com.intellstone.officeserver.SendThread.CMD_SEND_BITMAP)) {
            com.intellstone.officeserver.SendThread.mHandler.removeMessages(com.intellstone.officeserver.SendThread.CMD_SEND_BITMAP);
        }

        // 메세지 객체에 비트맵 객체를 넣어서 SendThread의 메세지 큐에 보낸다.
        Message msg = Message.obtain();
        msg.what = com.intellstone.officeserver.SendThread.CMD_SEND_BITMAP;
        msg.obj  = bitmap;
        com.intellstone.officeserver.SendThread.mHandler.sendMessage(msg);
    }
}
