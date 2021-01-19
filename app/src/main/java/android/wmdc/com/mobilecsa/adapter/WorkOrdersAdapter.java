package android.wmdc.com.mobilecsa.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.WorkOrders;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

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

/**Created by wmdcprog on 9/15/2018.*/

public class WorkOrdersAdapter extends
        RecyclerView.Adapter<WorkOrdersAdapter.WorkOrdersViewHolder> {

    //private Context context;
    private final FragmentActivity fragmentActivity;
    private final ArrayList<WorkOrders> workOrders;
    private final FloatingActionButton fabOptions;
    private int selected_position;

    private final FloatingActionButton fabExtension;
    private boolean isFabOpen = false;

    private void showFABMenu(){
        isFabOpen = true;
        fabExtension.animate().translationY(
                -fragmentActivity.getResources().getDimension(R.dimen.standard_75));
    }

    private void closeFABMenu(){
        isFabOpen = false;
        fabExtension.animate().translationY(0);
        fabOptions.setImageResource(R.drawable.ic_action_more_white);
    }

    private void setSnackBar(View root) {
        Snackbar.make(root, "Select a workorder first.", Snackbar.LENGTH_SHORT).show();
    }

    public WorkOrdersAdapter(final FragmentActivity fragmentActivity,
                             ArrayList<WorkOrders> workOrders,
                             final FloatingActionButton fabOptions,
                             FloatingActionButton fabExtension, final LinearLayout linearLayout) {

        this.fragmentActivity = fragmentActivity;
        this.workOrders = workOrders;
        this.fabOptions = fabOptions;
        this.fabExtension = fabExtension;
        this.selected_position = -1;

        this.fabOptions.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (selected_position > -1) {
                    if (!isFabOpen) {
                        fabOptions.setImageResource(R.drawable.ic_action_cancel_white);
                        showFABMenu();
                    } else {
                        closeFABMenu();
                    }
                } else {
                    setSnackBar(linearLayout);
                }
            }
        });

        this.fabExtension.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                closeFABMenu();

                LayoutInflater layoutInflater = LayoutInflater.from(fragmentActivity);

                ViewGroup vGroup = view.findViewById(R.id.custom_dialog_layout_design_user_input);

                View mView = layoutInflater.inflate(R.layout.work_extension_input_dialog, vGroup);

                AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
                builder.setView(mView);

                final EditText hoursInputDialog = mView.findViewById(R.id.hoursInputDialog);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String hours = hoursInputDialog.getText().toString();
                        new ExtensionTask(fragmentActivity).execute(hours);
                        selected_position = -1;
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            }
        });
    }

    @Override
    @NonNull
    public WorkOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(fragmentActivity).inflate(R.layout.workorders_rowitem,
                viewGroup, false);
        return new WorkOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkOrdersViewHolder workOrdersViewHolder, final int i) {
        if (i % 2 != 0) {
            workOrdersViewHolder.rootLayWorkOrders.setBackgroundResource(
                    R.drawable.custom_card_background_odd);
        } else {
            workOrdersViewHolder.rootLayWorkOrders.setBackgroundResource(
                    R.drawable.custom_card_background_even);
        }
        workOrdersViewHolder.tvScope.setText(workOrders.get(i).getScopeWork());
        workOrdersViewHolder.tvStatus.setText(workOrders.get(i).getStatus());

        /*
        if (workOrders.get(i).isSelected()) {
            workOrdersViewHolder.rootLayWorkOrders.setSelected(true);
        } else {
            workOrdersViewHolder.rootLayWorkOrders.setSelected(false);
        }*/

        workOrdersViewHolder.rootLayWorkOrders.setSelected(selected_position == i);

        workOrdersViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selected_position = i;
                notifyDataSetChanged();
            }
        });

        workOrdersViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                selected_position = -1;
                notifyDataSetChanged();
                return true;
            }
        });
    }

    public int getItemCount() {
        return workOrders.size();
    }

    static class WorkOrdersViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvScope;
        private final TextView tvStatus;
        private final LinearLayout rootLayWorkOrders;

        private WorkOrdersViewHolder(View itemView) {
            super(itemView);
            this.tvScope = itemView.findViewById(R.id.tvScope);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.rootLayWorkOrders = itemView.findViewById(R.id.rootLayWorkOrders);
        }
    }

    private static class ExtensionTask extends AsyncTask<String, String, String> {

        private final WeakReference<FragmentActivity> activityWeakReference;

        private HttpURLConnection conn = null;

        private final ProgressDialog progressDialog;

        private final SharedPreferences prefs;

        private ExtensionTask(FragmentActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
            this.progressDialog = new ProgressDialog(activity);
            prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Extending Work...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {

            try {
                URL url = new URL(prefs.getString("domain", null)+"<required>");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(10_000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; " +
                        "charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID=" +
                        prefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "mcsa-android");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("<param-name>",
                        params[0]);
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
            progressDialog.dismiss();

            FragmentActivity mainActivity = activityWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject resJson = new JSONObject(result);
                Util.shortToast(mainActivity, resJson.getString("reason"));
            } catch (Exception e) {
                Util.shortToast(mainActivity, "Error");
            }
        }
    }
}
