package com.jjoey.envisionocr;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.jjoey.envisionocr.utils.CameraPreview;

public class OtherMainActivity extends AppCompatActivity {

    private FrameLayout frameLayout; //camera surface container
    private ImageView captureImgBtn;

    private Camera camera;
    private CameraPreview preview;
    private int type = 0;

    public static final int REQ_CAMERA = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity_main);

        checkPerms();
        initViews();

        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(mShutterCallback, null, mPictureCallback);
            }
        });

    }

    private void startCamera() {
        if (checkCameraHardware()){
            camera = getCameraInstance(type);
            preview = new CameraPreview(this, camera, type);
            frameLayout.addView(preview);
            setCameraFocus();

        } else {
            Toast.makeText(getApplicationContext(), "Device not support camera feature", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(23)
    private void checkPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            askPerms();
        } else {
            startCamera();
        }
    }

    @TargetApi(23)
    private void askPerms() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQ_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startCamera();
                } else {
                    finish();
                }
                break;
        }
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void setCameraFocus() {

        releaseCameraAndPreview();

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        camera.setParameters(parameters);
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // do ntn
        }
    };

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };

    private Camera getCameraInstance(int type) {
        Camera cam = null;
        try {
            cam = Camera.open(type);
        } catch (Exception e){
            Log.d("tag", "Error setting camera not open " + e);
            e.printStackTrace();
        }
        return cam;
    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    private void initViews() {
        frameLayout = (FrameLayout) findViewById(R.id.frameCamera);
        captureImgBtn = findViewById(R.id.captureImgBtn);
    }

}
