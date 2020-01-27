package android.wmdc.com.mobilecsa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.Fragment;
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

/**Created by wmdcprog on 8/29/2018.*/

public class CreatePassword extends Fragment {

    private EditText etPassword, etRetypePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.create_password, container, false);

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor spEditor = sPrefs.edit();
        spEditor.apply();

        Button btnSubmit = v.findViewById(R.id.btnSubmit);
        etPassword = v.findViewById(R.id.etPassword);
        etRetypePassword = v.findViewById(R.id.etRetypePassword);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            final String lockedUsername = bundle.getString("lockedUsername");

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String pass = etPassword.getText().toString();
                    String rePass = etRetypePassword.getText().toString();

                    if (pass.isEmpty() || rePass.isEmpty()) {
                        Util.shortToast(getContext(), "Fill all fields");
                        return;
                    }

                    if (!pass.equals(rePass)) {
                        Util.shortToast(getContext(),
                                "Password does not match with Retype password.");
                        return;
                    }

                    if (pass.length() < 8 || pass.length() > 32) {
                        Util.shortToast(getContext(),
                                "Password must be 8-32 characters in length.");
                        return;
                    }

                    new ActivateUserTask(getActivity()).execute(pass, rePass, lockedUsername);
                }
            });
        }

        return v;
    }

    private static class ActivateUserTask extends AsyncTask<String, Void, String> {

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private ProgressDialog progressDialog;

        private HttpURLConnection conn = null;

        private ActivateUserTask(FragmentActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Activating you account...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(taskPrefs.getString("domain", null)+"activateuser");
                conn= (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(Util.READ_TIMEOUT - 15000);
                conn.setConnectTimeout(Util.CONNECTION_TIMEOUT - 15000);
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+taskPrefs.getString("sessionId",
                        null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer",
                        "http://localhost:8080/mcsa/searchcustomerfromuser");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1: Win64; x64; " +
                        "rv:59.0) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("password", params[0])
                        .appendQueryParameter("retypepassword", params[1])
                        .appendQueryParameter("lockedUsername", params[2]);

                String query = builder.build().getEncodedQuery();

                OutputStream outputStream = conn.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, StandardCharsets.UTF_8));

                bufferedWriter.write(query);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
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
        protected void onPostExecute(String resultString) {
            progressDialog.dismiss();

            FragmentActivity loginActivity = activityWeakReference.get();

            if (loginActivity == null || loginActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject result = new JSONObject(resultString);
                SharedPreferences.Editor taskSpEditor = taskPrefs.edit();

                if (result.getBoolean("success")) {
                    int csaId = result.getInt("csaId");

                    taskSpEditor.remove("csaId");
                    taskSpEditor.apply();

                    taskSpEditor.putString("user", result.getString("username"));
                    taskSpEditor.putInt("csaId", csaId);
                    taskSpEditor.putString("csaFullName", result.getString("csaFullName"));
                    taskSpEditor.putString("publicAddressNorth",
                            result.getString("publicAddressNorth"));
                    taskSpEditor.putString("localAddressNorth",
                            result.getString("localAddressNorth"));

                    taskSpEditor.apply();

                    loginActivity.startActivity(new Intent(loginActivity, MainActivity.class));
                    loginActivity.finish();
                } else {
                    Util.alertBox(loginActivity, result.getString("reason"));
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        }
    }
}