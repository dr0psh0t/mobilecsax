package android.wmdc.com.mobilecsa.adapter;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.ContactsInformationFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.SetThumbnailTask;
import android.wmdc.com.mobilecsa.model.Contacts;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
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
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 12/27/2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private FragmentActivity fragmentActivity;
    private ArrayList<Contacts> contacts;
    private SharedPreferences sharedPreferences;
    private boolean heightSet = false;

    public ContactsAdapter(FragmentActivity fragmentActivity, ArrayList<Contacts> contacts) {
        this.fragmentActivity = fragmentActivity;
        this.contacts = contacts;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fragmentActivity);
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(fragmentActivity).inflate(R.layout.search_result_layout,
                viewGroup, false);

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
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder contactsViewHolder, int i) {
        contactsViewHolder.tvName.setText(contacts.get(i).getName());
        contactsViewHolder.tvCsa.setText(contacts.get(i).getCsa());
        contactsViewHolder.tvId.setText(String.valueOf(contacts.get(i).getId()));

        if (contacts.get(i).getIsTransferred() > 0) {
            contactsViewHolder.ivTransferred.setImageResource(
                    R.drawable.ic_action_check_colored_round);
        } else {
            contactsViewHolder.ivTransferred.setImageResource(R.drawable.ic_action_x_colored_round);
        }

        new SetThumbnailTask(contacts.get(i).getImageUrl(), contactsViewHolder.ivProfile,
                fragmentActivity).execute();
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfile;
        private ImageView ivTransferred;
        private TextView tvName;
        private TextView tvCsa;
        private TextView tvId;

        private ContactsViewHolder(View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivTransferred = itemView.findViewById(R.id.ivTransferred);
            tvName = itemView.findViewById(R.id.tvName);
            tvCsa = itemView.findViewById(R.id.tvCsa);
            tvId = itemView.findViewById(R.id.tvId);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (tvCsa.getText().toString().equals(sharedPreferences.getString(
                            "csaFullName", null))) {
                        new GetContactsTask(fragmentActivity).execute(tvId.getText().toString());
                    } else {
                        Util.alertBox(fragmentActivity, "Denied");
                    }
                }
            });
        }
    }

    private static class GetContactsTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        private HttpURLConnection conn = null;

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private GetContactsTask(FragmentActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            progressDialog = new ProgressDialog(activity);
        }

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
                URL url = new URL(taskPrefs.getString("domain", null)+"getcontactsbyparams");

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
                        "JSESSIONID="+taskPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/httpsessiontest");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder build = new Uri.Builder().appendQueryParameter("contactId", params[0]);
                String query = build.build().getEncodedQuery();

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
            progressDialog.dismiss();

            FragmentActivity mainActivity = activityWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject resJson = new JSONObject(result);

                if (resJson.getBoolean("success")) {
                    ContactsInformationFragment contactsInformationFragment =
                            new ContactsInformationFragment();

                    Bundle bundle = new Bundle();

                    bundle.putString("result", resJson.toString());
                    contactsInformationFragment.setArguments(bundle);

                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.setCustomAnimations(R.anim.enter,
                            R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

                    fragmentTransaction.replace(R.id.content_main, contactsInformationFragment)
                            .addToBackStack(null).commit();   //  required
                } else {
                    Util.alertBox(mainActivity, resJson.getString("reason"));
                }

            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.ADAPTER_PACKAGE, "",
                        je.toString());
                Util.alertBox(mainActivity, "Parse error");
            }
        }
    }
}