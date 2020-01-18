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
import android.widget.Toast;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.Fragment;

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

/**Created by wmdcprog on 8/29/2018.*/

public class CreatePassword extends Fragment {
    private Button btnSubmit;
    private EditText etPassword, etRetypePassword;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.create_password, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        spEditor = sharedPreferences.edit();

        btnSubmit = v.findViewById(R.id.btnSubmit);
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
                        Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!pass.equals(rePass)) {
                        Toast.makeText(getContext(), "Password does not match with Retype password.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (pass.length() < 8 || pass.length() > 32) {
                        Toast.makeText(getContext(), "Password should be 8-32 characters in length.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new ActivateUserTask().execute(pass, rePass, lockedUsername);
                }
            });
        }

        return v;
    }

    private class ActivateUserTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog = new ProgressDialog(getContext());
        private HttpURLConnection conn = null;
        private URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Activating you account...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try
            {
                url = new URL(sharedPreferences.getString("domain", null)+"activateuser");
                conn= (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(Util.READ_TIMEOUT - 15000);
                conn.setConnectTimeout(Util.CONNECTION_TIMEOUT - 15000);
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1: Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("password", params[0])
                        .appendQueryParameter("retypepassword", params[1])
                        .appendQueryParameter("lockedUsername", params[2]);

                String query = builder.build().getEncodedQuery();

                OutputStream outputStream = conn.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

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
                    return "{\"success\": false, \"reason\": \"Request did not succeed. Status Code: "+statusCode+"\"}";
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
            try {
                JSONObject result = new JSONObject(resultString);
                if (result.getBoolean("success")) {
                    int csaId = result.getInt("csaId");

                    spEditor.remove("csaId");
                    spEditor.apply();

                    spEditor.putString("user", result.getString("username"));
                    spEditor.putInt("csaId", csaId);
                    spEditor.putString("csaFullName", result.getString("csaFullName"));
                    spEditor.putString("publicAddressNorth", result.getString("publicAddressNorth"));
                    spEditor.putString("localAddressNorth", result.getString("localAddressNorth"));
                    spEditor.apply();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Util.alertBox(getContext(), result.getString("reason"), "", false);
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        }
    }
}