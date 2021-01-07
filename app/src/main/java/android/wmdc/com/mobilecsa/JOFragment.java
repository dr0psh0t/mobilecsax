package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckExpiryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetInitialJoborderListTask;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**Created by wmdcprog on 4/13/2018.*/

public class JOFragment extends Fragment {

    private SharedPreferences sPrefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.jo_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Job Order");
        } else {
            Util.longToast(getContext(), "Title error");
            Log.e("Null", "Activity is null. Cannot set title of this fragment.");
        }

        setHasOptionsMenu(true);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Button btnNewJo = v.findViewById(R.id.btnNewJO);
        Button btnSearchJO = v.findViewById(R.id.btnSearchJO);
        Button btnJOApproval = v.findViewById(R.id.btnJOApproval);
        Button btnJOQuotationList = v.findViewById(R.id.btnJOQuotationList);

        btnJOQuotationList.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                new GetInitialJoborderListTask(getActivity()).execute(String.valueOf(
                        sPrefs.getInt("csaId", 0)));
            }
        });

        btnNewJo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter,
                            R.anim.exit, R.anim.pop_enter, R.anim.pop_exit).replace(
                                    R.id.content_main, new InitialJobOrder()).commit();
                } else {
                    Util.alertBox(getContext(), "Error");
                    Log.e("Null", "Fragment Manager is null. Cannot add joborder.");
                }
            }
        });

        btnSearchJO.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter,
                            R.anim.exit, R.anim.pop_enter, R.anim.pop_exit).replace(
                                    R.id.content_main, new SearchJOFragment()).commit();
                } else {
                    Util.alertBox(getContext(), "Error");
                    Log.e("Null", "Fragment Manager is null. Cannot search joborder.");
                }
            }
        });

        btnJOApproval.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                                    R.anim.pop_exit).replace(R.id.content_main,
                            new ApprovalFragment()).commit();
                } else {
                    Util.alertBox(getContext(), "Error");
                    Log.e("Null", "Fragment Manager is null. Cannot go to Approval.");
                }
            }
        });

        new CheckExpiryTask(getActivity()).execute();
        return v;
    }
}