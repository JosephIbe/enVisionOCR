package com.jjoey.envisionocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.jjoey.envisionocr.utils.Utils;

import java.io.File;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = PreviewActivity.class.getSimpleName();

    private TextView textView;

    private Bitmap bitmap;
    private FirebaseVisionImage visionImage;
    private String filePath, content;
    private File file;
    private List<FirebaseVisionText.Block> blocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        filePath = (getIntent().getExtras().getString("image_path"));
        bitmap = BitmapFactory.decodeFile(filePath);

//        byte[] bytes = getIntent().getByteArrayExtra("image");
//        if (bytes != null) {
//            bitmap = Utils.byteArraytoBitmap(bytes);
//            Log.d(TAG, "Bytes Rcvd");
//            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            recognizeText(bitmap);
//        } else {
//            Log.d(TAG, "Bytes Empty");
//        }

        textView = findViewById(R.id.textView);

    }

    private void recognizeText(Bitmap resultImage) {
        visionImage = FirebaseVisionImage.fromBitmap(resultImage);
        FirebaseVisionTextDetector textDetector = FirebaseVision.getInstance().getVisionTextDetector();
        textDetector.detectInImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        getResults(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Recog Error:\t" + e.getMessage().toString());
                Snackbar.make(findViewById(android.R.id.content), "No Text Found in Image", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void getResults(FirebaseVisionText firebaseVisionText) {
        blocks = firebaseVisionText.getBlocks();
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            Rect boundBox = block.getBoundingBox();
            Point[] cornerPoints = block.getCornerPoints();

//            content = "";
//            content += block.getText();
//            Log.d(TAG, "Txt:\t" + content);
//            textView.setText(content);

            for (FirebaseVisionText.Line line :  block.getLines()){
                List<FirebaseVisionText.Line> lines = block.getLines();
                Log.d(TAG, "Lines:\t" + line.toString());

                for (FirebaseVisionText.Element element : line.getElements()){
                    String elements = element.getText();
//                    Log.d(TAG, "Elements:\t" + elements);

                    content = "";
                    content += elements;
                    Log.d(TAG, "Elements Txt:\t" + elements);
                    textView.setText(content);

                }

            }

        }
//        for (int m = 0; m < blocks.size(); m++) {
//            List<FirebaseVisionText.Line> lines = blocks.get(m).getLines();
//            Log.d(TAG, "Lines Size:\t" + lines.size());
//            for (int p = 0; p < lines.size(); p++) {
//                List<FirebaseVisionText.Element> elements = lines.get(p).getElements();
//                Log.d(TAG, "Elements Size:\t" + elements.size());
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteImage();
    }

    private void deleteImage() {
        file = new File(filePath);
        Log.d(TAG, "File from path:\t" + file);

        if (file.exists()){
            file.delete();
            Log.d(TAG, "File Deleted");
        }
    }

}
