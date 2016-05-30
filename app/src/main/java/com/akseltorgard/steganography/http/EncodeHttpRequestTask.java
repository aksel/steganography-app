package com.akseltorgard.steganography.http;

import android.util.Log;

import com.akseltorgard.steganography.AsyncResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class EncodeHttpRequestTask extends HttpRequestTask{

    public EncodeHttpRequestTask(AsyncResponse<RestParams> delegate) {
        super(delegate);
    }

    @Override
    protected RestParams execute(RestParams restParams) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();

        //Handles Strings
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        //Handles MultiValueMaps
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        //Handles the encoded image byte array
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap();

        long heapFreeSize = Runtime.getRuntime().freeMemory();
        FileSystemResource fsr = new FileSystemResource(new File(restParams.getFilePath()));
        heapFreeSize = Runtime.getRuntime().freeMemory() - heapFreeSize;
        Log.d("STEGANOGRAPHY", heapFreeSize + "");
        map.add("image", fsr);
        map.add("messageString", restParams.getMessage());

        map.get("image");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> imageEntity = new HttpEntity(map, headers);

        String url = WEBSERVICE + ENCODE;
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, imageEntity, byte[].class);

        restParams.setEncodedImageBytes(response.getBody());
        restParams.setType(AsyncResponse.Type.ENCODE_SUCCESS);

        return restParams;
    }
}