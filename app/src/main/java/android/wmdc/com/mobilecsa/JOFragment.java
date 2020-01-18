package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckExpiryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetInitialJoborderListTask;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**Created by wmdcprog on 4/13/2018.*/

public class JOFragment extends Fragment {

    private Button btnNewJo;
    private Button btnSearchJO;
    private Button btnJOApproval;
    private Button btnJOQuotationList;

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.jo_fragment, container, false);
        getActivity().setTitle("Job Order");
        setHasOptionsMenu(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        btnNewJo = v.findViewById(R.id.btnNewJO);
        btnSearchJO = v.findViewById(R.id.btnSearchJO);
        btnJOApproval = v.findViewById(R.id.btnJOApproval);
        btnJOQuotationList = v.findViewById(R.id.btnJOQuotationList);

        btnJOQuotationList.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                new GetInitialJoborderListTask(getActivity())
                        .execute(String.valueOf(sharedPreferences.getInt("csaId", 0)));
            }
        });

        btnNewJo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                        .replace(R.id.content_main, new InitialJobOrder())
                        .commit();
            }
        });

        btnSearchJO.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                        .replace(R.id.content_main, new SearchJOFragment())
                        .commit();
            }
        });

        btnJOApproval.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                        .replace(R.id.content_main, new ApprovalFragment())
                        .commit();
            }
        });

        new CheckExpiryTask(getActivity()).execute();
        return v;
    }
}