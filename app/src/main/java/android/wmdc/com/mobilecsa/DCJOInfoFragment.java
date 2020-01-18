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

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wmdcprog on 3/12/2018.
 */

public class DCJOInfoFragment extends Fragment {

    private TextView tvSwipe;

    private DateCommitAdapter dateCommitAdapter;
    private ArrayList<DateCommitModel> dcData;
    private int dcJoborderId;

    private final ArrayList<String> DC_KEY = new ArrayList<>(Arrays.asList(
            "JO Number",
            "Customer",
            "Item Image",
            "Date Received",
            "Date Commit"
    ));

    private SharedPreferences sharedPreferences;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dc_jo_info_viewholder_layout, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tvSwipe = v.findViewById(R.id.tvSwipe);

        NavigationView navView = getActivity().findViewById(R.id.nav_view);

        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvDCInfo);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> dcInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject responseJson = new JSONObject(result);

                if (responseJson.getBoolean("success")) {
                    getActivity().setTitle(responseJson.getString("customer"));

                    dcInfos.add(new KeyValueInfo(DC_KEY.get(0), responseJson.getString("joNum")));
                    dcInfos.add(new KeyValueInfo(DC_KEY.get(1), responseJson.getString("customer")));
                    dcInfos.add(new KeyValueInfo(DC_KEY.get(2), "Tap to view item image"));
                    dcInfos.add(new KeyValueInfo(DC_KEY.get(3), responseJson.getString("dateReceive")));
                    dcInfos.add(new KeyValueInfo(DC_KEY.get(4), responseJson.getString("dateCommit")));

                    DCValueInfoAdapter dcValueInfoAdapter = new DCValueInfoAdapter(
                                    dcInfos,
                                    getActivity(),
                                    thisBundle.getInt("joId"),
                                    responseJson.getBoolean("csaApproved"));

                    recyclerView.setAdapter(dcValueInfoAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

                    //  cid, akey, source, joid
                    SwipeButton swipeButton = v.findViewById(R.id.swipe_btn);
                    swipeButton.setTVSwipe(tvSwipe);
                    swipeButton.setParameters(
                            sharedPreferences.getInt("csaId", 0), "mcsa", responseJson.getInt("joId"), 0,  //  <-- DC has no work_order_id
                            false);

                    swipeButton.setDateCommitAdapter(dateCommitAdapter);
                    swipeButton.setDateCommitList(dcData);
                    swipeButton.setDcJoborderId(dcJoborderId);

                    if (responseJson.getBoolean("csaApproved")) {
                        swipeButton.setVisibility(View.GONE);
                        tvSwipe.setVisibility(View.GONE);
                    }
                } else {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity());

                    builder.setMessage(responseJson.getString("reason"));
                    builder.setTitle("Error");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Util.handleBackPress(null, getContext());
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
                Util.alertBox(getActivity(), je.getMessage(), "JSON Exception", false);
            }
        }
        return v;
    }
}