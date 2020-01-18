package android.wmdc.com.mobilecsa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.adapter.InitialJoborderAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.DeleteInitialJoborderTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.UpdateInitialJoborderTransferredTask;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**Created by wmdcprog on 6/7/2018.*/

public class InitialJoborderFragment extends Fragment {

    private InitialJoborderAdapter initialJoborderAdapter;
    private RecyclerView initialJoborderRecyclerView;
    private ArrayList<KeyValueInfo> initialJoKeyValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.initial_joborder_fragment, container, false);
        Bundle bundle = this.getArguments();
        setHasOptionsMenu(true);
        getActivity().setTitle("");

        initialJoborderRecyclerView = v.findViewById(R.id.rvInitialJOInfo);
        initialJoKeyValue = new ArrayList<>();

        try {

            JSONObject initJo = new JSONObject(bundle.getString("quotation"));
            int initJoId = initJo.getInt("initialJoborderId");

            initialJoKeyValue.add(new KeyValueInfo("Status", getStatus(initJo.getInt("isAdded"))));
            initialJoKeyValue.add(new KeyValueInfo("JO Number", initJo.getString("joNumber")));
            initialJoKeyValue.add(new KeyValueInfo("Customer", initJo.getString("customer")));
            initialJoKeyValue.add(new KeyValueInfo("PO Date", initJo.getString("poDate")));

            String po = initJo.getString("purchaseOrder");
            String mobile = initJo.getString("mobile");
            String refNo = initJo.getString("referenceNo");
            String model = String.valueOf(initJo.getInt("model"));
            String make = initJo.getString("make");
            String cat = initJo.getString("category");

            if (po.equals("0")) { po = "- - - - - -"; }
            if (mobile.equals("0")) { mobile = "- - - - - -"; }
            if (refNo.isEmpty() || refNo.equals("0")) { refNo = "- - - - - -"; }
            if (model.equals("0")) { model = make = cat = "- - - - - -"; }

            initialJoKeyValue.add(new KeyValueInfo("Purchase Order", po));
            initialJoKeyValue.add(new KeyValueInfo("Mobile", mobile));
            initialJoKeyValue.add(new KeyValueInfo("Reference No", refNo));
            initialJoKeyValue.add(new KeyValueInfo("Engine Model", model));
            initialJoKeyValue.add(new KeyValueInfo("Engine Make", make));
            initialJoKeyValue.add(new KeyValueInfo("Engine Category", cat));
            initialJoKeyValue.add(new KeyValueInfo("Serial No", initJo.getString("serialNo")));
            initialJoKeyValue.add(new KeyValueInfo("Date Received",
                    initJo.getString("dateReceived")));
            initialJoKeyValue.add(new KeyValueInfo("Date Committed",
                    initJo.getString("dateCommitted")));
            initialJoKeyValue.add(new KeyValueInfo("Remarks",
                    Util.filterSpecialChars(initJo.getString("remarks"))));
            initialJoKeyValue.add(new KeyValueInfo("View Image", "Tap to view image"));
            initialJoKeyValue.add(new KeyValueInfo("View Signature", "Tap to view signature"));

            initialJoborderAdapter = new InitialJoborderAdapter(getActivity(), initialJoKeyValue,
                    initJoId);

            initialJoborderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            initialJoborderRecyclerView.setAdapter(initialJoborderAdapter);
            initialJoborderRecyclerView.setItemAnimator(new DefaultItemAnimator());

            initialJoborderRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                    LinearLayoutManager.VERTICAL));

            objects.put("initialJoborderId", initJoId);
            objects.put("purchaseOrder", initJo.getString("purchaseOrder"));
            objects.put("referenceNo", initJo.getString("referenceNo"));
            objects.put("mobile", initJo.getString("mobile"));
            objects.put("poDate", initJo.getString("poDate"));
            objects.put("serialNo", initJo.getString("serialNo"));
            objects.put("dateReceive", initJo.getString("dateReceived"));
            objects.put("dateCommit", initJo.getString("dateCommitted"));
            objects.put("remarks", Util.filterSpecialChars(initJo.getString("remarks")));
            objects.put("model", String.valueOf(initJo.getInt("model")));
            objects.put("cat", initJo.getString("category"));
            objects.put("make", initJo.getString("make"));
            objects.put("joNumber", initJo.getString("joNumber"));
            objects.put("customerId", initJo.getInt("customerId"));
            objects.put("customer", initJo.getString("customer"));
            objects.put("source", initJo.getString("source"));

        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                    "json_exception", je.toString());
            Util.alertBox(getActivity(), je.getMessage(), "Error", false);
        }

        return v;
    }

    public String getStatus(int isAdded) {
        if (isAdded == 0) {
            return "Pending";
        } else if (isAdded == 1) {
            return "Accepted";
        } else {
            return "Declined";
        }
    }

    private JSONObject objects = new JSONObject();

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem backNavAction = menu.findItem(R.id.action_back_nav);
        backNavAction.setVisible(false);

        MenuItem editAction = menu.findItem(R.id.action_edit);
        editAction.setVisible(true);

        MenuItem transferAction = menu.findItem(R.id.action_transfer);
        transferAction.setVisible(true);

        MenuItem deleteAction = menu.findItem(R.id.action_delete);
        deleteAction.setVisible(true);

        deleteAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (objects.toString().isEmpty()) {
                    Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
                    return false;
                }

                AlertDialog.Builder aBox = new AlertDialog.Builder(getContext());
                aBox.setTitle("Confirm");
                aBox.setMessage("Do you really want to delete joborder?");
                aBox.setCancelable(false);

                aBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String initialJoId = String.valueOf(objects.getInt("initialJoborderId"));
                            new DeleteInitialJoborderTask(getContext()).execute(initialJoId);
                        } catch (JSONException je) {
                            Util.alertBox(getContext(), je.toString(), "Error JSON", false);
                        }
                    }
                });

                aBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                aBox.create().show();

                return false;
            }
        });

        editAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                final UpdateInitialJOFragment updateInitialJOFragment =
                        new UpdateInitialJOFragment();

                Bundle bundle = new Bundle();
                bundle.putString("prevValues", objects.toString());

                updateInitialJOFragment.setArguments(bundle);

                final ProgressDialog progress = new ProgressDialog(getActivity());
                progress.setTitle("Loading");
                progress.setMessage("Please wait...");
                progress.setCancelable(false);
                progress.show();
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        progress.cancel();

                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                                        R.anim.pop_exit)
                                .replace(R.id.content_main, updateInitialJOFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                };

                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 1000);

                return false;
            }
        });

        transferAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (objects.toString().isEmpty()) {
                    Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
                    return false;
                }

                AlertDialog.Builder aBox = new AlertDialog.Builder(getContext());
                aBox.setTitle("Confirm");
                aBox.setMessage("Confirm Transfer?");
                aBox.setCancelable(false);

                aBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            new UpdateInitialJoborderTransferredTask(getContext()).execute(
                                    String.valueOf(objects.getInt("initialJoborderId")));
                        } catch (JSONException je) {
                            Util.alertBox(getContext(), je.getMessage(), "Error JSON", false);
                        }
                    }
                });

                aBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
                });

                aBox.create().show();
                return false;
            }
        });
    }
}