package android.wmdc.com.mobilecsa;

import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.wmdc.com.mobilecsa.adapter.DateCommitAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetDCJOListTask;
import android.wmdc.com.mobilecsa.model.DateCommitModel;
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

/**Created by wmdcprog on 4/13/2018.*/

public class DateCommitFragment extends Fragment {

    private ArrayList<DateCommitModel> dcDataModels = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        final View v = inflater.inflate(R.layout.date_commit_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Date Commit");
        } else {
            Util.shortToast(getContext(), "Title error");
            Log.e("Null", "\"getActivity()\" is null. " +
                    "Cannot set title of this fragment.");
        }

        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        LinearLayout dcHeaderLinLay = v.findViewById(R.id.dcTitleLL);

        dcHeaderLinLay.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                if (getActivity() != null) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.dc_header_layout);

                    WindowManager.LayoutParams wmlp;

                    if (dialog.getWindow() != null) {
                        wmlp = dialog.getWindow().getAttributes();
                        wmlp.width = Util.systemWidth;
                        wmlp.gravity = Gravity.TOP;
                        wmlp.y = 200;

                        dialog.show();
                    } else {
                        Util.alertBox(getActivity(), "Cannot open dialog.");
                        Log.e("Null", "dialog.getWindow() is null. Cannot open dialog.");
                    }
                } else {
                    Util.alertBox(getActivity(), "Cannot open dialog.");
                    Log.e("Null", "getActivity() is null. Cannot open dialog.");
                }
            }
        });

        final RecyclerView recyclerView = v.findViewById(R.id.rvDCData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!Variables.dcRawResult.isEmpty()) {

            //  begin replaced
            JSONObject jsonObject = null;
            JSONArray jsonArray;

            try {
                jsonObject = new JSONObject(Variables.dcRawResult);
                Variables.qcStore = jsonObject;

            } catch (JSONException je) {
                Util.alertBox(getActivity(), "Invalid DC data received. " +
                        "The server might be loading. Try again later.");
            }

            if (jsonObject != null) {
                try {
                    jsonArray = jsonObject.getJSONArray("joborders");

                    int loopLen = Math.min(jsonArray.length(), 50);

                    for (int i = 0; i < loopLen; ++i) {
                        JSONObject itemObj = jsonArray.getJSONObject(i);

                        dcDataModels.add(
                                new DateCommitModel(
                                        itemObj.getInt("joId"),
                                        itemObj.getString("joNum"),
                                        itemObj.getString("customerId"),
                                        itemObj.getString("customer"),
                                        itemObj.getBoolean("isCsaApproved"),
                                        itemObj.getBoolean("isPnmApproved"),
                                        itemObj.getString("dateCommit"),
                                        itemObj.getString("dateReceived"))
                        );
                    }

                    recyclerView.setAdapter(new DateCommitAdapter(dcDataModels, getActivity()));

                } catch (JSONException je) {
                    Util.alertBox(getActivity(), "Cannot build the list. " +
                            "The server might be loading. Try again later.");
                }
            }

            /*
            try {
                JSONObject jsonObject = new JSONObject(Variables.dcRawResult);
                JSONArray jsonArray = jsonObject.getJSONArray("joborders");

                int loopLen = Math.min(jsonArray.length(), 50);
                //int loopLen = jsonArray.length() - 1;

                for (int i = 0; i < loopLen; ++i) {
                    JSONObject itemObj = jsonArray.getJSONObject(i);

                    dcDataModels.add(
                            new DateCommitModel(
                                    itemObj.getInt("joId"),
                                    itemObj.getString("joNum"),
                                    itemObj.getString("customerId"),
                                    itemObj.getString("customer"),
                                    itemObj.getBoolean("isCsaApproved"),
                                    itemObj.getBoolean("isPnmApproved"),
                                    itemObj.getString("dateCommit"),
                                    itemObj.getString("dateReceived"))
                    );
                }

                recyclerView.setAdapter(new DateCommitAdapter(dcDataModels, getActivity()));

            } catch (JSONException je) {
                Util.alertBox(getContext(), je.getMessage());

                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }*/
        }

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem refreshAction = menu.findItem(R.id.action_refresh);
        refreshAction.setVisible(true);

        refreshAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GetDCJOListTask(getActivity(), null, true).execute(
                        String.valueOf(sharedPreferences.getInt("csaId", 0)), "mcsa");
                return true;
            }
        });

        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}