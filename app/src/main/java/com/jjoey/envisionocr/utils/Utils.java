package com.jjoey.envisionocr.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {

    public static Bitmap byteArraytoBitmap(byte[] input) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(input, 0, input.length);
        return bitmap;
    }
}
