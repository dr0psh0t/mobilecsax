package android.wmdc.com.mobilecsa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.CustomerAdapter;
import android.wmdc.com.mobilecsa.model.Customer;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 12/26/2017.
 */

public class CustomerResultFragment extends Fragment {

    TextView tvPage;
    ArrayList<Customer> customerPages = new ArrayList<>();

    CustomerAdapter customerAdapter;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.search_viewholder_layout, container, false);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle bundle = this.getArguments();

        tvPage = v.findViewById(R.id.tvPage);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        final RecyclerView recyclerView = v.findViewById(R.id.rvEntities);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL));

        if (bundle != null) {
            final String searchResult = bundle.getString("searchResult");

            try {

                final JSONObject jsonbObject = new JSONObject(searchResult);
                final JSONArray jsonArray = jsonbObject.getJSONArray("result");
                final JSONArray infoArray = jsonbObject.getJSONArray("info");
                final JSONObject infoObj = infoArray.getJSONObject(0);
                int arrayLength = jsonArray.length();

                Variables.headerTitle = Html.fromHtml("\""+infoObj.getString("wordQuery")+"\""+
                        " <i><small>"+infoObj.getString("searchCount")+" results</small></i>");

                getActivity().setTitle(Variables.headerTitle);

                for (int i = 0; i < arrayLength; i++) {
                    JSONObject itemObj = jsonArray.getJSONObject(i);

                    customerPages.add(new Customer(
                        itemObj.get("label").toString(),
                        sharedPreferences.getString("domain", null)+itemObj.get("src"),
                        itemObj.get("salesman").toString(),
                        23,
                        Integer.parseInt(itemObj.get("id").toString()),
                        Boolean.parseBoolean(itemObj.get("isAPerson").toString()),
                        itemObj.getInt("isTransferred")
                    ));
                }

                customerAdapter = new CustomerAdapter(getActivity(), customerPages);
                recyclerView.setAdapter(customerAdapter);

            } catch (Exception je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", je.toString());
            }
        } else {
            Util.alertBox(getContext(), "A bundle containing the result is empty", "", false);
        }

        return v;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}