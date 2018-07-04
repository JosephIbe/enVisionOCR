package com.jjoey.envisionocr;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.jjoey.envisionocr.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private ImageView captureImgBtn, img;
    private Camera camera;
    private int rotation = 0;

    public static final int REQ_CAMERA = 102;
    private int cameraType;

    private FirebaseVisionImage visionImage;
    private String content;
    private String text = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPerms();

        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, mPictureCallback);
            }
        });

    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

            Bitmap bitmap = Utils.byteArraytoBitmap(bytes);
            visionImage = FirebaseVisionImage.fromBitmap(bitmap);
//            img.setImageBitmap(bitmap);
            FirebaseVisionTextDetector textDetector = FirebaseVision.getInstance().getVisionTextDetector();
            textDetector.detectInImage(visionImage)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            processText(firebaseVisionText);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    img.setVisibility(View.GONE);
                                    refreshCamera();
                                }
                            }, 5000);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    };

    private void processText(FirebaseVisionText visionText) {
        for (FirebaseVisionText.Block block : visionText.getBlocks()) {
            text = text + block.getText();
            if (!text.equals("")) {
                Log.d(TAG, "Text Found:\t" + text);
                Toast.makeText(this, "text found:\t" + text, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                intent.putExtra("text_found", text);
                startActivity(intent);

                Log.d(TAG, "Text Found:\t" + text);
            } else {
                Log.d(TAG, "No Text Found:\t" + text);
                Toast.makeText(this, "No text found:\t" + text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @TargetApi(23)
    private void checkPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            askPerms();
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
    }

    @TargetApi(23)
    private void askPerms() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CAMERA);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int mResults : grantResults) {
            if (mResults == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_CAMERA);
            } else {
                startCamera();
                Toast.makeText(MainActivity.this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surfaceView);
        captureImgBtn = findViewById(R.id.captureImgBtn);
//        img = findViewById(R.id.img);
    }

    private void refreshCamera() {
        if (holder.getSurface() == null) {
            Toast.makeText(this, "No Surface Available for Camera Render", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewDisplay(holder);
                setCameraDisplayOrientation(this, cameraType, camera);
                camera.startPreview();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException rex) {
            System.err.println(rex);
        }

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        parameters.setPreviewSize(352, 288); // TODO: 7/4/2018 get best review size for device
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(this, cameraType, camera);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        camera.stopPreview();
        camera.release();
        camera = null;
        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private class SavePhotoAsync extends AsyncTask<byte[], Void, String> {

        private File imageFile;

        @Override
        protected String doInBackground(byte[]... bytes) {
            // convert byte array into bitmap
            Bitmap loadedImage = null;
            loadedImage = BitmapFactory.decodeByteArray(bytes[0], 0,
                    bytes[0].length);

            String state = Environment.getExternalStorageState();
            File folder = null;
            if (state.contains(Environment.MEDIA_MOUNTED)) {
                folder = new File(Environment
                        .getExternalStorageDirectory() + "/EnVisionOCR");
            } else {
                folder = new File(Environment
                        .getExternalStorageDirectory() + "/EnVisionOCR");
                //.getExternalStorageDirectory() + getPackageName());
            }

            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                java.util.Date date = new java.util.Date();
                imageFile = new File(folder.getAbsolutePath()
                        + File.separator
                        + new Timestamp(date.getTime()).toString()
                        + "Image.jpg");

                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // save testImage into gallery
            loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(imageFile);
                fout.write(baos.toByteArray());
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "testImage/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());

            getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            return imageFile.getAbsolutePath();
        }

    }

}
