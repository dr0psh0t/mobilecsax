package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageTask;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 4/19/2018.
 */

public class JOSearchResultInfoAdapter extends
        RecyclerView.Adapter<JOSearchResultInfoAdapter.JOSearchResViewHolder> {

    private Context context;
    private ArrayList<KeyValueInfo> joInfos;
    private int initialJoborderId;
    private SharedPreferences sharedPreferences;

    public JOSearchResultInfoAdapter(Context context, ArrayList<KeyValueInfo> joInfos,
                                     int initialJoborderId) {
        this.context = context;
        this.joInfos = joInfos;
        this.initialJoborderId = initialJoborderId;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    @NonNull
    public JOSearchResViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.jo_search_info_layout, viewGroup,
                false);
        return new JOSearchResViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JOSearchResViewHolder viewHolder, int i) {
        viewHolder.index = i;
        viewHolder.tvKey.setText(joInfos.get(i).getKey());
        viewHolder.tvValue.setText(joInfos.get(i).getValue());

        String key = joInfos.get(i).getKey();

        switch (key) {
            case "JO #":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_format_list_number);
                break;
            case "Customer":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_domain);
                break;
            case "Date Created":
            case "Date Started":
            case "Date Target":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_date);
                break;
            case "CSA":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_person);
                break;
            case "Model":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_nav_build);
                break;
            case "Make":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_gavel);
                break;
            case "Serial":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_linear_scale);
                break;
            case "Image":
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_photo);
                viewHolder.joSearchInfoConLay.setBackgroundResource(R.drawable.card_view_border);
                break;
            default:
                viewHolder.joIcon.setImageResource(R.drawable.ic_action_check);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return joInfos.size();
    }

    class JOSearchResViewHolder extends RecyclerView.ViewHolder {
        private TextView tvKey;
        private TextView tvValue;
        private ImageView joIcon;
        private ConstraintLayout joSearchInfoConLay;

        private int index;

        private JOSearchResViewHolder(View itemView) {
            super(itemView);

            this.tvKey = itemView.findViewById(R.id.tvKey);
            this.tvValue = itemView.findViewById(R.id.tvValue);
            this.joIcon = itemView.findViewById(R.id.joIcon);
            this.joSearchInfoConLay = itemView.findViewById(R.id.joSearchInfoConLay);

            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    if (joInfos.get(index).getKey().equals("Image")) {

                        String url = sharedPreferences.getString("domain", null)+
                                "getinitialjoborderphoto?initialJoborderId="+initialJoborderId;

                        new DialogImageTask(context).execute(url);
                    }
                }
            });
        }
    }
}
