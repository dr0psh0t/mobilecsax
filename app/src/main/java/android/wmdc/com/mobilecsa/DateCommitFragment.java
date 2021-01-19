package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Collections;

/**Created by wmdcprog on 4/13/2018.*/

public class DateCommitFragment extends Fragment {

    ArrayList<DateCommitModel> dcDataModels = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        final View v = inflater.inflate(R.layout.date_commit_fragment, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setHasOptionsMenu(true);

        if (getActivity() != null) {
            getActivity().setTitle("Date Commit");
        } else {
            Util.shortToast(getContext(), "Title error");
            Log.e("Null", "\"getActivity()\" is null. Cannot set title of this fragment.");
        }

        if (!Variables.dcRawResult.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(Variables.dcRawResult);

                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("joborders");

                    int loopLen = Math.min(jsonArray.length(), 100);
                    dcDataModels.clear();

                    for (int i = 0; i <= loopLen; ++i) {
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

                    Collections.sort(dcDataModels);

                    RecyclerView recyclerView = v.findViewById(R.id.rvDCData);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(new DateCommitAdapter(dcDataModels, getActivity()));

                } catch (JSONException je) {
                    Util.alertBox(getActivity(), "Cannot build the list. " +
                            "The server might be loading. Try again later.");
                }

            } catch (JSONException je) {
                Util.alertBox(getActivity(), "Invalid DC data received. " +
                        "The server might be loading. Try again later.");
            }
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }
}