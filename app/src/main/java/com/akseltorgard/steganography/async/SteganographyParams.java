package com.akseltorgard.steganography.async;

import android.net.Uri;

public class SteganographyParams {

    private String mFilePath;
    private Uri mResultUri;
    private String mMessage;
    private AsyncResponse.Type mType;

    public SteganographyParams(String filePath, String message) {
        mFilePath = filePath;
        mMessage = message;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public AsyncResponse.Type getType() {
        return mType;
    }

    public void setType(AsyncResponse.Type type) {
        mType = type;
    }

    public Uri getResultUri() {
        return mResultUri;
    }

    public void setResultUri(Uri resultUri) {
        mResultUri = resultUri;
    }
}