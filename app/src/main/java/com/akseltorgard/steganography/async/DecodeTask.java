package com.akseltorgard.steganography.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.akseltorgard.steganography.utils.BitmapUtils;
import com.akseltorgard.steganography.utils.SteganographyUtils;

public class DecodeTask extends SteganographyTask {
    public DecodeTask(AsyncResponse<SteganographyParams> delegate) {
        super(delegate);
    }

    /**
     * Decodes a message out of an encoded image.
     * @param steganographyParams Contains filepath to image.
     * @return Contains decoded message.
     */
    @Override
    protected SteganographyParams execute(SteganographyParams steganographyParams) {
        Bitmap bitmap = BitmapFactory.decodeFile(steganographyParams.getFilePath());

        String message = SteganographyUtils.decode(BitmapUtils.getPixels(bitmap));

        steganographyParams.setMessage(message);
        steganographyParams.setType(AsyncResponse.Type.DECODE_SUCCESS);
        return steganographyParams;
    }
}