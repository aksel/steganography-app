package com.akseltorgard.steganography.utils;

import android.util.Log;

public class SteganographyUtils {

    /**
     * Max length to attempt to decode.
     */
    private static final int MAX_DECODABLE_LENGTH = 0x00FFFFFF;

    private static final int LSB = 1;

    /**
     * Breaks down message into bits. Each bit replaces the LSB of a pixel.
     * The minimum number of pixels is 32 + (message.length*8), as the length of the message is
     * also encoded into the pixels, and each bit requires one pixel.
     * @param pixels Pixels as (A)RGB integers: R=0xFF0000, G=0x00FF00, B=0x0000FF
     * @param message Message to encode.
     * @return Pixels encoded with message.
     */
    public static int[] encode(int[] pixels, String message) {
        Log.d("Steganography.Encode", "Encode Begin");
        byte[] data = message.getBytes();

        //Insert length into data
        {
            byte[] dataWithLength = new byte[4 + data.length];

            int dataLength = data.length;
            for (int i = 0; i < 4; i++) {
                dataWithLength[i] = (byte)(dataLength & 0xff);
                dataLength >>>= 8;
            }

            System.arraycopy(data, 0, dataWithLength, 4, data.length);
            data = dataWithLength;
        }

        int pixelIndex = 0;

        //For each byte of data, insert its 8 bits into the LSB of 8 pixels.
        for (byte b : data) {
            for (int i = 0; i < 8; i++, pixelIndex++) {
                //Remove LSB from pixel
                pixels[pixelIndex] &= ~LSB;
                //OR LSB of byte into pixel
                pixels[pixelIndex] |= b & LSB;
                //Bit-shift byte into next position
                b >>>= 1;
            }
        }

        Log.d("Steganography.Encode", "Encode End");

        return pixels;
    }
    /**
     * Decodes data out of pixels, and constructs a String out of it. .
     * Each byte of data is constructed out of the LSB of 8 consecutive pixels.
     * @param pixels Pixels as (A)RGB integers: R=0xFF0000, G=0x00FF00, B=0x0000FF
     * @return Decoded data.
     */
    public static String decode(int[] pixels) {
        Log.d("Steganography.Decode", "Decode Begin");

        int pixelIndex = 0;

        //Decode length;
        int length = decodeBitsFromPixels(pixels, 32, pixelIndex);
        pixelIndex += 32;

        if (length < 0 || length > MAX_DECODABLE_LENGTH) {
            throw new IllegalArgumentException("Failed to decode. Are you sure the image is encoded?");
        }

        byte[] data = new byte[length];

        for (int byteIndex = 0; byteIndex < length; byteIndex++, pixelIndex+=8) {
            data[byteIndex] = (byte) decodeBitsFromPixels(pixels, 8, pixelIndex);
        }

        Log.d("Steganography.Decode", "Decode End");

        return new String(data);
    }

    /**
     * Decodes specified number of bits out of pixels, returns them as a single integer.
     * @param pixels Pixels as (A)RGB integers: R=0xFF0000, G=0x00FF00, B=0x0000FF
     * @param numberOfBits Number of bits to decode.
     * @param pixelIndex Index to start at.
     * @return Decoded integer.
     */
    private static int decodeBitsFromPixels(int[] pixels,
                                            int numberOfBits,
                                            int pixelIndex) {

        int decodedValue = 0;

        for (int i = 0; i < numberOfBits; i++, pixelIndex++) {
            //Get bit
            int bit = pixels[pixelIndex] & LSB;
            //Shift into position
            decodedValue |= bit << i;
        }

        return decodedValue;

    }
}