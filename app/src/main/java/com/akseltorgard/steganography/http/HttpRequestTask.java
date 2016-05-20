package com.akseltorgard.steganography.http;

import android.os.AsyncTask;
import android.util.Log;

import com.akseltorgard.steganography.AsyncResponse;

public abstract class HttpRequestTask extends AsyncTask<RestParams, Void, RestParams> {

    protected static final String WEBSERVICE = "http://52.28.29.249/steganography/steganography";
    protected static final String ENCODE = "/encodeimage";
    protected static final String DECODE = "/decodeimage";

    protected AsyncResponse<RestParams> mDelegate;

    public HttpRequestTask(AsyncResponse<RestParams> delegate) {
        mDelegate = delegate;
    }

    @Override
    protected abstract RestParams doInBackground(RestParams... params);

    @Override
    protected void onPostExecute(RestParams result) {
        mDelegate.processResult(result, result.getType());
    }

    protected RestParams handleFailure(Exception e, RestParams restParams) {
        Log.e("HttpRequestTask", "ERROR");
        Log.e("HttpRequestTask", e.getMessage(), e);

        restParams.setMessage(e.getMessage());
        restParams.setType(AsyncResponse.Type.FAILURE);
        return restParams;
    }
}