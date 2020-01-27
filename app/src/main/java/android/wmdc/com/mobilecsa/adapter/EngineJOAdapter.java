package android.wmdc.com.mobilecsa.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.Engine;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**Created by wmdcprog on 7/2/2018.*/

public class EngineJOAdapter extends RecyclerView.Adapter<EngineJOAdapter.EngineViewHolder> {

    private ArrayList<Engine> engineList;
    private Context context;

    private EditText etEngine;
    private EditText etMakeCat;

    private Dialog dialog;

    public EngineJOAdapter(ArrayList<Engine> engineList, Context context, EditText etEngine,
                           EditText etMakeCat, Dialog dialog) {

        this.engineList = engineList;
        this.context = context;
        this.etEngine = etEngine;
        this.etMakeCat = etMakeCat;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public EngineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.engine_row_item, viewGroup,
                false);
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
        private TextView tvEngineJO;
        private int index;
        private LinearLayout rowItemJOEngineLinLay;

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

                        Variables.modelId = engineList.get(index).getModelId();

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
