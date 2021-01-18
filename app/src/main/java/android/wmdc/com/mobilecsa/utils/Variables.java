package android.wmdc.com.mobilecsa.utils;

import org.json.JSONObject;

/**
 * Created by wmdcprog on 12/2/2017.
 */

public class Variables {

    public static CharSequence headerTitle = "";

    public static JSONObject qcStore;
    //public static JSONObject dcStore;

    public static String source;

    public static int attemptedApproves = 0;

    public static final String ADAPTER_PACKAGE = "android.wmdc.com.mobilecsa.adapter";
    public static final String ASYNCHRONOUS_PACKAGE = "android.wmdc.com.mobilecsa.asynchronousclasses";
    public static final String MODEL_PACKAGE = "android.wmdc.com.mobilecsa.model";
    public static final String MOBILECSA_PACKAGE = "android.wmdc.com.mobilecsa";

    public static int currentPage = -1;
    public static int totalCount = 0;
    public static int lastPage = 0;

    public static String dcRawResult = "";
}