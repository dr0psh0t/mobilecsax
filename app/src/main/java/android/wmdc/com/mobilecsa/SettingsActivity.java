package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.wmdc.com.mobilecsa.adapter.SettingsAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private ArrayList<KeyValueInfo> settingsModelList;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        settingsModelList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvSettingsList);

        String northSim = sharedPreferences.getString("northSim", null);
        String centralSim = sharedPreferences.getString("centralSim", null);
        String southSim = sharedPreferences.getString("southSim", null);
        String dumagueteSim = sharedPreferences.getString("dumagueteSim", null);

        String northWifi = sharedPreferences.getString("northWifi", null);
        String centralWifi = sharedPreferences.getString("centralWifi", null);
        String southWifi = sharedPreferences.getString("southWifi", null);
        String dumagueteWifi = sharedPreferences.getString("dumagueteWifi", null);

        String mcsaPasswordPrefs = sharedPreferences.getString("mcsaPasswordPrefs", null);
        String domain = sharedPreferences.getString("domain", null);

        settingsModelList.add(new KeyValueInfo("North SIM", northSim == null ? "" : northSim));
        settingsModelList.add(new KeyValueInfo("North Wifi", northWifi == null ? "" : northWifi));

        settingsModelList.add(new KeyValueInfo("Central SIM", centralSim == null ? "" : centralSim));
        settingsModelList.add(new KeyValueInfo("Central Wifi", centralWifi == null ? "" : centralWifi));

        settingsModelList.add(new KeyValueInfo("South SIM", southSim == null ? "" : southSim));
        settingsModelList.add(new KeyValueInfo("South Wifi", southWifi == null ? "" : southWifi));

        settingsModelList.add(new KeyValueInfo("Dumaguete SIM", dumagueteSim == null ? "" : dumagueteSim));
        settingsModelList.add(new KeyValueInfo("Dumaguete Wifi", dumagueteWifi == null ? "" : dumagueteWifi));

        settingsModelList.add(new KeyValueInfo("Domain", domain == null ? "" : domain));
        settingsModelList.add(new KeyValueInfo("Password", mcsaPasswordPrefs == null ? "" : mcsaPasswordPrefs));

        settingsModelList.add(new KeyValueInfo("", ""));

        SettingsAdapter settingsAdapter = new SettingsAdapter(settingsModelList, SettingsActivity.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(SettingsActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(settingsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(SettingsActivity.this, LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}