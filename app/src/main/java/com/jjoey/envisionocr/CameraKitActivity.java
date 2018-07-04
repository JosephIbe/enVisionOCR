package com.jjoey.envisionocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;

public class CameraKitActivity extends AppCompatActivity {

    private static final String TAG = CameraKitActivity.class.getSimpleName();

    private ImageView liveTextImg;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_kit);

        initViews();

        addCameraListener();

        liveTextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

    }

    private void addCameraListener() {
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
                // do ntn
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
                if (cameraKitError != null)
                    Toast.makeText(CameraKitActivity.this, "Error Occurred:\t" + cameraKitError.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap resultImage = cameraKitImage.getBitmap();
                resultImage = Bitmap.createScaledBitmap(resultImage, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                Intent intent = new Intent(CameraKitActivity.this, PreviewActivity.class);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resultImage.compress(Bitmap.CompressFormat.PNG, 100, baos);

                intent.putExtra("image", baos.toByteArray());
                startActivity(intent);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
                // do ntn
            }
        });
    }

    private void captureImage() {
        cameraView.captureImage();
    }

    private void initViews() {
        liveTextImg = findViewById(R.id.liveTextImg);
        cameraView = findViewById(R.id.cameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        if (cameraView != null){
            cameraView.stop();
        }
        super.onPause();
    }

}
