package android.wmdc.com.mobilecsa;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wmdcprog on 12/29/2017.
 */

public class PhotoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {

        View v = inflater.inflate(R.layout.photo_layout, container, false);

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ImageView ivPhoto = v.findViewById(R.id.ivPhoto);
        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            String url = sPrefs.getString("domain", null)+thisBundle.getString("url");
            new SetImageTask(getActivity(), ivPhoto).execute(url);
        }

        return v;
    }

    private static class SetImageTask extends AsyncTask<String, String, Bitmap> {

        private WeakReference<ImageView> imageViewWeakReference;

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private HttpURLConnection conn = null;

        private SetImageTask(FragmentActivity activity, ImageView imageView) {
            activityWeakReference = new WeakReference<>(activity);
            imageViewWeakReference = new WeakReference<>(imageView);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        protected Bitmap doInBackground(String[] params) {
            try {
                URL myFileUrl = new URL(params[0]);

                conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setReadTimeout(Util.READ_TIMEOUT);
                conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie",
                        "JSESSIONID="+taskPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/getcontactsphoto");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1: Win64; x64; " +
                        "rv:59.0) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();

                return BitmapFactory.decodeStream(conn.getInputStream());

            } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "NetworkException", e.toString());
                return null;
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        protected void onPostExecute(Bitmap result) {
            final FragmentActivity mainActivity = activityWeakReference.get();
            ImageView imageView = imageViewWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            if (result == null) {
                AlertDialog.Builder warningBox = new AlertDialog.Builder(mainActivity);

                TextView errMsg = new TextView(mainActivity);
                errMsg.setText(R.string.photo_prob);
                errMsg.setTextSize(17);
                errMsg.setPadding(20, 0, 10, 0);
                errMsg.setGravity(Gravity.CENTER_HORIZONTAL);

                warningBox.setView(errMsg);
                warningBox.setTitle("Error");
                warningBox.setCancelable(true);

                warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mainActivity.onBackPressed();
                    }
                });

                warningBox.create().show();
            } else {
                int bmWidth = result.getWidth();
                int bmHeight = result.getHeight();

                if (bmWidth > Util.systemWidth && bmHeight > Util.systemHeight) {
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(
                            result, Util.systemWidth, Util.systemHeight, true));
                } else {
                    imageView.setImageBitmap(result);
                }
            }
        }
    }
}