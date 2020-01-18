package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wmdcprog on 2/15/2018.
 */

public class GetCountryCodeTask extends AsyncTask<String, Void, String> {
    private Context context;
    private ArrayList<String> countryCodeCategory;
    private HashMap<String, Integer> countryCodeMap;
    private ArrayAdapter<String> countryCodeAdapter;
    private Spinner spinnerCountryCode;
    private Spinner spinnerFaxCountryCode;

    private SharedPreferences sharedPreferences;

    private HttpURLConnection conn = null;
    private URL url = null;

    public GetCountryCodeTask(Context context,
                              ArrayList<String> countryCodeCategory,
                              HashMap<String, Integer> countryCodeMap,
                              Spinner spinnerCountryCode,
                              Spinner spinnerFaxCountryCode) {
        this.context = context;
        this.countryCodeCategory = countryCodeCategory;
        this.countryCodeMap = countryCodeMap;
        this.spinnerCountryCode = spinnerCountryCode;
        this.spinnerFaxCountryCode = spinnerFaxCountryCode;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            url = new URL(sharedPreferences.getString("domain", null)+"getcountrycode");
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
            if (responseJson.getBoolean("success")) {
                JSONArray countryCodeJsonArray = responseJson.getJSONArray("countryCodeStore");
                JSONObject tempJson;

                countryCodeMap.put("- Select Country Code -", 0);
                countryCodeCategory.add("- Select Country Code -");

                for (int i = 0; i < countryCodeJsonArray.length(); ++i) {
                    tempJson = (JSONObject) countryCodeJsonArray.get(i);

                    String name = tempJson.get("country").toString();
                    int codeId = Integer.parseInt(tempJson.get("countryCodeId").toString());

                    countryCodeMap.put(name, codeId);
                    countryCodeCategory.add(name);
                }

                countryCodeAdapter = new ArrayAdapter<>(context,
                        R.layout.support_simple_spinner_dropdown_item, countryCodeCategory);
                countryCodeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                spinnerCountryCode.setAdapter(countryCodeAdapter);
                spinnerFaxCountryCode.setAdapter(countryCodeAdapter);

                spinnerFaxCountryCode.setSelection(1);
                spinnerCountryCode.setSelection(1);
            } else {
                Log.d("json_success_false", responseJson.getString("reason")+" on " +
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