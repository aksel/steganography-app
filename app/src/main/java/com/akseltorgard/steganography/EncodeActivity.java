package com.akseltorgard.steganography;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class EncodeActivity extends ImageActivity {

    static final String EXTRA_MESSAGE = "Extra Message";
    static final String KEY_MESSAGE = "Key Message";

    EditText mEditText;
    Button mEncodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        mImageView = (ImageView) findViewById(R.id.imageView_encode_thumbnail);
        mEditText  = (EditText)  findViewById(R.id.editText_message);
        mEncodeButton = (Button) findViewById(R.id.button_upload_encode);

        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getString(KEY_MESSAGE));
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEncodeButton.setEnabled(mEditText.getText().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEncodeButton.setEnabled(mEditText.getText().length() > 0);
        mEncodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_MESSAGE, mEditText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString(KEY_MESSAGE, mEditText.getText().toString());
    }
}