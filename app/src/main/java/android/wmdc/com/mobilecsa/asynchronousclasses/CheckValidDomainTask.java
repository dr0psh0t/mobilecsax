package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/** Created by wmdcprog on 6/9/2018.*/

public class CheckValidDomainTask extends AsyncTask<String, Void, String> {

    private final WeakReference<FragmentActivity> activityWeakReference;

    private final WeakReference<TextView> textViewWeakReferenceDomain;

    private final ProgressDialog pDialog;

    private HttpURLConnection conn = null;

    private final String branch;

    public CheckValidDomainTask(FragmentActivity activity, TextView domain, String branch) {
        this.textViewWeakReferenceDomain = new WeakReference<>(domain);
        this.activityWeakReference = new WeakReference<>(activity);
        this.branch = branch;
        this.pDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog.setMessage("Please wait...");
        pDialog.show();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(2500);
            conn.setConnectTimeout(2500);
            conn.connect();

            return "{\"success\": true, \"code\": "+conn.getResponseCode()+", \"domain\": \""
                    +params[0]+"\"}";

        } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "NetworkException", e.toString());
            if (e instanceof MalformedURLException) {
                return "{\"success\": false, \"reason\": \"Malformed URL.\"}";
            } else if (e instanceof ConnectException) {
                return "{\"success\": false, \"reason\": \"Cannot connect to server. " +
                        "Check wifi or mobile data and check if server is available.\"}";
            } else {
                return "{\"success\": false, \"reason\": \"Connection timed out. " +
                        "The server is taking too long to reply.\"}";
            }
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
            return "{\"success\": false, \"reason\": \""+e.getMessage()+"\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        pDialog.dismiss();

        FragmentActivity activity = activityWeakReference.get();

        if (activity == null || activity.isFinishing()) {
            return;
        }

        TextView textViewDomain = textViewWeakReferenceDomain.get();

        try {
            JSONObject responseJson = new JSONObject(result);

            if (responseJson.getBoolean("success")) {
                SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(
                        activity);
                SharedPreferences.Editor editor = mySPrefs.edit();

                editor.remove("domain");
                editor.apply();
                editor.putString("domain", responseJson.getString("domain"));
                editor.apply();

                editor.remove("branch");
                editor.apply();
                editor.putString("branch", branch);
                editor.apply();

                if (textViewDomain != null) {
                    textViewDomain.setText(responseJson.getString("domain"));
                }

                activity.setTitle(branch);
                Util.shortToast(activity, "You are now connected to "+branch);
            } else {
                Util.alertBox(activity, responseJson.getString("reason"));
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "JSONException", je.toString());
            Util.alertBox(activity, "Parse error");
        }
    }
}