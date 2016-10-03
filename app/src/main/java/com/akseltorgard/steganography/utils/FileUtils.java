package com.akseltorgard.steganography.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {

    /**
     * Converts Uri to filepath.
     * @param uri Uri to get path from
     * @return Path to file.
     */
    public static String uriToFilePath(Context context, Uri uri) {
        String filePath;
        if (uri.getScheme().equals("content")) {
            String[] imageColumns = new String[] { MediaStore.Images.ImageColumns.DATA };

            Cursor cursor = context.getContentResolver().query(uri, imageColumns, null, null, null);
            cursor.moveToFirst();

            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = uri.getPath();
        }

        return filePath;
    }

    /**
     * Save bitmap to file.
     * @param bitmap Encoded image.
     * @return Uri to saved encoded image.
     */
    public static Uri saveBitmap(Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String folder = "/encoded_images";
        File myDir = new File(root + folder);

        myDir.mkdirs();

        String filename = System.currentTimeMillis() + ".png";

        File file = new File(myDir, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(file);
    }

    public static void scanFile(Context context, File file) {
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }
        );
    }
}
