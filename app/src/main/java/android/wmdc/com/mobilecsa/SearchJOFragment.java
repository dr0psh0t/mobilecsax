package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchJOTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class SearchJOFragment extends Fragment {

    private Button btnSearchJO;
    private EditText etSearchJO;
    private ProgressBar progressBarSearchJo;
    private SearchJOTask searchJOTask;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.search_jo_fragment, container, false);
        getActivity().setTitle("Search Job Order");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        btnSearchJO = v.findViewById(R.id.btnSearchJO);

        etSearchJO = v.findViewById(R.id.etSearchJO);
        progressBarSearchJo = v.findViewById(R.id.progressBarSearchJo);

        v.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (searchJOTask != null) {
                    if (!searchJOTask.isCancelled()) {
                        progressBarSearchJo.setVisibility(View.GONE);
                        searchJOTask.cancel(true);
                        btnSearchJO.setEnabled(true);
                    }
                }
            }
        });

        etSearchJO.setFilters(new InputFilter[] {
                new EmojiExcludeFilter(),
                new InputFilter.LengthFilter(16)
        });

        btnSearchJO.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String query = etSearchJO.getText().toString();
                String cid = String.valueOf(sharedPreferences.getInt("csaId", 0));

                if (query.length() < 3) {
                    Toast.makeText(getActivity(), "Too short", Toast.LENGTH_LONG).show();
                } else {
                    progressBarSearchJo.setVisibility(View.VISIBLE);
                    searchJOTask = new SearchJOTask(getActivity(), progressBarSearchJo,
                            btnSearchJO);
                    Util.minKey(getContext());

                    try {
                        Integer.parseInt(query);
                        searchJOTask.execute(cid, "1", query);
                    } catch (NumberFormatException nfe) {
                        searchJOTask.execute(cid, "2", query);
                    }
                }
            }
        });

        return v;
    }
}