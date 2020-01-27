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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.initial_joborder_fragment, container, false);

        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();

        if (bundle != null) {

            if (getActivity() != null) {
                getActivity().setTitle("");
            } else {
                Util.longToast(getContext(),
                        "Activity is null. Cannot set title of this fragment.");
            }

            RecyclerView initJoRecView = v.findViewById(R.id.rvInitialJOInfo);
            ArrayList<KeyValueInfo> initJoKeyVal = new ArrayList<>();

            try {

                JSONObject initJo = new JSONObject(bundle.getString("quotation"));
                int initJoId = initJo.getInt("initialJoborderId");

                initJoKeyVal.add(new KeyValueInfo("Status", getStatus(initJo.getInt("isAdded"))));
                initJoKeyVal.add(new KeyValueInfo("JO Number", initJo.getString("joNumber")));
                initJoKeyVal.add(new KeyValueInfo("Customer", initJo.getString("customer")));
                initJoKeyVal.add(new KeyValueInfo("PO Date", initJo.getString("poDate")));

                String po = initJo.getString("purchaseOrder");
                String mobile = initJo.getString("mobile");
                String refNo = initJo.getString("referenceNo");
                String model = String.valueOf(initJo.getInt("model"));
                String make = initJo.getString("make");
                String cat = initJo.getString("category");

                if (po.equals("0")) {
                    po = "- - - - - -";
                }
                if (mobile.equals("0")) {
                    mobile = "- - - - - -";
                }
                if (refNo.isEmpty() || refNo.equals("0")) {
                    refNo = "- - - - - -";
                }
                if (model.equals("0")) {
                    model = make = cat = "- - - - - -";
                }

                initJoKeyVal.add(new KeyValueInfo("Purchase Order", po));
                initJoKeyVal.add(new KeyValueInfo("Mobile", mobile));
                initJoKeyVal.add(new KeyValueInfo("Reference No", refNo));
                initJoKeyVal.add(new KeyValueInfo("Engine Model", model));
                initJoKeyVal.add(new KeyValueInfo("Engine Make", make));
                initJoKeyVal.add(new KeyValueInfo("Engine Category", cat));
                initJoKeyVal.add(new KeyValueInfo("Serial No", initJo.getString("serialNo")));
                initJoKeyVal.add(new KeyValueInfo("Date Received",
                        initJo.getString("dateReceived")));
                initJoKeyVal.add(new KeyValueInfo("Date Committed",
                        initJo.getString("dateCommitted")));
                initJoKeyVal.add(new KeyValueInfo("Remarks",
                        Util.filterSpecialChars(initJo.getString("remarks"))));
                initJoKeyVal.add(new KeyValueInfo("View Image", "Tap to view image"));
                initJoKeyVal.add(new KeyValueInfo("View Signature", "Tap to view signature"));

                InitialJoborderAdapter initialJoborderAdapter = new InitialJoborderAdapter(
                        getActivity(), initJoKeyVal, initJoId);

                initJoRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
                initJoRecView.setAdapter(initialJoborderAdapter);
                initJoRecView.setItemAnimator(new DefaultItemAnimator());

                initJoRecView.addItemDecoration(new DividerItemDecoration(getActivity(),
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
        } else {
            Util.alertBox(getActivity(), "Bundle is null. Cannot get data of initial joborder.");
        }

        return v;
    }

    private String getStatus(int isAdded) {
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
                    Util.shortToast(getContext(), "Empty");
                    return false;
                }

                if (getContext() != null) {
                    AlertDialog.Builder aBox = new AlertDialog.Builder(getContext());
                    aBox.setTitle("Confirm");
                    aBox.setMessage("Do you really want to delete joborder?");
                    aBox.setCancelable(false);

                    aBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String initialJoId = String.valueOf(
                                        objects.getInt("initialJoborderId"));
                                new DeleteInitialJoborderTask(getContext()).execute(initialJoId);
                            } catch (JSONException je) {
                                Util.alertBox(getContext(), je.toString());
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

                } else {
                    Util.alertBox(getActivity(), "Context is null. Cannot open dialog");
                }

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

                        if (getFragmentManager() != null) {
                            getFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter, R.anim.exit,
                                            R.anim.pop_enter, R.anim.pop_exit)
                                    .replace(R.id.content_main, updateInitialJOFragment)
                                    .addToBackStack(null) .commit();
                        } else {
                            Util.longToast(getActivity(),
                                    "Fragment Manager is null. Cannot update joborder");
                        }
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
                    Util.shortToast(getContext(), "Empty");
                    return false;
                }

                if (getContext() != null) {
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
                                Util.alertBox(getContext(), je.getMessage());
                            }
                        }
                    });

                    aBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    aBox.create().show();
                } else {
                    Util.alertBox(getActivity(), "Activity is null. Cannot open dialog.");
                }

                return false;
            }
        });
    }
}