package android.wmdc.com.mobilecsa.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.AddCompanyFragment;
import android.wmdc.com.mobilecsa.AddContactFragment;
import android.wmdc.com.mobilecsa.AddCustomerFragment;
import android.wmdc.com.mobilecsa.ApprovalFragment;
import android.wmdc.com.mobilecsa.CRMFragment;
import android.wmdc.com.mobilecsa.ContactFragment;
import android.wmdc.com.mobilecsa.ContactsResultFragment;
import android.wmdc.com.mobilecsa.CustomerFragment;
import android.wmdc.com.mobilecsa.CustomerResultFragment;
import android.wmdc.com.mobilecsa.DateCommitFragment;
import android.wmdc.com.mobilecsa.HomeFragment;
import android.wmdc.com.mobilecsa.JOFragment;
import android.wmdc.com.mobilecsa.JOResultFragment;
import android.wmdc.com.mobilecsa.InitialJobOrder;
import android.wmdc.com.mobilecsa.JoborderStatusFragment;
import android.wmdc.com.mobilecsa.QualityCheckFragment;
import android.wmdc.com.mobilecsa.InitialJoborderFragment;
import android.wmdc.com.mobilecsa.InitialJoborderList;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.SearchContactFragment;
import android.wmdc.com.mobilecsa.SearchCustomerFragment;
import android.wmdc.com.mobilecsa.SearchJOFragment;
import android.wmdc.com.mobilecsa.asynchronousclasses.GetInitialJoborderListTask;
import android.wmdc.com.mobilecsa.model.EmojiExcludeFilter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wmdcprog on 12/2/2017.
 */

public class Util {

    public static ProgressBar progressBarMain;

    public static SharedPreferences sharedPreferences;

    public static int systemHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    public static int systemWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final int CONNECTION_TIMEOUT = 25_000;
    public static final int READ_TIMEOUT = 25_000;

    public static int recyclerViewItemHeight = 0;

    public static boolean validUserPass(String pass) {
        return (pass.length() > 7 && pass.length() < 33);
    }

    public static void minKey(Context context) {
        View viewFocused = ((AppCompatActivity)context).getCurrentFocus();

        if (viewFocused != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocused.getWindowToken(), 0);
        }
    }

    public static Dialog getProgressBar(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } else {
            Util.longToast(context, "Dialog Window is null.");
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_layout);
        dialog.setCancelable(true);
        return dialog;
    }

    public static void shortToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void alertBox(final Context context, String msg) {
        AlertDialog.Builder warningBox = new AlertDialog.Builder(context);

        warningBox.setMessage(msg);
        warningBox.setCancelable(false);

        warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        warningBox.create().show();
    }

    public static void alertBox(final Context context, String message, String title,
                                final boolean minimizable) {

        AlertDialog.Builder warningBox = new AlertDialog.Builder(context);

        warningBox.setTitle(title);
        warningBox.setMessage(message);
        warningBox.setCancelable(false);

        warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

                if (minimizable) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(startMain);
                }
            }
        });

        warningBox.create().show();
    }

    /*
    public static boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return true;
        } else {
            return email.matches(EMAIL_PATTERN);
        }
    }*/

    public static boolean isInvalidEmail(String email) {
        if (email.isEmpty()) {
            return false;   //  empty email is always valid since email is not required.
        } else {
            return !email.matches(EMAIL_PATTERN);
        }
    }

    public static boolean validURL(String url) {
        if (url.isEmpty()) {
            return true;
        }

        Pattern pattern = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?" +
                "[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
        Matcher matcher = pattern.matcher(url);

        return matcher.matches();
    }

    public static ArrayList<Integer> getOneHundredNumbers() {
        ArrayList<Integer> numberList = new ArrayList<>(100);

        for (int x = 0; x <= 100; ++x) {
            numberList.add(x);
        }

        return numberList;
    }

    public static void handleBackPress(Fragment currFrag, FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        if (currFrag == null) {
            activity.onBackPressed();
        } else {
            if (currFrag instanceof CRMFragment) {
                HomeFragment homeFragment = new HomeFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit)
                        .replace(R.id.content_main, homeFragment)
                        .commit();
            } else if (currFrag instanceof ContactFragment
                    || currFrag instanceof CustomerFragment) {

                Fragment crmFragment = new CRMFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit)
                        .replace(R.id.content_main, crmFragment)
                        .commit();
            } else if (currFrag instanceof AddCustomerFragment
                    || currFrag instanceof AddCompanyFragment
                    || currFrag instanceof SearchCustomerFragment) {

                CustomerFragment customersFragment = new CustomerFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, customersFragment).commit();
            } else if (currFrag instanceof AddContactFragment
                    || currFrag instanceof SearchContactFragment) {
                ContactFragment contactsFragment = new ContactFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, contactsFragment).commit();
            } else if (currFrag instanceof ContactsResultFragment) {
                SearchContactFragment searchContactFragment = new SearchContactFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, searchContactFragment)
                        .commit();
            } else if (currFrag instanceof CustomerResultFragment) {
                SearchCustomerFragment searchCustomersFragment = new SearchCustomerFragment();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, searchCustomersFragment)
                        .commit();
            } else if (currFrag instanceof HomeFragment) {
                activity.finish();
            } else if (currFrag instanceof JOFragment) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, new HomeFragment())
                        .commit();
            } else if (currFrag instanceof ApprovalFragment || currFrag instanceof SearchJOFragment
                    || currFrag instanceof InitialJobOrder
                    || currFrag instanceof InitialJoborderList) {

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, new JOFragment()).commit();
            } else if (currFrag instanceof QualityCheckFragment
                    || currFrag instanceof DateCommitFragment) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, new ApprovalFragment())
                        .commit();
            } else if (currFrag instanceof JoborderStatusFragment) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, new SearchJOFragment())
                        .commit();
            } else if (currFrag instanceof JOResultFragment) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter,
                                R.anim.exit).replace(R.id.content_main, new SearchJOFragment())
                        .commit();
            } else if (currFrag instanceof InitialJoborderFragment) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                new GetInitialJoborderListTask(activity).execute(String.valueOf(
                        prefs.getInt("csaId", 0)));
            }
        }
    }

    public static String val(Object var) {
        return String.valueOf(var);
    }

    public static String filterSpecialChars(String str) {
        return getFiltered(str);
    }

    public static String getFiltered(String str) {
        str = str.replaceAll("[^A-Za-z0-9\\s+]", "");
        str = str.trim();
        str = str.replaceAll("[\\s+]{2,}", " ");
        str = str.replaceAll("\n", "");

        return str;
    }

    public static String getDecodedHTMLEntity(String origText, String escapedText) {
        if (Build.VERSION.SDK_INT >= 24) {
            escapedText = Html.fromHtml(origText, Html.FROM_HTML_MODE_LEGACY).toString();

            if (escapedText.length() == origText.length()) {
                return escapedText;
            } else {
                return getDecodedHTMLEntity(escapedText, escapedText);
            }
        } else {
            escapedText = Html.fromHtml(origText).toString();

            if (escapedText.length() == origText.length()) {
                return escapedText;
            } else {
                return getDecodedHTMLEntity(escapedText, escapedText);
            }
        }
    }

    public static InputFilter[] getEmojiFilters(int length) {
        return new InputFilter[] { new EmojiExcludeFilter(), new InputFilter.LengthFilter(length) };
    }

    public static InputFilter[] getEmojiFilters() {
        return new InputFilter[] { new EmojiExcludeFilter() };
    }

    public static File reduceBitmapFile(File file) {
        try {
            //  BitmapFactory options to downsize the image
            BitmapFactory.Options options = new BitmapFactory.Options();

            /*
            Setting the inJustDecodeBounds property to true while decoding
            avoids memory allocation, returning null for the bitmap object
            but setting outWidth, outHeight, and outMimeType. This technique
            allows you to read the dimensions and type of the image data
            prior to construction (and memory allocation) of the bitmap.
             */
            options.inJustDecodeBounds = true;

            options.inSampleSize = 6;

            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            //  the new size we want to scale to
            final int REQUIRED_SIZE = 75;

            //  find the correct scale value. it should be the power of 2.
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    options.outHeight / scale / 2 >= REQUIRED_SIZE) {

                scale *= 2;
            }

            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options2);
            inputStream.close();

            //  here override the original image file
            file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            return file;
        } catch (Exception e) {
            displayStackTraceArray(e.getStackTrace(), "android.wmdc.com.mobilecsa.utils",
                    "Exception", e.toString());
            return null;
        }
    }

    public static void displayStackTraceArray(StackTraceElement[] stackTraceElements,
        String packageRoot, String exceptionName, String toString) {

        Log.e(exceptionName, toString);

        for (StackTraceElement elem : stackTraceElements) {
            if (elem.toString().contains(packageRoot)) {
                Log.e("src", elem.toString());
            }
        }
    }

    public static File createImageFile(FragmentActivity fragmentActivity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /*
    public static void performFileSearch(FragmentActivity activity, int request_code) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, request_code);
    }*/

    public static void copyInputStreamToFile(InputStream in, File file, Context context) {
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
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            } catch ( IOException e ) {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MOBILECSA_PACKAGE,
                        "IOException", e.toString());
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static InputStream getStreamFromUri(Uri uri, FragmentActivity fragmentActivity)
            throws IOException {
        return fragmentActivity.getContentResolver().openInputStream(uri);
    }
}