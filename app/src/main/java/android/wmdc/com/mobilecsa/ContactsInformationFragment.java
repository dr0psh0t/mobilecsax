package android.wmdc.com.mobilecsa;

import android.content.DialogInterface;
import android.os.Bundle;
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

/**
 * Created by wmdcprog on 12/29/2017.
 */

public class ContactsInformationFragment extends Fragment {

    private final ArrayList<String> COLUMNS = new ArrayList<>(Arrays.asList(
            "Photo",
            "Lastname",
            "Firstname",
            "MI",
            "Address",
            "City",
            "Province",
            "Country",
            "Zip Code",
            "Location",
            "Industry",
            "Plant",
            "Date of Birth",
            "CSA",
            "Job Position",
            "Telephone",
            "Mobile",
            "Emergency Contact",
            "Signature",
            "ER",
            "MF",
            "Calib"
    ));

    private final ArrayList<String> JSON_KEY = new ArrayList<>(Arrays.asList(
            "photo",
            "lastname",
            "firstname",
            "mi",
            "address",
            "city",
            "province",
            "country",
            "zipCode",
            "location",
            "industry",
            "plantAssociated",
            "dateOfBirth",
            "csa",
            "jobPosition",
            "telNum",
            "mobile",
            "emergencyContact",
            "signature",
            "er",
            "mf",
            "calib"
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

            ArrayList<KeyValueInfo> contactInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject object = new JSONObject(result);
                if (object.getBoolean("success")) {
                    getActivity().setTitle(object.getString("firstname")+" "+object.getString("lastname"));

                    for (int i = 0; i < LIST_LENGTH; ++i) {
                        contactInfos.add(
                            new KeyValueInfo(COLUMNS.get(i), object.getString(JSON_KEY.get(i)))
                        );
                    }

                    KeyValueInfoAdapter customerInfoAdapter = new KeyValueInfoAdapter(
                        getActivity(),
                        contactInfos,
                        object.getInt("contactId"),
                        false
                    );

                    recyclerView.setAdapter(customerInfoAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(object.getString("reason"));
                    builder.setTitle("Error");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Util.handleBackPress(null, getContext());
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
                Util.alertBox(getActivity(), je.getMessage(), "JSON Exception", false);
            }
        }

        return v;
    }
}