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
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchContactTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class SearchContactFragment extends Fragment {
    EditText etSearchCont;
    Button btnSearchCont;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.search_contact_fragment, container, false);
        getActivity().setTitle(
                R.string.search_contact);
        setHasOptionsMenu(true);

        etSearchCont = v.findViewById(R.id.etSearchCont);
        etSearchCont.setFilters(new InputFilter[] {new EmojiExcludeFilter(),
                new InputFilter.LengthFilter(16)});

        btnSearchCont = v.findViewById(R.id.btnSearchCont);
        btnSearchCont.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (etSearchCont.getText().toString().length() < 2) {
                    Toast.makeText(getActivity(), "Too short", Toast.LENGTH_LONG).show();
                } else {
                    new SearchContactTask(getActivity()).execute(etSearchCont.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getActivity().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
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