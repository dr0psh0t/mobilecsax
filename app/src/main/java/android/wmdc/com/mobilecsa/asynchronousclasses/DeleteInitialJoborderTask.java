package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

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

public class DeleteInitialJoborderTask extends AsyncTask<String, String, String> {

    private WeakReference<FragmentActivity> activityWeakReference;

    private SharedPreferences sPrefs;

    private ProgressDialog progressDialog;

    private HttpURLConnection conn = null;

    public DeleteInitialJoborderTask(FragmentActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Deleting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            URL url = new URL(sPrefs.getString("domain", null)+"deleteinitialjoborder");
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
            conn.setRequestProperty("Cookie", "JSESSIONID="+sPrefs.getString("sessionId", null));
            conn.setRequestProperty("Host", "localhost:8080");
            conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
            conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("initialJoborderId",
                    params[0]);
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
            Util.displayStackTraceArray(e.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "NetworkException", e.toString());
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
            Util.displayStackTraceArray(e.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "Exception", e.toString());
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

        final FragmentActivity mainActivity = activityWeakReference.get();

        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);

            if (jsonObject.getBoolean("success")) {
                AlertDialog.Builder aBox = new AlertDialog.Builder(mainActivity);

                aBox.setTitle("Success");
                aBox.setMessage(jsonObject.getString("reason"));
                aBox.setCancelable(false);
                aBox.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        new GetInitialJoborderListTask(mainActivity).execute(String.valueOf(
                                sPrefs.getInt("csaId", 0)));
                    }
                });
                aBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new GetInitialJoborderListTask(mainActivity).execute(String.valueOf(
                                sPrefs.getInt("csaId", 0)));
                    }
                });
                aBox.create().show();
            }
        } catch (JSONException je) {
            Util.alertBox(mainActivity, je.getMessage());

            Util.displayStackTraceArray(je.getStackTrace(),
                    Variables.ASYNCHRONOUS_PACKAGE, "JSONException", je.toString());
        }
    }
}