package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.WorkOrderStatus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**Created by wmdcprog on 8/31/2018.*/

public class WorkorderStatusAdapter extends
        RecyclerView.Adapter<WorkorderStatusAdapter.WorkorderStatusViewHolder> {

    private final ArrayList<WorkOrderStatus> workorderStatusList;
    private final Context context;

    public WorkorderStatusAdapter(ArrayList<WorkOrderStatus> workorderStatusList, Context context) {
        this.workorderStatusList = workorderStatusList;
        this.context = context;
    }

    @Override
    @NonNull
    public WorkorderStatusViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.work_order_status_row,
                viewGroup, false);
        return new WorkorderStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkorderStatusViewHolder workorderStatusViewHolder,
                                 int i) {
        if (i % 2 != 0) {
            workorderStatusViewHolder.workorderStatusLinLay
                    .setBackgroundResource(R.drawable.custom_card_background_odd);
        } else {
            workorderStatusViewHolder.workorderStatusLinLay
                    .setBackgroundResource(R.drawable.custom_card_background_even);
        }

        workorderStatusViewHolder.tvWorkItem.setText(workorderStatusList.get(i).getWorkItem());

        if (workorderStatusList.get(i).getIsCompleted()) {
            workorderStatusViewHolder.ivIsCompletedIcon.setImageResource(R.drawable.ic_action_check_colored_round);
        } else {
            workorderStatusViewHolder.ivIsCompletedIcon.setImageResource(R.drawable.ic_action_x_colored_round);
        }
    }

    @Override
    public int getItemCount() {
        return workorderStatusList.size();
    }

    static class WorkorderStatusViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout workorderStatusLinLay;
        private final TextView tvWorkItem;
        private final ImageView ivIsCompletedIcon;

        private WorkorderStatusViewHolder(View itemView) {

            super(itemView);
            tvWorkItem = itemView.findViewById(R.id.tvWorkItem);
            ivIsCompletedIcon = itemView.findViewById(R.id.ivIsCompletedIcon);
            workorderStatusLinLay = itemView.findViewById(R.id.workorderStatusLinLay);
        }
    }
}