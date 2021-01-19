package android.wmdc.com.mobilecsa.adapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.MapsActivity;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageTask;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 12/28/2017.
 */

public class KeyValueInfoAdapter extends
        RecyclerView.Adapter<KeyValueInfoAdapter.CustomerInfoViewHolder> {

    private final FragmentActivity fragmentActivity;
    private final ArrayList<KeyValueInfo> customerInfos;

    private final int kvId;
    private final boolean isCustomer;

    private final SharedPreferences sharedPreferences;

    public KeyValueInfoAdapter(FragmentActivity activity, ArrayList<KeyValueInfo> customerInfos,
                               int kvId, boolean isCustomer) {

        fragmentActivity = activity;
        this.customerInfos = customerInfos;
        this.kvId = kvId;
        this.isCustomer =isCustomer;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fragmentActivity);
    }

    @Override
    @NonNull
    public CustomerInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(fragmentActivity).inflate(
                R.layout.customer_information_layout, viewGroup, false);
        return new CustomerInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerInfoViewHolder customerInfoViewHolder, int i) {
        String key = customerInfos.get(i).getKey();

        switch (key) {
            case "Photo":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_photo);
                break;
            case "Lastname":
            case "Firstname":
            case "MI":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_translate);
                break;
            case "Country":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_flag);
                break;
            case "Zip Code":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_post_code);
                break;
            case "Address":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_directions);
                break;
            case "City":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_location_city);
                break;
            case "Province":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_terrain);
                break;
            case "Location":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_location);
                break;
            case "Industry":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_industry);
                break;
            case "Plant":
                customerInfoViewHolder.icon.setImageResource(
                        R.drawable.ic_action_local_convenience_store);
                break;
            case "Date of Birth":
            case "Date Added":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_date);
                break;
            case "CSA":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_person);
                break;
            case "Job Position":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_work);
                break;
            case "Telephone":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_phone);
                break;
            case "Fax":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_perm_phone_msg);
                break;
            case "Mobile":
            case "Emergency Contact":
            case "Contact Number":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_phone_android);
                break;
            case "Signature":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_check);
                break;
            case "ER":
            case "MF":
            case "Calib":
                customerInfoViewHolder.icon.setImageResource(
                        R.drawable.ic_action_format_list_number);
                break;
            case "Contact Person":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_contact_phone);
                break;
            case "Company":
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_domain);
                break;
            default:
                customerInfoViewHolder.icon.setImageResource(R.drawable.ic_action_clear);
                break;
        }

        customerInfoViewHolder.tvKey.setText(key);
        customerInfoViewHolder.tvValue.setText(customerInfos.get(i).getValue());
    }

    @Override
    public int getItemCount() {
        return customerInfos.size();
    }

    class CustomerInfoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView tvKey;
        private final TextView tvValue;

        private CustomerInfoViewHolder(View itemView) {
            super(itemView);

            this.icon = itemView.findViewById(R.id.icon);
            this.tvKey = itemView.findViewById(R.id.tvKey);
            this.tvValue = itemView.findViewById(R.id.tvValue);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {

                    if (tvKey.getText().toString().equals("Photo")) {
                        String url;

                        if (isCustomer) {
                            url = sharedPreferences.getString("domain", null)
                                    +"getcustomerphoto?customerId="+kvId;
                        } else {
                            url = sharedPreferences.getString("domain", null)
                                    +"getcontactsphoto?contactId="+kvId;
                        }

                        new DialogImageTask(fragmentActivity).execute(url);
                    } else if (tvKey.getText().toString().equals("Signature")) {
                        String url;

                        if (isCustomer) {
                            url = sharedPreferences.getString("domain", null)
                                    +"getcustomersignature?customerId="+kvId;
                        } else {
                            url = sharedPreferences.getString("domain", null)
                                    +"getcontactsignature?contactId="+kvId;
                        }

                        new DialogImageTask(fragmentActivity).execute(url);
                    } else if (tvKey.getText().toString().equals("Location")) {
                        final ProgressDialog progress = new ProgressDialog(fragmentActivity);

                        progress.setTitle("Maps");
                        progress.setMessage("Opening maps. Please wait...");
                        progress.setCancelable(false);
                        progress.show();

                        Runnable progressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                new LocationTask(fragmentActivity).execute(""+kvId, ""+isCustomer);
                                progress.dismiss();
                            }
                        };

                        Handler handler = new Handler();
                        handler.postDelayed(progressRunnable, 1500);
                    }
                }
            });
        }
    }

    private static class LocationTask extends AsyncTask<String, String, String> {

        private final WeakReference<FragmentActivity> activityWeakReference;

        private HttpURLConnection conn = null;

        private final SharedPreferences taskPrefs;

        private LocationTask(FragmentActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] args) {
            try {
                URL url = new URL(taskPrefs.getString("domain", null)+"getlocationandroid");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(Util.READ_TIMEOUT);
                conn.setConnectTimeout(Util.READ_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", args[0])
                        .appendQueryParameter("isCustomer", args[1]);
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

                if (statusCode == 200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }

                    is.close();
                    br.close();

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
                Util.displayStackTraceArray(e.getStackTrace(), Variables.ADAPTER_PACKAGE,
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
                Util.displayStackTraceArray(e.getStackTrace(), Variables.ADAPTER_PACKAGE,
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
            FragmentActivity mainActivity = activityWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject responseJson = new JSONObject(result);

                if (responseJson.getBoolean("success")) {
                    Intent intent = new Intent(mainActivity, MapsActivity.class);

                    intent.putExtra("lat", responseJson.getDouble("lat"));
                    intent.putExtra("lng", responseJson.getDouble("lng"));
                    intent.putExtra("object", responseJson.getString("object"));

                    mainActivity.startActivity(intent);
                } else {
                    Util.longToast(mainActivity, responseJson.getString("reason"));
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.ADAPTER_PACKAGE,
                        "Exception", e.toString());
                Util.longToast(mainActivity, "Error");
            }
        }
    }
}