package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.wmdc.com.mobilecsa.adapter.WorkOrderAdapter;
import android.wmdc.com.mobilecsa.model.WorkOrderModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.recyclerview.widget.DefaultItemAnimator;
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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**Created by wmdcprog on 5/7/2018.*/

public class GetWorkOrderQCList extends AsyncTask<String, String, String> {
    private SharedPreferences sharedPreferences;
    private Context context;
    private ProgressDialog progressDialog;
    private HttpURLConnection conn = null;
    private URL url = null;

    private RecyclerView workOrderRecyclerView;
    private ArrayList<WorkOrderModel> workOrderList;

    public GetWorkOrderQCList(Context context, RecyclerView workOrderRecyclerView,
                              ArrayList<WorkOrderModel> workOrderList) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.progressDialog = new ProgressDialog(this.context);
        this.workOrderList = workOrderList;
        this.workOrderRecyclerView = workOrderRecyclerView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String[] params) {
        try{
            url = new URL(sharedPreferences.getString("domain", null)+"getworkorderqclist");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Util.READ_TIMEOUT);
            conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; " +
                    "charset=utf-8");
            conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString(
                    "sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            joid = Integer.parseInt(params[2]);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("cid", params[0])
                    .appendQueryParameter("source", params[1])
                    .appendQueryParameter("joid", params[2]);
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

    private int joid;

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray workOrderArray = jsonObject.getJSONArray("workOrders");

            for (int x = 0; x < workOrderArray.length(); ++x) {
                JSONObject workOrderInd = (JSONObject) workOrderArray.get(x);
                workOrderList.add(new WorkOrderModel(
                        workOrderInd.getString("scopeOfWork"),
                        workOrderInd.getBoolean("isSupervisorId"),
                        workOrderInd.getBoolean("isCsaQc"),
                        workOrderInd.getInt("woId"),
                        joid,
                        workOrderInd.getInt("isCompleted")
                ));
                workOrderRecyclerView.setAdapter(new WorkOrderAdapter(context, workOrderList));
                workOrderRecyclerView.setItemAnimator(new DefaultItemAnimator());
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
            Util.alertBox(context, je.toString(), "", false);
        }
    }
}