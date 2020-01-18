package android.wmdc.com.mobilecsa;

import android.app.Dialog;
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
import android.widget.Toast;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

/*** Created by wmdcprog on 4/13/2018.*/

public class SecurityKeyFragment extends Fragment {
    private Button buttonOk;
    private EditText editTextKey;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private SecurityKeyTask securityTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.security_key_fragment, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spEditor = sharedPreferences.edit();

        editTextKey = v.findViewById(R.id.editTextKey);

        buttonOk = v.findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextKey.getText().toString().isEmpty()) {
                    securityTask = new SecurityKeyTask(Util.getProgressBar(getContext()));
                    securityTask.execute(editTextKey.getText().toString());

                    buttonOk.setEnabled(true);
                    editTextKey.setEnabled(true);
                } else {
                    Util.alertBox(getContext(), "Empty security key.", "Empty", false);
                }
            }
        });
        return v;
    }

    private class SecurityKeyTask extends AsyncTask<String, String, String> {
        private HttpURLConnection conn = null;
        private URL url = null;
        private Dialog dialog;

        public SecurityKeyTask(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                url = new URL(sharedPreferences.getString("domain", null)+"securitykey");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(10_000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; " +
                        "charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID=" +
                        sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/httpsessiontest");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("securityKey",
                        params[0]);
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
            try {
                JSONObject responseJson = new JSONObject(result);
                if (responseJson.getBoolean("success")) {
                    Util.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                            getContext());
                    spEditor.putString("user", responseJson.getString("username"));
                    spEditor.putInt("csaId", responseJson.getInt("csaId"));
                    spEditor.putString("csaFullName", responseJson.getString("csaFullName"));
                    spEditor.putString("publicAddressNorth",
                            responseJson.getString("publicAddressNorth"));
                    spEditor.putString("localAddressNorth",
                            responseJson.getString("localAddressNorth"));
                    spEditor.apply();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Util.alertBox(getContext(), responseJson.getString("reason"), "", false);
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}