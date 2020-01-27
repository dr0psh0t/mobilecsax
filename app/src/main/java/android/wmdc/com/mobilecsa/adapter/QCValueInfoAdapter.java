package android.wmdc.com.mobilecsa.adapter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageTask;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**Created by wmdcprog on 3/10/2018.*/

public class QCValueInfoAdapter extends RecyclerView.Adapter {

    private WeakReference<FragmentActivity> weakReference;
    private ArrayList<KeyValueInfo> customerInfo;
    private int joId;
    private SharedPreferences sharedPreferences;

    public QCValueInfoAdapter(FragmentActivity activity, ArrayList<KeyValueInfo> customerInfo,
                              int joId) {

        this.weakReference = new WeakReference<>(activity);
        this.customerInfo = customerInfo;
        this.joId = joId;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(weakReference.get()).inflate(R.layout.qc_jo_info_fragment,
                viewGroup, false);
        return new QCInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        QCInfoViewHolder qcInfoViewHolder = (QCInfoViewHolder) viewHolder;

        String key = customerInfo.get(i).getKey();

        switch (key) {
            case "JO Number":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_format_list_number);
                break;
            case "Customer ID":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_filter_1);
                break;
            case "Customer":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_domain);
                break;
            case "Model":
            case "Category":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_nav_build);
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_nav_build);
                break;
            case "Make":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_gavel);
                break;
            case "Serial":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_linear_scale);
                break;
            case "Date Received":
            case "Date Committed":
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_date);
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_date);
                break;
            case "View Image":
                qcInfoViewHolder.rootLinLay.setBackgroundResource(
                        R.drawable.custom_card_background_even);
                qcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_photo);
                break;
        }

        qcInfoViewHolder.tvKey.setText(key);
        qcInfoViewHolder.tvValue.setText(customerInfo.get(i).getValue());
    }

    @Override
    public int getItemCount() {
        return customerInfo.size();
    }

    private class QCInfoViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView tvKey;
        private TextView tvValue;
        private LinearLayout rootLinLay;

        private QCInfoViewHolder(View itemView) {
            super(itemView);

            this.icon = itemView.findViewById(R.id.iconQCInfo);
            this.tvKey = itemView.findViewById(R.id.tvKeyQCJO);
            this.tvValue = itemView.findViewById(R.id.tvValueQCJO);
            this.rootLinLay = itemView.findViewById(R.id.rootLinLayInd);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    if (tvKey.getText().toString().equals("View Image")) {
                        int csaId = sharedPreferences.getInt("csaId", 0);
                        String url = sharedPreferences.getString("domain", null)
                                +"getmcsajoimage?csaId="+csaId+"&joId="+joId;

                        new DialogImageTask(weakReference.get()).execute(url);
                    }
                }
            });
        }
    }
}
