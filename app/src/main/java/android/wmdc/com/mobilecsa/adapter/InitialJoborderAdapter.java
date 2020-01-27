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
 * Created by wmdcprog on 6/7/2018.
 */

public class InitialJoborderAdapter extends
        RecyclerView.Adapter<InitialJoborderAdapter.InitialJoborderViewHolder> {

    private Context context;
    private ArrayList<KeyValueInfo> initialJoborderList;
    private SharedPreferences sharedPreferences;
    private int quotationId;

    public InitialJoborderAdapter(Context context, ArrayList<KeyValueInfo> initialJoborderList,
                                  int quotationId) {

        this.context = context;
        this.initialJoborderList = initialJoborderList;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.quotationId = quotationId;
    }

    @NonNull
    public InitialJoborderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.initial_joborder_row_item,
                viewGroup, false);

        return new InitialJoborderViewHolder(view);
    }

    public void onBindViewHolder(InitialJoborderViewHolder initialJoborderViewHolder, int i) {
        initialJoborderViewHolder.index = i;

        String key = initialJoborderList.get(i).getKey();

        switch (key) {
            case "Status":
                String status = initialJoborderList.get(i).getValue();

                switch (status) {
                    case "Pending":
                        initialJoborderViewHolder.ivStatusInitialJO.setImageResource(
                                R.drawable.ic_action_pending_blue);
                        break;
                    case "Accepted":
                        initialJoborderViewHolder.ivStatusInitialJO.setImageResource(
                                R.drawable.ic_action_check_colored_round);
                        break;
                    case "Declined":
                        initialJoborderViewHolder.ivStatusInitialJO.setImageResource(
                                R.drawable.ic_action_x_colored_round);
                }

                break;
            case "Customer":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_person);
                break;
            case "Mobile":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_phone_android);
                break;
            case "Purchase Order":
            case "Reference No":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_format_list_number);
                break;
            case "Date Received":
            case "Date Committed":
            case "PO Date":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_date);
                break;
            case "Engine Model":
            case "Engine Category":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_nav_build);
                break;
            case "Engine Make":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_gavel);
                break;
            case "Serial No":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_linear_scale);
                break;
            case "Remarks":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_translate);
                break;
            case "View Image":
                initialJoborderViewHolder.initialJoIcon.setImageResource(
                        R.drawable.ic_action_photo);
                initialJoborderViewHolder.initialJOReLay.setBackgroundResource(
                        R.drawable.card_view_border);
                break;
        }

        initialJoborderViewHolder.tvKeyInitialJO.setText(key);
        initialJoborderViewHolder.tvValueInitialJO.setText(
                initialJoborderList.get(i).getValue());
    }

    public int getItemCount() {
        return initialJoborderList.size();
    }

    class InitialJoborderViewHolder extends RecyclerView.ViewHolder {

        private ImageView initialJoIcon;
        private ConstraintLayout initialJOReLay;
        private TextView tvKeyInitialJO;
        private TextView tvValueInitialJO;
        private ImageView ivStatusInitialJO;

        private int index;

        private InitialJoborderViewHolder(View itemView) {
            super(itemView);

            this.initialJoIcon = itemView.findViewById(R.id.initialJoIcon);
            this.initialJOReLay = itemView.findViewById(R.id.initialJOReLay);
            this.tvKeyInitialJO = itemView.findViewById(R.id.tvKeyInitialJO);
            this.tvValueInitialJO = itemView.findViewById(R.id.tvValueInitialJO);
            this.ivStatusInitialJO = itemView.findViewById(R.id.ivStatusInitialJO);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    String key = initialJoborderList.get(index).getKey();

                    if (key.equals("View Image")) {
                        String url = sharedPreferences.getString("domain", null)
                                +"getinitialjoborderphoto?initialJoborderId="+quotationId;
                        new DialogImageTask(context).execute(url);
                    } else if (key.equals("View Signature")) {
                        String url = sharedPreferences.getString("domain", null)
                                +"getinitialjobordersignature?initialJoborderId="+quotationId;
                        new DialogImageTask(context).execute(url);
                    }
                }
            });
        }
    }
}