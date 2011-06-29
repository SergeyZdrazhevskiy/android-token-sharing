package com.soundcloud.android.examples;

import static com.soundcloud.android.examples.TokenSharing.TAG;

import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * An AsyncTask which shows how to upload a track to SoundCloud using the
 * <a href="https://github.com/soundcloud/java-api-wrapper">Java API wrapper</a>.
 */
public class UploadTask extends AsyncTask<File, Long, HttpResponse> {
    private Exception mException;
    private ProgressDialog mProgress;
    private WeakReference<Activity> mContext;

    public UploadTask(Activity context) {
       mContext = new WeakReference<Activity>(context);
    }

    @Override
    protected HttpResponse doInBackground(File... params) {
        final File file = params[0];

        try {
            return Api.wrapper.post(Request.to(Endpoints.TRACKS)
                    .withFile(Params.Track.ASSET_DATA, file)
                    .add(Params.Track.TITLE, file.getName())
                    .add(Params.Track.SHARING, Params.Track.PRIVATE)
                    .setProgressListener(new Request.TransferProgressListener() {
                        @Override
                        public void transferred(long l) throws IOException {
                            if (isCancelled()) throw new IOException("canceled");
                            publishProgress(l, file.length());
                        }
                    }));

        } catch (IOException e) {
            Log.w(TAG, "error", e);
            mException = e;
            return null;
        }
    }

   public void attachContext(Activity context) {
        if (getStatus() != Status.FINISHED) {
            mContext = new WeakReference<Activity>(context);
            onPreExecute();
        }
    }

    @Override
    protected void onPreExecute() {
        mProgress = new ProgressDialog(mContext.get());
        mProgress.setTitle(R.string.uploading);
        mProgress.setIndeterminate(false);
        mProgress.setCancelable(true);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override public void onCancel(DialogInterface dialog) {
                cancel(true);
                dialog.dismiss();

                Context ctxt = mContext.get();
                if (ctxt != null) {
                    Toast.makeText(ctxt, R.string.canceled, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        if (!mProgress.isShowing()) mProgress.show();

        mProgress.setProgress(values[0].intValue());
        mProgress.setMax(values[1].intValue());
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
        Activity activity = mContext.get();
        if (activity != null) {
            if (!activity.isFinishing()) mProgress.dismiss();

            if (response == null) {
                Toast.makeText(activity, "Error during upload: " + mException.getMessage(),
                        Toast.LENGTH_LONG).show();

            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                Toast.makeText(activity, "File has been uploaded",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Error uploading file: " + response.getStatusLine(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
