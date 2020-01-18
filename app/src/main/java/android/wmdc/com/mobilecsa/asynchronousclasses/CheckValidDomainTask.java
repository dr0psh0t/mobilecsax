package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/** Created by wmdcprog on 6/9/2018.*/

public class CheckValidDomainTask extends AsyncTask<String, Void, String> {
    private ProgressDialog pDialog;
    private HttpURLConnection conn = null;
    private URL url = null;
    private Context context;
    private TextView domain;
    private String branch;

    public CheckValidDomainTask(TextView domain, Context context, String branch) {
        this.domain = domain;
        this.context = context;
        this.branch = branch;
        this.pDialog = new ProgressDialog(context);
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
            url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(2500);
            conn.setConnectTimeout(2500);
            conn.connect();

            return "{\"success\": true, \"code\": "+conn.getResponseCode()+", \"domain\": \""+params[0]+"\"}";

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

        try {
            JSONObject responseJson = new JSONObject(result);
            if (responseJson.getBoolean("success")) {
                SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = mySPrefs.edit();

                editor.remove("domain");
                editor.apply();
                editor.putString("domain", responseJson.getString("domain"));
                editor.apply();

                editor.remove("branch");
                editor.apply();
                editor.putString("branch", branch);
                editor.apply();

                if (domain != null) {
                    domain.setText(responseJson.getString("domain"));
                }

                ((AppCompatActivity)context).setTitle(branch);
                Toast.makeText(context, "You are now connected to "+branch, Toast.LENGTH_SHORT).show();
            } else {
                Util.alertBox(context, responseJson.getString("reason"), "Error", false);
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "JSONException", je.toString());
            Util.alertBox(context, je.getMessage(), "JSON Error", false);
        }
    }
}