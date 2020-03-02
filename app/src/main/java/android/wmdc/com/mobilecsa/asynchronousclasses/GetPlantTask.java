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

/**Created by wmdcprog on 2/14/2018.*/

public class GetPlantTask extends AsyncTask<String, Void, String> {

    private WeakReference<FragmentActivity> weakReference;

    private WeakReference<Spinner> spinnerPlantWeakReference;

    private ArrayList<String> plantCategory;
    private HashMap<String, Integer> plantMap;

    private SharedPreferences sharedPreferences;
    private HttpURLConnection conn = null;

    public GetPlantTask(FragmentActivity activity, ArrayList<String> plantCategory,
                        HashMap<String, Integer> plantMap, Spinner spinnerPlant) {

        this.weakReference = new WeakReference<>(activity);

        this.plantCategory = plantCategory;
        this.plantMap = plantMap;
        this.spinnerPlantWeakReference = new WeakReference<>(spinnerPlant);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] args) {
        try {
            URL url = new URL(sharedPreferences.getString("domain", null)+"getplantassociated");
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
        try {
            JSONObject responseJson = new JSONObject(result);
            if (responseJson.getBoolean("success")) {
                JSONArray plantJsonArray = responseJson.getJSONArray("plantStore");
                JSONObject tempJson;

                plantMap.put("- Select Plant -", 0);
                plantCategory.add("- Select Plant -");

                for (int i = 0; i < plantJsonArray.length(); ++i) {
                    tempJson = (JSONObject) plantJsonArray.get(i);

                    String name = tempJson.get("plant_name").toString();
                    int id = (int) tempJson.get("plant_id");

                    plantMap.put(name, id);
                    plantCategory.add(name);
                }

                ArrayAdapter<String> plantDataAdapter = new ArrayAdapter<>(weakReference.get(),
                        R.layout.support_simple_spinner_dropdown_item, plantCategory);
                plantDataAdapter.setDropDownViewResource(
                        R.layout.support_simple_spinner_dropdown_item);
                spinnerPlantWeakReference.get().setAdapter(plantDataAdapter);
            } else {
                Log.d("json_success_false", responseJson.getString("reason")+" on " +
                        this.getClass().getSimpleName());
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "Exception", e.toString());
        }
    }
}