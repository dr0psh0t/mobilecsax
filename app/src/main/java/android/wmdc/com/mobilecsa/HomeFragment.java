package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**Created by wmdcprog on 4/13/2018.*/

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, container, false);
        getActivity().setTitle("Home");
        setHasOptionsMenu(true);

        final FragmentTransaction fragmentTransaction =
                getActivity().getSupportFragmentManager().beginTransaction();

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

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem backMenu = menu.findItem(R.id.action_back_nav);
        backMenu.setVisible(false);
    }
}