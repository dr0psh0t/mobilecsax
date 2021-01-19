package android.wmdc.com.mobilecsa.adapter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.QCJOInfoFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.WorkOrderModel;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**Created by wmdcprog on 5/7/2018.*/

public class WorkOrderAdapter extends RecyclerView.Adapter<WorkOrderAdapter.WorkOrderViewHolder> {

    private final FragmentActivity activity;
    private final ArrayList<WorkOrderModel> workOrderList;
    private final QCJOInfoFragment fragment;
    private final SharedPreferences sPrefs;

    public WorkOrderAdapter(FragmentActivity activity, ArrayList<WorkOrderModel> workOrderList,
                            QCJOInfoFragment fragment) {
        this.activity = activity;
        this.workOrderList = workOrderList;
        this.fragment = fragment;
        this.sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    @NonNull
    public WorkOrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.work_order_info_fragment,
                viewGroup, false);
        return new WorkOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkOrderViewHolder workOrderViewHolder, int i) {
        workOrderViewHolder.index = i;

        if (i % 2 != 0) {
            workOrderViewHolder.rootLayItemInfo.setBackgroundResource(
                    R.drawable.custom_card_background_odd);
        } else {
            workOrderViewHolder.rootLayItemInfo.setBackgroundResource(
                    R.drawable.custom_card_background_even);
        }

        workOrderViewHolder.tvItemDC.setText(workOrderList.get(i).getScopeWork());

        if (workOrderList.get(i).getIsCompleted() == 1) {
            workOrderViewHolder.doneIcon.setImageResource(R.drawable.ic_action_check_colored_round);
        } else {
            workOrderViewHolder.doneIcon.setImageResource(R.drawable.ic_action_slide_disabled);
        }

        if (workOrderList.get(i).getIsCsaQc()) {
            workOrderViewHolder.iconItemDC.setImageResource(
                    R.drawable.ic_action_check_colored_round);
        } else {
            workOrderViewHolder.iconItemDC.setImageResource(R.drawable.ic_action_x_colored_round);
        }

        if (workOrderList.get(i).getIsSupervisorId()) {
            workOrderViewHolder.ivWorkPM.setImageResource(R.drawable.ic_action_check_colored_round);
        }
        else {
            workOrderViewHolder.ivWorkPM.setImageResource(R.drawable.ic_action_x_colored_round);
        }
    }

    public int getItemCount() {
        return workOrderList.size();
    }

    class WorkOrderViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rootLayItemInfo;
        private final ImageView doneIcon;
        private final ImageView iconItemDC;
        private final ImageView ivWorkPM;
        private final TextView tvItemDC;
        private int index;

        //private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //OkHttpClient client = new OkHttpClient();

        private WorkOrderViewHolder(View itemView) {
            super(itemView);
            this.rootLayItemInfo = itemView.findViewById(R.id.rootLayItemInfo);
            this.doneIcon = itemView.findViewById(R.id.doneIcon);
            this.iconItemDC = itemView.findViewById(R.id.iconItemDC);
            this.tvItemDC = itemView.findViewById(R.id.tvItemDC);
            this.ivWorkPM = itemView.findViewById(R.id.ivWorkPM);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {

                    boolean isCsaQc = workOrderList.get(index).getIsCsaQc();
                    int isCompleted = workOrderList.get(index).getIsCompleted();
                    int csaId = sPrefs.getInt("csaId", 0);
                    int joId = workOrderList.get(index).getJoId();
                    int workOrderId = workOrderList.get(index).getWorkOrderId();

                    if (isCompleted == 1) {   //  if its done
                        if (!isCsaQc) {
                            fragment.dispatchTakePictureIntent(iconItemDC, csaId, joId,
                                    workOrderId, isCsaQc, workOrderList.get(index));
                        }
                    }
                }
            });
        }
    }
}
