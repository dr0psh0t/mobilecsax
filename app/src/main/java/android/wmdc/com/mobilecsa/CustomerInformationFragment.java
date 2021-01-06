package android.wmdc.com.mobilecsa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.wmdc.com.mobilecsa.adapter.KeyValueInfoAdapter;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**Created by wmdcprog on 12/28/2017.*/

public class CustomerInformationFragment extends Fragment {

    private final ArrayList<String> COLUMNS = new ArrayList<>(Arrays.asList(
            "Photo",
            "Lastname",
            "Firstname",
            "Address",
            "City",
            "Country",
            "Zip Code",
            "Location",
            "Telephone",
            "Fax",
            "Date of Birth",
            "Date Added",
            "CSA",
            "Signature"
    ));

    private final ArrayList<String> JSON_KEY = new ArrayList<>(Arrays.asList(
            "photo",   //  photo
            "lastname",
            "firstname",
            "address",
            "city",
            "country",
            "zip",
            "location",     //  location
            "telephone",
            "fax",
            "dateofbirth",
            "dateadded",
            "assignedCsa",
            "signature"            //  signature
    ));

    private final int LIST_LENGTH = COLUMNS.size();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.information_viewholder_layout, container, false);

        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvInformation);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> customerInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject object = new JSONObject(result);

                if (getActivity() != null) {
                    if (object.getBoolean("success")) {

                        getActivity().setTitle(object.getString("firstname") + " "
                                + object.getString("lastname"));

                        for (int i = 0; i < LIST_LENGTH; ++i) {
                            customerInfos.add(new KeyValueInfo(COLUMNS.get(i),
                                    object.getString(JSON_KEY.get(i))));
                        }

                        KeyValueInfoAdapter customerInfoAdapter = new KeyValueInfoAdapter(
                                getActivity(), customerInfos, object.getInt("customerId"), true);

                        recyclerView.setAdapter(customerInfoAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                LinearLayoutManager.VERTICAL));
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(object.getString("reason"));
                        builder.setTitle("Error");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getActivity() != null) {
                                    Util.handleBackPress(null, getActivity());
                                } else {
                                    Util.longToast(getContext(), "Activity is null.");
                                }
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                } else {
                    Util.alertBox(getContext(), "Cannot show customer information");
                    Log.e("Null", "getActivity() is null. Cannot show customer information");
                }

            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());
                Util.alertBox(getActivity(), "Parse error");
            }
        }

        return v;
    }
}