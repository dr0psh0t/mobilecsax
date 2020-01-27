package android.wmdc.com.mobilecsa;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.DCValueInfoAdapter;
import android.wmdc.com.mobilecsa.adapter.DateCommitAdapter;
import android.wmdc.com.mobilecsa.model.DateCommitModel;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.SwipeButton;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wmdcprog on 3/12/2018.
 */

public class DCJOInfoFragment extends Fragment {

    private DateCommitAdapter dateCommitAdapter;
    private ArrayList<DateCommitModel> dcData;
    private int dcJoborderId;

    private final ArrayList<String> DC_KEY = new ArrayList<>(Arrays.asList("JO Number", "Customer",
            "Item Image", "Date Received", "Date Commit"));

    public void setDateCommitAdapter(DateCommitAdapter dateCommitAdapter) {
        this.dateCommitAdapter = dateCommitAdapter;
    }

    public void setDateCommitList(ArrayList<DateCommitModel> dcData) {
        this.dcData = dcData;
    }

    public void setDcJoborderId(int dcJoborderId) {
        this.dcJoborderId = dcJoborderId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "FRAGMENT IS DESTROYED");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.dc_jo_info_viewholder_layout, container, false);

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        TextView tvSwipe = v.findViewById(R.id.tvSwipe);

        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvDCInfo);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> dcInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject resJson = new JSONObject(result);

                if (getActivity() != null) {
                    if (resJson.getBoolean("success")) {
                        getActivity().setTitle(resJson.getString("customer"));

                        dcInfos.add(new KeyValueInfo(DC_KEY.get(0), resJson.getString("joNum")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(1), resJson.getString("customer")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(2), "Tap to view item image"));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(3), resJson.getString("dateReceive")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(4), resJson.getString("dateCommit")));

                        DCValueInfoAdapter dcValueInfoAdapter = new DCValueInfoAdapter(dcInfos,
                                getActivity(), thisBundle.getInt("joId"),
                                resJson.getBoolean("csaApproved"));

                        recyclerView.setAdapter(dcValueInfoAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                LinearLayoutManager.VERTICAL));

                        SwipeButton swipeButton = v.findViewById(R.id.swipe_btn);
                        swipeButton.setTVSwipe(tvSwipe);
                        swipeButton.setParameters(sPrefs.getInt("csaId", 0), "mcsa",
                                resJson.getInt("joId"), 0, false);

                        swipeButton.setDateCommitAdapter(dateCommitAdapter);
                        swipeButton.setDateCommitList(dcData);
                        swipeButton.setDcJoborderId(dcJoborderId);

                        if (resJson.getBoolean("csaApproved")) {
                            swipeButton.setVisibility(View.GONE);
                            tvSwipe.setVisibility(View.GONE);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(resJson.getString("reason"));
                        builder.setTitle("Error");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getActivity() != null) {
                                    Util.handleBackPress(null, getActivity());
                                } else {
                                    Util.longToast(getContext(), "Activity is null.");
                                }
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else {
                    Util.alertBox(getContext(),
                            "Activity is null. Cannot adapter, recycler view and swipe button");
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
                Util.alertBox(getActivity(), je.getMessage());
            }
        }
        return v;
    }
}