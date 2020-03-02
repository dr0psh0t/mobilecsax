package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.wmdc.com.mobilecsa.adapter.EngineJOAdapter;
import android.wmdc.com.mobilecsa.model.Engine;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
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
import java.util.ArrayList;

/**Created by wmdcprog on 7/2/2018.*/

public class SearchEngineTaskFromJO extends AsyncTask<String, String, String> {

    private WeakReference<FragmentActivity> weakReference;

    private WeakReference<RecyclerView> recViewWeakReference;

    private SharedPreferences sharedPreferences;

    private HttpURLConnection conn = null;

    private ArrayList<Engine> engineList;

    private EngineJOAdapter engineJOAdapter;

    public SearchEngineTaskFromJO(FragmentActivity activity, ArrayList<Engine> engineList,
                                  RecyclerView recyclerView, EngineJOAdapter engineJOAdapter) {

        this.weakReference = new WeakReference<>(activity);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        this.engineList = engineList;
        this.recViewWeakReference = new WeakReference<>(recyclerView);
        this.engineJOAdapter = engineJOAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(sharedPreferences.getString("domain", null)+"getmcsaenginemodellist");
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
                    .appendQueryParameter("filter", params[0]);
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
        FragmentActivity mainActivity = weakReference.get();

        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.getInt("totalCount") > 0) {
                JSONArray modelArray = jsonObject.getJSONArray("models");

                int i;
                int modelArrayLength = modelArray.length();

                for (i = 0; i < modelArrayLength; ++i) {
                    JSONObject eachObj = modelArray.getJSONObject(i);
                    engineList.add(
                        new Engine(
                            eachObj.getString("makeId"),
                            eachObj.getString("modelId"),
                            eachObj.getString("year"),
                            eachObj.getString("model"),
                            eachObj.getString("category"),
                            eachObj.getString("make")
                        ));
                }
            } else {
                Log.e("FAILED", jsonObject.toString());
            }
            recViewWeakReference.get().setLayoutManager(new LinearLayoutManager(mainActivity));
            recViewWeakReference.get().setAdapter(engineJOAdapter);
            recViewWeakReference.get().setItemAnimator(new DefaultItemAnimator());
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
            Util.longToast(mainActivity, je.getMessage());
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
            Util.longToast(mainActivity, e.getMessage());
        }
    }
}