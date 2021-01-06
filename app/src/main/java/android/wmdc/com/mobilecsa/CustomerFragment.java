package android.wmdc.com.mobilecsa;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckExpiryTask;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**Created by wmdcprog on 4/13/2018.*/

public class CustomerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.customer_fragment, container, false);

        if (getActivity() != null) {

            getActivity().setTitle("Customer");

            Button btnAddCustomer = v.findViewById(R.id.btnAddCustomer);
            Button btnAddCompany = v.findViewById(R.id.btnAddCompany);
            Button btnSearchCustomer = v.findViewById(R.id.btnSearchCustomer);

            btnAddCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (getActivity() != null) {

                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else {
                            final ProgressDialog progress = new ProgressDialog(getActivity());

                            progress.setTitle("Loading");
                            progress.setMessage("Please wait...");
                            progress.setCancelable(false);
                            progress.show();
                            Runnable progressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    progress.cancel();

                                    if (getFragmentManager() != null) {
                                        getFragmentManager().beginTransaction()
                                                .setCustomAnimations(R.anim.enter, R.anim.exit,
                                                        R.anim.pop_enter, R.anim.pop_exit)
                                                .replace(R.id.content_main,
                                                        new AddCustomerFragment()).commit();
                                    } else {
                                        Util.shortToast(getActivity(), "Fragment Manager is " +
                                                "null. Cannot add customer.");
                                    }
                                }
                            };

                            Handler pdCanceller = new Handler();
                            pdCanceller.postDelayed(progressRunnable, 1000);
                        }
                    } else {
                        Util.shortToast(getContext(), "Activity is null. Cannot add customer.");
                    }
                }
            });

            btnAddCompany.setOnClickListener(new View.OnClickListener() {
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

                            if (getFragmentManager() != null) {
                                getFragmentManager().beginTransaction().setCustomAnimations(
                                        R.anim.enter, R.anim.exit, R.anim.pop_enter,
                                        R.anim.pop_exit).replace(R.id.content_main,
                                        new AddCompanyFragment()).commit();
                            } else {
                                Util.shortToast(getActivity(), "Fragment Manager is " +
                                        "null. Cannot add customer.");
                            }
                        }
                    };

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 1000);
                }
            });

            btnSearchCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getFragmentManager() != null) {
                        getFragmentManager().beginTransaction().setCustomAnimations(
                                R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                                .replace(R.id.content_main, new SearchCustomerFragment())
                                .commit();
                    } else {
                        Util.shortToast(getActivity(),
                                "Fragment Manager is null. Cannot search customer.");
                    }
                }
            });

            new CheckExpiryTask(getActivity()).execute();
        } else {
            Util.alertBox(getContext(), "Some actions cannot be executed.");
            Log.e("Null", "getActivity() is null");
        }

        return v;
    }
}