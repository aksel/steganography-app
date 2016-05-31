package com.akseltorgard.steganography;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse<RestParams>{
    static final String TAG = "Steganography";

    static final String EXTRA_FILE_PATH = "Extra File Path";
    static final String EXTRA_ACTION = "Extra Action";

    static final int PICK_IMAGE_ENCODE = 3;
    static final int PICK_IMAGE_DECODE = 4;
    static final int PICK_IMAGE_SEND   = 5;
    static final int ENCODE_IMAGE = 6;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final String KEY_FILEPATH = "Filepath";
    private static final String KEY_CAMERA_IMAGE_URI = "Camera Image URI";
    private static final String KEY_LOADING = "Loading";

    String mFilePath;
    Uri mCameraImageUri;
    boolean mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        Button encodeButton = (Button) findViewById(R.id.button_encode);
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraGalleryIntent();
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
            mCameraImageUri = savedInstanceState.getParcelable(KEY_CAMERA_IMAGE_URI);
            mLoading = savedInstanceState.getBoolean(KEY_LOADING);

            if (mLoading) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            }
        }

        verifyStoragePermissions(this);
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

    /**
     * Camera and ImageGallery chooser implementation.
     * Originally from: http://stackoverflow.com/a/12347567
     */
    private void cameraGalleryIntent() {
        //Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        //Initialize mCameraImageUri
        {
            final File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator
                    + "DCIM"
                    + File.separator
                    + "Steganography"
                    + File.separator);

            root.mkdirs();
            final String fileName = System.currentTimeMillis() + ".jpg";
            final File sdImageMainDirectory = new File(root, fileName);
            mCameraImageUri = Uri.fromFile(sdImageMainDirectory);
        }

        final List<Intent> cameraIntents = new ArrayList<>();
        //Get Camera intents
        {
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

            for(ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
                cameraIntents.add(intent);
            }
        }

        //Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Image");

        //Camera apps were found
        if (!cameraIntents.isEmpty()) {
            //Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        }

        startActivityForResult(chooserIntent, PICK_IMAGE_ENCODE);
    }

    private void pickImage(int requestCode) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Image"), requestCode);
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

    private void startSendActivity(Uri encodedImagePath) {
        Intent intent = new Intent(this, SendImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, encodedImagePath);
        startActivity(intent);
    }

    /**
     * Necessary for KitKat
     * @param uri Uri to get path from
     * @return Path to file.
     */
    private String uriToFilePath(Uri uri) {
        String filePath;
        if (uri.getScheme().equals("content")) {
            String[] imageColumns = new String[] { MediaStore.Images.ImageColumns.DATA };

            Cursor cursor = getContentResolver().query(uri, imageColumns, null, null, null);
            cursor.moveToFirst();

            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = uri.getPath();
        }

        return filePath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        mLoading = false;

        if (resultCode == RESULT_OK) {
            RestParams restParams;
            Intent intent;

            switch (requestCode) {
                case PICK_IMAGE_ENCODE :
                    /**
                     * Camera and ImageGallery intent implementation from:
                     * http://stackoverflow.com/a/12347567
                     */
                    boolean isCamera;

                    if (data == null || data.getScheme().equals("file")) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }

                    Uri selectedImageUri;
                    if (isCamera) {
                        selectedImageUri = mCameraImageUri;
                    } else {
                        selectedImageUri = data.getData();
                    }

                    mFilePath = uriToFilePath(selectedImageUri);
                    intent = new Intent(this, EncodeActivity.class);
                    intent.putExtra(EXTRA_FILE_PATH, selectedImageUri);
                    startActivityForResult(intent, ENCODE_IMAGE);
                    break;

                case PICK_IMAGE_DECODE :
                    mFilePath = uriToFilePath(data.getData());
                    restParams = new RestParams(mFilePath, null);
                    DecodeHttpRequestTask decodeTask = new DecodeHttpRequestTask(this);
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    mLoading = true;
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
                        mLoading = true;
                        encodeTask.execute(restParams);
                    }
                    break;
            }
        }
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
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                break;
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

        if (mCameraImageUri != null) {
            b.putParcelable(KEY_CAMERA_IMAGE_URI, mCameraImageUri);
        }

        b.putBoolean(KEY_LOADING, mLoading);
    }

    /**
     * http://stackoverflow.com/a/33292700
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}