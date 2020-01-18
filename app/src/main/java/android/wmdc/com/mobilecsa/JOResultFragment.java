package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.JOResultAdapter;
import android.wmdc.com.mobilecsa.model.JobOrder;
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

public class JOResultFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.jo_search_result_container, container, false);

        Util.minKey(getActivity());
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            try {
                ArrayList<JobOrder> jobOrders = new ArrayList<>();
                JSONObject searchResult = new JSONObject(bundle.getString("searchResult"));
                JSONArray joborderArray = searchResult.getJSONArray("initialJos");

                getActivity().setTitle(joborderArray.getJSONObject(0).getString("customer"));

                int arrayLength = joborderArray.length();
                for (int i = 0; i < arrayLength; ++i) {
                    JSONObject itemObject = joborderArray.getJSONObject(i);

                    jobOrders.add(new JobOrder(
                            itemObject.getString("joNumber"),
                            itemObject.getInt("joId"),
                            itemObject.getString("serialNum"),
                            itemObject.getString("dateCommit"),
                            itemObject.getString("dateReceived"),
                            itemObject.getString("customerId"),
                            itemObject.getString("model"),
                            itemObject.getString("make"),
                            itemObject.getString("customer")
                    ));
                }

                JOResultAdapter joResAdapter = new JOResultAdapter(getContext(), jobOrders);
                RecyclerView recyclerViewForJO = v.findViewById(R.id.recyclerViewSearchJO);
                recyclerViewForJO.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerViewForJO.setAdapter(joResAdapter);

            } catch (JSONException je) {
                Util.alertBox(getContext(), "JSON Error occured", "Joborder", false);
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        }

        return v;
    }
}