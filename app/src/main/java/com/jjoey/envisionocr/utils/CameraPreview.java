package com.jjoey.envisionocr.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Context context;
    private SurfaceHolder holder;
    private Camera camera;
    private int cameraType;

    public CameraPreview(Context context, Camera camera, int cameraType) {
        super(context);
        this.context = context;
        this.camera = camera;
        this.cameraType = cameraType;
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            setCameraDisplayOrientation((Activity) context, cameraType, camera);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("tag", "Error setting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (holder == null)
            return;
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(surfaceHolder);
            setCameraDisplayOrientation((Activity) context, cameraType, camera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void setCameraDisplayOrientation(Activity context, int cameraType, Camera camera) {
//        Camera.CameraInfo info = new Camera.CameraInfo();
//        Camera.getCameraInfo(cameraType, info);
//        int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
//        int degrees = 0;
//        switch (rotation){
//            case Surface.ROTATION_0:
//                degrees = 0;
//                bringToFront();
//            case Surface.ROTATION_90:
//                degrees = 90;
//                bringToFront();
//            case Surface.ROTATION_180:
//                degrees = 180;
//                bringToFront();
//            case Surface.ROTATION_270:
//                degrees = 270;
//                bringToFront();
//        }
//        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        camera.setDisplayOrientation(result);
//    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.release();
    }

}
