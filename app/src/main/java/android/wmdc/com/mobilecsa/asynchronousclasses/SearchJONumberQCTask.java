package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.wmdc.com.mobilecsa.QualityCheckFragment;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**Created by wmdcprog on 5/18/2018.*/

public class SearchJONumberQCTask extends AsyncTask<String, String, JSONObject> {

    private WeakReference<FragmentActivity> weakReference;

    private ProgressDialog progressDialog;

    public SearchJONumberQCTask(FragmentActivity activity) {
        this.weakReference = new WeakReference<>(activity);
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String[] params) {
        try {
            JSONArray objectArray = Variables.qcStore.getJSONArray("joborders");
            int len = objectArray.length();
            JSONObject obj;

            if (Boolean.parseBoolean(params[1])) {
                for (int i = 0; i < len; ++i) {
                    obj = objectArray.getJSONObject(i);
                    if (obj.getString("joNum").equals(params[0])) {
                        JSONObject customer = objectArray.getJSONObject(i);
                        ArrayList<JSONObject> joborders = new ArrayList<>();

                        joborders.add(customer);
                        JSONObject finalObj = new JSONObject();

                        finalObj.put("success", true);
                        finalObj.put("joborders", new JSONArray(joborders));

                        return finalObj;
                    }
                }

                obj = new JSONObject();
                obj.put("success", false);
                obj.put("reason", "No JO Number found.");

                return obj;
            } else {
                ArrayList<JSONObject> joborders = new ArrayList<>();
                for (int i = 0; i < len; ++i) {
                    obj = objectArray.getJSONObject(i);
                    String customerStr = obj.getString("customer");

                    if (customerStr.length() > 2) {
                        if (params[0].length() <= customerStr.length()) {
                            if (customerStr.toLowerCase().substring(0, params[0].length())
                                    .contains(params[0].toLowerCase())) {

                                joborders.add(obj);
                            }
                        }
                    }
                }

                JSONObject finalObj = new JSONObject();

                if (!joborders.isEmpty()) {
                    finalObj.put("success", true);
                    finalObj.put("joborders", new JSONArray(joborders));
                    finalObj.put("reason", "Successful");
                    return finalObj;
                } else {
                    finalObj.put("success", false);
                    finalObj.put("reason", "No Job Order found.");
                    return finalObj;
                }
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
            return Variables.qcStore;
        }
    }

    @Override
    protected void onPostExecute(JSONObject obj) {
        progressDialog.dismiss();

        FragmentActivity mainActivity = weakReference.get();

        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }

        try {
            if (obj.getBoolean("success")) {
                QualityCheckFragment qcFrag = new QualityCheckFragment();
                Bundle bundle = new Bundle();

                bundle.putString("searchResult", obj.toString());
                qcFrag.setArguments(bundle);

                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                        R.anim.pop_exit);
                fragmentTransaction.replace(R.id.content_main, qcFrag);
                fragmentTransaction.commit();
            } else {
                Util.alertBox(mainActivity, obj.getString("reason"));
            }
        } catch (JSONException je) {
            Util.displayStackTraceArray(je.getStackTrace(), Variables.ASYNCHRONOUS_PACKAGE,
                    "JSONException", je.toString());
            Util.alertBox(mainActivity, je.toString());
        }
    }
}