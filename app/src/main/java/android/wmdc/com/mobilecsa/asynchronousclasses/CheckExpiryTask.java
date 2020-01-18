package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.LoginActivity;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/*** Created by wmdcprog on 2/22/2018.*/

public class CheckExpiryTask extends AsyncTask<String, Void, String> {
    private HttpURLConnection conn = null;
    private URL url;

    private Context context;
    private SharedPreferences sharedPreferences;

    public CheckExpiryTask(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            url = new URL(sharedPreferences.getString("domain", null)+"checksessionexpiry");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Util.READ_TIMEOUT);
            conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/getcontactsphoto");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            int statusCode = conn.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                if (stringBuilder.toString().isEmpty()) {
                    return "{\"success\": false, \"reason\": \"No response from server.\"}";
                } else {
                    return stringBuilder.toString();
                }
            } else {
                return "{\"success\": false, \"reason\": \"Request did not succeed. Status Code: "+statusCode+"\"}";
            }
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
        try {
            JSONObject responseJson = new JSONObject(result);
            if (!responseJson.getBoolean("success")) {
                AlertDialog.Builder warningBox = new AlertDialog.Builder(context);

                TextView errMsg = new TextView(context);
                errMsg.setText("Relogin to continue.");
                errMsg.setTextSize(17);
                errMsg.setPadding(20, 0, 10, 0);
                errMsg.setGravity(Gravity.CENTER_HORIZONTAL);

                warningBox.setView(errMsg);
                warningBox.setTitle("Relogin");
                warningBox.setCancelable(false);
                warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.remove("csaId");
                        editor.remove("csaFullname");
                        editor.remove("sessionId");
                        editor.apply();

                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        ((AppCompatActivity) context).finish();
                    }
                });
                warningBox.create().show();
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
        }
    }
}
