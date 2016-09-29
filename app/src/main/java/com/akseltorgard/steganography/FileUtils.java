package com.akseltorgard.steganography;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

class FileUtils {

    /**
     * Converts Uri to filepath.
     * @param uri Uri to get path from
     * @return Path to file.
     */
    static String uriToFilePath(Context context, Uri uri) {
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
     * http://stackoverflow.com/a/21191262
     * @param encodedImageBytes Encoded image as byte array.
     * @return Uri to saved encoded image.
     */
    static Uri saveEncodedImage(Context context, byte[] encodedImageBytes) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String folder = "\\encoded_images";
        File myDir = new File(root + folder);

        myDir.mkdirs();

        String filename = System.currentTimeMillis() + ".png";

        File file = new File(myDir, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(encodedImageBytes);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        return Uri.fromFile(file);
    }
}
