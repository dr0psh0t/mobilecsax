package android.wmdc.com.mobilecsa;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.wmdc.com.mobilecsa.adapter.QCValueInfoAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetWorkOrderQCList;
import android.wmdc.com.mobilecsa.model.KeyValueInfo;
import android.wmdc.com.mobilecsa.model.SwipeButton;
import android.wmdc.com.mobilecsa.model.WorkOrderModel;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static android.app.Activity.RESULT_OK;

/**
 * Created by wmdcprog on 3/10/2018.
 */

public class QCJOInfoFragment extends Fragment {

    private final ArrayList<String> QC_KEY = new ArrayList<>(Arrays.asList("JO Number",
            "Customer ID", "Model", "Make", "Category", "Serial", "Date Received", "Date Committed",
            "View Image"));

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setLogo(R.drawable.ic_action_white_domain);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "FRAGMENT IS DESTROYED");
    }

    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.qc_jo_info_viewholder_layout, container, false);

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (getActivity() != null) {
            toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setLogo(R.drawable.ic_action_white_domain);
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot inflate toolbar.");
        }

        Bundle thisBundle = this.getArguments();

        if (thisBundle != null) {
            RecyclerView recyclerView = v.findViewById(R.id.rvQCInfo);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ArrayList<KeyValueInfo> qcInfos = new ArrayList<>();
            String result = thisBundle.getString("result");

            try {
                JSONObject resJson = new JSONObject(result);

                if (resJson.getBoolean("success")) {
                    getActivity().setTitle("  "+resJson.getString("customer"));

                    qcInfos.add(new KeyValueInfo(QC_KEY.get(0), resJson.getString("joNumber")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(1), resJson.getString("customerID")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(2), resJson.getString("model")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(3), resJson.getString("make")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(4), resJson.getString("category")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(5), resJson.getString("serial")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(6), resJson.getString("dateReceived")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(7), resJson.getString("dateCommit")));
                    qcInfos.add(new KeyValueInfo(QC_KEY.get(8), "Tap to view Image"));

                    QCValueInfoAdapter qcValueInfoAdapter = new QCValueInfoAdapter(getActivity(),
                            qcInfos, thisBundle.getInt("joid"));

                    recyclerView.setAdapter(qcValueInfoAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                            LinearLayoutManager.VERTICAL));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(resJson.getString("reason"));
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

            } catch (JSONException je) {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "JSONException", je.toString());

                Util.alertBox(getActivity(), je.getMessage());
            }

            RecyclerView workOrderRecyclerView = v.findViewById(R.id.rvItemsList);
            workOrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            ArrayList<WorkOrderModel> workOrderList = new ArrayList<>();

            new GetWorkOrderQCList(
                    getActivity(),
                    workOrderRecyclerView,
                    workOrderList,
                    QCJOInfoFragment.this).execute(
                    String.valueOf(sPrefs.getInt("csaId", 0)),
                    "mcsa",
                    String.valueOf(thisBundle.getInt("joid"))
            );
        }

        return v;
    }

    private String photoName;
    private Uri fileUri = null;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private InputStream fileInputStream;

    private ImageView[] iconItemDC = new ImageView[2];
    private int[] csaId = new int[2];
    private int[] joId = new int[2];
    private int[] workorderId = new int[2];
    private boolean[] isCsaQC = new boolean[2];

    public void dispatchTakePictureIntent(ImageView icon, int id, int jid, int woid,
                                          boolean isCsaQcLocal) {

        iconItemDC[1] = icon;
        csaId[1] = id;
        joId[1] = jid;
        workorderId[1] = woid;
        isCsaQC[1] = isCsaQcLocal;

        //  new workorder detected. so take picture first.
        if (workorderId[0] < 1 || workorderId[0] != workorderId[1]) {

            if (getActivity() != null) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;

                    try {
                        photoFile = Util.createImageFile(getActivity());

                    } catch (IOException ex) {
                        Util.displayStackTraceArray(ex.getStackTrace(),
                                "android.wmdc.com.mobilecsa", "IOException", ex.toString());

                        Util.shortToast(getContext(), ex.toString());
                    }

                    if (photoFile != null) {
                        fileUri = FileProvider.getUriForFile(getActivity(),
                                BuildConfig.APPLICATION_ID+ ".provider", photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                        System.out.println("TEST3");
                    }
                }
            } else {
                Util.alertBox(getContext(), "Activity is null. Cannot open camera.");
            }

        } else {    //  workorder has a picture already. proceed to swipe.
            showSwipe();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //System.out.println("TEST2");
            dumpImageMetaData(fileUri);
        }
    }

    private void dumpImageMetaData(final Uri uri) {
        final ProgressDialog pd = new ProgressDialog(getActivity());

        pd.setMessage("Dumping image. Please wait...");
        pd.setCancelable(false);
        pd.show();

        Runnable pdRun = new Runnable() {
            @Override
            public void run() {

                if (getActivity() != null) {

                    try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null,
                            null, null, null)) {

                        if (cursor != null && cursor.moveToFirst()) {

                            photoName = cursor.getString(cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME));

                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            String size;

                            if (!cursor.isNull(sizeIndex)) {
                                size = cursor.getString(sizeIndex);
                            } else {
                                Util.shortToast(getActivity(), "Unknown size.");
                                return;
                            }

                            int intSize = Integer.parseInt(size);

                            if (intSize > 512_000) {
                                File file = Util.createImageFile(getActivity());

                                Util.copyInputStreamToFile(Util.getStreamFromUri(uri,
                                        getActivity()), file, getContext());

                                File smallFile = Util.reduceBitmapFile(file);

                                if (smallFile != null) {
                                    fileInputStream = new FileInputStream(smallFile);
                                } else {
                                    Util.shortToast(getActivity(), "Small file is null.");
                                }
                            } else {
                                fileInputStream = Util.getStreamFromUri(uri, getActivity());
                            }

                            pd.cancel();

                            if (fileInputStream == null) {
                                System.err.println("The fileInputStream for workorder is null");
                            } else {
                                iconItemDC[0] = iconItemDC[1];
                                csaId[0] = csaId[1];
                                joId[0] = joId[1];
                                workorderId[0] = workorderId[1];
                                isCsaQC[0] = isCsaQC[1];

                                showSwipe();
                            }
                        }
                    } catch (IOException ie) {
                        pd.cancel();

                        Util.displayStackTraceArray(ie.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                                "IOException", ie.toString());

                        Util.shortToast(getContext(), ie.toString());
                    }
                } else {
                    Util.longToast(getContext(),
                            "\"getActivity()\" is null. Cannot dump image meta data.");
                }
            }
        };

        Handler pdHandler = new Handler();
        pdHandler.postDelayed(pdRun, 2000);
    }

    private void showSwipe() {

        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.slide_layout);

            SwipeButton swipeButton = dialog.findViewById(R.id.swipe_btn_qc);
            swipeButton.setItemStat(iconItemDC[0]);
            swipeButton.setDialogContainer(dialog);
            swipeButton.setFragmentActivity(getActivity());

            swipeButton.setParameters(csaId[0], "mcsa", joId[0], workorderId[0], fileInputStream,
                    photoName);

            if (!isCsaQC[0]) {
                dialog.show();
            }
        } else {
            Util.longToast(getContext(), "Activity is null. Cannot show dialog swipe.");
        }
    }
}