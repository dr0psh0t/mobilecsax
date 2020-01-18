package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 8/31/2018.
 */

public class JOStatusDetailsAdapter extends
        RecyclerView.Adapter<JOStatusDetailsAdapter.DetailsViewHolder> {

    private ArrayList<KeyValueInfo> joborderStatusDetails;
    private Context context;

    public JOStatusDetailsAdapter(ArrayList<KeyValueInfo> joborderStatusDetails, Context context) {
        this.joborderStatusDetails = joborderStatusDetails;
        this.context = context;
    }

    @Override
    public DetailsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.jo_search_info_layout,
                viewGroup, false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DetailsViewHolder detailsViewHolder, int i) {

        detailsViewHolder.joIcon.setImageResource(R.drawable.ic_action_photo);
        detailsViewHolder.tvKey.setText(joborderStatusDetails.get(i).getKey());
        detailsViewHolder.tvValue.setText(joborderStatusDetails.get(i).getValue());

        String key = joborderStatusDetails.get(i).getKey();

        switch (key) {
            case "JO Number":
                detailsViewHolder.joIcon.setImageResource(R.drawable.ic_action_format_list_number);
                break;
            case "Customer":
                detailsViewHolder.joIcon.setImageResource(R.drawable.ic_action_domain);
                break;
            case "Date Target":
            case "Date Commit":
                detailsViewHolder.joIcon.setImageResource(R.drawable.ic_action_date);
                break;
            default:
                detailsViewHolder.joIcon.setImageResource(R.drawable.ic_action_check);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return joborderStatusDetails.size();
    }

    class DetailsViewHolder extends RecyclerView.ViewHolder {
        private ImageView joIcon;
        private TextView tvKey;
        private TextView tvValue;

        public DetailsViewHolder(View itemView) {
            super(itemView);

            joIcon = itemView.findViewById(R.id.joIcon);
            tvKey = itemView.findViewById(R.id.tvKey);
            tvValue = itemView.findViewById(R.id.tvValue);
        }
    }
}