package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.wmdc.com.mobilecsa.QualityCheckFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;
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
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**Created by wmdcprog on 4/23/2018.*/

public class GetQCJOListTask extends AsyncTask<String, String, String> {

    private WeakReference<FragmentActivity> weakReference;

    private SharedPreferences sharedPreferences;

    private ProgressDialog progressDialog;

    private HttpURLConnection conn = null;

    public GetQCJOListTask(FragmentActivity activity) {
        this.weakReference = new WeakReference<>(activity);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(weakReference.get());
        this.progressDialog = new ProgressDialog(weakReference.get());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setTitle("Quality Check");
        progressDialog.setMessage("Loading Quality Check. Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(sharedPreferences.getString("domain", null)+"getqclist");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60_000);
            conn.setConnectTimeout(60_000);
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
                    .appendQueryParameter("cid", params[0])
                    .appendQueryParameter("source", params[1])
                    .appendQueryParameter("page", params[2]);
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,
                    StandardCharsets.UTF_8));

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

                input.close();
                reader.close();

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
        progressDialog.dismiss();

        FragmentActivity mainActivity = weakReference.get();

        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            Variables.qcStore = jsonObject;
            Variables.totalCount = jsonObject.getInt("totalCount");
            Variables.lastPage = Variables.totalCount / 36;

            if (jsonObject.getBoolean("success")) {
                QualityCheckFragment qcFrag = new QualityCheckFragment();
                Bundle bundle = new Bundle();
                bundle.putString("searchResult", jsonObject.toString());
                qcFrag.setArguments(bundle);
                Variables.currentPage = 1;

                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                        R.anim.pop_exit);
                fragmentTransaction.replace(R.id.content_main, qcFrag);
                fragmentTransaction.commit();
            } else {
                Util.alertBox(mainActivity, jsonObject.getString("reason"));
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
            Util.alertBox(mainActivity, je.toString());
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
            Util.alertBox(mainActivity, e.toString());
        }
    }
}