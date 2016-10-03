package com.akseltorgard.steganography;

import com.akseltorgard.steganography.utils.SteganographyUtils;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;

public class SteganographyTest {

    private final String MESSAGE = "Hello world! lorem ipsum bla bla bla bla";

    /**
     * Int array of opaque white pixels. Length is 8 + 32.
     * 32 bytes for the length (32 bit int), and 8 bytes for each letter in MESSAGE.
     * @return Array of opaque white pixels.
     */
    private int[] getUnencodedPixels() {
        int[] pixels = new int[MESSAGE.length()*8 + 32];
        int opaqueWhite = 0xFFFFFFFF;
        Arrays.fill(pixels, opaqueWhite);

        return pixels;
    }

    private int[] getEncodedPixels() {
        return SteganographyUtils.encode(getUnencodedPixels(), MESSAGE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEncodeMessageTooLong() {
        int[] pixels = getUnencodedPixels();

        SteganographyUtils.encode(pixels, MESSAGE + "Message too long to fit into pixels.");
    }

    @Test
    public void testEncodeChangesPixels() {
        int[] pixels = getUnencodedPixels();

        pixels = SteganographyUtils.encode(pixels, MESSAGE);

        Assert.assertEquals(false, Arrays.equals(pixels, getUnencodedPixels()));
    }

    @Test
    public void testDecode() {
        int[] pixels = getEncodedPixels();

        String decodedMessage = SteganographyUtils.decode(pixels);

        Assert.assertEquals(MESSAGE, decodedMessage);
    }
}
