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

    private String wifi;
    private String sim;
    private FragmentActivity fragAct;
    private String branch;

    public SwitchPlant(String wifi, String sim, String branch, FragmentActivity fragAct) {
        this.wifi = wifi;
        this.sim = sim;
        this.branch = branch;
        this.fragAct = fragAct;

        new WifiTask().execute(this.wifi);
    }

    class WifiTask extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;
        private HttpURLConnection conn = null;
        private URL url = null;

        public WifiTask() {
            pDialog = new ProgressDialog(fragAct);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Switching to "+branch+" (wifi)...");
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                url = new URL(params[0]);
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
                    SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(fragAct);
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

                    Toast.makeText(fragAct, "You are now connected to "+branch+".",
                            Toast.LENGTH_SHORT).show();
                } else {
                    new SimTask().execute(sim);
                }
            } catch (Exception je) {
                Util.displayStackTraceArray(je.getStackTrace(),
                        Variables.ASYNCHRONOUS_PACKAGE, "Exception", je.toString());
                Util.alertBox(fragAct, je.getMessage(), "Error", false);
            }
        }
    }

    class SimTask extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;
        private HttpURLConnection conn = null;
        private URL url = null;

        public SimTask() {
            pDialog = new ProgressDialog(fragAct);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Switching to "+branch+" (sim)...");
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                url = new URL(params[0]);
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
                    SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(fragAct);
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

                    Toast.makeText(fragAct, "You are now connected to "+branch+".",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Util.alertBox(fragAct, resJson.getString("reason"), "", false);
                }
            } catch (Exception je) {
                Util.displayStackTraceArray(je.getStackTrace(),
                        Variables.ASYNCHRONOUS_PACKAGE, "Exception", je.toString());
                Util.alertBox(fragAct, je.getMessage(), "Error", false);
            }
        }
    }
}