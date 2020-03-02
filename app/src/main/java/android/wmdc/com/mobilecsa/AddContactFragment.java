package android.wmdc.com.mobilecsa;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageUriTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogSignatureTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetAreaCodeTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetCityTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetCountryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetIndustryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetPlantTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetProvinceTask;
import android.wmdc.com.mobilecsa.model.GPSTracker;
import android.wmdc.com.mobilecsa.model.Signature;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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

/**Created by wmdcprog on 12/4/*/

public class AddContactFragment extends Fragment {

    private int year;
    private int month;
    private int day;

    private String contactSignature = "";

    private EditText editTextLastname;
    private EditText editTextFirstname;
    private EditText editTextMI;
    private EditText editTextCompany;
    private EditText editTextJobPos;
    private EditText editTextAddress;
    private EditText editTextDate;
    private EditText editTextFax;
    private EditText editTextTelephone;
    private EditText editTextMobile;
    private EditText editTextEmail;
    private EditText editTextWebsite;
    private EditText editTextEmergencyContact;
    private EditText editTextEmergency;
    private EditText editTextZip = null;

    private Spinner spinnerIndustry;
    private Spinner spinnerPlant;
    private Spinner spinnerCity;
    private Spinner spinnerProvince;
    private Spinner spinnerCountry;
    private Spinner spinnerAreaCode;
    private Spinner spinnerFaxCode;
    private Spinner spinnerER;
    private Spinner spinnerMF;
    private Spinner spinnerCalibration;
    private Spinner spinnerSpareParts;

    private ArrayList<String> industryCategory;
    private ArrayList<String> plantCategory;
    private ArrayList<String> cityCategory;
    private ArrayList<String> provinceCategory;
    private ArrayList<String> countryCategory;
    private ArrayList<String> areaCodeCategory;
    private ArrayList<Integer> numberCategory;

    private HashMap<String, Integer> industryMap;
    private HashMap<String, Integer> plantMap;
    private HashMap<String, Integer> cityMap = null;
    private HashMap<String, Integer> provinceMap;
    private HashMap<String, Integer> countryMap;
    private HashMap<String, Integer> areaCodeMap;
    private HashMap<String, Integer> zipCodeMap;

    private Dialog dialog;
    private View signatureview;
    private Signature mSignature;

    private SharedPreferences sharedPreferences;

    private TextView tvPhotoName, tvPhotoSize;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instanceState) {
        View v = inflater.inflate(R.layout.add_contact_fragment, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("Add Contact");
        } else {
            Util.longToast(getContext(),
                    "\"getActivity()\" is null. Cannot set title of this fragment");
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Button btnPhoto = v.findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(specimenListener);

        Button btnPhotoPrev = v.findViewById(R.id.btnPhotoPrev);
        btnPhotoPrev.setOnClickListener(photoPrevListener);

        tvPhotoName = v.findViewById(R.id.tvPhotoName);
        tvPhotoSize = v.findViewById(R.id.tvPhotoSize);

        Button btnSign = v.findViewById(R.id.btnSign);
        btnSign.setOnClickListener(signatureClickListner);

        Button btnSignPrev = v.findViewById(R.id.btnSignPrev);
        btnSignPrev.setOnClickListener(signPrevListener);

        Button btnSubmit = v.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(submitListener);

        editTextLastname = v.findViewById(R.id.editTextLastname);
        editTextLastname.setFilters(Util.getEmojiFilters(32));

        editTextFirstname = v.findViewById(R.id.editTextFirstname);
        editTextFirstname.setFilters(Util.getEmojiFilters(32));

        editTextMI = v.findViewById(R.id.editTextMI);

        editTextCompany = v.findViewById(R.id.editTextCompany);
        editTextCompany.setFilters(Util.getEmojiFilters(100));

        editTextJobPos = v.findViewById(R.id.editTextJobPos);

        editTextAddress = v.findViewById(R.id.editTextAddress);
        editTextAddress.setFilters(Util.getEmojiFilters());

        editTextDate = v.findViewById(R.id.editTextDate);
        editTextFax = v.findViewById(R.id.editTextFax);
        editTextTelephone = v.findViewById(R.id.editTextTelephone);
        editTextMobile = v.findViewById(R.id.editTextMobile);

        editTextEmail = v.findViewById(R.id.editTextEmail);
        editTextEmail.setFilters(Util.getEmojiFilters(64));

        editTextWebsite = v.findViewById(R.id.editTextWebsite);
        editTextWebsite.setFilters(Util.getEmojiFilters(64));

        editTextEmergencyContact = v.findViewById(R.id.editTextEmergencyContact);

        editTextEmergency = v.findViewById(R.id.editTextEmergency);
        editTextEmergency.setFilters(Util.getEmojiFilters(64));

        editTextZip = v.findViewById(R.id.editTextZip);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //  industry
        spinnerIndustry = v.findViewById(R.id.spinnerIndustry);
        spinnerIndustry.setOnItemSelectedListener(spinnerIndustryListener);
        spinnerIndustry.setOnTouchListener(touchListener);

        //  plant
        spinnerPlant = v.findViewById(R.id.spinnerPlant);
        spinnerPlant.setOnItemSelectedListener(spinnerPlantListener);
        spinnerPlant.setOnTouchListener(touchListener);

        //  city
        spinnerCity = v.findViewById(R.id.spinnerCity);
        spinnerCity.setOnItemSelectedListener(spinnerCityListener);
        spinnerCity.setOnTouchListener(touchListener);

        //  province
        spinnerProvince = v.findViewById(R.id.spinnerProvince);
        spinnerProvince.setOnItemSelectedListener(spinnerProvinceListener);
        spinnerProvince.setOnTouchListener(touchListener);

        //  country
        spinnerCountry = v.findViewById(R.id.spinnerCountry);
        spinnerCountry.setOnItemSelectedListener(spinnerCountryListener);
        spinnerCountry.setOnTouchListener(touchListener);

        //  area code
        spinnerAreaCode = v.findViewById(R.id.spinnerAreaCode);
        spinnerAreaCode.setOnItemSelectedListener(spinnerAreaCodeListener);
        spinnerAreaCode.setOnTouchListener(touchListener);

        //  fax code
        spinnerFaxCode = v.findViewById(R.id.spinnerFaxCode);
        spinnerFaxCode.setOnItemSelectedListener(spinnerFaxCodeListener);
        spinnerFaxCode.setOnTouchListener(touchListener);

        //  er
        spinnerER = v.findViewById(R.id.spinnerER);
        spinnerER.setOnItemSelectedListener(spinnerERListener);
        spinnerER.setOnTouchListener(touchListener);

        //  mf
        spinnerMF = v.findViewById(R.id.spinnerMF);
        spinnerMF.setOnItemSelectedListener(spinnerMFListener);
        spinnerMF.setOnTouchListener(touchListener);

        //  calib
        spinnerCalibration = v.findViewById(R.id.spinnerCalib);
        spinnerCalibration.setOnItemSelectedListener(spinnerCalibrationListener);
        spinnerCalibration.setOnTouchListener(touchListener);

        //  spinner spare parts
        spinnerSpareParts = v.findViewById(R.id.spinnerSpareParts);
        spinnerSpareParts.setOnItemSelectedListener(spinnerSparePartsListener);
        spinnerSpareParts.setOnTouchListener(touchListener);

        industryCategory = new ArrayList<>();
        plantCategory = new ArrayList<>();
        cityCategory = new ArrayList<>();
        provinceCategory = new ArrayList<>();
        countryCategory = new ArrayList<>();
        areaCodeCategory = new ArrayList<>();

        numberCategory = Util.getOneHundredNumbers();

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    new DatePickerDialog(getActivity(), R.style.DialogTheme, dateListener, year,
                            month, day).show();
                } else {
                    Util.alertBox(getContext(), "Activity is null. Cannot open date.");
                }
            }
        });

        loadOptions();

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Initializing");
        progress.setMessage("Preparing to add contact.\nPlease wait...");
        progress.setCancelable(false);
        progress.show();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progress.isShowing()) {
                    progress.cancel();
                }

                if (spinnerIndustry.getSelectedItem() == null) {
                    if (getFragmentManager() != null) {
                        Fragment currFrag = getFragmentManager().findFragmentById(
                                R.id.content_main);

                        if (getActivity() != null) {
                            Util.handleBackPress(currFrag, getActivity());
                        } else {
                            Util.longToast(getContext(), "Activity is null.");
                        }

                        Util.alertBox(getActivity(), "Connection was not established." +
                                "\nCheck data/wifi internet connectivity." +
                                "\nCheck server availability.", "Resource Empty", false);
                    } else {
                        Util.longToast(getActivity(), "Fragment Manager is null.");
                    }
                }
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 3000);

        return v;
    }

    private View.OnClickListener submitListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (is_submitted) {
                dumpImageMetaData(fileUri, true);
            } else {
                addContacts();
            }
        }
    };

    private View.OnClickListener signPrevListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (contactSignature == null) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            }
            if (contactSignature.isEmpty()) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            }
            new DialogSignatureTask(getActivity()).execute(contactSignature);
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

    private void displaySignatureDialog() {

        if (getActivity() != null) {
            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_signature);
            dialog.setCancelable(false);

            LinearLayout mContent = dialog.findViewById(R.id.linearLayout);
            mSignature = new Signature(getActivity().getApplicationContext(), null, mContent);
            mSignature.setBackgroundColor(Color.WHITE);

            mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            Button mClear = dialog.findViewById(R.id.btnSignClear);
            Button mGetSign = dialog.findViewById(R.id.btn_sign_save);
            Button mCancel = dialog.findViewById(R.id.btnSignCancel);

            signatureview = mContent;

            mClear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSignature.clear();
                }
            });

            mGetSign.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    signatureview.setDrawingCacheEnabled(true);
                    contactSignature = mSignature.save(signatureview);
                    dialog.dismiss();
                }
            });

            mCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSignature.clear();
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            Util.alertBox(getContext(), "Activity is null. Cannot open signature.");
        }
    }

    private void loadOptions() {
        industryMap = new HashMap<>();
        plantMap = new HashMap<>();
        zipCodeMap = new HashMap<>();
        cityMap = new HashMap<>();
        provinceMap = new HashMap<>();
        areaCodeMap = new HashMap<>();
        countryMap = new HashMap<>();

        new GetIndustryTask(getActivity(), industryCategory, industryMap,
                spinnerIndustry).execute();

        new GetPlantTask(getActivity(), plantCategory, plantMap, spinnerPlant).execute();
        new GetCityTask(getActivity(), cityCategory, cityMap, zipCodeMap, spinnerCity).execute();
        new GetCountryTask(getActivity(), countryCategory, countryMap, spinnerCountry).execute();

        new GetProvinceTask(getActivity(), provinceCategory, provinceMap,
                spinnerProvince).execute();

        new GetAreaCodeTask(getActivity(), areaCodeCategory, areaCodeMap,
                spinnerAreaCode, spinnerFaxCode).execute();

        if (getActivity() != null) {

            ArrayAdapter<Integer> numberAdapter = new ArrayAdapter<>(getActivity(),
                    R.layout.support_simple_spinner_dropdown_item, numberCategory);
            numberAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            spinnerER.setAdapter(numberAdapter);
            spinnerMF.setAdapter(numberAdapter);
            spinnerCalibration.setAdapter(numberAdapter);
            spinnerSpareParts.setAdapter(numberAdapter);
        } else {
            Util.shortToast(getContext(), "Number Adapter not initialized. Cannot load options.");
        }
    }

    private DatePickerDialog.OnDateSetListener dateListener =
            new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {
            editTextDate.setText(new StringBuilder().append(year).append("-")
                    .append(monthOfYear+1).append("-").append(dayOfMonth));
        }
    };

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            hideSoftKey();
            view.performClick();
            return false;
        }
    };

    private View.OnClickListener specimenListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

    private static String displayName = null;
    private Uri fileUri = null;
    private InputStream fileInputStream;
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
                    fileUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID +
                            ".provider", photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Util.alertBox(getContext(), "Activity is null. Cannot open camera.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
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
                            displayName = cursor.getString(
                                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            String size;

                            if (!cursor.isNull(sizeIndex)) {
                                size = cursor.getString(sizeIndex);
                            } else {
                                Util.shortToast(getActivity(), "Unknown size.");
                                return;
                            }

                            int intSize = Integer.parseInt(size);
                            String smallFileSize = "";

                            if (intSize > 512_000) {
                                File file = Util.createImageFile(getActivity());

                                Util.copyInputStreamToFile(Util.getStreamFromUri(uri,
                                        getActivity()), file, getContext());

                                File smallFile = Util.reduceBitmapFile(file);

                                if (smallFile != null) {
                                    smallFileSize = smallFile.length() + "";
                                    fileInputStream = new FileInputStream(smallFile);
                                } else {
                                    Util.shortToast(getActivity(), "Small file is null.");
                                }
                            } else {
                                fileInputStream = Util.getStreamFromUri(uri, getActivity());
                                smallFileSize = size;
                            }

                            pd.cancel();

                            if (toSubmit) {
                                addContacts();
                            } else {
                                tvPhotoName.setText(displayName);

                                String txt = Integer.parseInt(size) / 1000 + " KB." +
                                        Integer.parseInt(smallFileSize) / 1000 + "KB.";

                                tvPhotoSize.setText(txt);
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

    private AdapterView.OnItemSelectedListener spinnerIndustryListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerPlantListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerCityListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                String item = parent.getItemAtPosition(position).toString();

                if (editTextZip != null) {
                    editTextZip.setText(String.valueOf(zipCodeMap.get(item)));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    private AdapterView.OnItemSelectedListener spinnerProvinceListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerCountryListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerAreaCodeListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            };

    private AdapterView.OnItemSelectedListener spinnerFaxCodeListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerERListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerMFListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerCalibrationListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    private AdapterView.OnItemSelectedListener spinnerSparePartsListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                Util.shortToast(getActivity(), "Camera and Storage permission denied");
            }
        }
    }

    private static boolean is_submitted = false;

    private static class AddContactTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        private WeakReference<FragmentActivity> activityWeakReference;

        private SharedPreferences taskPrefs;

        private HttpURLConnection conn = null;

        private InputStream fileStream;

        private HashMap<String, String> parameters;

        private AddContactTask(FragmentActivity activity, InputStream fileStream,
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

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(taskPrefs.getString("domain", null)+"addcontacts");

                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                int bytesRead, bytesAvailable, bufferSize;
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
                conn.setRequestProperty("Referer", "daryll");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                DataOutputStream outputStream;
                outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"reference\""
                        +lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes("my_reference_text");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: " +
                        "form-data; name=\"specimenPhoto\";filename=\"" +displayName+ "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead =  fileStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                for (Map.Entry<String, String> mapEntry : parameters.entrySet()) {

                    String key = mapEntry.getKey();
                    String value = mapEntry.getValue();

                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" +key+ "\""
                            +lineEnd);
                    outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                int statusCode = conn.getResponseCode();

                outputStream.flush();
                outputStream.close();

                if (fileStream != null) {
                    fileStream.close();
                }

                is_submitted = true;

                if (statusCode == 200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }

                    is.close();
                    br.close();

                    if (stringBuilder.toString().isEmpty()) {
                        return "{\"success\": false, \"reason\": \"No response from server.\"}";
                    } else {
                        return stringBuilder.toString();
                    }
                } else {
                    return "{\"success\": false, \"reason\": \"Request did not succeed. Status Code: "+statusCode+"\"}";
                }
            } catch (MalformedURLException | ConnectException | SocketTimeoutException e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "NetworkException", e.toString());

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
                        "Exception", e.toString());
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

            final FragmentActivity mainActivity = activityWeakReference.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            try {
                JSONObject response = new JSONObject(result);

                if (response.getBoolean("success")) {
                    AlertDialog.Builder warningBox = new AlertDialog.Builder(mainActivity);

                    TextView errMsg = new TextView(mainActivity);
                    errMsg.setText(response.getString("reason"));
                    errMsg.setTextSize(17);
                    errMsg.setPadding(20, 0, 10, 0);
                    errMsg.setGravity(Gravity.CENTER_HORIZONTAL);

                    warningBox.setView(errMsg);
                    warningBox.setTitle("Success");
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
                        "Exception", e.toString());

                Util.longToast(mainActivity, e.getMessage());
            }
        }
    }

    private void addContacts() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        Log.d(String.valueOf(gpsTracker.getLatitude()), String.valueOf(gpsTracker.getLongitude()));

        if (displayName == null) {
            Util.shortToast(getActivity(), "Include Photo."); return;
        } else {
            if (displayName.isEmpty()) {
                Util.shortToast(getActivity(), "Include photo."); return;
            }
        }

        if (contactSignature == null) {
            Util.shortToast(getActivity(), "Include signature."); return;
        } else {
            if (contactSignature.isEmpty()) {
                Util.shortToast(getActivity(), "Include signature.");
                return;
            }
        }

        try {

            String lastname = editTextLastname.getText().toString();
            String firstname = editTextFirstname.getText().toString();
            String mi = editTextMI.getText().toString();
            String company = editTextCompany.getText().toString();
            String jobPosition = editTextJobPos.getText().toString();
            String address = editTextAddress.getText().toString();
            String date = editTextDate.getText().toString();
            String telephone = editTextTelephone.getText().toString();
            String mobile = editTextMobile.getText().toString();
            String email = editTextEmail.getText().toString();
            String website = editTextWebsite.getText().toString();
            String emergencyContact = editTextEmergencyContact.getText().toString();
            String emergency = editTextEmergency.getText().toString();
            String zip = editTextZip.getText().toString();
            String fax = editTextFax.getText().toString();

            if (spinnerIndustry.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Industry"); return;
            }

            if (spinnerPlant.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Plant"); return;
            }

            if (spinnerCity.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select City"); return;
            }

            if (spinnerProvince.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Province"); return;
            }

            if (spinnerCountry.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Country"); return;
            }

            if (spinnerFaxCode.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Faxcode"); return;
            }

            if (spinnerAreaCode.getSelectedItem() == null) {
                Util.shortToast(getActivity(), "Select Area Code");
                return;
            }

            Integer industryInt = industryMap.get(spinnerIndustry.getSelectedItem().toString());
            int industry = (industryInt != null) ? industryInt : 0;

            Integer plantInt = plantMap.get(spinnerPlant.getSelectedItem().toString());
            int plant = (plantInt != null) ? plantInt : 0;

            Integer cityInt = cityMap.get(spinnerCity.getSelectedItem().toString());
            int city = (cityInt != null) ? cityInt : 0;

            Integer provinceInt = provinceMap.get(spinnerProvince.getSelectedItem().toString());
            int province = (provinceInt != null) ? provinceInt : 0;

            Integer countryInt = countryMap.get(spinnerCountry.getSelectedItem().toString());
            int country = (countryInt != null) ? countryInt : 0;

            Integer faxCodeInt = areaCodeMap.get(spinnerFaxCode.getSelectedItem().toString());
            int faxCode = (faxCodeInt != null) ? faxCodeInt : 0;

            Integer areaCodeInt = areaCodeMap.get(spinnerAreaCode.getSelectedItem().toString());
            int areaCode = (areaCodeInt != null) ? areaCodeInt : 0;

            String er = spinnerER.getSelectedItem().toString();
            String mf = spinnerMF.getSelectedItem().toString();
            String calib = spinnerCalibration.getSelectedItem().toString();
            String spareParts = spinnerSpareParts.getSelectedItem().toString();
            String lat = "" + gpsTracker.getLatitude();
            String lng = "" + gpsTracker.getLongitude();
            String csaId = sharedPreferences.getInt("csaId", 0) + "";
            String signature = contactSignature;

            if (Util.isInvalidEmail(email)) {
                Util.longToast(getActivity(), "Email format is wrong");
                return;
            }

            if (!Util.validURL(website)) {
                Util.longToast(getActivity(), "Website format is wrong");
                return;
            }

            if (lastname.isEmpty()) {
                Util.longToast(getActivity(), "Lastname is required.");
                return;
            }

            if (firstname.isEmpty()) {
                Util.longToast(getActivity(), "Firstname is required.");
                return;
            }

            if (mi.isEmpty()) {
                Util.longToast(getActivity(), "Middle Initial is required.");
                return;
            }

            if (jobPosition.isEmpty()) {
                Util.longToast(getActivity(), "Job position is required.");
                return;
            }

            if (address.isEmpty()) {
                Util.longToast(getActivity(), "Address is required.");
                return;
            }

            if (date.isEmpty()) {
                Util.longToast(getActivity(), "Date of Birth is required.");
                return;
            }

            if (mobile.isEmpty()) {
                Util.longToast(getActivity(), "Mobile number is required.");
                return;
            }

            if (emergencyContact.isEmpty()) {
                Util.longToast(getActivity(), "Emergency contact is required.");
                return;
            }

            if (industry == 0) {
                Util.longToast(getActivity(), "Select an industry.");
                return;
            }

            if (plant == 0) {
                Util.longToast(getActivity(), "Select a plant.");
                return;
            }

            if (city == 0) {
                Util.longToast(getActivity(), "Select a city.");
                return;
            }

            if (province == 0) {
                Util.longToast(getActivity(), "Select a province.");
                return;
            }

            if (country == 0) {
                Util.longToast(getActivity(), "Select a country.");
                return;
            }

            if (faxCode == 0) {
                Util.longToast(getActivity(), "Select fax code.");
                return;
            }

            if (areaCode == 0) {
                Util.longToast(getActivity(), "Select an area code.");
                return;
            }

            final HashMap<String, String> stringParams = new HashMap<>();

            stringParams.put("lastname", lastname);
            stringParams.put("firstname", firstname);
            stringParams.put("mi", mi);
            stringParams.put("company", company);
            stringParams.put("jobPosition", jobPosition);
            stringParams.put("address", address);
            stringParams.put("birthDate", date);
            stringParams.put("fax", fax);
            stringParams.put("telephone", telephone);
            stringParams.put("mobile", mobile);
            stringParams.put("email", email);
            stringParams.put("website", website);
            stringParams.put("emergencyContact", emergencyContact);
            stringParams.put("emergency", emergency);
            stringParams.put("zip", zip);
            stringParams.put("industry", "" + industry);
            stringParams.put("plant", "" + plant);
            stringParams.put("city", "" + city);
            stringParams.put("province", "" + province);
            stringParams.put("country", "" + country);
            stringParams.put("faxCode", "" + faxCode);
            stringParams.put("areaCode", "" + areaCode);
            stringParams.put("er", er);
            stringParams.put("mf", mf);
            stringParams.put("calib", calib);
            stringParams.put("spareParts", spareParts);
            stringParams.put("lat", lat);
            stringParams.put("lng", lng);
            stringParams.put("csaId", csaId);
            stringParams.put("signature", signature);
            stringParams.put("signStatus", "true");

            if (getActivity() != null) {
                AlertDialog.Builder confirmBox = new AlertDialog.Builder(getActivity());
                confirmBox.setMessage("Do really want to add contacts?");
                confirmBox.setTitle("Confirm");
                confirmBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AddContactTask(getActivity(), fileInputStream, stringParams).execute();
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
                Util.alertBox(getContext(), "\"getActivity()\" is null. Cannot build alertdialog.");
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());

            Util.alertBox(getContext(), e.toString());
        }
    }

    private void hideSoftKey() {

        if (getActivity() != null) {
            View viewFocused = getActivity().getCurrentFocus();

            if (viewFocused != null) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(viewFocused.getWindowToken(), 0);

                viewFocused.clearFocus();
            }
        }
    }
}