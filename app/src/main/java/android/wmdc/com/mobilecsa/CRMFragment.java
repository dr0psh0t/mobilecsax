package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Created by wmdcprog on 6/21/2018.
 */

public class CRMFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View v = inflater.inflate(R.layout.crm_layout, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Home");
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot set fragment title.");
        }

        final FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                R.anim.pop_exit);

        final Button btnCustomer = v.findViewById(R.id.btnCustomer);
        btnCustomer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                fragmentTransaction.replace(R.id.content_main, new CustomerFragment());
                fragmentTransaction.commit();
            }
        });

        final Button btnContact = v.findViewById(R.id.btnContact);
        btnContact.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                fragmentTransaction.replace(R.id.content_main, new ContactFragment());
                fragmentTransaction.commit();
            }
        });

        return v;
    }
}