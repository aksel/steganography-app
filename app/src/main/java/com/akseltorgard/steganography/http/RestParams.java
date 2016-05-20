package com.akseltorgard.steganography.http;

import com.akseltorgard.steganography.AsyncResponse;

public class RestParams {
    private String mFilePath;
    private String mMessage;
    private AsyncResponse.Type mType;
    private byte[] mEncodedImageBytes;

    /**
     * @param filePath Filepath to image.
     * @param message Message to encode. Can be null.
     */
    public RestParams(String filePath, String message) {
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

    public byte[] getEncodedImageBytes() {
        return mEncodedImageBytes;
    }

    public void setEncodedImageBytes(byte[] encodedImageBytes) {
        mEncodedImageBytes = encodedImageBytes;
    }
}