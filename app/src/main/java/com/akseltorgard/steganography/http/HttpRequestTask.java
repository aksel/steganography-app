package com.akseltorgard.steganography.http;

import android.os.AsyncTask;
import android.util.Log;

import com.akseltorgard.steganography.AsyncResponse;

import org.springframework.web.client.RestClientException;

public abstract class HttpRequestTask extends AsyncTask<RestParams, Void, RestParams> {

    protected static final String WEBSERVICE = "http://52.28.29.249/steganography/steganography";
    protected static final String ENCODE = "/encodeimage";
    protected static final String DECODE = "/decodeimage";

    protected AsyncResponse<RestParams> mDelegate;

    public HttpRequestTask(AsyncResponse<RestParams> delegate) {
        mDelegate = delegate;
    }

    @Override
    protected RestParams doInBackground(RestParams... params) {
        RestParams restParams = params[0];
        try {
            return execute(restParams);
        } catch (RestClientException e) {
            return handleFailure(e, restParams);
        }
    }

    protected abstract RestParams execute(RestParams restParams) throws RestClientException;

    @Override
    protected void onPostExecute(RestParams result) {
        mDelegate.processResult(result, result.getType());
    }

    protected RestParams handleFailure(RestClientException e, RestParams restParams) {
        restParams.setMessage("Error: " + e.getMostSpecificCause().getMessage());
        restParams.setType(AsyncResponse.Type.FAILURE);

        Log.e("HttpRequestTask", restParams.getMessage(), e);
        return restParams;
    }
}