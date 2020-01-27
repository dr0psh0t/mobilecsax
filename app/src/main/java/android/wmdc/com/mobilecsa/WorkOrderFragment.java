package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.wmdc.com.mobilecsa.adapter.WorkOrdersAdapter;
import android.wmdc.com.mobilecsa.model.WorkOrders;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 9/14/2018.
 */

public class WorkOrderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {

        View v = inflater.inflate(R.layout.workorder_fragment, container, false);
        LinearLayout linearLayoutTitle = v.findViewById(R.id.linearLayoutTitle);
        Bundle bundle = this.getArguments();

        FloatingActionButton fabOptions = v.findViewById(R.id.fabOptions);
        FloatingActionButton fabExtension = v.findViewById(R.id.fabExtension);

        if (bundle != null) {
            try {
                ArrayList<WorkOrders> workOrders = new ArrayList<>();
                JSONObject searchResult = new JSONObject(bundle.getString("searchResult"));
                JSONArray workordersArray = searchResult.getJSONArray("workOrders");

                int arrayLength = workordersArray.length();
                for (int i = 0; i < arrayLength; ++i) {
                    JSONObject itemObject = workordersArray.getJSONObject(i);

                    workOrders.add(new WorkOrders(
                            itemObject.getInt("ctr"),
                            itemObject.getString("scope_of_work"),
                            itemObject.getBoolean("isQcCsa"),
                            itemObject.getBoolean("isQcSup"),
                            itemObject.getString("status")
                    ));

                    WorkOrdersAdapter workOrdersAdapter = new WorkOrdersAdapter(getActivity(),
                            workOrders, fabOptions, fabExtension, linearLayoutTitle);
                    RecyclerView recyclerViewWorkOrders =
                            v.findViewById(R.id.recyclerViewWorkOrders);
                    recyclerViewWorkOrders.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerViewWorkOrders.setAdapter(workOrdersAdapter);
                }
            } catch (JSONException je) {
                Util.alertBox(getContext(), je.getMessage());

                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        } else {
            Util.alertBox(getContext(), "Bundle containing data is null.");
        }

        return v;
    }
}