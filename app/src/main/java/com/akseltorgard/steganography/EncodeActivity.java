package com.akseltorgard.steganography;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class EncodeActivity extends ImageActivity {

    static final String EXTRA_MESSAGE = "Extra Message";

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        mImageView = (ImageView) findViewById(R.id.imageView_encode_thumbnail);
        mEditText  = (EditText)  findViewById(R.id.editText_message);

        Button encodeButton = (Button) findViewById(R.id.button_upload_encode);
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_MESSAGE, mEditText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}