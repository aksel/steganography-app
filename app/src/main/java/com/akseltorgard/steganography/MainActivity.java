package com.akseltorgard.steganography;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.akseltorgard.steganography.http.DecodeHttpRequestTask;
import com.akseltorgard.steganography.http.EncodeHttpRequestTask;
import com.akseltorgard.steganography.http.RestParams;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AsyncResponse<RestParams>{
    static final String TAG = "Steganography";

    static final String EXTRA_FILE_PATH = "Extra File Path";
    static final String EXTRA_ACTION = "Extra Action";

    static final int PICK_IMAGE_ENCODE = 3;
    static final int PICK_IMAGE_DECODE = 4;
    static final int PICK_IMAGE_SEND   = 5;

    static final int ENCODE_IMAGE = 6;

    static final String KEY_FILEPATH = "Filepath";

    String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        Button encodeButton = (Button) findViewById(R.id.button_encode);
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_IMAGE_ENCODE);
            }
        });

        Button decodeButton = (Button) findViewById(R.id.button_decode);
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_IMAGE_DECODE);
            }
        });

        Button sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_IMAGE_SEND);
            }
        });

        if (savedInstanceState != null) {
            mFilePath = savedInstanceState.getString(KEY_FILEPATH);
        }
    }

    private void createAboutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AboutDialogFragment dialog = AboutDialogFragment.newInstance();
        dialog.show(fm, TAG);
    }

    private void createDecodeDialog(String message) {
        FragmentManager fm = getSupportFragmentManager();
        DecodedMessageDialogFragment dialog = DecodedMessageDialogFragment.newInstance(message);
        dialog.show(fm, TAG);
    }

    private void pickImage(int requestCode) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Image"), requestCode);
    }

    private void startSendActivity(Uri encodedImagePath) {
        Intent intent = new Intent(this, SendImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, encodedImagePath);
        startActivity(intent);
    }

    /**
     * http://stackoverflow.com/a/21191262
     * @param encodedImageBytes Encoded image as byte array.
     * @return Uri to saved encoded image.
     */
    private Uri saveEncodedImage(byte[] encodedImageBytes) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        File myDir = new File(root + "/encoded_images");

        myDir.mkdirs();

        String filename = System.currentTimeMillis() + ".png";

        File file = new File(myDir, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(encodedImageBytes);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        return Uri.fromFile(file);
    }

    private String uriToFilePath(Uri uri) {
        String filePath;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = uri.getPath();
        }

        return filePath;
    }

    @Override
    public void processResult(RestParams result, Type t) {

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        
        switch (t) {
            case ENCODE_SUCCESS :
                byte[] bytes = result.getEncodedImageBytes();
                Uri encodedImagePath = saveEncodedImage(bytes);
                startSendActivity(encodedImagePath);
                break;

            case DECODE_SUCCESS :
                createDecodeDialog(result.getMessage());
                break;

            case FAILURE:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        if (resultCode == RESULT_OK) {
            RestParams restParams;

            switch (requestCode) {
                case PICK_IMAGE_ENCODE :
                    mFilePath = uriToFilePath(data.getData());
                    Intent intent = new Intent(this, EncodeActivity.class);
                    intent.putExtra(EXTRA_FILE_PATH, data.getData());
                    startActivityForResult(intent, ENCODE_IMAGE);
                    break;

                case PICK_IMAGE_DECODE :
                    mFilePath = uriToFilePath(data.getData());
                    restParams = new RestParams(mFilePath, null);
                    DecodeHttpRequestTask decodeTask = new DecodeHttpRequestTask(this);
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    decodeTask.execute(restParams);
                    break;

                case PICK_IMAGE_SEND :
                    startSendActivity(data.getData());

                case ENCODE_IMAGE:
                    String message = data.getStringExtra(EncodeActivity.EXTRA_MESSAGE);
                    if (message != null && !message.equals("")) {
                        restParams = new RestParams(mFilePath, message);
                        EncodeHttpRequestTask encodeTask = new EncodeHttpRequestTask(this);
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        encodeTask.execute(restParams);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                createAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        if (mFilePath != null) {
            b.putString(KEY_FILEPATH, mFilePath);
        }
    }
}