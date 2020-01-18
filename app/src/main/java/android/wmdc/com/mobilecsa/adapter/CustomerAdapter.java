package android.wmdc.com.mobilecsa.adapter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.CompanyInformationFragment;
import android.wmdc.com.mobilecsa.CustomerInformationFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.SetThumbnailTask;
import android.wmdc.com.mobilecsa.model.Customer;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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
import java.util.ArrayList;

/**
 * Created by wmdcprog on 12/26/2017.
 */

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private ArrayList<Customer> customers;
    private SharedPreferences sharedPreferences;
    private boolean heightSet = false;

    public CustomerAdapter(Context context, ArrayList<Customer> customers) {
        this.context = context;
        this.customers = customers;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void replaceAdapterList(ArrayList<Customer> customers) {
        this.customers = null;
        this.customers = customers;
        this.notifyDataSetChanged();
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.search_result_layout, viewGroup,
                false);

        if (!heightSet) {
            final ConstraintLayout rootLay = view.findViewById(R.id.rootLay);
            final ViewTreeObserver observer = rootLay.getViewTreeObserver();

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Util.recyclerViewItemHeight = rootLay.getHeight();
                }
            });
        }

        heightSet = true;
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomerViewHolder entityViewHolder, int i) {

        entityViewHolder.tvName.setText(customers.get(i).getName());
        entityViewHolder.tvCsa.setText(customers.get(i).getCsa());
        entityViewHolder.tvId.setText(customers.get(i).getId()+"");
        entityViewHolder.index = i;

        if (customers.get(i).getIsTransferred() > 0) {
            entityViewHolder.ivTransferred.setImageResource(
                    R.drawable.ic_action_check_colored_round);
        } else {
            entityViewHolder.ivTransferred.setImageResource(R.drawable.ic_action_x_colored_round);
        }

        new SetThumbnailTask(customers.get(i).getImageUrl(),
                entityViewHolder.ivProfile, context).execute();
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfile;
        private ImageView ivTransferred;
        private TextView tvName;
        private TextView tvCsa;
        private TextView tvId;
        private int index;

        public CustomerViewHolder(View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivTransferred = itemView.findViewById(R.id.ivTransferred);
            tvName = itemView.findViewById(R.id.tvName);
            tvCsa = itemView.findViewById(R.id.tvCsa);
            tvId = itemView.findViewById(R.id.tvId);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (tvCsa.getText().toString().equals(
                            sharedPreferences.getString("csaFullName", null))) {

                        new GetCustomerTask().execute(tvId.getText().toString(),
                                String.valueOf(customers.get(index).isAPerson()));
                    } else {
                        Util.alertBox(context, "Not your customer.", "Not Allowed", false);
                    }
                }
            });
        }
    }

    private class GetCustomerTask extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog = new ProgressDialog(context);
        private HttpURLConnection conn = null;
        private URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Searching...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                if (Boolean.parseBoolean(params[1])) {
                    url = new URL(sharedPreferences.getString("domain", null)+"getindividual");
                } else {
                    url = new URL(sharedPreferences.getString("domain", null)
                            +"getcustomercompanybyparams");
                }

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(Util.READ_TIMEOUT);
                conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie",
                        "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/httpsessiontest");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("customerId",
                        params[0]);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

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
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("success")) {
                    Fragment cifFrag;

                    if (Boolean.parseBoolean(jsonObject.get("isCompany").toString())) {
                        cifFrag = new CompanyInformationFragment();
                    } else {
                        cifFrag = new CustomerInformationFragment();
                    }

                    Bundle bundle = new Bundle();

                    bundle.putString("result", jsonObject.toString());
                    cifFrag.setArguments(bundle);

                    FragmentManager fragmentManager = ((AppCompatActivity) context)
                            .getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                            R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.replace(R.id.content_main, cifFrag);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    Util.alertBox(context, jsonObject.getString("reason"), "Error", false);
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.ADAPTER_PACKAGE, "",
                        je.toString());
                Util.alertBox(context, je.getMessage(), "", false);
            }
        }
    }
}