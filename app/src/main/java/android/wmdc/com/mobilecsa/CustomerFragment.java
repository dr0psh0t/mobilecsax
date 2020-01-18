package android.wmdc.com.mobilecsa;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

    private String localAddressNorth;
    private String publicAddressNorth;
    private String progLocalAddressNorth;
    private String usedUrl;

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.customer_fragment, container, false);
        getActivity().setTitle("Customer");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Button btnAddCustomer = v.findViewById(R.id.btnAddCustomer);
        Button btnAddCompany = v.findViewById(R.id.btnAddCompany);
        Button btnSearchCustomer = v.findViewById(R.id.btnSearchCustomer);

        localAddressNorth = sharedPreferences.getString("localAddressNorth", null);
        publicAddressNorth = sharedPreferences.getString("publicAddressNorth", null);
        progLocalAddressNorth = "http://192.168.1.30:8080/mcsa/";
        usedUrl = sharedPreferences.getString("domain", null);

        btnAddCustomer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (usedUrl.equals(localAddressNorth) ||
                        usedUrl.equals(publicAddressNorth) ||
                        usedUrl.equals(progLocalAddressNorth)) {

                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] {
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
                        return;
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
                                getFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.enter, R.anim.exit,
                                                R.anim.pop_enter, R.anim.pop_exit)
                                        .replace(R.id.content_main, new AddCustomerFragment())
                                        .commit();
                            }
                        };

                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 1000);
                    }
                } else {
                    Util.alertBox(getContext(), "You are not connected to branch North.",
                            "Change Branch.", false);
                }
            }
        });

        btnAddCompany.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (usedUrl.equals(localAddressNorth) ||
                        usedUrl.equals(publicAddressNorth) ||
                        usedUrl.equals(progLocalAddressNorth)) {

                    final ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setTitle("Loading");
                    progress.setMessage("Please wait...");
                    progress.setCancelable(false);
                    progress.show();
                    Runnable progressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            progress.cancel();
                            getFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter, R.anim.exit,
                                            R.anim.pop_enter, R.anim.pop_exit)
                                    .replace(R.id.content_main, new AddCompanyFragment())
                                    .commit();
                        }
                    };

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 1000);
                } else {
                    Util.alertBox(getContext(), "You are not connected to branch North.",
                            "Change Branch.", false);
                }
            }
        });

        btnSearchCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usedUrl.equals(localAddressNorth) ||
                        usedUrl.equals(publicAddressNorth) ||
                        usedUrl.equals(progLocalAddressNorth)) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter, R.anim.exit,
                                    R.anim.pop_enter, R.anim.pop_exit)
                            .replace(R.id.content_main, new SearchCustomerFragment())
                            .commit();
                } else {
                    Util.alertBox(getContext(), "You are not connected to branch North.",
                            "Change Branch.", false);
                }
            }
        });

        new CheckExpiryTask(getActivity()).execute();
        return v;
    }
}