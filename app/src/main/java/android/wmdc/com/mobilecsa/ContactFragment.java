package android.wmdc.com.mobilecsa;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckExpiryTask;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Created by wmdcprog on 4/12/2018.
 */

public class ContactFragment extends Fragment {
    private FragmentTransaction fragmentTransaction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.contact_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Contacts");
        } else {
            Util.longToast(getContext(),
                    "Activity is null. Cannot set fragment title and preferences.");
        }

        Button btnAddContacts = v.findViewById(R.id.btnAddContact);
        Button btnSearchContacts = v.findViewById(R.id.btnSearchContacts);

        fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                R.anim.pop_exit);

        btnAddContacts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                final ProgressDialog progress = new ProgressDialog(getActivity());
                progress.setTitle("Loading");
                progress.setMessage("Please wait...");
                progress.setCancelable(false);
                progress.show();
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        progress.cancel();

                        fragmentTransaction.replace(R.id.content_main, new AddContactFragment());
                        fragmentTransaction.commit();
                    }
                };

                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 1000);
            }
        });

        btnSearchContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentTransaction.replace(R.id.content_main, new SearchContactFragment());
                fragmentTransaction.commit();
            }
        });

        new CheckExpiryTask(getActivity()).execute();

        return v;
    }
}