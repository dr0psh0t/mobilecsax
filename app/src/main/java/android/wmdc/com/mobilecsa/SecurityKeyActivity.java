package android.wmdc.com.mobilecsa;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

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

/**
 * Created by wmdcprog on 7/2/2018.
 */

public class SecurityKeyActivity extends AppCompatActivity {

    private EditText editTextKeyAct;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_key);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SecurityKeyActivity.this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.apply();

        editTextKeyAct = findViewById(R.id.editTextKeyAct);
        Button buttonOkAct = findViewById(R.id.buttonOkAct);
        buttonOkAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = editTextKeyAct.getText().toString();

                if (!key.isEmpty()) {
                    String domain = sharedPreferences.getString("domain", null);

                    new SecurityTask(SecurityKeyActivity.this,
                            Util.getProgressBar(SecurityKeyActivity.this)).execute(domain, key);
                } else {
                    Util.alertBox(SecurityKeyActivity.this, "Empty security key.");
                }
            }
        });
    }

    private static class SecurityTask extends AsyncTask<String, String, String> {

        private WeakReference<FragmentActivity> activityWeakReference;

        private HttpURLConnection conn = null;

        private SharedPreferences taskPrefs;

        private Dialog dialog;

        private SecurityTask(FragmentActivity activity, Dialog dialog) {
            activityWeakReference = new WeakReference<>(activity);
            this.dialog = dialog;
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(params[0]+"securitykey");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie",
                        "JSESSIONID=" + taskPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/httpsessiontest");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("securityKey",
                        params[1]);
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
                    return "{\"success\": false, \"reason\": \"Request did not succeed. " +
                            "Status Code: "+statusCode+"\"}";
                }
            } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
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
            this.dialog.dismiss();

            FragmentActivity securityActivity = activityWeakReference.get();

            if (securityActivity == null || securityActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject resJson = new JSONObject(result);

                if (resJson.getBoolean("success")) {
                    SharedPreferences.Editor spEditor = taskPrefs.edit();

                    spEditor.putString("user", resJson.getString("username"));
                    spEditor.putInt("csaId", resJson.getInt("csaId"));
                    spEditor.putString("csaFullName", resJson.getString("csaFullName"));
                    spEditor.putString("publicAddressNorth",
                            resJson.getString("publicAddressNorth"));
                    spEditor.putString("localAddressNorth", resJson.getString("localAddressNorth"));
                    spEditor.apply();

                    Intent intent = new Intent(securityActivity, MainActivity.class);
                    securityActivity.startActivity(intent);
                    securityActivity.finish();
                } else {
                    Util.alertBox(securityActivity, resJson.getString("reason"));
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());

                Util.longToast(securityActivity, "Error");
            }
        }
    }
}