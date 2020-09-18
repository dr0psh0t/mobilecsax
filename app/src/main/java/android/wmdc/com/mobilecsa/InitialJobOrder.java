package android.wmdc.com.mobilecsa;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.adapter.CustomerJOAdapter;
import android.wmdc.com.mobilecsa.adapter.EngineJOAdapter;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageUriTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogSignatureTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchCustomerTaskFromJO;
import android.wmdc.com.mobilecsa.asynchronousclasses.SearchEngineTaskFromJO;
import android.wmdc.com.mobilecsa.model.CustomerJO;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;
import android.wmdc.com.mobilecsa.model.Engine;
import android.wmdc.com.mobilecsa.model.Signature;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/*** Created by wmdcprog on 4/27/2018.*/

public class InitialJobOrder extends Fragment {

    private TextView textViewCustomerId;
    private TextView textViewModelId;
    private TextView textViewSource;

    private Calendar calendar = Calendar.getInstance();
    private SharedPreferences sharedPreferences;

    private int year = calendar.get(Calendar.YEAR);
    private int month = calendar.get(Calendar.MONTH);
    private int day = calendar.get(Calendar.DAY_OF_MONTH);

    private EditText etCustomer;
    private EditText etPODate;
    private EditText etDateReceive;
    private EditText etEngineModel;
    private EditText etMakeCat;
    private EditText etMobile;
    private EditText etPurchaseOrder;
    private EditText etRemarks;
    private EditText etRefNo;
    private EditText etSerialNo;
    private EditText etJONumber;

    private TextView tvPhotoName, tvPhotoSize;

    private Dialog signatureDialog;
    private View signatureview;
    private Signature mSignature;
    private String joborderSignature;

    private RecyclerView recyclerViewEngine, recyclerViewCustomer;
    private EngineJOAdapter engineJOAdapter;
    private CustomerJOAdapter customerJOAdapter;

    private View.OnClickListener engineModelClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            if (getActivity() != null) {

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);

                View searchView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.engine_recycler_layout, containerViewGroup, false);

                recyclerViewEngine = searchView.findViewById(R.id.recyclerViewEngine);

                final ArrayList<Engine> engineList = new ArrayList<>();

                EditText editTextSearchEngine = searchView.findViewById(R.id.editTextSearchEngine);
                editTextSearchEngine.setFilters(new InputFilter[]{new EmojiExcludeFilter(),
                        new InputFilter.LengthFilter(16)});

                editTextSearchEngine.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence query, int start, int before, int count)
                    {
                        query = query.toString().toLowerCase();
                        String trimmedQuery = query.toString().trim();

                        if (trimmedQuery.length() < 1) {
                            engineList.clear();

                            if (engineJOAdapter != null) {
                                engineJOAdapter.notifyDataSetChanged();
                            }
                        } else {
                            engineList.clear();

                            engineJOAdapter = new EngineJOAdapter(engineList, getActivity(),
                                    etEngineModel, etMakeCat, dialog, textViewModelId);

                            new SearchEngineTaskFromJO(getActivity(), engineList,
                                    recyclerViewEngine, engineJOAdapter).execute(trimmedQuery);
                        }

                        if (trimmedQuery.contains("none")) {
                            engineList.clear();

                            engineJOAdapter = new EngineJOAdapter(engineList, getActivity(),
                                    etEngineModel, etMakeCat, dialog, null);

                            engineList.add(new Engine(0, 0, 0, "0", "0", "0"));

                            recyclerViewEngine.setLayoutManager(new LinearLayoutManager(
                                    getContext()));
                            recyclerViewEngine.setAdapter(engineJOAdapter);
                            recyclerViewEngine.setItemAnimator(new DefaultItemAnimator());
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                dialog.setContentView(searchView);

                if (dialog.getWindow() != null) {
                    WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

                    wmlp.width = Util.systemWidth;
                    wmlp.gravity = Gravity.TOP;
                    wmlp.y = 200;

                    dialog.show();
                } else {
                    Util.alertBox(getActivity(),
                            "Activity is null. Cannot set layout params to dialog.");
                }
            } else {
                Util.alertBox(getActivity(), "Activity is null. Cannot open engine model dialog.");
            }
        }
    };

    private View.OnClickListener customerClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            if (getActivity() != null) {
                final Dialog dialog = new Dialog(getActivity());

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);

                View searchView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.searchable_recycler_layout, containerViewGroup, false);

                recyclerViewCustomer = searchView.findViewById(R.id.recyclerViewCustomer);

                final ArrayList<CustomerJO> customerList = new ArrayList<>();

                EditText editText = searchView.findViewById(R.id.editTextSearchCustomer);
                editText.setFilters(new InputFilter[]{ new EmojiExcludeFilter(),
                        new InputFilter.LengthFilter(16)});

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence query, int start, int before, int count)
                    {
                        query = query.toString().toLowerCase();
                        String trimmedQuery = query.toString().trim();

                        if (trimmedQuery.length() < 1) {
                            customerList.clear();

                            if (customerJOAdapter != null) {
                                customerJOAdapter.notifyDataSetChanged();
                            }

                        } else if (trimmedQuery.length() >= 2) {
                            customerList.clear();

                            if (customerJOAdapter != null) {
                                customerJOAdapter.notifyDataSetChanged();
                            }

                            customerJOAdapter = new CustomerJOAdapter(customerList, getActivity(),
                                    etCustomer, dialog, textViewCustomerId, textViewSource);

                            new SearchCustomerTaskFromJO(getActivity(), customerList,
                                    recyclerViewCustomer, customerJOAdapter).execute(trimmedQuery);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                dialog.setContentView(searchView);

                if (dialog.getWindow() != null) {
                    WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                    wmlp.width = Util.systemWidth;
                    wmlp.gravity = Gravity.TOP;
                    wmlp.y = 200;

                    dialog.show();
                } else {
                    Util.alertBox(getActivity(),
                            "Activity is null. Cannot set layout params to dialog.");
                }
            } else {
                Util.alertBox(getActivity(), "Activity is null. Cannot open customer dialog.");
            }
        }
    };

    private ViewGroup containerViewGroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.initial_joborder, container, false);

        containerViewGroup = container;

        if (getActivity() != null) {
            getActivity().setTitle("Initial Job Order");
        } else {
            Util.alertBox(getContext(), "Activity is null. Cannot set title of this fragment.");
        }

        textViewCustomerId = v.findViewById(R.id.textViewCustomerId);
        textViewModelId = v.findViewById(R.id.textViewModelId);
        textViewSource = v.findViewById(R.id.textViewSource);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        etCustomer = v.findViewById(R.id.etCustomerJO);
        etPODate = v.findViewById(R.id.etPODate);
        etDateReceive = v.findViewById(R.id.etDateReceive);
        etEngineModel = v.findViewById(R.id.etEngineModel);
        etMakeCat = v.findViewById(R.id.etMakeCat);
        etMobile = v.findViewById(R.id.etMobile);
        etPurchaseOrder = v.findViewById(R.id.etPurchaseOrder);
        etRemarks = v.findViewById(R.id.etRemarks);
        etRefNo = v.findViewById(R.id.etRefNo);
        etSerialNo = v.findViewById(R.id.etSerialNo);
        etJONumber = v.findViewById(R.id.etJONumber);

        etRemarks.setScroller(new Scroller(getActivity()));
        etRemarks.setVerticalScrollBarEnabled(true);
        etRemarks.setMovementMethod(new ScrollingMovementMethod());
        //etRemarks.setOnTouchListener(etRemarksTouchListener);

        etPODate.setOnClickListener(datePOClickListener);
        etDateReceive.setOnClickListener(dateDRClickListener);
        etCustomer.setOnClickListener(customerClickListener);
        etEngineModel.setOnClickListener(engineModelClickListener);

        Button btnSubmit = v.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(submitListener);

        Button btnPhoto = v.findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(photoListener);

        Button btnPhotoPrev = v.findViewById(R.id.btnPhotoPrev);
        btnPhotoPrev.setOnClickListener(photoPrevListener);

        tvPhotoName = v.findViewById(R.id.tvPhotoName);
        tvPhotoSize = v.findViewById(R.id.tvPhotoSize);

        Button btnSign = v.findViewById(R.id.btnSign);
        btnSign.setOnClickListener(signatureClickListner);

        Button btnSignPrev = v.findViewById(R.id.btnSignPrev);
        btnSignPrev.setOnClickListener(signPrevListener);

        //Button btnCheckJo = v.findViewById(R.id.btnCheckJo);
        //btnCheckJo.setOnClickListener(checkJoListener);

        return v;
    }

    private View.OnClickListener signPrevListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (joborderSignature == null) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            }

            if (joborderSignature.isEmpty()) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            }

            new DialogSignatureTask(getActivity()).execute(joborderSignature);
        }
    };

    private View.OnClickListener photoPrevListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (displayName == null) {
                Util.shortToast(getActivity(), "Include photo.");
                return;
            }

            if (displayName.isEmpty()) {
                Util.shortToast(getActivity(), "Include photo.");
                return;
            }

            new DialogImageUriTask(getActivity()).execute(fileUri);
        }
    };

    private View.OnClickListener signatureClickListner = new View.OnClickListener() {
        public void onClick(View view) {
            displaySignatureDialog();
        }
    };

    private View.OnClickListener submitListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (is_submitted) {
                dumpImageMetaData(fileUri, true);
            } else {
                newQuotation();
            }
        }
    };

    private DatePickerDialog.OnDateSetListener datePOListener =
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            etPODate.setText(new StringBuilder().append(year).append("-").append(month+1)
                    .append("-").append(dayOfMonth));
        }
    };

    private DatePickerDialog.OnDateSetListener dateDRListener =
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            etDateReceive.setText(new StringBuilder().append(year).append("-").append(month + 1)
                    .append("-").append(dayOfMonth));
        }
    };

    private View.OnClickListener photoListener = new View.OnClickListener() {
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            View selectionView = inflater.inflate(R.layout.photo_select_layout, containerViewGroup,
                    false);

            Button btnCameraSelection = selectionView.findViewById(R.id.btnCameraSelection);
            Button btnGallerySelection = selectionView.findViewById(R.id.btnGallerySelection);

            if (getContext() != null) {
                final Dialog builder = new Dialog(getContext());

                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

                if (builder.getWindow() != null) {
                    builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                } else {
                    Util.alertBox(getActivity(),
                            "Builder Window is null. Cannot set background drawable of dialog.");
                }

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });

                builder.addContentView(selectionView, new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                builder.show();

                btnCameraSelection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dispatchTakePictureIntent();
                        builder.dismiss();
                    }
                });

                btnGallerySelection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performFileSearch();
                        builder.dismiss();
                    }
                });
            } else {
                Util.alertBox(getActivity(), "Context is null. Cannot show dialog.");
            }
        }
    };

    private static String displayName = null;
    private Uri fileUri = null;
    private InputStream fileInputStream = null;

    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (getActivity() != null) {
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;

                try {
                    photoFile = Util.createImageFile(getActivity());
                } catch (IOException ex) {
                    Util.displayStackTraceArray(ex.getStackTrace(), "android.wmdc.com.mobilecsa",
                            "IOException", ex.toString());

                    Util.shortToast(getContext(), ex.toString());
                }

                if (photoFile != null) {
                    fileUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID
                            + ".provider", photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } else {
                    Util.shortToast(getContext(), "\"photoFile\" is null");
                }
            } else {
                Util.alertBox(getActivity(), "Activity is null. Cannot take picture.");
            }
        } else {
            Util.alertBox(getActivity(),
                    "Resolve Activity of picture intent is null. Cannot take picture.");
        }
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                fileUri = resultData.getData();
                dumpImageMetaData(fileUri, false);
            } else {
                Util.shortToast(getContext(), "\"photoFile\" is null");
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            dumpImageMetaData(fileUri, false);
        }
    }

    private void dumpImageMetaData(final Uri uri, final boolean toSubmit) {
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

                            displayName = cursor.getString(cursor.getColumnIndex(
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
                            String small_file_size = "";

                            if (intSize > 512_000) {
                                File file = Util.createImageFile(getActivity());

                                Util.copyInputStreamToFile(Util.getStreamFromUri(uri,
                                        getActivity()), file, getContext());

                                File small_file = Util.reduceBitmapFile(file);

                                if (small_file != null) {
                                    small_file_size = small_file.length() + "";
                                    fileInputStream = new FileInputStream(small_file);
                                } else {
                                    Util.shortToast(getActivity(), "Small file is null.");
                                }
                            } else {
                                fileInputStream = Util.getStreamFromUri(uri, getActivity());
                                small_file_size = size;
                            }

                            pd.cancel();

                            if (toSubmit) {
                                newQuotation();
                            } else {
                                textPhoto = Integer.parseInt(size) / 1000 + "KB. " +
                                        Integer.parseInt(small_file_size) / 1000 + "KB.";

                                tvPhotoName.setText(displayName);
                                tvPhotoSize.setText(textPhoto);
                            }
                        }
                    } catch (IOException ie) {
                        pd.cancel();

                        Util.displayStackTraceArray(ie.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                                "IOException", ie.toString());

                        Util.alertBox(getActivity(), ie.toString());
                    }
                } else {
                    Util.alertBox(getActivity(), "Activity is null. Cannot dump image meta data.");
                }
            }
        };

        Handler pdHandler = new Handler();
        pdHandler.postDelayed(pdRun, 2000);
    }

    private String textPhoto;

    private void newQuotation() {

        try {
            int customerId = Integer.parseInt(textViewCustomerId.getText().toString());
            int modelId = Integer.parseInt(textViewModelId.getText().toString());

            if (displayName == null) {
                Util.shortToast(getActivity(), "Include photo.");
                return;
            } else {
                if (displayName.isEmpty()) {
                    Util.shortToast(getActivity(), "Include photo.");
                    return;
                }
            }

            if (joborderSignature == null) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            } else {
                if (joborderSignature.isEmpty()) {
                    Util.shortToast(getActivity(), "Include signature.");
                    return;
                }
            }

            String source = textViewSource.getText().toString();
            String customer = etCustomer.getText().toString();
            String mobile = etMobile.getText().toString();
            String purchaseOrder = etPurchaseOrder.getText().toString();
            String poDate = etPODate.getText().toString();
            String serialNo = etSerialNo.getText().toString();
            String dateReceive = etDateReceive.getText().toString();
            String refNo = etRefNo.getText().toString();
            String remarks = etRemarks.getText().toString();
            String joSignature = joborderSignature;
            String joNumber = etJONumber.getText().toString();
            String engineModel = etEngineModel.getText().toString();
            String makeCat = etMakeCat.getText().toString();
            String make;
            String cat;

            if (makeCat.isEmpty()) {
                Util.shortToast(getActivity(), "Engine Model required.");
                return;
            } else {
                String[] splitMakeCat = etMakeCat.getText().toString().split("/");
                make = splitMakeCat[0];
                cat = splitMakeCat[1];
            }

            if (joNumber.isEmpty()) {
                Util.shortToast(getActivity(), "Include JO Number.");
                return;
            }

            if (customer.isEmpty()) {
                Util.shortToast(getActivity(), "Customer is required.");
                return;
            }

            if (refNo.isEmpty()) {
                Util.shortToast(getActivity(), "Reference number is required.");
                return;
            }

            if (purchaseOrder.isEmpty()) {
                Util.shortToast(getActivity(), "Purchase Order is required.");
                return;
            }

            if (customerId < 0) {
                Util.shortToast(getActivity(), "Customer is required.");
                return;
            }

            if (modelId < 0) {
                Util.shortToast(getActivity(), "Model is required.");
                return;
            }

            if (mobile.isEmpty()) {
                Util.shortToast(getActivity(), "Mobile is required");
                return;
            }

            if (serialNo.isEmpty()) {
                Util.shortToast(getActivity(), "Serial Number is required");
                return;
            }

            if (remarks.isEmpty()) {
                Util.shortToast(getActivity(), "Remarks is required");
                return;
            }

            if (poDate.isEmpty()) {
                Util.shortToast(getActivity(), "PO date is required");
                return;
            }

            if (dateReceive.isEmpty()) {
                Util.shortToast(getActivity(), "Date Receive is required");
                return;
            }

            if (engineModel.isEmpty()) {
                Util.shortToast(getActivity(),
                        "Include Engine Model or \ntype \"none\" for machining.");
                return;
            }

            if (!joNumber.equals(refNo)) {
                Util.shortToast(getActivity(),
                        "Reference number should be the same with jo number.");
                return;
            }

            remarks = Util.filterSpecialChars(remarks);

            final HashMap<String, String> params = new HashMap<>();

            params.put("customerId", String.valueOf(customerId));
            params.put("customer", customer);
            params.put("source", source);
            params.put("mobile", mobile);
            params.put("purchaseOrder", purchaseOrder);
            params.put("poDate", poDate);
            params.put("make", make);
            params.put("cat", cat);
            params.put("modelId", String.valueOf(modelId));
            params.put("serialNo", serialNo);
            params.put("dateReceive", dateReceive);
            params.put("dateCommit", dateReceive);
            params.put("refNo", refNo);
            params.put("remarks", remarks);
            params.put("preparedBy", String.valueOf(sharedPreferences.getInt("csaId", 0)));
            params.put("imageType", "JPEG");
            params.put("joSignature", joSignature);
            params.put("joNumber", joNumber);

            if (getActivity() != null) {
                AlertDialog.Builder confirmBox = new AlertDialog.Builder(getActivity());

                confirmBox.setMessage("Confirm to add job order");
                confirmBox.setTitle("Confirm");

                confirmBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new InitialJoborderTask(getActivity(), fileInputStream, params).execute();
                    }
                });

                confirmBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                confirmBox.show();
            } else {
                Util.alertBox(getContext(), "Activity is null. Cannot build Alert Dialog.");
            }
        } catch (Exception e) {
            Util.alertBox(getContext(), e.toString());
        }
    }

    private static boolean is_submitted = false;

    private static class InitialJoborderTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private HttpURLConnection conn = null;

        private InputStream fileStream;

        private HashMap<String, String> parameters;

        private InitialJoborderTask(FragmentActivity activity, InputStream fileStream,
                                    HashMap<String, String> parameters) {

            activityWeakReference = new WeakReference<>(activity);
            progressDialog = new ProgressDialog(activity);
            taskPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            this.fileStream = fileStream;
            this.parameters = parameters;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("\tSaving. Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(taskPrefs.getString("domain", null)+"initialjoborder");

                String lineEnd = "\r\n";
                String twoHypens = "--";
                String boundary = "*****";

                int bytesRead;
                int bytesAvailable;
                int bufferSize;
                int maxBufferSize = 1024 * 1024;

                byte[] buffer;

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(Util.READ_TIMEOUT);
                conn.setConnectTimeout(Util.CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                conn.setRequestProperty("Cookie",
                        "JSESSIONID="+taskPrefs.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "newquotation-android");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes(twoHypens+boundary+lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name\"reference\""+
                        lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes("my_reference_text");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHypens+boundary+lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; " +
                        "name=\"photo\";filename\""+displayName+"\""+lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                for (Map.Entry<String, String> mapEntry : parameters.entrySet()) {

                    String key = mapEntry.getKey();
                    String value = mapEntry.getValue();

                    outputStream.writeBytes(twoHypens+boundary+lineEnd);
                    outputStream.writeBytes("Content-Disposition: " +
                            "form-data; name=\""+key+"\""+lineEnd);
                    outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }

                outputStream.writeBytes(twoHypens+boundary+twoHypens+lineEnd);
                int statusCode = conn.getResponseCode();

                outputStream.flush();
                outputStream.close();

                if (fileStream != null) {
                    fileStream.close();
                }

                is_submitted = true;

                if (statusCode == 200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    String inputLine;

                    while ((inputLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }

                    inputStream.close();
                    bufferedReader.close();

                    if (stringBuilder.toString().isEmpty()) {
                        return "{\"success\": false, \"reason\": \"No response from server.\"}";
                    } else {
                        return stringBuilder.toString();
                    }
                } else {
                    return "{\"success\": false, \"reason\": \"Request did not succeed. " +
                            "Status Code: "+statusCode+"\"}";
                }
            } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "network_exception", e.toString());

                if (e instanceof MalformedURLException) {
                    return "{\"success\": false, \"reason\": \"Malformed URL.\"}";
                } else if (e instanceof ConnectException) {
                    return "{\"success\": false, \"reason\": \"Cannot connect to server. " +
                            "Check wifi or mobile data and check if server is available.\"}";
                } else {
                    return "{\"success\": false, \"reason\": \"Connection timed out. " +
                            "The server is taking too long to reply.\"}";
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                return "{\"success\": false, \"reason\": \""+e.getMessage()+"\"}";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            final FragmentActivity mainActivity = activityWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject response = new JSONObject(result);

                if (response.getBoolean("success")) {
                    AlertDialog.Builder warningBox = new AlertDialog.Builder(mainActivity);

                    warningBox.setTitle("Success");
                    warningBox.setMessage(response.getString("reason"));
                    warningBox.setCancelable(false);
                    warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            Util.handleBackPress(mainActivity.getSupportFragmentManager()
                                    .findFragmentById(R.id.content_main), mainActivity);
                        }
                    });
                    warningBox.create().show();
                } else {
                    Util.alertBox(mainActivity, response.getString("reason"));
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());

                Util.longToast(mainActivity, e.getMessage());
            }
        }
    }

    private View.OnClickListener datePOClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            if (getActivity() != null) {
                new DatePickerDialog(getActivity(), R.style.DialogTheme, datePOListener, year,
                        month, day).show();
            } else {
                Util.alertBox(getActivity(), "Activity is null. Cannot open date.");
            }
        }
    };

    private View.OnClickListener dateDRClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            if (getActivity() != null) {
                new DatePickerDialog(getActivity(), R.style.DialogTheme, dateDRListener, year,
                        month, day).show();
            } else {
                Util.alertBox(getActivity(), "Activity is null. Cannot open date.");
            }
        }
    };

    private void displaySignatureDialog() {
        if (getActivity() != null) {
            signatureDialog = new Dialog(getActivity());

            signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            signatureDialog.setContentView(R.layout.dialog_signature);
            signatureDialog.setCancelable(false);

            LinearLayout mContent = signatureDialog.findViewById(R.id.linearLayout);
            mSignature = new Signature(getActivity().getApplicationContext(), null, mContent);
            mSignature.setBackgroundColor(Color.WHITE);

            mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            Button mClear = signatureDialog.findViewById(R.id.btnSignClear);
            Button mGetSign = signatureDialog.findViewById(R.id.btn_sign_save);
            Button mCancel = signatureDialog.findViewById(R.id.btnSignCancel);

            signatureview = mContent;

            mClear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSignature.clear();
                }
            });

            mGetSign.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    signatureview.setDrawingCacheEnabled(true);
                    joborderSignature = mSignature.save(signatureview);
                    signatureDialog.dismiss();
                }
            });

            mCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSignature.clear();
                    signatureDialog.dismiss();
                }
            });

            signatureDialog.show();
        } else {
            Util.alertBox(getActivity(), "Activity is null. Cannot open signature.");
        }
    }
}