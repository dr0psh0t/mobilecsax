package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wmdcprog on 6/16/2018.
 */
public class DialogImageTask extends AsyncTask<String, String, Bitmap> {

    private final WeakReference<FragmentActivity> activityWeakReference;

    private HttpURLConnection conn = null;

    private final SharedPreferences sPrefs;

    private final ProgressDialog progressDialog;

    public DialogImageTask(FragmentActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
        this.progressDialog = new ProgressDialog(activity);
        this.sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Viewing Photo. Please wait.");
        progressDialog.show();
    }

    protected Bitmap doInBackground(String[] params) {
        try {
            URL fileURL = new URL(params[0]);

            conn = (HttpURLConnection) fileURL.openConnection();
            conn.setReadTimeout(Util.READ_TIMEOUT);
            conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=utf-8");
            conn.setRequestProperty("Cookie", "JSESSIONID="+sPrefs.getString("sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/getcontactsphoto");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            return BitmapFactory.decodeStream(conn.getInputStream());
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

    /** increase dimensions */
    private int[] getIncreasedDimensions(int height, int width)
    {
        while (width < Util.systemWidth && height < Util.systemHeight)
        {
            height = (int)(height * 0.01)+height;
            width = (int)(width * 0.01)+width;
        }

        height = height - (int)(height * 0.01);
        width = width - (int)(width * 0.01);

        int[] dimensions = new int[2];
        dimensions[0] = width;
        dimensions[1] = height;

        return dimensions;
    }

    //  decrease dimensions
    private int[] getDecreasedDimensions(int height, int width)
    {
        while (width > Util.systemWidth)
        {
            height = height - (int)(height * 0.05);
            width = width - (int)(width * 0.05);
        }

        int[] dimensions = new int[2];
        dimensions[0] = width;
        dimensions[1] = height;

        return dimensions;
    }
    /********/

    protected void onPostExecute(Bitmap result)
    {
        progressDialog.dismiss();

        PhotoView imageView = new PhotoView(activityWeakReference.get());

        if (result != null)
        {
            int bmWidth = result.getWidth();
            int bmHeight = result.getHeight();

            //  height is greater than width
            if (bmHeight > bmWidth)
            {
                //  height is lesser than system height
                if (bmHeight < Util.systemHeight)
                {
                    //  width is greater than system width. decrease width, then height.
                    if (bmWidth > Util.systemWidth)
                    {
                        /* ex:
                        D/bmWidth: 1200
                        D/bmHeight: 1600
                         */

                        int[] dimensions = getDecreasedDimensions(bmHeight, bmWidth);

                        imageView.setImageBitmap(Bitmap.createScaledBitmap(result,
                                dimensions[0],
                                dimensions[1],
                                true));
                    }
                    //  width is lesser than system width. increase width, then height.
                    else
                    {
                        /* ex:
                        D/bmWidth: 900
                        D/bmHeight: 1600
                         */

                        int[] dimensions = getIncreasedDimensions(bmHeight, bmWidth);

                        imageView.setImageBitmap(Bitmap.createScaledBitmap(result,
                                dimensions[0],
                                dimensions[1],
                                true));
                    }
                }
                //  height is greater than system height, decrease height.
                else
                {
                    /*
                    D/bmWidth: 1836
                    D/bmHeight: 3264
                    */

                    int[] dimensions = getDecreasedDimensions(bmHeight, bmWidth);

                    imageView.setImageBitmap(Bitmap.createScaledBitmap(result,
                            dimensions[0],
                            dimensions[1],
                            true));
                }
            }
            else if (bmWidth > bmHeight)
            {
                /*
                D/bmWidth: 548
                D/bmHeight: 286
                 */

                // width is lesser than system width. increase width, then height.
                if (bmWidth < Util.systemWidth)
                {
                    int[] dimensions = getIncreasedDimensions(bmHeight, bmWidth);

                    imageView.setImageBitmap(Bitmap.createScaledBitmap(result,
                            dimensions[0],
                            dimensions[1],
                            true));
                }
                // width is greater than system width. decrease width, then height.
                else
                {
                    int[] dimensions = getDecreasedDimensions(bmHeight, bmWidth);

                    imageView.setImageBitmap(Bitmap.createScaledBitmap(result,
                            dimensions[0],
                            dimensions[1],
                            true));
                }
            }

            Dialog builder = new Dialog(activityWeakReference.get());
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

            if (builder.getWindow() != null) {
                builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            } else {
                Log.e("Null", "Builder Window is null. Cannot set background drawable to dialog");
            }

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //  nothing
                }
            });

            builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            builder.show();
        }
        else
        {
            AlertDialog.Builder warningBox = new AlertDialog.Builder(activityWeakReference.get());

            warningBox.setMessage("A problem has occured in displaying Photo.");
            warningBox.setTitle("Error");
            warningBox.setCancelable(true);

            warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            warningBox.create().show();
        }
    }
}
