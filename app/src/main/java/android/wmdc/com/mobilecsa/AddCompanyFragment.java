package android.wmdc.com.mobilecsa;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.asynchronousclasses.CheckSessionExpiryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogImageUriTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.DialogSignatureTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetAreaCodeTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetCityTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetCountryCodeTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetCountryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetIndustryTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetPlantTask;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetProvinceTask;
import android.wmdc.com.mobilecsa.model.GPSTracker;
import android.wmdc.com.mobilecsa.model.Signature;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import static android.app.Activity.RESULT_OK;

/**
 * Created by wmdcprog on 12/12/2017.
 * */

public class AddCompanyFragment extends Fragment {
    private EditText etCompany;
    private EditText etAddrComp;
    private EditText etFaxComp;
    private EditText etTeleComp;
    private EditText etMobComp;
    private EditText etEmailComp;
    private EditText etWebComp;
    private EditText etEmergComp;
    private EditText etZipComp = null;
    private EditText etContPersComp;
    private EditText etContNumComp;

    private Spinner spinIndComp;
    private Spinner spinPlantComp;
    private Spinner spinCityComp;
    private Spinner spinProvComp;
    private Spinner spinCountComp;
    private Spinner spinFaxCodeComp;
    private Spinner spinFaxCountCodeComp;
    private Spinner spinAreaCodeComp;
    private Spinner spinCountCodeComp;
    private Spinner spinERComp;
    private Spinner spinMFComp;
    private Spinner spinCalibComp;
    private Spinner spinSparePartsComp;

    private ArrayList<String> industryCategory;
    private ArrayList<String> plantCategory;
    private ArrayList<String> cityCategory;
    private ArrayList<String> provinceCategory;
    private ArrayList<String> countryCategory;
    private ArrayList<String> areaCodeCategory;
    private ArrayList<String> countryCodeCategory;
    private ArrayList<Integer> numberCategory;

    private HashMap<String, Integer> industryMap;
    private HashMap<String, Integer> plantMap;
    private HashMap<String, Integer> cityMap;
    private HashMap<String, Integer> provinceMap;
    private HashMap<String, Integer> countryMap;
    private HashMap<String, Integer> areaCodeMap;
    private HashMap<String, Integer> countryCodeMap;
    private HashMap<String, Integer> zipCodeMap;

    private HashMap<String, String> stringParams;

    //  signatures
    private Button mClear;
    private Button mGetSign;
    private Button mCancel;

    private Dialog dialog;
    private LinearLayout mContent;  //  signature content
    private View signatureview;
    private Signature mSignature;

    private String companySignature = "";
    private SharedPreferences sharedPreferences;

    private Button btnPhoto, btnPhotoPrev;
    private TextView tvPhotoName, tvPhotoSize;
    private Button btnSign, btnSignPrev;
    private Button btnSubmit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_company_fragment, container, false);
        getActivity().setTitle("Add Company");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        etCompany = v.findViewById(R.id.etCompany);
        etCompany.setFilters(Util.getEmojiFilters(64));

        etAddrComp = v.findViewById(R.id.etAddrComp);
        etAddrComp.setFilters(Util.getEmojiFilters());

        etFaxComp = v.findViewById(R.id.etFaxComp);
        etTeleComp = v.findViewById(R.id.etTeleComp);
        etMobComp = v.findViewById(R.id.etMobComp);

        etEmailComp = v.findViewById(R.id.etEmailComp);
        etEmailComp.setFilters(Util.getEmojiFilters(64));

        etWebComp = v.findViewById(R.id.etWebComp);
        etWebComp.setFilters(Util.getEmojiFilters());

        etEmergComp = v.findViewById(R.id.etEmergComp);
        etEmergComp.setFilters(Util.getEmojiFilters(64));

        etZipComp = v.findViewById(R.id.etZipComp);
        etContPersComp = v.findViewById(R.id.etContPersComp);
        etContNumComp = v.findViewById(R.id.etContNumComp);

        spinIndComp = v.findViewById(R.id.spinIndComp);
        spinPlantComp = v.findViewById(R.id.spinPlantComp);
        spinCityComp = v.findViewById(R.id.spinCityComp);
        spinProvComp = v.findViewById(R.id.spinProvComp);
        spinCountComp = v.findViewById(R.id.spinCountComp);
        spinFaxCodeComp = v.findViewById(R.id.spinFaxCodeComp);
        spinFaxCountCodeComp = v.findViewById(R.id.spinFaxCountCodeComp);
        spinAreaCodeComp = v.findViewById(R.id.spinAreaCodeComp);
        spinCountCodeComp = v.findViewById(R.id.spinCountCodeComp);
        spinERComp = v.findViewById(R.id.spinERComp);
        spinMFComp = v.findViewById(R.id.spinMFComp);
        spinCalibComp = v.findViewById(R.id.spinCalibComp);
        spinSparePartsComp = v.findViewById(R.id.spinSparePartsComp);

        spinIndComp.setOnItemSelectedListener(spinnerIndustryListener);
        spinPlantComp.setOnItemSelectedListener(spinnerPlantListener);
        spinCityComp.setOnItemSelectedListener(spinnerCityListener);
        spinProvComp.setOnItemSelectedListener(spinnerProvinceListener);
        spinCountComp.setOnItemSelectedListener(spinnerCountryListener);
        spinFaxCodeComp.setOnItemSelectedListener(spinnerFaxCodeListener);
        spinAreaCodeComp.setOnItemSelectedListener(spinnerAreaCodeListener);
        spinERComp.setOnItemSelectedListener(spinnerERListener);
        spinMFComp.setOnItemSelectedListener(spinnerMFListener);
        spinCalibComp.setOnItemSelectedListener(spinnerCalibrationListener);
        spinSparePartsComp.setOnItemSelectedListener(spinnerSparePartsListener);

        spinIndComp.setOnTouchListener(touchListener);
        spinPlantComp.setOnTouchListener(touchListener);
        spinCityComp.setOnTouchListener(touchListener);
        spinProvComp.setOnTouchListener(touchListener);
        spinCountComp.setOnTouchListener(touchListener);
        spinFaxCodeComp.setOnTouchListener(touchListener);
        spinAreaCodeComp.setOnTouchListener(touchListener);
        spinERComp.setOnTouchListener(touchListener);
        spinMFComp.setOnTouchListener(touchListener);
        spinCalibComp.setOnTouchListener(touchListener);
        spinSparePartsComp.setOnTouchListener(touchListener);

        industryCategory = new ArrayList<>();
        plantCategory = new ArrayList<>();
        cityCategory = new ArrayList<>();
        provinceCategory = new ArrayList<>();
        countryCategory = new ArrayList<>();
        areaCodeCategory = new ArrayList<>();
        countryCodeCategory = new ArrayList<>();
        numberCategory = Util.getOneHundredNumbers();

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
        btnSubmit = v.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(submitListener);

        loadOptions();

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Initializing");
        progress.setMessage("Preparing to add customer.\nPlease wait...");
        progress.setCancelable(false);
        progress.show();

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                progress.cancel();
                if (spinIndComp.getSelectedItem() == null) {
                    Fragment currFrag = getFragmentManager().findFragmentById(R.id.content_main);
                    Util.handleBackPress(currFrag, getActivity());
                    Util.alertBox(getActivity(), "Connection was not established." +
                                    "\nCheck data/wifi internet connectivity." +
                                    "\nCheck server availability.",
                            "Resource Empty", false);
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
                addCustomer();
            }
        }
    };

    private View.OnClickListener signPrevListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (companySignature == null) {
                Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (companySignature.isEmpty()) {
                Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show();
                return;
            }
            new DialogSignatureTask(getContext()).execute(companySignature);
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

    public void displaySignatureDialog() {
        //  create dialog for signature
        dialog = new Dialog(getActivity());

        //  removing features of normal dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(false);

        mContent = dialog.findViewById(R.id.linearLayout);
        mSignature = new Signature(getActivity().getApplicationContext(), null, mContent);
        mSignature.setBackgroundColor(Color.WHITE);

        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mClear = dialog.findViewById(R.id.btnSignClear);
        mGetSign = dialog.findViewById(R.id.btn_sign_save);
        mCancel = dialog.findViewById(R.id.btnSignCancel);

        signatureview = mContent;

        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSignature.clear();
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signatureview.setDrawingCacheEnabled(true);
                companySignature = mSignature.save(signatureview);
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
    }

    private void loadOptions() {
        industryMap = new HashMap<>();
        plantMap = new HashMap<>();
        zipCodeMap = new HashMap<>();
        cityMap = new HashMap<>();
        provinceMap = new HashMap<>();
        areaCodeMap = new HashMap<>();
        countryCodeMap = new HashMap<>();
        countryMap = new HashMap<>();

        new GetIndustryTask(getActivity(), industryCategory, industryMap, spinIndComp).execute();
        new GetPlantTask(getActivity(), plantCategory, plantMap, spinPlantComp).execute();
        new GetCityTask(getActivity(), cityCategory, cityMap, zipCodeMap, spinCityComp).execute();
        new GetProvinceTask(getActivity(), provinceCategory, provinceMap, spinProvComp).execute();
        new GetCountryTask(getActivity(), countryCategory, countryMap, spinCountComp).execute();

        new GetAreaCodeTask(getActivity(), areaCodeCategory, areaCodeMap, spinAreaCodeComp,
                spinFaxCodeComp).execute();

        new GetCountryCodeTask(getActivity(), countryCodeCategory, countryCodeMap,
                spinCountCodeComp, spinFaxCountCodeComp).execute();

        ArrayAdapter<Integer> numberAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, numberCategory);
        numberAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinERComp.setAdapter(numberAdapter);
        spinMFComp.setAdapter(numberAdapter);
        spinCalibComp.setAdapter(numberAdapter);
        spinSparePartsComp.setAdapter(numberAdapter);
    }

    /*** Listeners ***/

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            hideSoftKey();
            return false;
        }
    };

    private View.OnClickListener photoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

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

                if (etZipComp != null) {
                    etZipComp.setText(""+zipCodeMap.get(item));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
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
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    private AdapterView.OnItemSelectedListener spinnerCalibrationListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    private AdapterView.OnItemSelectedListener spinnerSparePartsListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera and storage permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String displayName = null;
    private Uri fileUri = null;
    private InputStream fileInputStream = null;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = Util.createImageFile(getActivity());
            } catch (IOException ex) {
                Util.displayStackTraceArray(ex.getStackTrace(), "android.wmdc.com.mobilecsa",
                        "IOException", ex.toString());
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
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
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
                        displayName = cursor.getString(
                                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        String size;

                        if (!cursor.isNull(sizeIndex)) {
                            size = cursor.getString(sizeIndex);
                        } else {
                            Toast.makeText(getActivity(), "Size unknown", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int intSize = Integer.parseInt(size);
                        String smallFileSize;

                        if (intSize > 512_000) {
                            File file = Util.createImageFile(getActivity());
                            Util.copyInputStreamToFile(Util.getStreamFromUri(uri, getActivity()),
                                    file, getContext());

                            File smallFile = Util.reduceBitmapFile(file);
                            smallFileSize = smallFile.length()+"";
                            fileInputStream = new FileInputStream(smallFile);
                        } else {
                            fileInputStream = Util.getStreamFromUri(uri, getActivity());
                            smallFileSize = size;
                        }

                        pd.cancel();

                        if (toSubmit) {
                            addCustomer();
                        } else {
                            tvPhotoName.setText(displayName);
                            tvPhotoSize.setText(Integer.parseInt(size)/1000+"KB. "+
                                    Integer.parseInt(smallFileSize)/1000+"KB.");
                        }
                    }
                } catch (IOException ie) {
                    pd.cancel();

                    Util.displayStackTraceArray(ie.getStackTrace(),
                            Variables.MOBILECSA_PACKAGE, "IOException", ie.toString());
                    Toast.makeText(getContext(), ie.toString(), Toast.LENGTH_SHORT).show();
                } finally {
                    cursor.close();
                }
            }
        };

        Handler pdHandler = new Handler();
        pdHandler.postDelayed(pdRun, 2000);
    }

    boolean is_submitted = false;

    private class AddCompanyTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        HttpURLConnection conn = null;
        URL url = null;

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
                url = new URL(sharedPreferences.getString("domain", null)+"addcustomercompany");

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
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "http://localhost:8080/mcsa/searchcustomerfromuser");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                DataOutputStream outputStream;
                outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"reference\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes("my_reference_text");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: " +
                        "form-data; name=\"photo\";filename=\"" +displayName+ "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead =  fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                Iterator<String> keys = stringParams.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = stringParams.get(key);

                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                int statusCode = conn.getResponseCode();

                outputStream.flush();
                outputStream.close();

                if (fileInputStream != null) {
                    fileInputStream.close();
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
            try {
                JSONObject response = new JSONObject(result);
                if (response.getBoolean("success")) {

                    AlertDialog.Builder warningBox = new AlertDialog.Builder(getActivity());

                    warningBox.setMessage(response.getString("reason"));
                    warningBox.setTitle("Success");
                    warningBox.setCancelable(false);
                    warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Util.handleBackPress(getActivity().getSupportFragmentManager().
                                        findFragmentById(R.id.content_main), getActivity());
                        }
                    });
                    warningBox.create().show();
                } else {
                    Util.alertBox(getActivity(), response.getString("reason"), "Failed", false);
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "Exception", e.toString());
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addCustomer() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        Log.d(String.valueOf(gpsTracker.getLatitude()), String.valueOf(gpsTracker.getLongitude()));

        if (displayName == null) {
            Toast.makeText(getActivity(), "Include Photo.", Toast.LENGTH_SHORT).show(); return;
        } else {
            if (displayName.isEmpty()) {
                Toast.makeText(getActivity(), "Include Photo.", Toast.LENGTH_SHORT).show(); return;
            }
        }

        if (companySignature == null) {
            Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show(); return;
        } else {
            if (companySignature.isEmpty()) {
                Toast.makeText(getActivity(), "Include signature.", Toast.LENGTH_SHORT).show(); return;
            }
        }

        try {
            stringParams = new HashMap<>();
            String company = etCompany.getText().toString();
            String address = etAddrComp.getText().toString();
            String telephone = etTeleComp.getText().toString();
            String mobile = etMobComp.getText().toString();
            String email = etEmailComp.getText().toString();
            String website = etWebComp.getText().toString();
            String emergency = etEmergComp.getText().toString();
            String zip = etZipComp.getText().toString();
            String fax = etFaxComp.getText().toString();
            String contactPerson = etContPersComp.getText().toString();
            String contactNumber = etContNumComp.getText().toString();

            String er = spinERComp.getSelectedItem().toString();
            String mf = spinMFComp.getSelectedItem().toString();
            String calib = spinCalibComp.getSelectedItem().toString();
            String spareParts = spinSparePartsComp.getSelectedItem().toString();
            String lat = "" + gpsTracker.getLatitude();
            String lng = "" + gpsTracker.getLongitude();
            String csaId = sharedPreferences.getInt("csaId", 0) + "";
            String signature = companySignature;

            if (spinIndComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Industry", Toast.LENGTH_LONG).show(); return;
            }

            if (spinPlantComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Plant", Toast.LENGTH_LONG).show(); return;
            }

            if (spinCityComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select City", Toast.LENGTH_LONG).show(); return;
            }

            if (spinProvComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Province", Toast.LENGTH_LONG).show(); return;
            }

            if (spinCountComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Country", Toast.LENGTH_LONG).show(); return;
            }

            if (spinFaxCodeComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Fax Code", Toast.LENGTH_LONG).show(); return;
            }

            if (spinFaxCountCodeComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Fax Country Code", Toast.LENGTH_LONG).show();
                return;
            }

            if (spinAreaCodeComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Area Code", Toast.LENGTH_LONG).show(); return;
            }

            if (spinCountCodeComp.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Select Country Code", Toast.LENGTH_LONG).show();
                return;
            }

            int industry = industryMap.get(spinIndComp.getSelectedItem().toString());
            int plant = plantMap.get(spinPlantComp.getSelectedItem().toString());
            int city = cityMap.get(spinCityComp.getSelectedItem().toString());
            int province = provinceMap.get(spinProvComp.getSelectedItem().toString());
            int country = countryMap.get(spinCountComp.getSelectedItem().toString());
            int faxCode = areaCodeMap.get(spinFaxCodeComp.getSelectedItem().toString());
            int faxCountryCode = countryCodeMap.get(spinFaxCountCodeComp.getSelectedItem().toString());
            int areaCode = areaCodeMap.get(spinAreaCodeComp.getSelectedItem().toString());
            int countryCode = countryCodeMap.get(spinCountCodeComp.getSelectedItem().toString());

            if (!Util.validEmail(email)) {
                Toast.makeText(getActivity(), "Email format is wrong", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Util.validURL(website)) {
                Toast.makeText(getActivity(), "Website format is wrong", Toast.LENGTH_LONG).show();
                return;
            }

            if (company.isEmpty()) {
                Toast.makeText(getActivity(), "Company is required.", Toast.LENGTH_LONG).show();
                return;
            }

            if (address.isEmpty()) {
                Toast.makeText(getActivity(), "Address is required.", Toast.LENGTH_LONG).show();
                return;
            }

            if (contactPerson.isEmpty()) {
                Toast.makeText(getActivity(), "Contact person is required.", Toast.LENGTH_LONG).show();
                return;
            }

            if (contactNumber.isEmpty()) {
                Toast.makeText(getActivity(), "Contact number is required.", Toast.LENGTH_LONG).show();
                return;
            }

            if (zip.isEmpty()) {
                Toast.makeText(getActivity(), "Zip is required.", Toast.LENGTH_LONG).show();
                return;
            }

            if (industry == 0) {
                Toast.makeText(getActivity(), "Select an industry.", Toast.LENGTH_LONG).show();
                return;
            }

            if (plant == 0) {
                Toast.makeText(getActivity(), "Select Plant.", Toast.LENGTH_LONG).show();
                return;
            }

            if (city == 0) {
                Toast.makeText(getActivity(), "Select City.", Toast.LENGTH_LONG).show();
                return;
            }

            if (province == 0) {
                Toast.makeText(getActivity(), "Select Province.", Toast.LENGTH_LONG).show();
                return;
            }

            if (country == 0) {
                Toast.makeText(getActivity(), "Select Country.", Toast.LENGTH_LONG).show();
                return;
            }

            if (faxCode == 0) {
                Toast.makeText(getActivity(), "Select Fax Code.", Toast.LENGTH_LONG).show();
                return;
            }

            if (faxCountryCode == 0) {
                Toast.makeText(getActivity(), "Select Fax Country Code.", Toast.LENGTH_LONG).show();
                return;
            }

            if (areaCode == 0) {
                Toast.makeText(getActivity(), "Select Area Code.", Toast.LENGTH_LONG).show();
                return;
            }

            if (countryCode == 0) {
                Toast.makeText(getActivity(), "Select Country Code.", Toast.LENGTH_LONG).show();
                return;
            }

            stringParams.put("company", company);
            stringParams.put("address", address);
            stringParams.put("industry", "" + industry);
            stringParams.put("plant", "" + plant);
            stringParams.put("city", "" + city);
            stringParams.put("province", "" + province);
            stringParams.put("country", "" + country);
            stringParams.put("fax", fax);
            stringParams.put("telephone", telephone);
            stringParams.put("mobile", mobile);
            stringParams.put("email", email);
            stringParams.put("website", website);
            stringParams.put("contactPerson", contactPerson);
            stringParams.put("contactNumber", contactNumber);
            stringParams.put("emergency", emergency);
            stringParams.put("faxCode", "" + faxCode);
            stringParams.put("faxCountryCode", "" + faxCountryCode);
            stringParams.put("areaCode", "" + areaCode);
            stringParams.put("countryCode", "" + countryCode);
            stringParams.put("zip", zip);
            stringParams.put("er", er);
            stringParams.put("mf", mf);
            stringParams.put("calib", calib);
            stringParams.put("spareParts", spareParts);
            stringParams.put("lat", lat);
            stringParams.put("lng", lng);
            stringParams.put("csaId", csaId);
            stringParams.put("username", "jeremy");
            stringParams.put("signature", signature);
            stringParams.put("signStatus", "true");

            AlertDialog.Builder confirmBox = new AlertDialog.Builder(getActivity());
            confirmBox.setMessage("Do really want to add customer?");
            confirmBox.setTitle("Confirm");
            confirmBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AddCompanyTask().execute();
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

    private void hideSoftKey() {
        View viewFocused = getActivity().getCurrentFocus();
        if (viewFocused != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocused.getWindowToken(), 0);
        }
        viewFocused.clearFocus();
    }
}