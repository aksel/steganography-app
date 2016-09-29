package com.akseltorgard.steganography.async;

public class SteganographyParams {

    private String mFilePath;
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
}