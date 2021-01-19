package android.wmdc.com.mobilecsa;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.wmdc.com.mobilecsa.asynchronousclasses.SwitchPlant;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class LoginFragment extends Fragment {

    private EditText user;
    private EditText pass;

    private SharedPreferences sPrefs;
    private SharedPreferences.Editor spEditor;

    private WifiManager wifiManager;

    private boolean isRemembered = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);

        setHasOptionsMenu(true);

        if (getActivity() != null) {
            this.wifiManager = (WifiManager) getActivity()
                    .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            Toolbar toolbarLogin = v.findViewById(R.id.toolbarLogin);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarLogin);

            sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            spEditor = sPrefs.edit();

            Button loginButton = v.findViewById(R.id.buttonLogin);
            user = v.findViewById(R.id.editTextUser);
            pass = v.findViewById(R.id.editTextPass);

            String username = sPrefs.getString("user", null);
            user.setText(username == null ? "" : username);

            pass.setText(sPrefs.getString("password", null));
            pass.setTransformationMethod(new PasswordTransformationMethod());

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String userStr = user.getText().toString();
                    String passStr = pass.getText().toString();
                    String address = sPrefs.getString("domain", null);

                    if (userStr.isEmpty() || passStr.isEmpty()) {
                        Util.alertBox(getActivity(), "Fields are empty");
                        return;
                    }

                    if (!Util.validUserPass(passStr)) {
                        Util.alertBox(getActivity(), "Password should be 8-32 in length");
                        return;
                    }

                    if (address != null) {
                        if (isRemembered) {
                            String password = pass.getText().toString();

                            if (!password.isEmpty()) {
                                spEditor.remove("password");
                                spEditor.apply();
                                spEditor.putString("password", password);
                                spEditor.apply();
                            }
                        }
                        new LoginTask(Util.getProgressBar(getContext()), getActivity()).execute(
                                user.getText().toString(), pass.getText().toString());
                    } else {
                        Util.shortToast(getActivity(), "Select Plant");
                    }
                }
            });
			
			setTitle(getActivity());
        } else {
            Util.alertBox(getContext(), "Login error");
            Log.e("Null", "Activity is null. Cannot render login page.");
        }

        return v;
    }
	
	public void setTitle(FragmentActivity fragmentActivity) {
        String domain = sPrefs.getString("domain", null);

        System.out.println("domain= "+domain);

        if (domain != null) {

            switch (domain) {
                case "http://122.3.176.235:1959/mcsa/":
                    fragmentActivity.setTitle("North-sim");
                    break;
                case "http://192.168.1.150:8080/mcsa/":
                    fragmentActivity.setTitle("North-wifi");
                    break;
                case "http://122.52.48.202:3316/mcsa/":
                    fragmentActivity.setTitle("Central-sim");
                    break;
                case "http://192.168.1.149:8080/mcsa/":
                    fragmentActivity.setTitle("Central-wifi");
                    break;
                case "http://122.52.155.109:1116/mcsa/":
                    fragmentActivity.setTitle("South-sim");
                    break;
                case "http://192.168.2.99:8080/mcsa/":
                    fragmentActivity.setTitle("South-wifi");
                    break;
                case "http://122.3.176.235:1188/mcsa/":
                    fragmentActivity.setTitle("Dumaguete-sim");
                    break;
                case "http://192.168.1.158:8080/mcsa/":
                    fragmentActivity.setTitle("Dumaguete-wifi");
                    break;
            }

        } else {
            Util.longToast(fragmentActivity, "Set the URL.");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.login_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                final String mcsaPassword = sPrefs.getString("mcsaPasswordPrefs", null);

                if (mcsaPassword != null && !mcsaPassword.isEmpty()) {
                    if (getContext() != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Password");

                        final EditText input = new EditText(getContext());
                        input.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String password = input.getText().toString();

                                if (!password.isEmpty()) {
                                    if (password.equals(mcsaPassword)) {
                                        startActivity(new Intent(getContext(),
                                                SettingsActivity.class));
                                    }
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        Util.alertBox(getActivity(), "Cannot go to settings");
                        Log.e("Null", "Activity is null. Cannot go to settings");
                    }
                } else {
                    startActivity(new Intent(getContext(), SettingsActivity.class));
                }
                return true;
            case R.id.rememberCheckBox:
                if (item.isChecked()) {
                    item.setChecked(false);
                    isRemembered = false;
                } else {
                    item.setChecked(true);
                    isRemembered = true;
                }
                return true;
            case R.id.plantSwitch:
                if (getContext() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    String[] plants = {"North", "Central", "South", "Dumaguete"};

                    builder.setItems(plants, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (isWifiEnabled() || isDataEnabled()) {
                                if (isInternetAvailable() || isNetworkConnected()) {
                                    switchPlant(which);
                                } else {
                                    Util.alertBox(getContext(), "No internet.");
                                }
                            } else if (isInternetAvailable() || isNetworkConnected()) {
                                switchPlant(which);
                            } else {
                                Util.alertBox(getContext(), "No internet. Turn on wifi or data.");
                            }
                        }
                    });

                    builder.create().show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchPlant(int which) {
        switch (which) {
            case 0:
                new SwitchPlant(sPrefs.getString("northWifi", null),
                        sPrefs.getString("northSim", null), "North", getActivity());
                break;
            case 1:
                new SwitchPlant(sPrefs.getString("centralWifi", null),
                        sPrefs.getString("centralSim", null), "Central", getActivity());
                break;
            case 2:
                new SwitchPlant(sPrefs.getString("southWifi", null),
                        sPrefs.getString("southSim", null), "South", getActivity());
                break;
            case 3:
                new SwitchPlant(sPrefs.getString("dumagueteWifi", null),
                        sPrefs.getString("dumagueteSim", null), "Dumaguete", getActivity());
                break;
        }
    }

    private static class LoginTask extends AsyncTask<String, String, String> {

        private final WeakReference<FragmentActivity> activityWeakReference;

        private final SharedPreferences taskPrefs;

        private final Dialog dialog;

        private HttpURLConnection conn = null;

        private LoginTask(Dialog dialog, FragmentActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
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
                URL url = new URL(taskPrefs.getString("domain", null)+"Login");

                System.out.println(url.toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(10_000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("password", params[1]);
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
                        "network_exception", e.toString());
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

            FragmentActivity loginActivity = activityWeakReference.get();

            if (loginActivity == null || loginActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject resJson = new JSONObject(result);
                SharedPreferences.Editor taskSpEditor = taskPrefs.edit();

                if (resJson.getBoolean("success")) {
                    if (!resJson.getBoolean("isAdmin")) {

                        taskSpEditor.putString("sessionId", resJson.getString("sessionId"));
                        taskSpEditor.apply();

                        FragmentTransaction fragmentTransaction = loginActivity
                                .getSupportFragmentManager().beginTransaction();

                        fragmentTransaction.setCustomAnimations(R.anim.enter,
                                R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                        fragmentTransaction.replace(R.id.content_login, new SecurityKeyFragment());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else {
                        AlertDialog.Builder warningBox = new AlertDialog.Builder(loginActivity);
                        warningBox.setMessage("The app cannot be used by administrators.");
                        warningBox.setCancelable(true);
                        warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        warningBox.create().show();
                    }

                } else {

                    try {
                        if (resJson.getBoolean("isLocked")) {
                            taskSpEditor.putString("sessionId", resJson.getString("sessionId"));
                            taskSpEditor.apply();

                            Bundle bundle = new Bundle();
                            bundle.putString("lockedUsername", resJson.getString("lockedUsername"));

                            CreatePassword createPassword = new CreatePassword();
                            createPassword.setArguments(bundle);

                            FragmentManager fragmentManager = loginActivity
                                    .getSupportFragmentManager();

                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();

                            fragmentTransaction.replace(R.id.content_login, createPassword)
                                    .setCustomAnimations(R.anim.enter, R.anim.exit,
                                            R.anim.pop_enter, R.anim.pop_exit).commit();
                        }

                    } catch (JSONException e) {
                        Util.alertBox(loginActivity, resJson.getString("reason"));
                    }
                }

            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                Util.longToast(loginActivity, "Error");
            }
        }
    }

    private boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    private boolean isDataEnabled() {
        if (getContext() != null) {
            boolean mobileDataEnabled;

            ConnectivityManager cm = (ConnectivityManager) getContext().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Class<?> cmClass = Class.forName(cm.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); //  make the method callable

                mobileDataEnabled = (Boolean) method.invoke(cm);
                return mobileDataEnabled;
            } catch (Exception e) {
                return false;
            }
        } else {
            Util.shortToast(getActivity(), "Error");
            Log.e("Null", "Context is null. Cannot get data enabled status.");
            return false;
        }
    }

    private boolean isNetworkConnected() {
        if (getContext() != null) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        } else {
            Util.shortToast(getActivity(), "Error");
            Log.e("Null", "Activity is null. Can't get network connected status.");
            return false;
        }
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddress = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddress.getHostAddress().equals("google");
        } catch (Exception e) {
            return false;
        }
    }
}