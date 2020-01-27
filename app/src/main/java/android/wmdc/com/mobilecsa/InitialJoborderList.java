package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.InitialJoborderListAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetInitialJoborderListTask;
import android.wmdc.com.mobilecsa.model.InitialJoborderRowModel;
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

/**Created by wmdcprog on 6/4/2018.*/

public class InitialJoborderList extends Fragment {

    private ArrayList<InitialJoborderRowModel> joList = new ArrayList<>();
    private SharedPreferences sPrefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View v = inflater.inflate(R.layout.initial_joborder_list, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Initial Joborder List");
        } else {
            Util.shortToast(getContext(), "Activity is null. Cannot set title of this fragment.");
        }

        setHasOptionsMenu(true);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final RecyclerView quotationRecyclerView = v.findViewById(R.id.rvQuotation);
        quotationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String quotationResult = bundle.getString("initialJoResult");

            try {
                final JSONObject jsonObject = new JSONObject(quotationResult);
                final JSONArray jsonArray = jsonObject.getJSONArray("initialJoborderList");

                int jsonArrayLen = jsonArray.length();
                if (jsonArrayLen > 0) {
                    for (int i = 0; i < jsonArrayLen; ++i) {
                        JSONObject eachItem = jsonArray.getJSONObject(i);

                        joList.add(new InitialJoborderRowModel(
                                eachItem.getInt("initialJoborderId"),
                                eachItem.getString("customer"),
                                eachItem.getString("dateAdded"),
                                eachItem.getString("serialNo"),
                                eachItem.getString("model"),
                                eachItem.getString("make"),
                                eachItem.getInt("isAdded"),
                                eachItem.getString("joNumber")
                        ));
                    }

                    InitialJoborderListAdapter initJoListAdapter = new InitialJoborderListAdapter(
                            getActivity(), joList);
                    quotationRecyclerView.setAdapter(initJoListAdapter);
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
                Util.alertBox(getActivity(), je.getMessage());
            }
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
                new GetInitialJoborderListTask(getActivity()).execute(String.valueOf(
                        sPrefs.getInt("csaId", 0)));
                return true;
            }
        });
    }
}