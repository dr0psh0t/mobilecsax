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
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchContactTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class SearchContactFragment extends Fragment {
    private EditText etSearchCont;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.search_contact_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.search_contact);
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot set title of the fragment.");
        }

        setHasOptionsMenu(true);

        etSearchCont = v.findViewById(R.id.etSearchCont);
        etSearchCont.setFilters(new InputFilter[] {new EmojiExcludeFilter(),
                new InputFilter.LengthFilter(16)});

        Button btnSearchCont = v.findViewById(R.id.btnSearchCont);
        btnSearchCont.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new CheckExpiryTask(getActivity()).execute();

                if (etSearchCont.getText().toString().length() < 2) {
                    Util.longToast(getActivity(), "Too short.");
                } else {
                    new SearchContactTask(getActivity()).execute(etSearchCont.getText().toString());
                }
            }
        });

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);

        MenuItem backAction = menu.findItem(R.id.action_back_nav);
        backAction.setVisible(true);
    }
}