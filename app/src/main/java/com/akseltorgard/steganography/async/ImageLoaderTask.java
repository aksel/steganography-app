package com.akseltorgard.steganography.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;

public class ImageLoaderTask extends AsyncTask<Uri, Integer, Bitmap> {

    private AsyncResponse<Bitmap> mDelegate;
    private Context mContext;

    public ImageLoaderTask(AsyncResponse<Bitmap> delegate, Context context) {
        mDelegate = delegate;
        mContext  = context;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    mContext.getContentResolver().openFileDescriptor(params[0], "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mDelegate.processResult(bitmap, AsyncResponse.Type.IMAGE_LOADED);
    }
}