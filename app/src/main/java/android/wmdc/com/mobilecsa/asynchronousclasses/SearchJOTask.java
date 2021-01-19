package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.wmdc.com.mobilecsa.JOResultFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

/** * Created by wmdcprog on 4/5/2018.*/

public class SearchJOTask extends AsyncTask<String, String, String> {

    private final WeakReference<FragmentActivity> weakReference;

    private final WeakReference<ProgressBar> progressBarWeakReference;

    private final WeakReference<Button> btnSearchJOWeakReference;

    private final SharedPreferences sharedPreferences;

    private HttpURLConnection conn = null;

    public SearchJOTask(FragmentActivity activity, ProgressBar progressBar, Button btnSearchJO) {
        this.weakReference = new WeakReference<>(activity);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        this.progressBarWeakReference = new WeakReference<>(progressBar);
        this.btnSearchJOWeakReference = new WeakReference<>(btnSearchJO);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Button btnSearchJO = btnSearchJOWeakReference.get();

        if (btnSearchJO != null) {
            btnSearchJO.setEnabled(false);
        }
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(sharedPreferences.getString("domain", null)+"getcsajolist");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15_000);
            conn.setConnectTimeout(15_000);
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
            conn.setRequestProperty("Referer", "Search-Joborder-Android");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("cid", params[0])
                    .appendQueryParameter("qType", params[1])
                    .appendQueryParameter("query", params[2]);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,
                    StandardCharsets.UTF_8));

            writer.write(builder.build().getEncodedQuery());
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            int statusCode = conn.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                InputStream connInputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(connInputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                connInputStream.close();
                bufferedReader.close();

                if (stringBuilder.toString().isEmpty()) {
                    return "{\"success\": false, \"reason\": \"No response from server.\"}";
                } else {
                    return stringBuilder.toString();
                }
            } else {
                return "{\"success\": false, \"reason\": \"Request did not succeed. " +
                        "Status Code: "+statusCode+"\"}";
            }
        } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                    "network_exception", e.toString());
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
            Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                    "exception", e.toString());
            return "{\"success\": false, \"reason\": \""+e.getMessage()+"\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        FragmentActivity mainActivity = weakReference.get();

        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }

        Button btnSearchJO = btnSearchJOWeakReference.get();
        ProgressBar progressBar = progressBarWeakReference.get();

        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        try {
            JSONObject jsonObject = new JSONObject(result);

            if (jsonObject.has("totalCount")) {
                if (jsonObject.getInt("totalCount") < 1) {
                    Util.alertBox(mainActivity, "No joborder found.");
                } else {
                    JOResultFragment joResFrag = new JOResultFragment();
                    Bundle bundle = new Bundle();

                    bundle.putString("searchResult", jsonObject.toString());
                    joResFrag.setArguments(bundle);
                    Variables.currentPage = -1;

                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_main, joResFrag);
                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                            R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else if (jsonObject.has("success")) {
                if (!jsonObject.getBoolean("success")) {
                    Util.alertBox(mainActivity, jsonObject.getString("reason"));
                } else {
                    Log.d("SearchJOTask", jsonObject.toString());
                }
            }
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
            Util.longToast(mainActivity, "Error");
        } finally {
            if (btnSearchJO != null) {
                btnSearchJO.setEnabled(true);
            }
        }
    }
}