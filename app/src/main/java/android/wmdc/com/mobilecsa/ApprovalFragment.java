package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetDCJOListTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetQCJOListTask;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Created by wmdcprog on 4/13/2018. */

public class ApprovalFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private GetQCJOListTask qcTask;
    private GetDCJOListTask dcTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.approval_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Approval");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        } else {
            Util.longToast(getContext(),
                    "Activity is null. Cannot set fragment title and preferences.");
        }

        Button btnQC = v.findViewById(R.id.btnQC);
        Button btnDateComm = v.findViewById(R.id.btnDateComm);

        btnQC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Variables.currentPage = 1;
                qcTask = new GetQCJOListTask(getActivity());

                qcTask.execute(String.valueOf(sharedPreferences.getInt("csaId", 0)),
                        "mcsa", String.valueOf(Variables.currentPage));
            }
        });

        btnDateComm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dcTask = new GetDCJOListTask(getActivity(), null, true);
                dcTask.execute(String.valueOf(sharedPreferences.getInt("csaId", 0)), "mcsa");
            }
        });

        return v;
    }
}