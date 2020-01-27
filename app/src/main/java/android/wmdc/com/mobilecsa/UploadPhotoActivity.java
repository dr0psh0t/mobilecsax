package android.wmdc.com.mobilecsa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import java.util.Date;

public class UploadPhotoActivity extends AppCompatActivity {
    SharedPreferences sPrefs;

    Button buttonCapture;
    Button buttonUpload;
    TextView textViewDetails;
    EditText editTextNum;

    String displayName;
    Uri fileUri = null;
    InputStream inputStream;
    int take_photo = 1;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(UploadPhotoActivity.this);

        buttonUpload = findViewById(R.id.buttonUpload);
        buttonCapture = findViewById(R.id.buttonCapture);
        textViewDetails = findViewById(R.id.textViewDetails);
        editTextNum = findViewById(R.id.editTextNum);

        progressDialog = new ProgressDialog(UploadPhotoActivity.this);
        progressDialog.setMessage("Dumping image. Please wait...");
        progressDialog.setCancelable(false);

        buttonCapture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                is_submitted = false;
                dispatchTakePictureIntent();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (is_submitted) {
                    System.out.println("Dumping again");
                    dumpImageMetaData(fileUri, true);
                } else {
                    System.out.println("no dump");
                    new UploadTask().execute("");
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                System.err.println(ex.toString());
                Toast.makeText(UploadPhotoActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                fileUri = FileProvider.getUriForFile(UploadPhotoActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, take_photo);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == take_photo && resultCode == RESULT_OK) {
            dumpImageMetaData(fileUri, false);
        }
    }

    public void dumpImageMetaData(final Uri uri, final boolean toSubmit) {
        final ProgressDialog pd = new ProgressDialog(UploadPhotoActivity.this);
        pd.setMessage("Dumping image. Please wait...");
        pd.setCancelable(false);
        pd.show();

        Runnable pdRun = new Runnable() {
            @Override
            public void run() {
                pd.cancel();

                Cursor cursor = getContentResolver().query(uri, null, null, null, null,null);

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        String size;

                        if (!cursor.isNull(sizeIndex)) {
                            size = cursor.getString(sizeIndex);
                        } else {
                            Toast.makeText(UploadPhotoActivity.this, "Size unknown", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int file_size = Integer.parseInt(size);
                        String small_file_size;

                        if (file_size > 512000) {
                            File file = createImageFile();
                            copyInputStreamToFile(getStreamFromUri(uri), file);

                            File smallFile = Util.reduceBitmapFile(file);
                            small_file_size = smallFile.length()+"";
                            inputStream = new FileInputStream(smallFile);
                        } else {
                            inputStream = getStreamFromUri(uri);
                            small_file_size = size;
                        }

                        if (toSubmit) {
                            new UploadTask().execute("");
                        } else {
                            String details = displayName + "\n" + (Integer.parseInt(size) / 1000 + "KB.\n" +
                                    Integer.parseInt(small_file_size) / 1000 + "KB.");
                            textViewDetails.setText(details);
                        }
                    }
                } catch (IOException ie) {
                    Util.displayStackTraceArray(ie.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                            "IOException", ie.toString());
                    Toast.makeText(UploadPhotoActivity.this, ie.toString(), Toast.LENGTH_SHORT).show();
                } finally {
                    cursor.close();
                }
            }
        };

        Handler pdHandler = new Handler();
        pdHandler.postDelayed(pdRun, 2000);
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        } catch (Exception e) {
            Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                    "Exception", e.toString());
            Toast.makeText(UploadPhotoActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            } catch ( IOException e ) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "IOException", e.toString());
                Toast.makeText(UploadPhotoActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private InputStream getStreamFromUri(Uri uri) throws IOException {
        return getContentResolver().openInputStream(uri);
    }

    boolean is_submitted = false;

    private class UploadTask extends AsyncTask<String, String, String> {
        HttpURLConnection conn = null;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            try {
                url = new URL(sPrefs.getString("domain", null)+"uploadphoto");

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
                conn.setRequestProperty("Cookie", "JSESSIONID="+sPrefs.getString("sessionId",
                        null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "newquotation-android");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes(twoHypens+boundary+lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; " +
                        "name\"reference\""+lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes("my_reference_text");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHypens+boundary+lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; " +
                        "name=\"photo\";filename\""+displayName+"\""+lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = inputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = inputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);

                outputStream.writeBytes(twoHypens+boundary+lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"num\""+lineEnd);
                outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(editTextNum.getText().toString());
                outputStream.writeBytes(lineEnd);

                outputStream.writeBytes(twoHypens+boundary+twoHypens+lineEnd);
                int statusCode = conn.getResponseCode();

                outputStream.flush();
                outputStream.close();
                inputStream.close();

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
            try {
                JSONObject response = new JSONObject(result);
                if (response.getBoolean("success")) {
                    AlertDialog.Builder warningBox =
                            new AlertDialog.Builder(UploadPhotoActivity.this);

                    warningBox.setTitle("Success");
                    warningBox.setMessage(response.getString("reason"));
                    warningBox.setCancelable(false);
                    warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    warningBox.create().show();
                } else {
                    Util.alertBox(UploadPhotoActivity.this, response.getString("reason"), "Failed", false);
                    displayName = null;
                }
            } catch (Exception e) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "exception", e.toString());
                Toast.makeText(UploadPhotoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
