package android.wmdc.com.mobilecsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetJoWoStatusListTask;
import android.wmdc.com.mobilecsa.model.JobOrder;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 4/6/2018.
 */

public class JOResultAdapter extends RecyclerView.Adapter<JOResultAdapter.JOViewHolder> {

    private WeakReference<FragmentActivity> weakReference;
    private ArrayList<JobOrder> jobOrders;

    public JOResultAdapter(FragmentActivity activity, ArrayList<JobOrder> jobOrders) {
        this.weakReference = new WeakReference<>(activity);
        this.jobOrders = jobOrders;
    }

    @Override
    @NonNull
    public JOViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(weakReference.get()).inflate(
                R.layout.jo_search_result_row_item, viewGroup, false);
        return new JOViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JOViewHolder joViewHolder, int i) {
        if (i % 2 != 0) {
            joViewHolder.rowItemJOLinLay.setBackgroundResource(
                    R.drawable.custom_card_background_odd);
        } else {
            joViewHolder.rowItemJOLinLay.setBackgroundResource(
                    R.drawable.custom_card_background_even);
        }

        joViewHolder.tvJONumber.setText(jobOrders.get(i).getJoNumber());
        joViewHolder.tvCustomer.setText(jobOrders.get(i).getCustomer());
        joViewHolder.tvSerial.setText(jobOrders.get(i).getSerialNum());
        joViewHolder.index = i;
    }

    @Override
    public int getItemCount() {
        return jobOrders.size();
    }

    class JOViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJONumber;
        private TextView tvCustomer;
        private TextView tvSerial;
        private LinearLayout rowItemJOLinLay;
        private int index;

        private JOViewHolder(View itemView) {
            super(itemView);
            this.tvJONumber = itemView.findViewById(R.id.tvJONumber);
            this.tvCustomer = itemView.findViewById(R.id.tvCustomer);
            this.tvSerial = itemView.findViewById(R.id.tvSerial);
            this.rowItemJOLinLay = itemView.findViewById(R.id.rowItemJOLinLay);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    new GetJoWoStatusListTask(weakReference.get()).execute(
                            String.valueOf(jobOrders.get(index).getJoId()));
                }
            });
        }
    }
}