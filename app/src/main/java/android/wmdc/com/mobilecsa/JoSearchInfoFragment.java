package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.wmdc.com.mobilecsa.adapter.JOSearchResultInfoAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wmdcprog on 4/19/2018.
 */

public class JoSearchInfoFragment extends Fragment {

    private final ArrayList<String> LABEL = new ArrayList<>(Arrays.asList("JO #", "Customer",
            "Date Created", "Date Started", "Date Target", "CSA", "Model", "Make", "Serial", "Image"
    ));

    private final ArrayList<String> JSON_KEY = new ArrayList<>(Arrays.asList("joNumber", "customer",
            "dateCreated", "dateStarted", "dateTarget", "csa", "model", "make", "serial", "image"));

    private final int LENGTH = LABEL.size();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.jo_search_info_container, container, false);

        v.setPadding(20, 0, 20, 0);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            ArrayList<KeyValueInfo> jobOrderInfos = new ArrayList<>();
            RecyclerView recyclerView = v.findViewById(R.id.rvSearchJoInfo);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    Util.systemWidth, (int)(Util.systemHeight * 0.75));
            recyclerView.setLayoutParams(params);

            try {
                JSONObject object = new JSONObject(bundle.getString("result"));

                if (getActivity() != null) {
                    getActivity().setTitle(object.getString("customer"));
                } else {
                    Util.longToast(getContext(),
                            "Activity is null. Cannot set title of this fragment.");
                }

                for (int i = 0; i < LENGTH; ++i) {
                    jobOrderInfos.add(new KeyValueInfo(LABEL.get(i), object.getString(
                            JSON_KEY.get(i))));
                }

                JOSearchResultInfoAdapter joResAdapter = new JOSearchResultInfoAdapter(getContext(),
                                jobOrderInfos, Integer.parseInt(object.getString("jobOrderId")));

                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(joResAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                        LinearLayout.VERTICAL));

            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());
            }
        }

        return v;
    }
}