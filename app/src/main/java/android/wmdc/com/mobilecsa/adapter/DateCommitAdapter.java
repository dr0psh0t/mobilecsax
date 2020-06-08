package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.DCJOInfoFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.DateCommitModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 3/9/2018.
 */

public class DateCommitAdapter extends RecyclerView.Adapter<DateCommitAdapter.DCViewHolder> {

    private ArrayList<DateCommitModel> dcData;
    private Context context;
    private boolean heightSet = false;

    public DateCommitAdapter(ArrayList<DateCommitModel> dcData, Context context) {
        this.dcData = dcData;
        this.context = context;
    }

    @NonNull
    @Override
    public DCViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_dc, viewGroup, false);

        if (!heightSet) {
            final LinearLayout rootLay = view.findViewById(R.id.rowItemDCRelLay);
            final ViewTreeObserver observer = rootLay.getViewTreeObserver();

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Util.recyclerViewItemHeight = rootLay.getHeight();
                }
            });
        }

        heightSet = true;
        return new DCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DCViewHolder dcViewHolder, int i) {

        if (i % 2 != 0) {
            dcViewHolder.rowItemDCRelLay.setBackgroundResource(
                    R.drawable.custom_card_background_odd);
        }
        else {
            dcViewHolder.rowItemDCRelLay.setBackgroundResource(R.drawable.custom_card_background_even);
        }

        if (dcData.get(i).getCsaApproved()) {
            dcViewHolder.ivCSAApprovedStatus.setImageResource(R.drawable.ic_action_check_colored_round);
        }
        else {
            dcViewHolder.ivCSAApprovedStatus.setImageResource(R.drawable.ic_action_x_colored_round);
        }

        if (dcData.get(i).getPnmApproved()) {
            dcViewHolder.ivPMApprovedStatus.setImageResource(R.drawable.ic_action_check_colored_round);
        }
        else {
            dcViewHolder.ivPMApprovedStatus.setImageResource(R.drawable.ic_action_x_colored_round);
        }

        dcViewHolder.tvJoNumberDC.setText(dcData.get(i).getJoNumber());
        dcViewHolder.tvCustomerIDDC.setText(dcData.get(i).getCustomerId());
        dcViewHolder.tvCustomerDC.setText(dcData.get(i).getCustomer());
        dcViewHolder.index = i;
    }

    class DCViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout rowItemDCRelLay;
        private ImageView ivCSAApprovedStatus;
        private ImageView ivPMApprovedStatus;
        private TextView tvJoNumberDC;
        private TextView tvCustomerIDDC;
        private TextView tvCustomerDC;
        private int index;

        private DCViewHolder(View itemView) {
            super(itemView);

            rowItemDCRelLay = itemView.findViewById(R.id.rowItemDCRelLay);

            ivCSAApprovedStatus = itemView.findViewById(R.id.ivCSAApprovedStatus);
            ivPMApprovedStatus = itemView.findViewById(R.id.ivPMApprovedStatus);
            tvJoNumberDC = itemView.findViewById(R.id.tvJONumberDC);
            tvCustomerIDDC = itemView.findViewById(R.id.tvCustomerIDDC);
            tvCustomerDC = itemView.findViewById(R.id.tvCustomerDC);

            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {

                    JSONObject object = new JSONObject();

                    try {
                        object.put("joNum", dcData.get(index).getJoNumber());
                        object.put("joId", dcData.get(index).getJoId());
                        object.put("customerId", dcData.get(index).getCustomerId());
                        object.put("customer", dcData.get(index).getCustomer());
                        object.put("dateCommit", dcData.get(index).getDateCommit());
                        object.put("dateReceive", dcData.get(index).getDateReceive());
                        object.put("csaApproved", dcData.get(index).getCsaApproved());
                        object.put("pmApproved", dcData.get(index).getPnmApproved());
                        object.put("success", true);
                    } catch (JSONException je) {
                        Util.displayStackTraceArray(je.getStackTrace(),
                                Variables.ADAPTER_PACKAGE, "JSONException", je.toString());
                        Toast.makeText(context, je.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            object.put("success", false);
                        } catch (JSONException jee) {
                            Util.displayStackTraceArray(jee.getStackTrace(),
                                    Variables.ADAPTER_PACKAGE, "JSONException", jee.toString());
                            Toast.makeText(context, jee.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("result", object.toString());
                    bundle.putInt("joId", dcData.get(index).getJoId());

                    DCJOInfoFragment dateCommitFragment = new DCJOInfoFragment();
                    dateCommitFragment.setDateCommitAdapter(DateCommitAdapter.this);
                    dateCommitFragment.setDateCommitList(dcData);
                    dateCommitFragment.setDcJoborderId(dcData.get(index).getJoId());
                    dateCommitFragment.setArguments(bundle);

                    FragmentManager fragmentManager = ((AppCompatActivity) context)
                            .getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                            R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.replace(R.id.content_main, dateCommitFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dcData.size();
    }
}