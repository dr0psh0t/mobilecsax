package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckExpiryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchCustomerTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class SearchCustomerFragment extends Fragment {

    private EditText etSearchCust;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.search_customer_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.search_customer);
            setHasOptionsMenu(true);
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot set title of the fragment.");
        }

        etSearchCust = v.findViewById(R.id.etSearchCust);
        etSearchCust.setFilters(new InputFilter[] {new EmojiExcludeFilter(),
                new InputFilter.LengthFilter(16)});

        Button btnSearchCust = v.findViewById(R.id.btnSearchCust);
        btnSearchCust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckExpiryTask(getActivity()).execute();

                if (etSearchCust.getText().toString().length() < 2) {
                    Util.longToast(getActivity(), "Too short.");
                } else {
                    new SearchCustomerTask(getActivity()).execute(etSearchCust.getText()
                            .toString());
                }
            }
        });

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}