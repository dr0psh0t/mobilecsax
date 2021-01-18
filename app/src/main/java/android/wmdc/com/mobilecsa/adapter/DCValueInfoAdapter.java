package android.wmdc.com.mobilecsa.adapter;

import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 3/12/2018.
 */

public class DCValueInfoAdapter extends RecyclerView.Adapter<DCValueInfoAdapter.DCInfoViewHolder> {

    private WeakReference<FragmentActivity> weakReference;
    private ArrayList<KeyValueInfo> customerInfo;
    private SharedPreferences sPrefs;
    private int joId;
    private boolean csaApproved;

    public DCValueInfoAdapter(ArrayList<KeyValueInfo> customerInfo, FragmentActivity activity,
                              int joId, boolean csaApproved) {

        this.weakReference = new WeakReference<>(activity);
        this.customerInfo = customerInfo;
        this.sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        this.joId = joId;
        this.csaApproved = csaApproved;
    }

    @NonNull
    @Override
    public DCInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(weakReference.get()).inflate(R.layout.dc_jo_info_fragment,
                viewGroup, false);
        return new DCInfoViewHolder(view);
    }

    private static DCInfoViewHolder dcInfoViewHolder = null;

    @Override
    public void onBindViewHolder(@NonNull DCInfoViewHolder viewHolder, int i) {
        dcInfoViewHolder = viewHolder;
        String key = customerInfo.get(i).getKey();

        switch (key) {
            case "JO Number":
                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_format_list_number);
                break;
            case "Customer":

                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_domain);

                LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                p4.weight = 0;
                dcInfoViewHolder.dcIcon.setLayoutParams(p4);

                LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                p3.weight = 1.2f;
                dcInfoViewHolder.tvValue.setLayoutParams(p3);

                break;
            case "Item Image":

                /*
                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_photo);
                dcInfoViewHolder.tvValue.setTextColor(Color.parseColor("#ffffff"));
                dcInfoViewHolder.tvValue.setBackgroundResource(R.drawable.item_image_bg);
                dcInfoViewHolder.tvValue.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                p.weight = 0;
                dcInfoViewHolder.dcIcon.setLayoutParams(p);

                LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                p2.weight = 1.2f;
                dcInfoViewHolder.tvValue.setLayoutParams(p2);
                */

                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_photo);
                dcInfoViewHolder.rootLinLay.setBackgroundResource(R.drawable.custom_card_background_even);

                break;
            case "Date Received":
                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_date);
                dcInfoViewHolder.tvValue.setTextColor(Color.parseColor("#006400"));
                break;
            case "Date Commit":
                dcInfoViewHolder.icon.setImageResource(R.drawable.ic_action_date);
                dcInfoViewHolder.tvValue.setTextColor(Color.parseColor("#006400"));

                if (csaApproved) {
                    dcInfoViewHolder.dcIcon.setImageResource(R.drawable.ic_action_check_colored_round);
                }
                else {
                    dcInfoViewHolder.dcIcon.setImageResource(R.drawable.ic_action_x_colored_round);
                }

                break;
        }

        dcInfoViewHolder.tvKey.setText(key);
        dcInfoViewHolder.tvValue.setText(customerInfo.get(i).getValue());
    }

    public static void setIconOnApprovedDC() {
        dcInfoViewHolder.dcIcon.setImageResource(R.drawable.ic_action_check_colored_round);
    }

    @Override
    public int getItemCount() {
        return customerInfo.size();
    }

    class DCInfoViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView tvKey;
        private TextView tvValue;
        private ImageView dcIcon;
        private LinearLayout rootLinLay;

        private DCInfoViewHolder(View itemView) {
            super(itemView);

            this.icon = itemView.findViewById(R.id.iconDCInfo);
            this.tvKey = itemView.findViewById(R.id.tvKeyDCJO);
            this.tvValue = itemView.findViewById(R.id.tvValueDCJO);
            this.dcIcon = itemView.findViewById(R.id.iconDC);
            this.rootLinLay = itemView.findViewById(R.id.rootLinLay);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    int csaId = sPrefs.getInt("csaId", 0);
                    String key = tvKey.getText().toString();

                    if (key.equals("Item Image")) {
                        String url = sPrefs.getString("domain", null)+
                                "getmcsajoimage?csaId="+csaId+"&joId="+joId;

                        new DialogImageTask(weakReference.get()).execute(url);
                    }
                }
            });
        }
    }
}