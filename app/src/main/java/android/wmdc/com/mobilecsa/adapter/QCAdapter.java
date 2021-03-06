package android.wmdc.com.mobilecsa.adapter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.QCJOInfoFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.QCDataModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 3/9/2018.
 */

public class QCAdapter extends RecyclerView.Adapter<QCAdapter.QCViewHolder> {
    private final ArrayList<QCDataModel> qcData;
    private final FragmentActivity activity;
    private boolean heightSet = false;

    public QCAdapter(ArrayList<QCDataModel> qcData, FragmentActivity act) {
        this.qcData = qcData;
        activity = act;
    }

    /*
    public void replaceAdapterList(ArrayList<QCDataModel> qcData) {
        this.qcData = null;
        this.qcData = qcData;
        this.notifyDataSetChanged();
    }*/

    @Override
    @NonNull
    public QCViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.row_item_qc, viewGroup, false);

        if (!heightSet) {
            final LinearLayout rootLay = view.findViewById(R.id.rowItemQCLinLay);
            final ViewTreeObserver observer = rootLay.getViewTreeObserver();

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Util.recyclerViewItemHeight = rootLay.getHeight();
                }
            });
        }

        heightSet = true;
        return new QCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QCViewHolder qcViewHolder, int i) {

        qcViewHolder.rowItemQCRelLay.setBackgroundResource((i % 2 != 0) ?
                R.drawable.custom_card_background_odd : R.drawable.custom_card_background_even);
        qcViewHolder.tvJONumberDC.setText(qcData.get(i).getJoNumber());
        qcViewHolder.tvCustomerIDQC.setText(qcData.get(i).getCustomerId());
        qcViewHolder.tvCustomerDC.setText(qcData.get(i).getCustomer());
        qcViewHolder.index = i;
    }

    class QCViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rowItemQCRelLay;
        private final TextView tvJONumberDC;
        private final TextView tvCustomerIDQC;
        private final TextView tvCustomerDC;

        private int index;

        private QCViewHolder(View itemView) {
            super(itemView);

            rowItemQCRelLay = itemView.findViewById(R.id.rowItemQCLinLay);

            tvJONumberDC = itemView.findViewById(R.id.tvJONumber);
            tvCustomerIDQC = itemView.findViewById(R.id.tvCustomerIDQC);
            tvCustomerDC = itemView.findViewById(R.id.tvCustomerQC);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Fragment frag = new QCJOInfoFragment();
                    JSONObject object = new JSONObject();

                    try {
                        object.put("joNumber", qcData.get(index).getJoNumber());
                        object.put("customerID", qcData.get(index).getCustomerId());
                        object.put("customer", qcData.get(index).getCustomer());
                        object.put("model", qcData.get(index).getModel());
                        object.put("make", qcData.get(index).getMake());
                        object.put("category", qcData.get(index).getCategory());
                        object.put("serial", qcData.get(index).getSerialNum());
                        object.put("dateReceived", qcData.get(index).getDateReceived());
                        object.put("dateCommit", qcData.get(index).getDateCommit());
                        object.put("success", true);

                    } catch (JSONException je) {
                        Util.displayStackTraceArray(je.getStackTrace(),
                                Variables.ADAPTER_PACKAGE, "JSONException", je.toString());

                        try {
                            object.put("success", false);
                        } catch (JSONException jee) {
                            Util.displayStackTraceArray(jee.getStackTrace(),
                                    Variables.ADAPTER_PACKAGE, "JSONException", jee.toString());
                        }
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("result", object.toString());
                    bundle.putInt("joid", qcData.get(index).getJoId());

                    frag.setArguments(bundle);

                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    final FragmentTransaction fragmentTransaction =
                            fragmentManager.beginTransaction();

                    //  -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

                    final ProgressDialog progress = new ProgressDialog(activity);
                    progress.setMessage("Loading...");
                    progress.setCancelable(false);
                    progress.show();

                    Runnable progressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            progress.cancel();
                            fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,
                                    R.anim.pop_enter, R.anim.pop_exit);
                            fragmentTransaction.replace(R.id.content_main, frag);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                    };

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 1000);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return qcData.size();
    }
}