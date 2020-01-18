package android.wmdc.com.mobilecsa;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.model.GPSTracker;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

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

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sPrefs;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gpsTracker = new GPSTracker(LoginActivity.this);
        Log.d("lat", String.valueOf(gpsTracker.getLatitude()));
        Log.d("lng", String.valueOf(gpsTracker.getLongitude()));

        sPrefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        /**
         *
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.remove("northWifi");
        editor.apply();
        editor.putString("northWifi", "http://192.168.1.150:8080/mcsa/");
        editor.apply();

        editor.remove("northSim");
        editor.apply();
        editor.putString("northSim", "http://122.3.176.235:1959/mcsa/");
        editor.apply();

        editor.remove("centralWifi");
        editor.apply();
        editor.putString("centralWifi", "http://192.168.1.149:8080/mcsa/");
        editor.apply();

        editor.remove("centralSim");
        editor.apply();
        editor.putString("centralSim", "http://122.52.48.202:3316/mcsa/");
        editor.apply();

        editor.remove("southSim");
        editor.apply();
        editor.putString("southSim", "http://122.52.155.109:1116/mcsa/");
        editor.apply();

        Log.d("", "Done Setting URL Prefs");
         */

        if (sPrefs.getString("user", null) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                    R.anim.pop_exit);
            fragmentTransaction.replace(R.id.content_login, new LoginFragment());
            fragmentTransaction.commit();
        } else {
            new SessionExpiryTask(Util.getProgressBar(LoginActivity.this)).execute(
                    sPrefs.getString("user", null),
                    sPrefs.getString("sessionId", null),
                    sPrefs.getInt("csaId", 0)+"");
        }

        //  CHECKING OF PERMISSION
        if (ActivityCompat.checkSelfPermission(LoginActivity.this ,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.
                        CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.
                        READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 1);
        } else {
            //  permission already been granted
            Log.d("PERMISSION", "Permissions are granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        for (int x = 0; x < permissions.length; ++x) {
            if (grantResults[x] == -1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                builder.setMessage("Permission " + permissions[x] + " " +
                        "was denied.\n\nThe program will exit if permissions are denied.").
                        setTitle("Permission Denied");

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private class SessionExpiryTask extends AsyncTask<String, String, String> {
        private HttpURLConnection conn = null;
        private URL url = null;
        private Dialog dialog;

        public SessionExpiryTask(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String[] args) {
            try {
                url = new URL(sPrefs.getString("domain", null)+"checksessionexpiry");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; " +
                        "charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+
                        sPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/checksessionexpiry");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("sessionId", args[0])
                        .appendQueryParameter("csaId", args[1]);

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
            this.dialog.dismiss();
            try {
                JSONObject responseJson = new JSONObject(result);
                if (responseJson.getBoolean("success")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    FragmentTransaction fragmentTransaction =
                            getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                            R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.replace(R.id.content_login, new LoginFragment());
                    fragmentTransaction.commit();
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}