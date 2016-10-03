package com.akseltorgard.steganography.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BitmapUtils {

    /**
     * '
     * Decodes a bitmap from file. If the bitmap is immutable, it is converted.
     *
     * @param filePath File
     * @return Mutable bitmap.
     */
    public static Bitmap decodeFile(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        if (!bitmap.isMutable()) {
            bitmap = convertToMutable(bitmap);
        }

        return bitmap;
    }

    /**
     * Bitmaps must be mutable in order for setPixels to works.
     * http://stackoverflow.com/a/9194259
     * <p>
     * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
     * more memory that there is already allocated.
     *
     * @param imgIn - Source image. It will be released, and should not be used more
     * @return a copy of imgIn, but muttable.
     */
    private static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }

    public static int[] getPixels(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        return pixels;
    }

    public static int[] getPixels(Bitmap bitmap, int requiredLength) {
        int[] bounds = getMinimumAreaBounds(requiredLength, bitmap.getWidth());

        int[] pixels = new int[bounds[0] * bounds[1]];
        bitmap.getPixels(pixels, 0, bounds[0], 0, 0, bounds[0], bounds[1]);

        return pixels;
    }

    public static void setPixels(Bitmap bitmap, int[] pixels) {
        int[] bounds = getMinimumAreaBounds(pixels.length, bitmap.getWidth());
        bitmap.setPixels(pixels, 0, bounds[0], 0, 0, bounds[0], bounds[1]);
    }

    /**
     * Gets the area required for numberOfBytes to fit into an image width width of imageWidth.
     * @param requiredLength Number of pixels required for message to fit in image.
     * @param imageWidth Width of image.
     * @return [width, height]
     */
    private static int[] getMinimumAreaBounds(int requiredLength, int imageWidth) {
        if (requiredLength < imageWidth) {
            return new int[] {requiredLength, 1};
        } else {
            return new int[] {imageWidth, (int) Math.ceil(requiredLength / imageWidth) };
        }
    }
}