package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.WorkOrderFragment;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**Created by wmdcprog on 9/14/2018.*/

public class GetJoWoStatusListTask extends AsyncTask<String, String, String> {
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private Context context;
    private HttpURLConnection conn = null;
    private URL url = null;

    public GetJoWoStatusListTask(Context context) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog.setMessage("Loading workorders...");
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();
    }

    protected String doInBackground(String[] args) {
        try {
            url = new URL(sharedPreferences.getString("domain", null)+"getjowostatuslist");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Util.READ_TIMEOUT);
            conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=utf-8");
            conn.setRequestProperty("Cookie",
                    "JSESSIONID="+sharedPreferences.getString("sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("joId", args[0])
                    .appendQueryParameter("cid",
                            String.valueOf(sharedPreferences.getInt("csaId", 0)));
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
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
                return "{\"success\": false, \"reason\": \"Request did not succeed. Status Code: "
                        +statusCode+"\"}";
            }
        } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
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
                    "JSONException", e.toString());
            return "{\"success\": false, \"reason\": \""+e.getMessage()+"\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        this.progressDialog.dismiss();
        try {
            JSONObject resJson = new JSONObject(result);
            if (resJson.getBoolean("success")) {
                if (resJson.getInt("totalCount") < 1) {
                    Util.alertBox(context, resJson.getString("reason"), "Failure", false);
                } else {
                    WorkOrderFragment workOrderFragment = new WorkOrderFragment();
                    Bundle bundle = new Bundle();

                    bundle.putString("searchResult", resJson.toString());
                    workOrderFragment.setArguments(bundle);
                    Variables.currentPage = -1;

                    FragmentManager fragmentManager = ((AppCompatActivity) context)
                            .getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                            R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.replace(R.id.content_main, workOrderFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else {
                Util.alertBox(context, resJson.getString("reason"), "Failure", false);
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
        }
    }
}