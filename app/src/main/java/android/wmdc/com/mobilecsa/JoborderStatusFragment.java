package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.JOStatusDetailsAdapter;
import android.wmdc.com.mobilecsa.adapter.WorkorderStatusAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.WorkOrderStatus;

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

    private RecyclerView recyclerViewJoborderDetails;
    private RecyclerView recyclerViewWorkorder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.joborder_status_container_layout, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(getContext(), 1);

        ArrayList<KeyValueInfo> joborderDetails = new ArrayList<>();
        ArrayList<WorkOrderStatus> workOrderStatuses = new ArrayList<>();

        joborderDetails.add(new KeyValueInfo("JO Number", "88888"));
        joborderDetails.add(new KeyValueInfo("Customer", "Test Customer Corporation"));
        joborderDetails.add(new KeyValueInfo("Date Target", "2018-8-31"));
        joborderDetails.add(new KeyValueInfo("Date Commit", "2018-8-31"));

        recyclerViewJoborderDetails = v.findViewById(R.id.recyclerViewJoborderDetails);
        recyclerViewJoborderDetails.setLayoutManager(gridLayoutManager);
        recyclerViewJoborderDetails.setAdapter(new JOStatusDetailsAdapter(joborderDetails, getContext()));
        recyclerViewJoborderDetails.setItemAnimator(new DefaultItemAnimator());
        recyclerViewJoborderDetails.addItemDecoration(
                new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

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

        recyclerViewWorkorder = v.findViewById(R.id.recyclerViewWorkorder);
        recyclerViewWorkorder.setLayoutManager(gridLayoutManager2);
        recyclerViewWorkorder.setAdapter(new WorkorderStatusAdapter(workOrderStatuses, getActivity()));
        recyclerViewWorkorder.setItemAnimator(new DefaultItemAnimator());

        return v;
    }
}