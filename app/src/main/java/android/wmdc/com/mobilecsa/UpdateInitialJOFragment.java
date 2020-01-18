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
import android.view.MotionEvent;
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
import android.widget.Toast;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import static android.app.Activity.RESULT_OK;

/**
 * Created by wmdcprog on 7/30/2018.
 */

public class UpdateInitialJOFragment extends Fragment {

    Calendar calendar = Calendar.getInstance();

    private int year = calendar.get(Calendar.YEAR);
    private int month = calendar.get(Calendar.MONTH);
    private int day = calendar.get(Calendar.DAY_OF_MONTH);

    private EditText etCustomer;
    private EditText etPODate;
    private EditText etDateReceive;
    private EditText etDateCommit;
    private EditText etEngineModel;
    private EditText etMakeCat;
    private EditText etMobile;
    private EditText etPurchaseOrder;
    private EditText etRemarks;
    private EditText etRefNo;
    private EditText etSerialNo;
    private EditText etJONumber;

    private Button btnPhoto, btnPhotoPrev;
    private TextView tvPhotoName, tvPhotoSize;

    private Button btnSign, btnSignPrev, btnSubmit;

    private SharedPreferences sharedPreferences;

    private View.OnClickListener datePOClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            new DatePickerDialog(getActivity(), R.style.DialogTheme, datePOListener, year, month, day).show();
        }
    };
    private View.OnClickListener dateDRClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            new DatePickerDialog(getActivity(), R.style.DialogTheme, dateDRListener, year, month, day).show();
        }
    };
    private View.OnClickListener dateDcClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            new DatePickerDialog(getActivity(), R.style.DialogTheme, dateDCListener, year, month, day).show();
        }
    };
    private View.OnTouchListener etRemarksTouchListener = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()&MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        }
    };

    private Button mClear;
    private Button mGetSign;
    private Button mCancel;
    private Dialog signatureDialog;
    private LinearLayout mContent;
    private View signatureview;
    private Signature mSignature;
    private String joborderSignature;

    public void displaySignatureDialog()
    {
        signatureDialog = new Dialog(getActivity());

        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        signatureDialog.setCancelable(false);

        mContent = signatureDialog.findViewById(R.id.linearLayout);
        mSignature = new Signature(getActivity().getApplicationContext(), null, mContent);
        mSignature.setBackgroundColor(Color.WHITE);

        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mClear = signatureDialog.findViewById(R.id.btnSignClear);
        mGetSign = signatureDialog.findViewById(R.id.btn_sign_save);
        mCancel = signatureDialog.findViewById(R.id.btnSignCancel);

        signatureview = mContent;

        mClear.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                mSignature.clear();
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                signatureview.setDrawingCacheEnabled(true);
                joborderSignature = mSignature.save(signatureview);
                signatureDialog.dismiss();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                mSignature.clear();
                signatureDialog.dismiss();
            }
        });
        signatureDialog.show();
    }

    RecyclerView recyclerViewEngine;
    EngineJOAdapter engineJOAdapter;

    Fragment currFrag;

    private View.OnClickListener engineModelClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);

            View searchView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.engine_recycler_layout, null);

            recyclerViewEngine = searchView.findViewById(R.id.recyclerViewEngine);

            final ArrayList<Engine> engineList = new ArrayList<>();

            EditText editTextSearchEngine = searchView.findViewById(R.id.editTextSearchEngine);
            editTextSearchEngine.setFilters(
                    new InputFilter[] {
                            new EmojiExcludeFilter(),
                            new InputFilter.LengthFilter(16)
                    }
            );

            editTextSearchEngine.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence query, int start, int before, int count) {
                    query = query.toString().toLowerCase();
                    String trimmedQuery = query.toString().trim();

                    if (trimmedQuery.length() < 1) {
                        engineList.clear();
                        if (engineJOAdapter != null) {
                            engineJOAdapter.notifyDataSetChanged();
                        }
                    } else if (trimmedQuery.length() >= 1) {
                        engineList.clear();
                        engineJOAdapter = new EngineJOAdapter(
                                engineList,
                                getActivity(),
                                etEngineModel,
                                etMakeCat,
                                dialog);
                        new SearchEngineTaskFromJO(
                                getContext(), engineList, recyclerViewEngine,
                                engineJOAdapter).execute(trimmedQuery);
                    }

                    if (trimmedQuery.contains("none")) {
                        engineList.clear();
                        engineJOAdapter = new EngineJOAdapter(
                                engineList,
                                getActivity(),
                                etEngineModel,
                                etMakeCat,
                                dialog);

                        engineList.add(new Engine("0", "0", "0", "0", "0", "0"));

                        recyclerViewEngine.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerViewEngine.setAdapter(engineJOAdapter);
                        recyclerViewEngine.setItemAnimator(new DefaultItemAnimator());
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            dialog.setContentView(searchView);

            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.width = Util.systemWidth;
            wmlp.gravity = Gravity.TOP;
            wmlp.y = 200;

            dialog.show();
        }
    };

    RecyclerView recyclerViewCustomer;
    CustomerJOAdapter customerJOAdapter;

    private View.OnClickListener customerClickListener = new View.OnClickListener() {
        public void onClick(View view)
        {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);

            View searchView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.searchable_recycler_layout, null);

            recyclerViewCustomer = searchView.findViewById(R.id.recyclerViewCustomer);

            final ArrayList<CustomerJO> customerList = new ArrayList<>();

            EditText editTextSearchCustomer = searchView.findViewById(R.id.editTextSearchCustomer);
            editTextSearchCustomer.setFilters(
                    new InputFilter[] {
                            new EmojiExcludeFilter(),
                            new InputFilter.LengthFilter(16)
                    }
            );

            editTextSearchCustomer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence query, int start, int before, int count) {
                    query = query.toString().toLowerCase();
                    String trimmedQuery = query.toString().trim();

                    if (trimmedQuery.length() < 1) {
                        customerList.clear();
                        if (customerJOAdapter != null) {
                            customerJOAdapter.notifyDataSetChanged();
                        }
                    } else if (trimmedQuery.length() >= 2) {
                        customerList.clear();

                        customerJOAdapter = new CustomerJOAdapter(customerList,
                                getActivity(), etCustomer, dialog);

                        new SearchCustomerTaskFromJO(
                                getContext(), customerList,
                                recyclerViewCustomer, customerJOAdapter)
                                .execute(trimmedQuery);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            dialog.setContentView(searchView);

            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.width = Util.systemWidth;
            wmlp.gravity = Gravity.TOP;
            wmlp.y = 200;

            dialog.show();
        }
    };

    private int initialJoborderId;
    private String localSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.update_jo_fragment, container, false);
        getActivity().setTitle("Update Joborder");
        setHasOptionsMenu(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle bundle = this.getArguments();
        JSONObject jsonBundle;

        currFrag = getActivity().getSupportFragmentManager().findFragmentById(R.id.content_main);

        etCustomer = v.findViewById(R.id.etCustomerJO);
        etPODate = v.findViewById(R.id.etPODate);
        etDateReceive = v.findViewById(R.id.etDateReceive);
        etDateCommit = v.findViewById(R.id.etDateCommit);
        etEngineModel = v.findViewById(R.id.etEngineModel);
        etMakeCat = v.findViewById(R.id.etMakeCat);
        etMobile = v.findViewById(R.id.etMobile);
        etPurchaseOrder = v.findViewById(R.id.etPurchaseOrder);
        etRemarks = v.findViewById(R.id.etRemarks);
        etRefNo = v.findViewById(R.id.etRefNo);
        etSerialNo = v.findViewById(R.id.etSerialNo);
        etJONumber = v.findViewById(R.id.etJONumber);

        try {
            jsonBundle = new JSONObject(bundle.getString("prevValues"));

            initialJoborderId = jsonBundle.getInt("initialJoborderId");

            etPurchaseOrder.setText(jsonBundle.getString("purchaseOrder"));
            etRefNo.setText(jsonBundle.getString("referenceNo"));
            etMobile.setText(jsonBundle.getString("mobile"));
            etPODate.setText(jsonBundle.getString("poDate"));
            etSerialNo.setText(jsonBundle.getString("serialNo"));
            etDateReceive.setText(jsonBundle.getString("dateReceive"));
            etDateCommit.setText(jsonBundle.getString("dateCommit"));
            etRemarks.setText(jsonBundle.getString("remarks"));
            etEngineModel.setText(jsonBundle.getString("model"));

            etMakeCat.setText(jsonBundle.getString("make")+"/"
                    +jsonBundle.getString("cat"));

            etJONumber.setText(String.valueOf(jsonBundle.getString("joNumber")));
            etCustomer.setText(jsonBundle.getString("customer"));

            Variables.customerIdForJO = jsonBundle.getInt("customerId");
            Variables.modelId = jsonBundle.getString("model");
            localSource = jsonBundle.getString("source");
        } catch (JSONException e) {
            Util.displayStackTraceArray(e.getStackTrace(), "android.wmdc.com.mobilecsa", "JSONException", e.toString());
            Util.alertBox(getContext(), e.getMessage(), "JSON error", false);
        }

        etRemarks.setScroller(new Scroller(getActivity()));
        etRemarks.setVerticalScrollBarEnabled(true);
        etRemarks.setMovementMethod(new ScrollingMovementMethod());
        etRemarks.setOnTouchListener(etRemarksTouchListener);

        btnSubmit = v.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(submitListener);

        etPODate.setOnClickListener(datePOClickListener);
        etDateCommit.setOnClickListener(dateDcClickListener);
        etDateReceive.setOnClickListener(dateDRClickListener);

        etCustomer.setOnClickListener(customerClickListener);
        etEngineModel.setOnClickListener(engineModelClickListener);

        btnPhoto = v.findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(photoListener);
        btnPhotoPrev = v.findViewById(R.id.btnPhotoPrev);
        btnPhotoPrev.setOnClickListener(photoPrevListener);
        tvPhotoName = v.findViewById(R.id.tvPhotoName);
        tvPhotoSize = v.findViewById(R.id.tvPhotoSize);

        btnSign = v.findViewById(R.id.btnSign);
        btnSign.setOnClickListener(signatureClickListner);
        btnSignPrev = v.findViewById(R.id.btnSignPrev);
        btnSignPrev.setOnClickListener(signPrevListener);

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Initializing");
        progress.setMessage("Preparing job order.\nPlease wait...");
        progress.setCancelable(false);
        progress.show();

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 3000);

        return v;
    }

    private View.OnClickListener signPrevListener = new View.OnClickListener() {
            public void onClick(View view) {
                if (joborderSignature == null) {
                    Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (joborderSignature.isEmpty()) {
                    Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new DialogSignatureTask(getContext()).execute(joborderSignature);
            }
        };

    private View.OnClickListener photoPrevListener = new View.OnClickListener() {
            public void onClick(View view) {
                if (displayName == null) {
                    Toast.makeText(getActivity(), "Include photo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (displayName.isEmpty()) {
                    Toast.makeText(getActivity(), "Include photo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new DialogImageUriTask(getContext()).execute(fileUri);
            }
        };

    private View.OnClickListener signatureClickListner = new View.OnClickListener() {
            public void onClick(View view) {
                displaySignatureDialog();
            }
        };

    private View.OnClickListener submitListener = new View.OnClickListener() {
            public void onClick(View view) {
                updateInitialJoborder();
            }
        };

    private DatePickerDialog.OnDateSetListener datePOListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                etPODate.setText(new StringBuilder().append(year).append("-").append(month+1)
                                .append("-").append(dayOfMonth));
            }
        };

    private DatePickerDialog.OnDateSetListener dateDCListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date dateCommit = format.parse(year+"-"+(month+1)+"-"+dayOfMonth);
                    String dateReceiveStr = etDateReceive.getText().toString();

                    if (!dateReceiveStr.isEmpty()) {
                        Date dateReceive = format.parse(dateReceiveStr);
                        if (dateCommit.after(dateReceive)) {
                            etDateCommit.setText(new StringBuilder().append(year).append("-").append(month+1)
                                            .append("-").append(dayOfMonth));
                        } else {
                            Util.alertBox(getActivity(), "Date Commit should not be on or before Date Receive",
                                    "", false);
                        }
                    } else {
                        etDateCommit.setText(new StringBuilder().append(year).append("-").append(month+1)
                                        .append("-").append(dayOfMonth));
                    }
                } catch (ParseException pe) {
                    Log.e("ParseException", pe.toString() + ">>" + pe.getStackTrace()[0].toString());
                    Toast.makeText(getActivity(), pe.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };

    private DatePickerDialog.OnDateSetListener dateDRListener =
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date dateReceive = format.parse(year + "-" + (month + 1) +"-"+ dayOfMonth);
                    String dateCommitStr = etDateCommit.getText().toString();

                    if (!dateCommitStr.isEmpty()) {
                        Date dateCommit = format.parse(dateCommitStr);
                        if (dateCommit.after(dateReceive)) {
                            etDateReceive.setText(new StringBuilder().append(year).append("-")
                                    .append(month + 1).append("-").append(dayOfMonth));
                        } else {
                            Util.alertBox(getActivity(), "Date Commit should not be on or before Date Receive",
                                    "", false);
                        }
                    } else {
                        etDateReceive.setText(new StringBuilder().append(year).append("-")
                                .append(month + 1).append("-").append(dayOfMonth));
                    }
                } catch (ParseException pe) {
                    Log.e("parse_exception", pe.toString() + ">>" + pe.getStackTrace()[0].toString());
                    Toast.makeText(getActivity(), pe.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };

    private View.OnClickListener photoListener = new View.OnClickListener(){
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            View selectionView = inflater.inflate(R.layout.photo_select_layout, null);

            Button btnCameraSelection = selectionView.findViewById(R.id.btnCameraSelection);
            Button btnGallerySelection = selectionView.findViewById(R.id.btnGallerySelection);

            btnCameraSelection.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    dispatchTakePictureIntent();
                }
            });

            btnGallerySelection.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Util.performFileSearch(getActivity(), READ_REQUEST_CODE);
                }
            });

            Dialog builder = new Dialog(getContext());
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //  nothing
                }
            });

            builder.addContentView(selectionView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            builder.show();
        }
    };

    private String displayName = null;
    private Uri fileUri = null;
    private InputStream fileInputStream;

    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = Util.createImageFile(getActivity());
            } catch (IOException ex) {
                Util.displayStackTraceArray(ex.getStackTrace(), "android.wmdc.com.mobilecsa", "IOException", ex.toString());
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                fileUri = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                fileUri = resultData.getData();
                dumpImageMetaData(fileUri, false);
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            dumpImageMetaData(fileUri, false);
        }
    }

    public void dumpImageMetaData(final Uri uri, final boolean toSubmit) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Dumping image. Please wait...");
        pd.setCancelable(false);
        pd.show();

        Runnable pdRun = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null,
                        null, null);

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        String size;

                        if (!cursor.isNull(sizeIndex)) {
                            size = cursor.getString(sizeIndex);
                        } else {
                            Toast.makeText(getActivity(), "Size unknown", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int intSize = Integer.parseInt(size);
                        String small_file_size;

                        if (intSize > 512_000) {
                            File file = Util.createImageFile(getActivity());
                            Util.copyInputStreamToFile(Util.getStreamFromUri(uri, getActivity()),
                                    file, getContext());

                            File smallFile = Util.reduceBitmapFile(file);
                            small_file_size = smallFile.length()+"";
                            fileInputStream = new FileInputStream(smallFile);
                        } else {
                            fileInputStream = Util.getStreamFromUri(uri, getActivity());
                            small_file_size = size;
                        }

                        pd.cancel();

                        if (toSubmit) {
                            updateInitialJoborder();
                        } else {
                            textPhoto = Integer.parseInt(size)/1000+"KB. "+
                                    Integer.parseInt(small_file_size)/1000+"KB.";
                            tvPhotoName.setText(displayName);
                            tvPhotoSize.setText(textPhoto);
                        }
                    }
                } catch (IOException ie) {
                    pd.cancel();

                    Util.displayStackTraceArray(ie.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                            "IOException", ie.toString());
                    Toast.makeText(getContext(), ie.toString(), Toast.LENGTH_SHORT).show();
                } finally {
                    cursor.close();
                }
            }
        };

        Handler pdHandler = new Handler();
        pdHandler.postDelayed(pdRun, 2000);
    }

    String textPhoto;
    private HashMap<String, String> params = new HashMap<>();

    private void updateInitialJoborder() {
        try {
            int customerId = Variables.customerIdForJO;
            String customer = etCustomer.getText().toString();
            String mobile = etMobile.getText().toString();
            String purchaseOrder = etPurchaseOrder.getText().toString();
            String poDate = etPODate.getText().toString();
            String serialNo = etSerialNo.getText().toString();
            String dateReceive = etDateReceive.getText().toString();
            String dateCommit = etDateCommit.getText().toString();
            String refNo = etRefNo.getText().toString();
            String remarks = etRemarks.getText().toString();
            String joSignature = joborderSignature;
            String joNumber = etJONumber.getText().toString();
            String engineModel = etEngineModel.getText().toString();
            String makeCat = etMakeCat.getText().toString();
            String make;
            String cat;

            if (makeCat.isEmpty()) {
                Toast.makeText(getActivity(), "Engine Model required.", customerId).show();
                return;
            } else {
                String[] splitMakeCat = etMakeCat.getText().toString().split("/");
                make = splitMakeCat[0];
                cat = splitMakeCat[1];
            }

            if (joNumber == null) {
                Toast.makeText(getActivity(), "Include JO Number.", customerId).show();
                return;
            } else {
                if (joNumber.isEmpty()) {
                    Toast.makeText(getActivity(), "Include JO Number.", customerId).show();
                    return;
                }
            }

            if (customer == null) {
                Toast.makeText(getActivity(), "Customer is required.", customerId).show();
                return;
            } else {
                if (customer.isEmpty()) {
                    Toast.makeText(getActivity(), "Customer is required.", customerId).show();
                    return;
                }
            }

            if (refNo == null) {
                Toast.makeText(getActivity(), "Reference number is required.", customerId).show();
                return;
            } else {
                if (refNo.isEmpty()) {
                    Toast.makeText(getActivity(), "Reference number is required.", customerId).show();
                    return;
                }
            }

            if (purchaseOrder == null) {
                Toast.makeText(getActivity(), "Purchase Order is required.", customerId).show();
                return;
            } else {
                if (purchaseOrder.isEmpty()) {
                    Toast.makeText(getActivity(), "Purchase Order is required.", customerId).show();
                    return;
                }
            }

            if (customerId < 0) {
                Toast.makeText(getActivity(), "Customer is required.", customerId).show();
                return;
            }
            if (mobile.isEmpty()) {
                Toast.makeText(getActivity(), "Mobile is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serialNo.isEmpty()) {
                Toast.makeText(getActivity(), "Serial Number is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (remarks.isEmpty()) {
                Toast.makeText(getActivity(), "Remarks is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (poDate.isEmpty()) {
                Toast.makeText(getActivity(), "PO date is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dateReceive.isEmpty()) {
                Toast.makeText(getActivity(), "Date Receive is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dateCommit.isEmpty()) {
                Toast.makeText(getActivity(), "Date Commit is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (engineModel.isEmpty()) {
                Toast.makeText(getActivity(),
                        "Include Engine Model or \ntype \"none\" for machining.", Toast.LENGTH_SHORT).show();
                return;
            }

            remarks = Util.filterSpecialChars(remarks);

            params.put("initialJoborderId", String.valueOf(initialJoborderId));
            params.put("customerId", Util.val(customerId));
            params.put("customer", customer);
            params.put("source", localSource);
            params.put("mobile", mobile);
            params.put("purchaseOrder", purchaseOrder);
            params.put("poDate", poDate);
            params.put("make", make);
            params.put("cat", cat);
            params.put("modelId", Variables.modelId);
            params.put("serialNo", serialNo);
            params.put("dateReceive", dateReceive);
            params.put("dateCommit", dateCommit);
            params.put("refNo", refNo);
            params.put("remarks", remarks);
            params.put("preparedBy", Util.val(sharedPreferences.getInt("csaId", 0)));
            params.put("imageType", "jpg");
            params.put("joSignature", (joSignature == null) ? "" : joSignature);
            params.put("joNumber", joNumber);

            AlertDialog.Builder confirmBox = new AlertDialog.Builder(getActivity());
            confirmBox.setMessage("Confirm to update job order");
            confirmBox.setTitle("Confirm");
            confirmBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new UpdateInitialJoborderTask().execute();
                }
            });
            confirmBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            confirmBox.show();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
            Util.alertBox(getContext(), e.toString(), "", false);
        }
    }

    private class UpdateInitialJoborderTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        HttpURLConnection conn = null;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("\tUpdating initial joborder.\nPlease wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String[] parameters) {
            try {
                url = new URL(sharedPreferences.getString("domain", null)+
                        "updateinitialjoborder");

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
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "newquotation-android");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

                if (fileInputStream != null) {
                    outputStream.writeBytes(twoHypens+boundary+lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name\"reference\""+lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes("my_reference_text");
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(twoHypens+boundary+lineEnd);

                    outputStream.writeBytes("Content-Disposition: form-data; " +
                            "name=\"photo\";filename\""+displayName+"\""+lineEnd);
                    outputStream.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    outputStream.writeBytes(lineEnd);
                }

                Iterator<String> keys = params.keySet().iterator();

                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = params.get(key);

                    outputStream.writeBytes(twoHypens+boundary+lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\""+key+"\""+lineEnd);
                    outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }

                outputStream.writeBytes(twoHypens+boundary+twoHypens+lineEnd);
                int statusCode = conn.getResponseCode();

                outputStream.flush();
                outputStream.close();

                if (statusCode == 200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String inputLine;
                    while ((inputLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
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

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject response = new JSONObject(result);
                if (response.getBoolean("success")) {
                    AlertDialog.Builder warningBox =
                            new AlertDialog.Builder(getActivity());

                    warningBox.setTitle("Success");
                    warningBox.setMessage(response.getString("reason"));
                    warningBox.setCancelable(false);
                    warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction()
                                    .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit)
                                    .replace(R.id.content_main, new JOFragment())
                                    .commit();
                        }
                    });
                    warningBox.create().show();
                } else {
                    Util.alertBox(getActivity(), response.getString("reason"), "Failed", false);
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}