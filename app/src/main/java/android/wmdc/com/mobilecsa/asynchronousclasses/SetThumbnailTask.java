package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wmdcprog on 2/7/2018.
 */
public class SetThumbnailTask extends AsyncTask<String, String, Bitmap> {

    private WeakReference<ImageView> imageViewWeakReference;

    private String fileUrl;

    private HttpURLConnection conn = null;

    private SharedPreferences sharedPreferences;

    public SetThumbnailTask(String fileUrl, ImageView imageView, Context context) {
        this.fileUrl = fileUrl;
        this.imageViewWeakReference = new WeakReference<>(imageView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Bitmap doInBackground(String[] asd) {
        try {
            URL myFileUrl = new URL (fileUrl);

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
                    "JSESSIONID="+sharedPreferences.getString("sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/getcontactsphoto");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            Bitmap bmPic = BitmapFactory.decodeStream(conn.getInputStream());

            return Bitmap.createScaledBitmap(bmPic, (int)(bmPic.getWidth() * 0.10),
                    (int)(bmPic.getHeight() * 0.10), false);

        } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "NetworkException", e.toString());

            return null;
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());

            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    protected void onPostExecute(Bitmap result) {
        ImageView imageView = imageViewWeakReference.get();
        imageView.setImageBitmap(result);
    }
}