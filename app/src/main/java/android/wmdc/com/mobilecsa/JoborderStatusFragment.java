package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.JOStatusDetailsAdapter;
import android.wmdc.com.mobilecsa.adapter.WorkorderStatusAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.WorkOrderStatus;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 8/31/2018.
 */

public class JoborderStatusFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.joborder_status_container_layout, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(getContext(), 1);

        ArrayList<KeyValueInfo> joborderDetails = new ArrayList<>();
        ArrayList<WorkOrderStatus> workOrderStatuses = new ArrayList<>();

        joborderDetails.add(new KeyValueInfo("JO Number", "88888"));
        joborderDetails.add(new KeyValueInfo("Customer", "Test Customer Corporation"));
        joborderDetails.add(new KeyValueInfo("Date Target", "2018-8-31"));
        joborderDetails.add(new KeyValueInfo("Date Commit", "2018-8-31"));

        RecyclerView recyclerJoDetails = v.findViewById(R.id.recyclerViewJoborderDetails);
        recyclerJoDetails.setLayoutManager(gridLayoutManager);
        recyclerJoDetails.setAdapter(new JOStatusDetailsAdapter(joborderDetails, getContext()));
        recyclerJoDetails.setItemAnimator(new DefaultItemAnimator());

        if (getActivity() != null) {
            recyclerJoDetails.addItemDecoration(new DividerItemDecoration(getActivity(),
                    LinearLayoutManager.VERTICAL));
        } else {
            Util.alertBox(getActivity(), "Activity is null. Cannot set divider for recycler view.");
        }

        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 1", false));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", false));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", false));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", false));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));
        workOrderStatuses.add(new WorkOrderStatus("Sample Work Item 2", true));

        RecyclerView recyclerWo = v.findViewById(R.id.recyclerViewWorkorder);
        recyclerWo.setLayoutManager(gridLayoutManager2);
        recyclerWo.setAdapter(new WorkorderStatusAdapter(workOrderStatuses, getActivity()));
        recyclerWo.setItemAnimator(new DefaultItemAnimator());

        return v;
    }
}