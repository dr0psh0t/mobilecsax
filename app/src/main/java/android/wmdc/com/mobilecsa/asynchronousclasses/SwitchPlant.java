package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class SwitchPlant {

    private static String wifi;
    private static FragmentActivity fragAct;
    private static String branch;

    public SwitchPlant(String wifiArg, String simArg, String branchArg, FragmentActivity fragActArg)
    {
        wifi = wifiArg;
        branch = branchArg;
        fragAct = fragActArg;

        new SimTask().execute(simArg);
    }

    static class WifiTask extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        private HttpURLConnection conn = null;

        private WifiTask() {
            pDialog = new ProgressDialog(fragAct);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Switching to "+branch+" (wifi)...");
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(2500);
                conn.setConnectTimeout(2500);
                conn.connect();

                return "{\"success\": true, \"code\": "+conn.getResponseCode()+", \"domain\": \""+
                        params[0]+"\"}";

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pDialog.dismiss();

            try {
                JSONObject resJson = new JSONObject(s);

                if (resJson.getBoolean("success")) {
                    SharedPreferences mySPrefs =
                            PreferenceManager.getDefaultSharedPreferences(fragAct);
                    SharedPreferences.Editor editor = mySPrefs.edit();

                    editor.remove("domain");
                    editor.apply();
                    editor.putString("domain", resJson.getString("domain"));
                    editor.apply();

                    editor.remove("branch");
                    editor.apply();
                    editor.putString("branch", branch);
                    editor.apply();

                    fragAct.setTitle(branch);

                    Util.shortToast(fragAct, "You are now connected to "+branch+".");

                } else {
                    Util.alertBox(fragAct, resJson.getString("reason"));
                }
            } catch (Exception je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                        "Exception", je.toString());

                Util.alertBox(fragAct, je.getMessage());
            }
        }
    }

    static class SimTask extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;
        private HttpURLConnection conn = null;

        private SimTask() {
            pDialog = new ProgressDialog(fragAct);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Switching to "+branch+" (sim)...");
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(2500);
                conn.setConnectTimeout(2500);
                conn.connect();

                return "{\"success\": true, \"code\": "+conn.getResponseCode()+", \"domain\": \""+
                        params[0]+"\"}";

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pDialog.dismiss();

            try {
                JSONObject resJson = new JSONObject(s);

                if (resJson.getBoolean("success")) {
                    SharedPreferences mySPrefs =
                            PreferenceManager.getDefaultSharedPreferences(fragAct);
                    SharedPreferences.Editor editor = mySPrefs.edit();

                    editor.remove("domain");
                    editor.apply();
                    editor.putString("domain", resJson.getString("domain"));
                    editor.apply();

                    editor.remove("branch");
                    editor.apply();
                    editor.putString("branch", branch);
                    editor.apply();

                    fragAct.setTitle(branch);

                    Util.longToast(fragAct, "You are now connected to "+branch+".");

                } else {
                    new WifiTask().execute(wifi);
                }
            } catch (Exception je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                        "Exception", je.toString());

                Util.alertBox(fragAct, je.getMessage());
            }
        }
    }
}