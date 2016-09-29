package com.akseltorgard.steganography;

import android.support.v7.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.akseltorgard.steganography.async.AsyncResponse;
import com.akseltorgard.steganography.async.ImageLoaderTask;

public abstract class ImageActivity extends AppCompatActivity implements AsyncResponse<Bitmap> {

    static final String KEY_FILEPATH = "Key FilePath";

    ImageView mImageView;
    Uri mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFilePath(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadImage();
    }

    private void initFilePath(Bundle savedInstanceState) {
        mFilePath  = getIntent().getParcelableExtra(MainActivity.EXTRA_FILE_PATH);

        if (savedInstanceState != null) {
            mFilePath = savedInstanceState.getParcelable(KEY_FILEPATH);
        }
    }

    private void loadImage() {
        if (mFilePath != null) {
            new ImageLoaderTask(this,this).execute(mFilePath);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putParcelable(KEY_FILEPATH, mFilePath);
    }

    @Override
    public void processResult(Bitmap result, AsyncResponse.Type t) {
        if (result == null) {
            return;
        }

        switch (t) {
            case IMAGE_LOADED:
                mImageView.setImageBitmap(result);
                break;
        }
    }
}
