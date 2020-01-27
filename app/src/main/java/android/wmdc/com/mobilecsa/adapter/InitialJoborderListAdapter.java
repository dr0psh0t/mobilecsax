package android.wmdc.com.mobilecsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetInitialJoborderTask;
import android.wmdc.com.mobilecsa.model.InitialJoborderRowModel;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 6/6/2018.
 */

public class InitialJoborderListAdapter extends
        RecyclerView.Adapter<InitialJoborderListAdapter.InitialJoborderListViewHolder> {

    private Context context;
    private ArrayList<InitialJoborderRowModel> initialJoborderList;
    private boolean heightSet = false;

    public InitialJoborderListAdapter(
            Context context,
            ArrayList<InitialJoborderRowModel> initialJoborderList) {

        this.context = context;
        this.initialJoborderList = initialJoborderList;
    }

    @NonNull
    public InitialJoborderListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.initial_joborder_item, viewGroup,
                false);

        if (!heightSet) {
            final LinearLayout rootLay = view.findViewById(R.id.initialJORowItem);
            final ViewTreeObserver observer = rootLay.getViewTreeObserver();

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Util.recyclerViewItemHeight = rootLay.getHeight();
                }
            });
        }

        heightSet = true;
        return new InitialJoborderListViewHolder(view);
    }

    public void onBindViewHolder(
            InitialJoborderListViewHolder initialJoborderListViewHolder, int i) {

        initialJoborderListViewHolder.index = i;

        if (i % 2 != 0) {
            initialJoborderListViewHolder.initialJORowItem.setBackgroundResource(
                    R.drawable.custom_card_background_odd);
        } else {
            initialJoborderListViewHolder.initialJORowItem.setBackgroundResource(
                    R.drawable.custom_card_background_even);
        }

        initialJoborderListViewHolder.tvJoNumber.setText(initialJoborderList.get(i).getJoNumber());
        initialJoborderListViewHolder.tvCustomer.setText(initialJoborderList.get(i).getCustomer());
        initialJoborderListViewHolder.tvSerial.setText(initialJoborderList.get(i).getSerial());

        if (initialJoborderList.get(i).getIsAdded() == 1) {
            initialJoborderListViewHolder.ivStatusInitialJO
                    .setImageResource(R.drawable.ic_action_check_colored_round);
        } else if (initialJoborderList.get(i).getIsAdded() == 0) {
            initialJoborderListViewHolder.ivStatusInitialJO
                    .setImageResource(R.drawable.ic_action_x_colored_round);
        }
    }

    public int getItemCount() {
        return initialJoborderList.size();
    }

    class InitialJoborderListViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout initialJORowItem;
        private TextView tvJoNumber;
        private TextView tvCustomer;
        private TextView tvSerial;
        private ImageView ivStatusInitialJO;

        private int index;

        private InitialJoborderListViewHolder(View itemView) {
            super(itemView);

            this.initialJORowItem = itemView.findViewById(R.id.initialJORowItem);
            this.tvJoNumber = itemView.findViewById(R.id.tvJoNumber);
            this.tvCustomer = itemView.findViewById(R.id.tvCustomer);
            this.tvSerial = itemView.findViewById(R.id.tvSerial);
            this.ivStatusInitialJO = itemView.findViewById(R.id.ivStatusInitialJO);

            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    new GetInitialJoborderTask(context)
                            .execute(String.valueOf(initialJoborderList.get(index)
                                    .getQuotationId()));
                }
            });
        }
    }
}