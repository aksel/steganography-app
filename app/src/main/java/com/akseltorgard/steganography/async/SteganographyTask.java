package com.akseltorgard.steganography.async;

import android.os.AsyncTask;
import android.util.Log;

public abstract class SteganographyTask extends AsyncTask<SteganographyParams, Void, SteganographyParams> {

    protected AsyncResponse<SteganographyParams> mDelegate;

    public SteganographyTask(AsyncResponse<SteganographyParams> delegate) {
        mDelegate = delegate;
    }

    @Override
    protected SteganographyParams doInBackground(SteganographyParams... params) {
        SteganographyParams steganographyParams = params[0];
        try {
            return execute(steganographyParams);
        } catch (Exception e) {
            return handleFailure(e, steganographyParams);
        }
    }

    protected abstract SteganographyParams execute(SteganographyParams steganographyParams);

    @Override
    protected void onPostExecute(SteganographyParams result) {
        mDelegate.processResult(result, result.getType());
    }

    private SteganographyParams handleFailure(Exception e, SteganographyParams steganographyParams) {
        steganographyParams.setMessage("Error: " + e.getCause().getMessage());
        steganographyParams.setType(AsyncResponse.Type.FAILURE);

        Log.e("SteganographyTask", steganographyParams.getMessage(), e);
        return steganographyParams;
    }
}