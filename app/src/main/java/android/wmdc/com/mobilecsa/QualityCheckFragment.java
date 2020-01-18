package android.wmdc.com.mobilecsa;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.QCAdapter;
import android.wmdc.com.mobilecsa.model.QCDataModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
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
import java.util.ArrayList;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class QualityCheckFragment extends Fragment {

    private TextView tvPage;

    private ImageButton next;
    private ImageButton prev;
    private ImageButton first;
    private ImageButton last;

    private ArrayList<QCDataModel> qcDataModels = new ArrayList<>();
    private QCAdapter qcAdapter;

    private SharedPreferences sharedPreferences;

    private LinearLayout qcHeaderLinLay;
    private SetPageTask setPageTask;

    @Override
    public void onResume() {
        super.onResume();
        setPage(Variables.currentPage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.quality_check_fragment, container, false);
        getActivity().setTitle("Quality Check");
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        qcHeaderLinLay = v.findViewById(R.id.qcTitleLL);
        qcHeaderLinLay.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.qc_header_layout);

                WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                wmlp.width = Util.systemWidth;
                wmlp.gravity = Gravity.TOP;
                wmlp.y = 200;

                dialog.show();
            }
        });

        tvPage = v.findViewById(R.id.tvPage);
        tvPage.setText(new StringBuilder().append(Variables.currentPage)
                .append(" of ").append(Variables.lastPage).toString());

        final RecyclerView recyclerView = v.findViewById(R.id.rvQCData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        next = v.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage < Variables.lastPage) {
                    setPage(++Variables.currentPage);
                }
            }
        });

        prev = v.findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage > 1) {
                    setPage(--Variables.currentPage);
                }
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            final String searchResult = bundle.getString("searchResult");

            try {
                final JSONObject jsonObject = new JSONObject(searchResult);
                final JSONArray jsonArray = jsonObject.getJSONArray("joborders");

                int jobordersArrayLength = jsonArray.length();

                if (jobordersArrayLength > 0) {
                    int i;

                    for (i = 1; i <= jobordersArrayLength; ++i) {
                        JSONObject itemObj = jsonArray.getJSONObject(i - 1);

                        qcDataModels.add(new QCDataModel(
                            itemObj.getInt("joId"),
                            itemObj.getString("serialNum"),
                            itemObj.getString("dateCommit"),
                            itemObj.getString("dateReceived"),
                            itemObj.getString("joNum"),
                            itemObj.getString("customerId"),
                            itemObj.getString("model"),
                            itemObj.getString("category"),
                            itemObj.getString("make"),
                            itemObj.getString("customer"),
                            itemObj.getBoolean("isPending")
                        ));
                    }

                    qcAdapter = new QCAdapter(qcDataModels, getActivity());
                    recyclerView.setAdapter(qcAdapter);
                } else {
                    Util.alertBox(getContext(), "Empty List", "Joborders", false);
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
            }
        }

        return v;
    }

    public void setPage(int currentPage) {
        setPageTask = new SetPageTask(getActivity());
        setPageTask.execute(String.valueOf(sharedPreferences.getInt("csaId", 0)),
                "mcsa", String.valueOf(currentPage));
    }

    private class SetPageTask extends AsyncTask<String, String, String> {
        private SharedPreferences sharedPreferences;
        private Context context;

        private ProgressDialog progressDialog;
        private HttpURLConnection conn = null;
        private URL url = null;

        public SetPageTask(Context context) {
            this.context = context;
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            this.progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Quality Check");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                url = new URL(sharedPreferences.getString("domain", null)+"getqclist");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60_000);
                conn.setConnectTimeout(60_000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("cid", params[0])
                        .appendQueryParameter("source", params[1])
                        .appendQueryParameter("page", params[2]);
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
                    return "{\"success\": false, \"reason\": \"Request did not succeed. Status Code: "+statusCode+"\"}";
                }
            } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
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
                Util.displayStackTraceArray(e.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
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

            try {
                JSONObject jsonObject = new JSONObject(result);
                Variables.qcStore = jsonObject;
                JSONArray jsonArray = jsonObject.getJSONArray("joborders");
                int jobordersArrayLength = jsonArray.length();

                qcDataModels.clear();

                if (jobordersArrayLength > 0) {
                    int i;

                    for (i = 1; i <= jobordersArrayLength; ++i) {
                        JSONObject itemObj = jsonArray.getJSONObject(i-1);

                        qcDataModels.add(
                                new QCDataModel(
                                        itemObj.getInt("joId"),
                                        itemObj.getString("serialNum"),
                                        itemObj.getString("dateCommit"),
                                        itemObj.getString("dateReceived"),
                                        itemObj.getString("joNum"),
                                        itemObj.getString("customerId"),
                                        itemObj.getString("model"),
                                        itemObj.getString("category"),
                                        itemObj.getString("make"),
                                        itemObj.getString("customer"),
                                        itemObj.getBoolean("isPending")
                                ));
                    }

                    qcAdapter.replaceAdapterList(qcDataModels);

                    tvPage.setText(new StringBuilder().append(Variables.currentPage)
                            .append(" of ").append(Variables.lastPage).toString());
                } else {
                    Util.alertBox(getContext(), "Empty List", "Joborders", false);
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem refreshAction = menu.findItem(R.id.action_refresh);
        refreshAction.setVisible(true);

        refreshAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setPage(Variables.currentPage);
                return true;
            }
        });

        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}