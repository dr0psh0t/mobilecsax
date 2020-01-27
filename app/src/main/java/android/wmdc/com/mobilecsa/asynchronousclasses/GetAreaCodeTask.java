package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/** Created by wmdcprog on 2/15/2018.*/

public class GetAreaCodeTask extends AsyncTask<String, Void, String> {

    private WeakReference<FragmentActivity> weakReference;

    private WeakReference<Spinner> spinnerAreaCodeWeakReference;
    private WeakReference<Spinner> spinnerFaxCodeWeakReference;

    private ArrayList<String> areaCodeCategory;
    private HashMap<String, Integer> areaCodeMap;

    private SharedPreferences sharedPreferences;
    private HttpURLConnection conn = null;

    public GetAreaCodeTask(FragmentActivity activity, ArrayList<String> areaCodeCategory,
                           HashMap<String, Integer> areaCodeMap, Spinner spinnerAreaCode,
                           Spinner spinnerFaxCode) {

        this.weakReference = new WeakReference<>(activity);
        this.areaCodeCategory = areaCodeCategory;
        this.areaCodeMap = areaCodeMap;

        spinnerAreaCodeWeakReference = new WeakReference<>(spinnerAreaCode);
        spinnerFaxCodeWeakReference = new WeakReference<>(spinnerFaxCode);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(weakReference.get());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(sharedPreferences.getString("domain", null)+"getareacode");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Util.READ_TIMEOUT);
            conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
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
                return "{\"success\": false, \"reason\": \"Request did not succeed. " +
                        "Status Code: "+statusCode+"\"}";
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
            if (responseJson.getBoolean("success")) {
                JSONArray areaCodeJsonArray = responseJson.getJSONArray("areaCodeStore");
                JSONObject tempJson;

                areaCodeMap.put("- Select Area Code -", 0);
                areaCodeCategory.add("- Select Area Code -");

                for (int i = 0; i < areaCodeJsonArray.length(); ++i)
                {
                    tempJson = (JSONObject) areaCodeJsonArray.get(i);

                    String name = tempJson.get("area").toString();
                    int id = Integer.parseInt(tempJson.get("areaCodeId").toString());

                    areaCodeMap.put(name, id);
                    areaCodeCategory.add(name);
                }

                ArrayAdapter<String> areaCodeAdapter = new ArrayAdapter<>(weakReference.get(),
                        R.layout.support_simple_spinner_dropdown_item, areaCodeCategory);

                areaCodeAdapter.setDropDownViewResource(
                        R.layout.support_simple_spinner_dropdown_item);

                spinnerAreaCodeWeakReference.get().setAdapter(areaCodeAdapter);
                spinnerFaxCodeWeakReference.get().setAdapter(areaCodeAdapter);

                spinnerAreaCodeWeakReference.get().setSelection(2);
                spinnerFaxCodeWeakReference.get().setSelection(2);
            } else {
                Log.d("json_success_false", responseJson.getString("reason")+" on "+
                        this.getClass().getSimpleName());
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "JSONException", je.toString());
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "Exception", e.toString());
        }
    }
}