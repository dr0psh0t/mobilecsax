package android.wmdc.com.mobilecsa;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchCustomerTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class SearchCustomerFragment extends Fragment {

    EditText etSearchCust;
    Button btnSearchCust;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.search_customer_fragment, container, false);
        getActivity().setTitle(R.string.search_customer);
        setHasOptionsMenu(true);

        etSearchCust = v.findViewById(R.id.etSearchCust);
        etSearchCust.setFilters(new InputFilter[] {new EmojiExcludeFilter(),
                new InputFilter.LengthFilter(16)});

        btnSearchCust = v.findViewById(R.id.btnSearchCust);
        btnSearchCust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etSearchCust.getText().toString().length() < 2) {
                    Toast.makeText(getActivity(), "Too short", Toast.LENGTH_LONG).show();
                } else {
                    new SearchCustomerTask(getActivity()).execute(
                            etSearchCust.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                            .getWindowToken(), 0);
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