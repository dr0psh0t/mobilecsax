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
import android.wmdc.com.mobilecsa.adapter.ContactsAdapter;
import android.wmdc.com.mobilecsa.model.Contacts;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wmdcprog on 12/29/2017.
 */

public class ContactsResultFragment extends Fragment {
    private final ArrayList<Contacts> contactPages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        final View v = inflater.inflate(R.layout.search_viewholder_layout, container, false);
        setHasOptionsMenu(true);

        if (getActivity() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    getActivity());

            final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            final RecyclerView recyclerView = v.findViewById(R.id.rvEntities);

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                    LinearLayoutManager.VERTICAL));

            Bundle bundle = this.getArguments();

            if (bundle != null) {
                String searchResult = bundle.getString("searchResult");
                contactPages.clear();

                try {
                    final JSONObject jsonbObject = new JSONObject(searchResult);
                    final JSONArray jsonArray = jsonbObject.getJSONArray("result");
                    final JSONArray infoArray = jsonbObject.getJSONArray("info");
                    final JSONObject infoObj = infoArray.getJSONObject(0);
                    int arrayLength = jsonArray.length();

                    Variables.headerTitle = Html.fromHtml("\"" + infoObj.getString("wordQuery")
                            +" <i><small>"+infoObj.getString("searchCount")+" results</small></i>");
                    getActivity().setTitle(Variables.headerTitle);

                    if (arrayLength > 0) {

                        for (int i = 0; i < arrayLength; ++i) {
                            JSONObject itemObj = jsonArray.getJSONObject(i);

                            contactPages.add(new Contacts(itemObj.get("label").toString(),
                                    sharedPreferences.getString("domain", null)+itemObj.get("src"),
                                    itemObj.get("salesman").toString(), 23,
                                    Integer.parseInt(itemObj.get("id").toString()),
                                    itemObj.getInt("isTransferred")));
                        }

                        ContactsAdapter contactsAdapter = new ContactsAdapter(getActivity(),
                                contactPages);
                        recyclerView.setAdapter(contactsAdapter);

                    } else {
                        Util.alertBox(getContext(), "Empty List");
                    }

                } catch (JSONException je) {
                    Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                            "json_exception", je.toString());

                    Util.alertBox(getActivity(), "Parse error");
                }
            }

        } else {
            Util.alertBox(getContext(), "Error");
            Log.e("Null", "Activity is null. Cannot load search result");
        }

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchAction = menu.findItem(R.id.action_search);
        searchAction.setVisible(true);
    }
}