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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

/** Created by wmdcprog on 3/14/2018.*/

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final ArrayList<KeyValueInfo> settingsData;
    private final FragmentActivity activity;
    private final SharedPreferences taskPrefs;
    private final SharedPreferences.Editor spEditor;
    private final WifiManager wifiManager;

    public SettingsAdapter(ArrayList<KeyValueInfo> settingsData, FragmentActivity activity) {
        this.settingsData = settingsData;
        this.activity = activity;
        this.taskPrefs = PreferenceManager.getDefaultSharedPreferences(this.activity);
        this.spEditor = this.taskPrefs.edit();
        spEditor.apply();
        this.wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
    }

    @Override
    @NonNull
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.settings_card_layout, viewGroup,
                false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder settingsViewHolder, int i) {
        settingsViewHolder.tvSettingsKey.setText(settingsData.get(i).getKey());
        settingsViewHolder.tvSettingsValue.setText(settingsData.get(i).getValue());
    }

    @Override
    public int getItemCount() {
        return settingsData.size();
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSettingsKey;
        private final TextView tvSettingsValue;

        private SettingsViewHolder(final View itemView) {
            super(itemView);
            tvSettingsKey = itemView.findViewById(R.id.tvSettingsKey);
            tvSettingsValue = itemView.findViewById(R.id.tvSettingsValue);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    String key = tvSettingsKey.getText().toString();
                    String val = tvSettingsValue.getText().toString();

                    View settingsView = LayoutInflater.from(activity).inflate(
                            R.layout.settings_input_dialog, (ViewGroup) itemView, false);

                    final EditText editText = settingsView.findViewById(R.id.etDomain);

                    AlertDialog.Builder aBox = new AlertDialog.Builder(activity);
                    aBox.setView(settingsView);
                    aBox.setCancelable(false);
                    aBox.setMessage("Update IP for "+key);
                    aBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });

                    switch (key) {
                        case "North SIM":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "northSim");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "North Wifi":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "northWifi");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Central SIM":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "centralSim");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Central Wifi":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "centralWifi");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "South SIM":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "southSim");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "South Wifi":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "southWifi");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Dumaguete SIM":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "dumagueteSim");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Dumaguete Wifi":
                            aBox.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateAddress(editText.getText().toString(), "dumagueteWifi");
                                }
                            });
                            aBox.create().show();
                            break;
                        case "Password":
                            final EditText etPassword = new EditText(activity);
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

                            AlertDialog.Builder aBuilder = new AlertDialog.Builder(activity);

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

                                        new CheckValidDomainTask(activity, tvSettingsValue, branch)
                                                .execute(address);
                                    } else {
                                        Util.alertBox(activity, "You are not connected to " +
                                                "network. Turn on Wifi/Data first.");
                                    }
                                }
                            });

                            aBuilder.create().show();
                    }
                }
            });
        }

        private void updateAddress(String ip, String prefsName) {
            if (!ip.isEmpty()) {
                spEditor.remove(prefsName);
                spEditor.apply();
                spEditor.putString(prefsName, "http://"+ip+"/mcsa/");
                spEditor.apply();
                tvSettingsValue.setText(ip);
            }
        }
    }

    private boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    private boolean isDataEnabled() {
        if (activity != null) {
            boolean mobileDataEnabled;

            ConnectivityManager cm = (ConnectivityManager) activity.getApplicationContext()
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
        if (activity != null) {
            ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(
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