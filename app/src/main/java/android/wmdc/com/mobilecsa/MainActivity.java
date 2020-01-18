package android.wmdc.com.mobilecsa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchContactTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchCustomerTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchJONumberDCTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchJONumberQCTask;
import android.wmdc.com.mobilecsa.model.GPSTracker;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;
    private ProgressBar progressBarMain;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarMain = findViewById(R.id.progressBarMain);
        Util.progressBarMain = progressBarMain;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setNavMenuItemThemeColors(Color.BLACK, navigationView);

        View header = navigationView.getHeaderView(0);

        TextView tvUserEmail = header.findViewById(R.id.tvUserEmail);
        TextView tvUserName = header.findViewById(R.id.tvUserName);
        TextView tvBranch = header.findViewById(R.id.tvBranch);
        TextView tvVersion = header.findViewById(R.id.tvVersion);

        tvUserEmail.setText(sharedPreferences.getString("csaFullName", null));
        tvUserName.setText(sharedPreferences.getString("user", null));
        tvBranch.setText(sharedPreferences.getString("branch", null));
        tvVersion.setText("Version 2.1");

        Log.d("csaFullName", sharedPreferences.getString("csaFullName", null));
        Log.d("user", sharedPreferences.getString("user", null));
        Log.d("branch", sharedPreferences.getString("branch", null));

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                R.anim.pop_exit);
        fragmentTransaction.replace(R.id.content_main, new HomeFragment());
        fragmentTransaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                Util.minKey(MainActivity.this);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        GPSTracker gpsTracker = new GPSTracker(MainActivity.this);
        Log.d(gpsTracker.getLatitude()+"", gpsTracker.getLongitude()+"");
    }

    @Override
    public void onBackPressed() {
        /*
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onB ackPressed();
        }*/
        
        toolbar.setLogo(null);
        Fragment currFrag = getSupportFragmentManager().findFragmentById(R.id.content_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (currFrag instanceof PhotoFragment
                || currFrag instanceof ContactsInformationFragment
                || currFrag instanceof CustomerInformationFragment
                || currFrag instanceof CompanyInformationFragment
                || currFrag instanceof JoSearchInfoFragment
                || currFrag instanceof QCJOInfoFragment
                || currFrag instanceof DCJOInfoFragment
                || currFrag instanceof UpdateInitialJOFragment
                || currFrag instanceof JOResultFragment
                || currFrag instanceof WorkOrderFragment) {
            super.onBackPressed();
        } else {
            Util.handleBackPress(currFrag, MainActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);

        final Fragment currFrag = getSupportFragmentManager().findFragmentById(R.id.content_main);

        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        if (currFrag instanceof DateCommitFragment ||
                currFrag instanceof QualityCheckFragment) {
            searchView.setQueryHint("Search JO Number");
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    if (query.length() > 1) {
                        Util.minKey(MainActivity.this);

                        if (currFrag instanceof SearchCustomerFragment ||
                                currFrag instanceof CustomerResultFragment) {
                            new SearchCustomerTask(MainActivity.this).execute(query);
                        } else if (currFrag instanceof SearchContactFragment ||
                                currFrag instanceof ContactsResultFragment) {
                            new SearchContactTask(MainActivity.this).execute(query);
                        } else if (currFrag instanceof DateCommitFragment) {
                            try {
                                new SearchJONumberDCTask(MainActivity.this)
                                        .execute(String.valueOf(Integer.parseInt(query)), "true");
                            } catch (NumberFormatException e) {
                                new SearchJONumberDCTask(MainActivity.this).execute(query, "false");
                            }
                        } else if (currFrag instanceof QualityCheckFragment) {
                            try {
                                new SearchJONumberQCTask(MainActivity.this).
                                        execute(String.valueOf(Integer.parseInt(query)), "true");
                            } catch (NumberFormatException e) {
                                new SearchJONumberQCTask(MainActivity.this).execute(query, "false");
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Too Short.", Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    return true;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_back_nav:
            case R.id.action_back:
                onBackPressed();
                return true;
            case R.id.action_edit:
                return true;
            case R.id.action_delete:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        toolbar.setLogo(null);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                R.anim.pop_exit);
        Fragment currFrag = getSupportFragmentManager().findFragmentById(R.id.content_main);

        switch (item.getItemId()) {
            case R.id.nav_home:
                if (!(currFrag instanceof HomeFragment)) {
                    fragmentTransaction.replace(R.id.content_main, new HomeFragment());
                    fragmentTransaction.commit();
                }
                break;
            case R.id.nav_customer:
                if(!(currFrag instanceof CustomerFragment)) {
                    fragmentTransaction.replace(R.id.content_main, new CustomerFragment());
                    fragmentTransaction.commit();
                }
                break;
            case R.id.nav_contact:
                if (!(currFrag instanceof ContactFragment)) {
                    fragmentTransaction.replace(R.id.content_main, new ContactFragment());
                    fragmentTransaction.commit();
                }
                break;
            case R.id.nav_job_order:
                if (!(currFrag instanceof JOFragment)) {
                    fragmentTransaction.replace(R.id.content_main, new JOFragment());
                    fragmentTransaction.commit();
                }
                break;
            case R.id.nav_setting:

                final String mcsaPassword = sharedPreferences.getString("mcsaPasswordPrefs", null);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Password");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        if (!password.isEmpty()) {
                            if (password.equals(mcsaPassword)) {
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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

                break;
            case R.id.nav_logout:
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.remove("csaId");
                editor.remove("csaFullname");
                editor.remove("sessionId");
                editor.apply();

                final ProgressDialog progress2 = new ProgressDialog(MainActivity.this);
                progress2.setTitle("Logout");
                progress2.setMessage("Logging out. Please wait...");
                progress2.setCancelable(false);
                progress2.show();
                Runnable progressRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        progress2.cancel();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                };

                Handler pdCanceller2 = new Handler();
                pdCanceller2.postDelayed(progressRunnable2, 1000);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setNavMenuItemThemeColors(int color, NavigationView mNavView) {
        //Setting default colors for menu item Text and Icon
        int navDefaultTextColor = Color.parseColor("#000000");
        int navDefaultIconColor = Color.parseColor("#000000");

        //Defining ColorStateList for menu item Text
        ColorStateList navMenuTextList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed}
                },
                new int[] {
                        color,
                        navDefaultTextColor,
                        navDefaultTextColor,
                        navDefaultTextColor,
                        navDefaultTextColor
                }
        );

        //Defining ColorStateList for menu item Icon
        ColorStateList navMenuIconList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed}
                },
                new int[] {
                        color,
                        navDefaultIconColor,
                        navDefaultIconColor,
                        navDefaultIconColor,
                        navDefaultIconColor
                }
        );

        mNavView.setItemTextColor(navMenuTextList);
        mNavView.setItemIconTintList(navMenuIconList);
    }

    private class SwitchPlantTask extends AsyncTask<String, String, String> {
        private HttpURLConnection conn = null;
        private URL url = null;
        private ProgressDialog progressDialog;
        private Context context;
        private String branch;

        public SwitchPlantTask(Context context, String branch) {
            this.context = context;
            this.branch = branch;
            progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Plant");
            progressDialog.setMessage("Switching to "+branch+ ". Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.remove("csaId");
                editor.remove("csaFullname");
                editor.remove("sessionId");
                editor.apply();

                url = new URL(params[0]);

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(10_000);
                conn.setRequestMethod("GET");
                conn.connect();

                int statusCode = conn.getResponseCode();
                return "{\"success\": true, \"code\": "+statusCode+", " +
                        "\"domain\": \""+params[0]+"\", \"reason\": \"Signing in to "+branch+"\"}";
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
        protected void onPostExecute(String res) {
            try {
                JSONObject result = new JSONObject(res);
                if (result.getBoolean("success")) {
                    if (result.getInt("code") == 200) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.remove("domain");
                        editor.apply();
                        editor.putString("domain", result.getString("domain"));
                        editor.apply();

                        editor.remove("branch");
                        editor.apply();
                        editor.putString("branch", branch);
                        editor.apply();

                        String username = sharedPreferences.getString("user", null);
                        String password = sharedPreferences.getString("password", null);

                        new AutoLoginTask(progressDialog).execute(
                                result.getString("domain"), username, password);
                    } else {
                        progressDialog.dismiss();
                        Util.alertBox(context, "Response code is not ok.", "Request Failed.", false);
                    }
                } else {
                    progressDialog.dismiss();
                    Util.alertBox(context, result.getString("reason"), "", false);
                }
            } catch (JSONException je) {
                progressDialog.dismiss();
                Util.displayStackTraceArray(je.getStackTrace(),
                        Variables.MOBILECSA_PACKAGE, "JSONException", je.toString());
                Util.alertBox(context, je.getMessage(), "", false);
            }
        }
    }

    private class AutoLoginTask extends AsyncTask<String, String, String> {
        private HttpURLConnection conn = null;
        private URL url = null;
        private ProgressDialog progressDialog;

        public AutoLoginTask(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                url = new URL(params[0]+"Login");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(10_000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[1])
                        .appendQueryParameter("password", params[2]);
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
            this.progressDialog.dismiss();
            try {
                JSONObject responseJson = new JSONObject(result);
                SharedPreferences.Editor spEditor = sharedPreferences.edit();

                spEditor.putString("sessionId", responseJson.getString("sessionId"));
                spEditor.apply();

                Intent intent = new Intent(MainActivity.this, SecurityKeyActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}