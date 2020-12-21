package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**Created by wmdcprog on 4/13/2018.*/

public class HomeFragment extends Fragment {
    //private String usedUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.home_fragment, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String usedUrl = prefs.getString("domain", null);

        if (getActivity() != null) {
            getActivity().setTitle("Home");
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot set title of this fragment.");
        }

        setHasOptionsMenu(true);

        final FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                .beginTransaction();

        fragmentTransaction.setCustomAnimations(
                R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

        final Button btnCRM = v.findViewById(R.id.btnCRM);

        btnCRM.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                fragmentTransaction.replace(R.id.content_main, new CRMFragment());
                fragmentTransaction.commit();
            }
        });

        final Button btnJobOrder = v.findViewById(R.id.btnJobOrder);

        btnJobOrder.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                fragmentTransaction.replace(R.id.content_main, new JOFragment());
                fragmentTransaction.commit();
            }
        });

        if (!usedUrl.equals("http://192.168.1.150:8080/mcsa/") && !usedUrl.equals("http://122.3.176.235:1959/mcsa/")) {
            btnCRM.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem backMenu = menu.findItem(R.id.action_back_nav);
        backMenu.setVisible(false);
    }
}