package com.jjoey.envisionocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = PreviewActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ImageView backIV, imageView;
    private TextView textView;

    private Bitmap bitmap;
    private FirebaseVisionImage visionImage;
    private String filePath, content;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.textView);
        backIV = findViewById(R.id.backIV);

//        imageView = findViewById(R.id.imageView);
//
//        filePath = (getIntent().getExtras().getString("image_path"));
//        Log.d(TAG, "Path:\t" + filePath);
//        bitmap = BitmapFactory.decodeFile(filePath);

        String found = getIntent().getExtras().getString("text_found");
        if (found != null){
            textView.setText(found);
        } else {
            textView.setText("Error Getting Text from Image");
        }

        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                deleteImage();
                startActivity(new Intent(PreviewActivity.this, MainActivity.class));
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.ACTION_DOWN) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;

    }

//    private void deleteImage() {
//        file = new File(filePath);
//        Log.d(TAG, "File from path:\t" + file);
//
//        if (file.exists()) {
//            file.delete();
//            Log.d(TAG, "File Deleted");
//        }
//    }

}
