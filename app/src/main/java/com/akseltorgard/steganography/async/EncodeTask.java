package com.akseltorgard.steganography.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.akseltorgard.steganography.utils.SteganographyUtils;

public class EncodeTask extends SteganographyTask {
    public EncodeTask(AsyncResponse<SteganographyParams> delegate) {
        super(delegate);
    }

    /**
     * Encodes an image with the specified message, and saves it.
     * @param steganographyParams Contains filepath to image, and specified message
     * @return Contains filepath to encoded image.
     */
    @Override
    protected SteganographyParams execute(SteganographyParams steganographyParams) {

        Bitmap image = BitmapFactory.decodeFile(steganographyParams.getFilePath());

        image = SteganographyUtils.encode(image, steganographyParams.getMessage());

        //TODO: Save Encoded image

        steganographyParams.setType(AsyncResponse.Type.ENCODE_SUCCESS);
        return steganographyParams;
    }
}