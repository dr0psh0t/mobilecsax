package android.wmdc.com.mobilecsa.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.CustomerJO;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 5/30/2018.
 */

public class CustomerJOAdapter extends
        RecyclerView.Adapter<CustomerJOAdapter.CustomerTestViewHolder> {

    private ArrayList<CustomerJO> customerList;
    private Context context;
    private SharedPreferences sharedPreferences;
    private EditText etCustomer;
    private Dialog dialog;

    public CustomerJOAdapter(ArrayList<CustomerJO> customerList, Context context,
                             EditText etCustomer, Dialog dialog) {

        this.customerList = customerList;
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.etCustomer = etCustomer;
        this.dialog = dialog;
    }

    @Override
    public CustomerTestViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.searchable_row_item, viewGroup,
                false);
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

        public CustomerTestViewHolder(View itemView) {
            super(itemView);
            this.tvCustomerJO = itemView.findViewById(R.id.tvCustomerJO);
            this.rowItemJOCustomerLinLay = itemView.findViewById(R.id.rowItemJOCustomerLinLay);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        etCustomer.setText(customerList.get(index).getCustomer());
                        Variables.customerIdForJO = customerList.get(index).getcId();
                        Variables.source = customerList.get(index).getSource();
                        dialog.cancel();
                    } catch (Exception e) {
                        Util.displayStackTraceArray(e.getStackTrace(), Variables.ADAPTER_PACKAGE,
                                "Exception", e.toString());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
