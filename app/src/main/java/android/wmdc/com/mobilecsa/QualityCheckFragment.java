package android.wmdc.com.mobilecsa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.QCAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetQCJOListTask;
import android.wmdc.com.mobilecsa.model.QCDataModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wmdcprog on 4/13/2018.
 */

public class QualityCheckFragment extends Fragment {

    private final ArrayList<QCDataModel> qcDataModels = new ArrayList<>();
    private QCAdapter qcAdapter;
    private JSONArray jsonArray;
    private SharedPreferences sp;

    @Override
    public void onResume() {
        super.onResume();
        //setPage(Variables.currentPage, tvPage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.quality_check_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Quality Check");
        } else {
            Util.shortToast(getContext(), "Title error");
            Log.e("Null", "Activity is null. Cannot set title of this fragment.");
        }

        TextView tvPage;
        RecyclerView recyclerView;

        setHasOptionsMenu(true);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        tvPage = v.findViewById(R.id.tvPage);
        tvPage.setText(String.valueOf(Variables.currentPage));

        recyclerView = v.findViewById(R.id.rvQCData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ImageButton next = v.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage < Variables.lastPage) {
                    new GetQCJOListTask(getActivity()).execute(String.valueOf(
                            sp.getInt("csaId", 0)), "mcsa", (++Variables.currentPage)+"");
                }
            }
        });

        ImageButton prev = v.findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Variables.currentPage > 1) {
                    new GetQCJOListTask(getActivity()).execute(String.valueOf(
                            sp.getInt("csaId", 0)), "mcsa", (--Variables.currentPage)+"");
                }
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            final String searchResult = bundle.getString("searchResult");

            try {
                final JSONObject jsonObject = new JSONObject(searchResult);
                jsonArray = jsonObject.getJSONArray("joborders");

                int jobordersArrayLength = jsonArray.length();

                if (jobordersArrayLength > 0) {

                    int i;
                    qcDataModels.clear();

                    for (i = 1; i <= jobordersArrayLength; ++i) {
                        JSONObject itemObj = jsonArray.getJSONObject(i - 1);

                        qcDataModels.add(new QCDataModel(
                            itemObj.getInt("joId"),
                            itemObj.getString("serialNum"),
                            itemObj.getString("dateCommit"),
                            itemObj.getString("dateReceived"),
                            itemObj.getString("joNum"),
                            itemObj.getString("customerId"),
                            itemObj.getString("model"),
                            itemObj.getString("category"),
                            itemObj.getString("make"),
                            itemObj.getString("customer"),
                            itemObj.getBoolean("isPending")
                        ));
                    }

                    //  sort list by number
                    Collections.sort(qcDataModels);

                    qcAdapter = new QCAdapter(qcDataModels, getActivity());
                    recyclerView.setAdapter(qcAdapter);

                } else {
                    Util.alertBox(getContext(), "Empty List");
                }
            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "json_exception", je.toString());
            }
        }

        return v;
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        MenuItem refreshAction = menu.findItem(R.id.action_refresh);
        refreshAction.setVisible(true);
        refreshAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GetQCJOListTask(getActivity()).execute(String.valueOf(sp.getInt("csaId", 0)),
                        "mcsa", Variables.currentPage+"");
                return true;
            }
        });

        MenuItem searchAction2 = menu.findItem(R.id.action_search2);
        searchAction2.setVisible(true);
        searchAction2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View mView = layoutInflater.inflate(R.layout.user_input_dialog, null);

                AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
                adBuilder.setView(mView);

                final EditText editText = mView.findViewById(R.id.userInputDialog);
                adBuilder.setCancelable(true);
                adBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String joQuery = editText.getText().toString();
                        int length = jsonArray.length();
                        int x;
                        JSONObject object;

                        ArrayList<QCDataModel> qcDataModels2 = new ArrayList<>();

                        for (x = 0; x < length; ++x) {
                            try {
                                object = jsonArray.getJSONObject(x);

                                if (object.getString("joNum").equals(joQuery)) {
                                    //System.out.println(object.getString("joNum")+"-"+(joQuery));

                                    qcDataModels2.add(new QCDataModel(
                                        object.getInt("joId"),
                                        object.getString("serialNum"),
                                        object.getString("dateCommit"),
                                        object.getString("dateReceived"),
                                        object.getString("joNum"),
                                        object.getString("customerId"),
                                        object.getString("model"),
                                        object.getString("category"),
                                        object.getString("make"),
                                        object.getString("customer"),
                                        object.getBoolean("isPending")
                                    ));

                                    break;
                                }

                            } catch (JSONException je) {
                                Util.longToast(getContext(), "Parse error");
                                break;
                            }
                        }

                        if (!qcDataModels2.isEmpty()) {
                            qcDataModels.clear();
                            qcDataModels.addAll(qcDataModels2);
                            qcAdapter.notifyDataSetChanged();
                        } else {
                            Util.longToast(getContext(), joQuery+" not found. Try other pages.");
                        }
                    }
                });

                adBuilder.create().show();

                return true;
            }
        });

        MenuItem actionBackNav = menu.findItem(R.id.action_back_nav);
        actionBackNav.setVisible(false);
    }
}