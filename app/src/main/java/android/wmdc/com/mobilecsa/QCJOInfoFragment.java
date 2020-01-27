package android.wmdc.com.mobilecsa;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.QCValueInfoAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetWorkOrderQCList;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.WorkOrderModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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
 * Created by wmdcprog on 3/10/2018.
 */

public class QCJOInfoFragment extends Fragment {

    private final ArrayList<String> QC_KEY = new ArrayList<>(Arrays.asList("JO Number",
            "Customer ID", "Model", "Make", "Category", "Serial", "Date Received", "Date Committed",
            "View Image"));

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setLogo(R.drawable.ic_action_white_domain);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "FRAGMENT IS DESTROYED");
    }

    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.qc_jo_info_viewholder_layout, container, false);

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (getActivity() != null) {
            toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setLogo(R.drawable.ic_action_white_domain);
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot inflate toolbar.");
        }

        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvQCInfo);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> qcInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject resJson = new JSONObject(result);

                if (resJson.getBoolean("success")) {
                    getActivity().setTitle("  "+resJson.getString("customer"));

                    qcInfos.add(new KeyValueInfo(QC_KEY.get(0), resJson.getString("joNumber")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(1), resJson.getString("customerID")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(2), resJson.getString("model")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(3), resJson.getString("make")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(4), resJson.getString("category")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(5), resJson.getString("serial")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(6), resJson.getString("dateReceived")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(7), resJson.getString("dateCommit")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(8), "Tap to view Image"));

                    QCValueInfoAdapter qcValueInfoAdapter = new QCValueInfoAdapter(getActivity(),
                            qcInfos, thisBundle.getInt("joid"));

                    recyclerView.setAdapter(qcValueInfoAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                            LinearLayoutManager.VERTICAL));
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
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());

                Util.alertBox(getActivity(), je.getMessage());
            }

            //  fill work order list
            RecyclerView workOrderRecyclerView = v.findViewById(R.id.rvItemsList);
            workOrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            ArrayList<WorkOrderModel> workOrderList = new ArrayList<>();

            new GetWorkOrderQCList(getActivity(), workOrderRecyclerView, workOrderList).execute(
                    String.valueOf(sPrefs.getInt("csaId", 0)), "mcsa",
                    String.valueOf(thisBundle.getInt("joid")));
        }

        return v;
    }
}