package android.wmdc.com.mobilecsa.adapter;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.model.DateCommitModel;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.SwipeButton;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by wmdcprog on 3/9/2018.
 */

public class DateCommitAdapter extends RecyclerView.Adapter<DateCommitAdapter.DCViewHolder> {

    private final ArrayList<DateCommitModel> dcData;
    private final FragmentActivity activity;
    private boolean heightSet = false;
    private final SharedPreferences sPrefs;

    public DateCommitAdapter(ArrayList<DateCommitModel> dcData, FragmentActivity act) {
        this.dcData = dcData;
        activity = act;
        sPrefs = PreferenceManager.getDefaultSharedPreferences(act);
    }

    @NonNull
    @Override
    public DCViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.row_item_dc, viewGroup, false);

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

        dcViewHolder.rowItemDCRelLay.setBackgroundResource((i % 2 != 0) ?
                R.drawable.custom_card_background_odd : R.drawable.custom_card_background_even);

        dcViewHolder.ivCSAApprovedStatus.setImageResource(dcData.get(i).getCsaApproved() ?
                R.drawable.ic_action_check_colored_round : R.drawable.ic_action_x_colored_round);

        dcViewHolder.ivPMApprovedStatus.setImageResource(dcData.get(i).getPnmApproved() ?
                R.drawable.ic_action_check_colored_round : R.drawable.ic_action_x_colored_round);

        dcViewHolder.tvJoNumberDC.setText(dcData.get(i).getJoNumber());
        dcViewHolder.tvCustomerIDDC.setText(dcData.get(i).getCustomerId());
        dcViewHolder.tvCustomerDC.setText(dcData.get(i).getCustomer());
        dcViewHolder.index = i;
    }

    class DCViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rowItemDCRelLay;
        private final ImageView ivCSAApprovedStatus;
        private final ImageView ivPMApprovedStatus;
        private final TextView tvJoNumberDC;
        private final TextView tvCustomerIDDC;
        private final TextView tvCustomerDC;
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

                        final Dialog dialog = new Dialog(activity);
                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dc_jo_info_viewholder_layout);

                        TextView tvSwipe = dialog.findViewById(R.id.tvSwipe);
                        RecyclerView recyclerView = dialog.findViewById(R.id.rvDCInfo);
                        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

                        ArrayList<String> DC_KEY = new ArrayList<>(Arrays.asList("JO Number",
                                "Customer", "Date Received", "Date Commit"));

                        ArrayList<KeyValueInfo> dcInfos = new ArrayList<>();
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(0), object.getString("joNum")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(1), object.getString("customer")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(2), object.getString("dateReceive")));
                        dcInfos.add(new KeyValueInfo(DC_KEY.get(3), object.getString("dateCommit")));

                        DCValueInfoAdapter dcValueInfoAdapter = new DCValueInfoAdapter(dcInfos,
                                activity, dcData.get(index).getJoId(),
                                object.getBoolean("csaApproved"));

                        recyclerView.setAdapter(dcValueInfoAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                LinearLayoutManager.VERTICAL));

                        SwipeButton swipeButton = dialog.findViewById(R.id.swipe_btn);
                        swipeButton.setParameters(sPrefs.getInt("csaId", 0), "mcsa",
                                object.getInt("joId"), 0, null, null, null);

                        swipeButton.setDateCommitAdapter(DateCommitAdapter.this);
                        swipeButton.setDateCommitList(dcData);
                        swipeButton.setDcJoborderId(dcData.get(index).getJoId());
                        swipeButton.setFragmentActivity(activity);

                        if (object.getBoolean("csaApproved")) {
                            swipeButton.setVisibility(View.GONE);
                            tvSwipe.setVisibility(View.GONE);
                        }

                        dialog.show();

                    } catch (JSONException je) {
                        Util.displayStackTraceArray(je.getStackTrace(),
                                Variables.ADAPTER_PACKAGE, "JSONException", je.toString());
                        Toast.makeText(activity, "Parse error", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Util.displayStackTraceArray(e.getStackTrace(),
                                Variables.ADAPTER_PACKAGE, "Exception", e.toString());
                        Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
                    }

                    /*
                    Bundle bundle = new Bundle();
                    bundle.putString("result", object.toString());
                    bundle.putInt("joId", dcData.get(index).getJoId());

                    DCJOInfoFragment dateCommitFragment = new DCJOInfoFragment();
                    dateCommitFragment.setDateCommitAdapter(DateCommitAdapter.this);
                    dateCommitFragment.setDateCommitList(dcData);
                    dateCommitFragment.setDcJoborderId(dcData.get(index).getJoId());
                    dateCommitFragment.setArguments(bundle);

                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.pop_enter, R.anim.pop_exit);
                    fragmentTransaction.replace(R.id.content_main, dateCommitFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();*/

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dcData.size();
    }
}