package android.wmdc.com.mobilecsa.adapter;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.Engine;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**Created by wmdcprog on 7/2/2018.*/

public class EngineJOAdapter extends RecyclerView.Adapter<EngineJOAdapter.EngineViewHolder> {

    private final ArrayList<Engine> engineList;
    private final WeakReference<FragmentActivity> activityWeakReference;
    private final WeakReference<TextView> textViewModelIdWeakReference;

    private final EditText etEngine;
    private final EditText etMakeCat;

    private final Dialog dialog;

    public EngineJOAdapter(ArrayList<Engine> engineList, FragmentActivity activity, EditText etEngine,
                           EditText etMakeCat, Dialog dialog, TextView textViewModelId) {

        this.engineList = engineList;
        this.activityWeakReference = new WeakReference<>(activity);
        this.textViewModelIdWeakReference = new WeakReference<>(textViewModelId);
        this.etEngine = etEngine;
        this.etMakeCat = etMakeCat;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public EngineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activityWeakReference.get()).inflate(
                R.layout.engine_row_item, viewGroup, false);
        return new EngineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EngineViewHolder engineViewHolder, int i) {
        engineViewHolder.tvEngineJO.setText(engineList.get(i).getModel());
        engineViewHolder.rowItemJOEngineLinLay.setBackgroundResource(
                R.drawable.custom_card_background_odd);
        engineViewHolder.index = i;
    }

    @Override
    public int getItemCount() {
        return engineList.size();
    }

    class EngineViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEngineJO;
        private int index;
        private final LinearLayout rowItemJOEngineLinLay;

        private EngineViewHolder(View itemView) {
            super(itemView);
            this.tvEngineJO = itemView.findViewById(R.id.tvEngineJO);
            this.rowItemJOEngineLinLay = itemView.findViewById(R.id.rowItemJOEngineLinLay);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    try {
                        etEngine.setText(engineList.get(index).getModel());

                        String txt = engineList.get(index).getMake() + "/" +
                                engineList.get(index).getCategory();

                        etMakeCat.setText(txt);

                        textViewModelIdWeakReference.get().setText(
                                String.valueOf(engineList.get(index).getModelId()));

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