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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class LoginFragment extends Fragment {

    private Button loginButton;
    private EditText user;
    private EditText pass;

    private SharedPreferences sPrefs;
    private SharedPreferences.Editor spEditor;

    private WifiManager wifiManager;

    private Toolbar toolbarLogin;
    private boolean isRemembered = false;

    public void setToolbarTitle(FragmentActivity thisActivity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
        thisActivity.setTitle(prefs.getString("branch", null));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);
        setHasOptionsMenu(true);
        //setToolbarTitle(getActivity());
        //getActivity().setTitle("TEST");

        this.wifiManager = (WifiManager) getContext()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        toolbarLogin = v.findViewById(R.id.toolbarLogin);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarLogin);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spEditor = sPrefs.edit();

        loginButton = v.findViewById(R.id.buttonLogin);
        user = v.findViewById(R.id.editTextUser);
        pass = v.findViewById(R.id.editTextPass);

        String username = sPrefs.getString("user", null);
        user.setText(username == null ? "" : username);

        pass.setText(sPrefs.getString("password", null));
        pass.setTransformationMethod(new PasswordTransformationMethod());

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String userStr = user.getText().toString();
                String passStr = pass.getText().toString();
                String address = sPrefs.getString("domain", null);

                if (userStr.isEmpty() || passStr.isEmpty()) {
                    Util.alertBox(getActivity(), "Fields are empty", "", false); return;
                }

                if (!Util.validUserPass(passStr)) {
                    Util.alertBox(getActivity(), "Password should be 8-32 characters in length",
                            "", false); return;
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
                    new LoginTask(Util.getProgressBar(getContext())).execute(
                            user.getText().toString(), pass.getText().toString());
                } else {
                    Toast.makeText(getActivity(), "Select Plant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
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
                                    startActivity(new Intent(getContext(), SettingsActivity.class));
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                String[] plants = {"North", "Central", "South", "Dumaguete"};

                builder.setItems(plants, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (isWifiEnabled() || isDataEnabled()) {
                            if (isInternetAvailable() || isNetworkConnected()) {
                                switchPlant(which);
                            } else {
                                Util.alertBox(getContext(), "No internet.", "", false);
                            }
                        } else if (isInternetAvailable() || isNetworkConnected()) {
                            switchPlant(which);
                        } else {
                            Util.alertBox(getContext(), "No internet. Turn on wifi or data.", "",
                                    false);
                        }
                    }
                });

                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void switchPlant(int which) {
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
                break;
        }
    }

    private class LoginTask extends AsyncTask<String, String, String> {
        private Dialog dialog;
        private HttpURLConnection conn = null;
        private URL url = null;

        public LoginTask(Dialog dialog) {
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
                url = new URL(sPrefs.getString("domain", null)+"Login");

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
            try {
                JSONObject responseJson = new JSONObject(result);
                if (responseJson.getBoolean("success")) {
                    if (!responseJson.getBoolean("isAdmin")) {
                        spEditor = sPrefs.edit();
                        spEditor.putString("sessionId", responseJson.getString("sessionId"));
                        spEditor.apply();

                        FragmentTransaction fragmentTransaction =
                                getFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.enter,
                                R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                        fragmentTransaction.replace(R.id.content_login, new SecurityKeyFragment());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else {
                        AlertDialog.Builder warningBox = new AlertDialog.Builder(getContext());
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
                        if (responseJson.getBoolean("isLocked")) {
                            spEditor.putString("sessionId", responseJson.getString("sessionId"));
                            spEditor.apply();

                            Bundle bundle = new Bundle();
                            bundle.putString("lockedUsername",
                                    responseJson.getString("lockedUsername"));

                            CreatePassword createPassword = new CreatePassword();
                            createPassword.setArguments(bundle);

                            FragmentManager fragmentManager = ((AppCompatActivity) getContext())
                                    .getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction =
                                    fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.content_login, createPassword)
                                    .setCustomAnimations(R.anim.enter, R.anim.exit,
                                            R.anim.pop_enter, R.anim.pop_exit)
                                    .commit();
                        } else {
                            Util.alertBox(getContext(), responseJson.getString("reason"), "Error",
                                    false);
                        }
                    } catch (JSONException e) {
                        Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                                "json_exception", e.toString());
                        Util.alertBox(getContext(), responseJson.getString("reason"), "", false);
                    }
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public boolean isDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) getContext().
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); //  make the method callable

            mobileDataEnabled = (Boolean) method.invoke(cm);
            return mobileDataEnabled;
        } catch (Exception e) {
            return mobileDataEnabled;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}