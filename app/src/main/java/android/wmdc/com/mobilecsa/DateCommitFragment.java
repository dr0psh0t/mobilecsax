package android.wmdc.com.mobilecsa;

import android.app.Dialog;
import android.content.SharedPreferences;
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
    private DateCommitAdapter dcAdapter;
    private SharedPreferences sharedPreferences;
    private LinearLayout dcHeaderLinLay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.date_commit_fragment, container, false);
        getActivity().setTitle("Date Commit");
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        dcAdapter = new DateCommitAdapter(dcDataModels, getActivity());

        dcHeaderLinLay = v.findViewById(R.id.dcTitleLL);
        dcHeaderLinLay.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dc_header_layout);

                WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                wmlp.width = Util.systemWidth;
                wmlp.gravity = Gravity.TOP;
                wmlp.y = 200;

                dialog.show();
            }
        });

        final RecyclerView recyclerView = v.findViewById(R.id.rvDCData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String searchResult = bundle.getString("searchResult");

            try {
                JSONObject jsonObject = new JSONObject(searchResult);
                JSONArray jsonArray = jsonObject.getJSONArray("joborders");

                for (int i = 0; i < jsonArray.length(); ++i) {
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

                dcAdapter = new DateCommitAdapter(dcDataModels, getContext());
                recyclerView.setAdapter(dcAdapter);
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
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
                new GetDCJOListTask(getActivity(), null, true).execute(
                        String.valueOf(sharedPreferences.getInt("csaId", 0)), "mcsa");
                return true;
            }
        });

        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}