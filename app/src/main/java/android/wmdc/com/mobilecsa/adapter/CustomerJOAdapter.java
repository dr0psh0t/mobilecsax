package android.wmdc.com.mobilecsa.adapter;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.CustomerJO;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 5/30/2018.
 */

public class CustomerJOAdapter extends
        RecyclerView.Adapter<CustomerJOAdapter.CustomerTestViewHolder> {

    private ArrayList<CustomerJO> customerList;
    private WeakReference<FragmentActivity> activityWeakReference;
    private WeakReference<TextView> textViewCustomerIdWeakReference;
    private WeakReference<TextView> textViewSourceWeakReference;
    private EditText etCustomer;
    private Dialog dialog;

    public CustomerJOAdapter(ArrayList<CustomerJO> customerList, FragmentActivity activity,
                             EditText etCustomer, Dialog dialog, TextView textViewCustomerId,
                             TextView textViewSource) {

        this.customerList = customerList;
        this.activityWeakReference = new WeakReference<>(activity);
        textViewCustomerIdWeakReference = new WeakReference<>(textViewCustomerId);
        textViewSourceWeakReference = new WeakReference<>(textViewSource);
        this.etCustomer = etCustomer;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public CustomerTestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(activityWeakReference.get()).inflate(
                R.layout.searchable_row_item, viewGroup, false);

        return new CustomerTestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomerTestViewHolder customerTestViewHolder, int i) {
        customerTestViewHolder.tvCustomerJO.setText(customerList.get(i).getCustomer());
        customerTestViewHolder.rowItemJOCustomerLinLay
                .setBackgroundResource(R.drawable.custom_card_background_odd);
        customerTestViewHolder.index = i;
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    class CustomerTestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCustomerJO;
        private int index;
        private LinearLayout rowItemJOCustomerLinLay;

        private CustomerTestViewHolder(View itemView) {
            super(itemView);
            this.tvCustomerJO = itemView.findViewById(R.id.tvCustomerJO);
            this.rowItemJOCustomerLinLay = itemView.findViewById(R.id.rowItemJOCustomerLinLay);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        etCustomer.setText(customerList.get(index).getCustomer());

                        textViewCustomerIdWeakReference.get().setText(String.valueOf(customerList.get(index).getcId()));
                        textViewSourceWeakReference.get().setText(customerList.get(index).getSource());

                        dialog.cancel();

                    } catch (Exception e) {
                        Util.displayStackTraceArray(e.getStackTrace(), Variables.ADAPTER_PACKAGE,
                                "Exception", e.toString());
                        Util.longToast(activityWeakReference.get(), "Error");
                    }
                }
            });
        }
    }
}
