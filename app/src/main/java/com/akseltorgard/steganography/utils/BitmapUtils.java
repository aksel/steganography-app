package com.akseltorgard.steganography.utils;

import android.graphics.Bitmap;

public class BitmapUtils {

    public static int[] getPixels(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        return pixels;
    }

    public static void setPixels(Bitmap bitmap, int[] pixels) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }
}
