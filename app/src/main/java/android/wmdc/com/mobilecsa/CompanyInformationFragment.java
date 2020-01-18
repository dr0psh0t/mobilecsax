package android.wmdc.com.mobilecsa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.KeyValueInfoAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wmdcprog on 12/28/2017.
 */

public class CompanyInformationFragment extends Fragment {

    private final ArrayList<String> COLUMNS = new ArrayList<>(Arrays.asList(
            "Photo",
            "Company",
            "Address",
            "Location",
            "City",
            "Country",
            "Zip Code",
            "Telephone",
            "Fax",
            "Contact Person",
            "Contact Number",
            "Date Added",
            "CSA",
            "Signature"
    ));

    private final ArrayList<String> JSON_KEY = new ArrayList<>(Arrays.asList(
            "photo",
            "company",
            "address",
            "location",
            "city",
            "country",
            "zip",
            "telnum",
            "faxnum",
            "contactperson",
            "contactnumber",
            "dateadded",
            "assignedCsa",
            "signature"
    ));

    private final int LIST_LENGTH = COLUMNS.size();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.information_viewholder_layout, container, false);
        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvInformation);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> customerInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject object = new JSONObject(result);
                getActivity().setTitle(object.getString("company"));

                for (int i = 0; i < LIST_LENGTH; ++i) {
                    customerInfos.add(
                            new KeyValueInfo(COLUMNS.get(i),object.getString(JSON_KEY.get(i))));
                }

                KeyValueInfoAdapter customerInfoAdapter =
                        new KeyValueInfoAdapter(
                                getActivity(),
                                customerInfos,
                                object.getInt("customerId"),
                                true);

                recyclerView.setAdapter(customerInfoAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                        LinearLayoutManager.VERTICAL));

            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
            }
        }

        return v;
    }
}