package android.wmdc.com.mobilecsa.adapter;

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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 6/6/2018.
 */

public class InitialJoborderListAdapter extends
        RecyclerView.Adapter<InitialJoborderListAdapter.InitialJoborderListViewHolder> {

    private final WeakReference<FragmentActivity> weakReference;
    private final ArrayList<InitialJoborderRowModel> initJoList;
    private boolean heightSet = false;

    public InitialJoborderListAdapter(FragmentActivity activity,
                                      ArrayList<InitialJoborderRowModel> initJoList) {
        this.weakReference = new WeakReference<>(activity);
        this.initJoList = initJoList;
    }

    @NonNull
    public InitialJoborderListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(weakReference.get()).inflate(R.layout.initial_joborder_item,
                viewGroup, false);

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

        initialJoborderListViewHolder.tvJoNumber.setText(initJoList.get(i).getJoNumber());
        initialJoborderListViewHolder.tvCustomer.setText(initJoList.get(i).getCustomer());
        initialJoborderListViewHolder.tvSerial.setText(initJoList.get(i).getSerial());

        if (initJoList.get(i).getIsAdded() == 1) {
            initialJoborderListViewHolder.ivStatusInitialJO
                    .setImageResource(R.drawable.ic_action_check_colored_round);
        } else if (initJoList.get(i).getIsAdded() == 0) {
            initialJoborderListViewHolder.ivStatusInitialJO
                    .setImageResource(R.drawable.ic_action_x_colored_round);
        }
    }

    public int getItemCount() {
        return initJoList.size();
    }

    class InitialJoborderListViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout initialJORowItem;
        private final TextView tvJoNumber;
        private final TextView tvCustomer;
        private final TextView tvSerial;
        private final ImageView ivStatusInitialJO;

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
                    new GetInitialJoborderTask(weakReference.get()).execute(
                            String.valueOf(initJoList.get(index).getQuotationId()));
                }
            });
        }
    }
}