package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

import android.wmdc.com.mobilecsa.asynchronousclasses.CheckValidDomainTask;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

/** Created by wmdcprog on 3/14/2018.*/

public class SettingsAdapter extends RecyclerView.Adapter {

    private ArrayList<KeyValueInfo> settingsData;
    private Context context;
    private SharedPreferences taskPrefs;
    private SharedPreferences.Editor spEditor;
    private WifiManager wifiManager;

    public SettingsAdapter(ArrayList<KeyValueInfo> settingsData, Context context) {
        this.settingsData = settingsData;
        this.context = context;
        this.taskPrefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.spEditor = this.taskPrefs.edit();
        spEditor.apply();
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.settings_card_layout, viewGroup,
                false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        SettingsViewHolder settingsViewHolder = (SettingsViewHolder) viewHolder;
        settingsViewHolder.tvSettingsKey.setText(settingsData.get(i).getKey());
        settingsViewHolder.tvSettingsValue.setText(settingsData.get(i).getValue());
    }

    @Override
    public int getItemCount() {
        return settingsData.size();
    }

    private class SettingsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSettingsKey;
        private TextView tvSettingsValue;

        private SettingsViewHolder(View itemView) {
            super(itemView);
            tvSettingsKey = itemView.findViewById(R.id.tvSettingsKey);
            tvSettingsValue = itemView.findViewById(R.id.tvSettingsValue);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    String key = tvSettingsKey.getText().toString();
                    String val = tvSettingsValue.getText().toString();

                    final EditText editText = new EditText(context);
                    editText.setText(val);

                    AlertDialog.Builder aBox = new AlertDialog.Builder(context);
                    aBox.setView(editText);

                    switch (key) {
                        case "North SIM":
                            aBox.setMessage("Set north sim");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String northSim = editText.getText().toString();

                                    if (!northSim.isEmpty()) {
                                        spEditor.remove("northSim");
                                        spEditor.apply();
                                        spEditor.putString("northSim", northSim);
                                        spEditor.apply();
                                        tvSettingsValue.setText(northSim);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "North Wifi":
                            aBox.setMessage("Set north wifi");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String northWifi = editText.getText().toString();

                                    if (!northWifi.isEmpty()) {
                                        spEditor.remove("northWifi");
                                        spEditor.apply();
                                        spEditor.putString("northWifi", northWifi);
                                        spEditor.apply();
                                        tvSettingsValue.setText(northWifi);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Central SIM":
                            aBox.setMessage("Set central sim");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String centralSim = editText.getText().toString();

                                    if (!centralSim.isEmpty()) {
                                        spEditor.remove("centralSim");
                                        spEditor.apply();
                                        spEditor.putString("centralSim", centralSim);
                                        spEditor.apply();
                                        tvSettingsValue.setText(centralSim);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Central Wifi":
                            aBox.setMessage("Set central wifi");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String centralWifi = editText.getText().toString();

                                    if (!centralWifi.isEmpty()) {
                                        spEditor.remove("centralWifi");
                                        spEditor.apply();
                                        spEditor.putString("centralWifi", centralWifi);
                                        spEditor.apply();
                                        tvSettingsValue.setText(centralWifi);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "South SIM":
                            aBox.setMessage("Set south sim.");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String southSim = editText.getText().toString();

                                    if (!southSim.isEmpty()) {
                                        spEditor.remove("southSim");
                                        spEditor.apply();
                                        spEditor.putString("southSim", southSim);
                                        spEditor.apply();
                                        tvSettingsValue.setText(southSim);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "South Wifi":
                            aBox.setMessage("Set south wifi");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String southWifi = editText.getText().toString();

                                    if (!southWifi.isEmpty()) {
                                        spEditor.remove("southWifi");
                                        spEditor.apply();
                                        spEditor.putString("southWifi", southWifi);
                                        spEditor.apply();
                                        tvSettingsValue.setText(southWifi);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Dumaguete SIM":
                            aBox.setMessage("Set dumaguete sim");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String dumagueteSim = editText.getText().toString();

                                    if (!dumagueteSim.isEmpty()) {
                                        spEditor.remove("dumagueteSim");
                                        spEditor.apply();
                                        spEditor.putString("dumagueteSim", dumagueteSim);
                                        spEditor.apply();
                                        tvSettingsValue.setText(dumagueteSim);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Dumaguete Wifi":
                            aBox.setMessage("Set dumaguete wifi");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String dumagueteWifi = editText.getText().toString();

                                    if (!dumagueteWifi.isEmpty()) {
                                        spEditor.remove("dumagueteWifi");
                                        spEditor.apply();
                                        spEditor.putString("dumagueteWifi", dumagueteWifi);
                                        spEditor.apply();
                                        tvSettingsValue.setText(dumagueteWifi);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Password":
                            final EditText etPassword = new EditText(context);
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            etPassword.setText(val);

                            aBox.setView(etPassword);
                            aBox.setTitle(key);
                            aBox.setMessage("Set mcsa android password.");
                            aBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = etPassword.getText().toString();
                                    if (!password.isEmpty()) {
                                        spEditor.remove("mcsaPasswordPrefs");
                                        spEditor.apply();
                                        spEditor.putString("mcsaPasswordPrefs", password);
                                        spEditor.apply();
                                        tvSettingsValue.setText(password);
                                    }
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Domain":

                            AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);

                            String[] plantURLs = {"North SIM", "North Wifi", "Central SIM",
                                    "Central Wifi", "South SIM", "South Wifi"};

                            aBuilder.setItems(plantURLs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isWifiEnabled() || isDataEnabled()
                                            || isInternetAvailable() || isNetworkConnected()) {

                                        String branch = "";
                                        String address = "";

                                        switch (which) {
                                            case 0:
                                                address = taskPrefs.getString("northSim", null);
                                                branch = "North SIM";
                                                break;
                                            case 1:
                                                address = taskPrefs.getString("northWifi", null);
                                                branch = "North Wifi";
                                                break;
                                            case 2:
                                                address = taskPrefs.getString("centralSim", null);
                                                branch = "Central SIM";
                                                break;
                                            case 3:
                                                address = taskPrefs.getString("centralWifi", null);
                                                branch = "Central Wifi";
                                                break;
                                            case 4:
                                                address = taskPrefs.getString("southSim", null);
                                                branch = "South SIM";
                                                break;
                                            case 5:
                                                address = taskPrefs.getString("southWifi", null);
                                                branch = "South Wifi";
                                                break;
                                        }

                                        new CheckValidDomainTask(tvSettingsValue, context, branch)
                                                .execute(address);
                                    } else {
                                        Util.alertBox(context, "You are not connected to " +
                                                "network. Turn on Wifi/Data first.");
                                    }
                                }
                            });

                            aBuilder.create().show();
                    }
                }
            });
        }
    }

    private boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    private boolean isDataEnabled() {
        if (context != null) {
            boolean mobileDataEnabled;

            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
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
            //Util.shortToast(context, "Context is null. Cannot get data enabled status.");
            Log.e("NULL", "Context is null. Cannot get data enabled status.");
            return false;
        }
    }

    private boolean isNetworkConnected() {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        } else {
            //Util.shortToast(context, "Activity is null. Can't get network connected status.");
            Log.e("NULL", "Activity is null. Can't get network connected status.");
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