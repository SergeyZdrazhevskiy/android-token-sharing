package com.soundcloud.android.examples.token;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;


/**
 * This activity demonstrates how to pick and upload a piece of audio. The main work is done in
 * {@link UploadTask}.
 */
public class UploadFile extends Activity {
    private TextView mFile;
    private UploadTask mUploadTask;
    private Button mUpload;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.upload_file);

        mFile = (TextView) findViewById(R.id.file);
        mUpload = (Button) findViewById(R.id.upload);

        UploadFile last = getLastNonConfigurationInstance();
        if (last != null) {
            mFile.setText(last.mFile.getText());
            mUpload.setEnabled(last.mUpload.isEnabled());
            mUploadTask = last.mUploadTask;
            if (mUploadTask != null) mUploadTask.attachContext(this);
        }

        findViewById(R.id.pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/*"), 0);
            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask == null || mUploadTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mUploadTask = new UploadTask(UploadFile.this);
                    mUploadTask.execute(new File(mFile.getText().toString()));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            File file = getFromMediaUri(getContentResolver(), data.getData());

            if (file != null) {
                mFile.setText(file.getAbsolutePath());
                mUpload.setEnabled(true);
            }
        }
    }

    @Override
    public UploadFile getLastNonConfigurationInstance() {
        return (UploadFile) super.getLastNonConfigurationInstance();
    }

    @Override
    public UploadFile onRetainNonConfigurationInstance() {
        return this;
    }

    // Helper method to get file from a content uri
    protected static File getFromMediaUri(ContentResolver resolver, Uri uri) {
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        } else if ("content".equals(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = resolver.query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        return new File(filePath);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }
}