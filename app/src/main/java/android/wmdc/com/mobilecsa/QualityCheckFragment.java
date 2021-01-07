package android.wmdc.com.mobilecsa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.QCAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetQCJOListTask;
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
import java.util.ArrayList;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class QualityCheckFragment extends Fragment {

    private static ArrayList<QCDataModel> qcDataModels = new ArrayList<>();
    private static QCAdapter qcAdapter;
    private JSONArray jsonArray;
    private SharedPreferences sp;

    @Override
    public void onResume() {
        super.onResume();
        //setPage(Variables.currentPage, tvPage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.quality_check_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Quality Check");
        } else {
            Util.shortToast(getContext(), "Title error");
            Log.e("Null", "Activity is null. Cannot set title of this fragment.");
        }

        TextView tvPage;
        RecyclerView recyclerView;

        setHasOptionsMenu(true);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        LinearLayout qcHeaderLinLay = v.findViewById(R.id.qcTitleLL);
        qcHeaderLinLay.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                if (getActivity() != null) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.qc_header_layout);

                    if (dialog.getWindow() != null) {
                        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                        wmlp.width = Util.systemWidth;
                        wmlp.gravity = Gravity.TOP;
                        wmlp.y = 200;

                        dialog.show();
                    } else {
                        Util.alertBox(getContext(), "Error");
                        Log.e("Null", "Dialog window is null. Cannot open dialog.");
                    }

                } else {
                    Util.alertBox(getContext(), "Error");
                    Log.e("Null", "Activity is null. Cannot open dialog.");
                }
            }
        });

        tvPage = v.findViewById(R.id.tvPage);
        tvPage.setText(String.valueOf(Variables.currentPage));

        recyclerView = v.findViewById(R.id.rvQCData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ImageButton next = v.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage < Variables.lastPage) {
                    new GetQCJOListTask(getActivity()).execute(String.valueOf(
                            sp.getInt("csaId", 0)), "mcsa", (++Variables.currentPage)+"");
                }
            }
        });

        ImageButton prev = v.findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage > 1) {
                    new GetQCJOListTask(getActivity()).execute(String.valueOf(
                            sp.getInt("csaId", 0)), "mcsa", (--Variables.currentPage)+"");
                }
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            final String searchResult = bundle.getString("searchResult");

            try {
                final JSONObject jsonObject = new JSONObject(searchResult);
                jsonArray = jsonObject.getJSONArray("joborders");

                int jobordersArrayLength = jsonArray.length();

                if (jobordersArrayLength > 0) {

                    int i;
                    qcDataModels.clear();

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
                    Util.alertBox(getContext(), "Empty List");
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
            }
        }

        return v;
    }

    /*
    private void setPage(int currentPage, TextView tvPage) {
        new SetPageTask(getActivity(), tvPage, qcAdapter, qcDataModels)
                .execute(String.valueOf(sp.getInt("csaId", 0)), "mcsa", currentPage+"");
    }

    private static class SetPageTask extends AsyncTask<String, String, String> {

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private ProgressDialog progressDialog;

        private HttpURLConnection conn = null;

        private WeakReference<TextView> tvPageWeakReference;

        private WeakReference<ArrayList<QCDataModel>> qcDataModelsWf;

        private WeakReference<QCAdapter> qcAdapterWf;

        private SetPageTask(FragmentActivity activity, TextView tvPage, QCAdapter qcAdapterParam,
                            ArrayList<QCDataModel> qcDataModelsParam) {

            activityWeakReference = new WeakReference<>(activity);
            this.taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            this.progressDialog = new ProgressDialog(activity);
            this.tvPageWeakReference = new WeakReference<>(tvPage);
            qcDataModelsWf = new WeakReference<>(qcDataModelsParam);
            qcAdapterWf = new WeakReference<>(qcAdapterParam);
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
                URL url = new URL(taskPrefs.getString("domain", null)+"getqclist");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60_000);
                conn.setConnectTimeout(60_000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie",
                        "JSESSIONID="+taskPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer",
                        "http://localhost:8080/mcsa/searchcustomerfromuser");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("akey", taskPrefs.getString("aKey", null))
                        .appendQueryParameter("cid", params[0])
                        .appendQueryParameter("source", params[1])
                        .appendQueryParameter("page", params[2]);

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

            FragmentActivity mainActivity = activityWeakReference.get();
            ArrayList<QCDataModel> qcDataModelsLocal = qcDataModelsWf.get();
            QCAdapter qcAdapterLocal = qcAdapterWf.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                Variables.qcStore = jsonObject;

                JSONArray jsonArray = jsonObject.getJSONArray("joborders");
                int length = jsonArray.length();

                //qcDataModels.clear();
                qcDataModelsLocal.clear();

                if (length > 0) {
                    int i;

                    for (i = 1; i <= length; ++i) {
                        JSONObject itemObj = jsonArray.getJSONObject(i-1);

                        qcDataModelsLocal.add(new QCDataModel(
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

                    //qcAdapter.replaceAdapterList(qcDataModels);
                    //qcAdapter.notifyDataSetChanged();
                    qcAdapterLocal.notifyDataSetChanged();

                    tvPageWeakReference.get().setText(String.valueOf(Variables.currentPage));

                } else {
                    Util.alertBox(mainActivity, "End of page");
                }

            } catch (JSONException je) {
                Util.alertBox(activityWeakReference.get(), je.getMessage());
            }
        }
    }*/

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        MenuItem refreshAction = menu.findItem(R.id.action_refresh);
        refreshAction.setVisible(true);
        refreshAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GetQCJOListTask(getActivity()).execute(String.valueOf(sp.getInt("csaId", 0)),
                        "mcsa", Variables.currentPage+"");
                return true;
            }
        });

        MenuItem searchAction2 = menu.findItem(R.id.action_search2);
        searchAction2.setVisible(true);
        searchAction2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View mView = layoutInflater.inflate(R.layout.user_input_dialog, null);

                AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
                adBuilder.setView(mView);

                final EditText editText = mView.findViewById(R.id.userInputDialog);
                adBuilder.setCancelable(true);
                adBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String joQuery = editText.getText().toString();
                        int length = jsonArray.length();
                        int x;
                        JSONObject object;

                        ArrayList<QCDataModel> qcDataModels2 = new ArrayList<>();

                        for (x = 0; x < length; ++x) {
                            try {
                                object = jsonArray.getJSONObject(x);

                                if (object.getString("joNum").equals(joQuery)) {
                                    System.out.println(object.getString("joNum")+"-"+(joQuery));

                                    qcDataModels2.add(new QCDataModel(
                                        object.getInt("joId"),
                                        object.getString("serialNum"),
                                        object.getString("dateCommit"),
                                        object.getString("dateReceived"),
                                        object.getString("joNum"),
                                        object.getString("customerId"),
                                        object.getString("model"),
                                        object.getString("category"),
                                        object.getString("make"),
                                        object.getString("customer"),
                                        object.getBoolean("isPending")
                                    ));

                                    break;
                                }

                            } catch (JSONException je) {
                                Util.longToast(getContext(), "Parse error");
                                break;
                            }
                        }

                        if (!qcDataModels2.isEmpty()) {
                            qcDataModels.clear();
                            qcDataModels.addAll(qcDataModels2);
                            qcAdapter.notifyDataSetChanged();
                        } else {
                            Util.longToast(getContext(), joQuery+" not found. Try other pages.");
                        }
                    }
                });

                adBuilder.create().show();

                return true;
            }
        });

        MenuItem actionBackNav = menu.findItem(R.id.action_back_nav);
        actionBackNav.setVisible(false);
    }
}