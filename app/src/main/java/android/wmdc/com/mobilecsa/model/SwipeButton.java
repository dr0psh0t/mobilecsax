package android.wmdc.com.mobilecsa.model;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wmdc.com.mobilecsa.R;
import android.wmdc.com.mobilecsa.adapter.DCValueInfoAdapter;
import android.wmdc.com.mobilecsa.adapter.DateCommitAdapter;
import android.wmdc.com.mobilecsa.utils.Util;
import android.wmdc.com.mobilecsa.utils.Variables;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by wmdcprog on 3/24/2018.
 * https://android.jlelse.eu/make-a-great-android-ux-how-to-make-a-swipe-button-eefbf060326d
 */

public class SwipeButton extends RelativeLayout
{
    private ImageView slidingButton;
    private float initialX;
    private boolean active;
    private int initialButtonWidth;
    private TextView centerText;

    private Drawable disabledDrawable;
    private Drawable enabledDrawable;

    private ImageView ivItemStatQC = null;
    private Dialog dialogContainer = null;
    private TextView tvSwipe = null;

    //  parameters
    private int cid;
    private String source;
    private int joid;
    private int woid;    //  for QC

    private boolean isQCTransaction;

    private DateCommitAdapter dateCommitAdapter;
    private ArrayList<DateCommitModel> dcData;
    private int dcJoborderId;

    public void setParameters(int cid, String source, int joid, int woid, boolean isQCTransaction)
    {
        this.cid = cid;
        this.source = source;
        this.joid = joid;
        this.woid = woid;
        this.isQCTransaction = isQCTransaction;
    }

    public void setDateCommitList(ArrayList<DateCommitModel> dcData) {
        this.dcData = dcData;
    }

    public void setDateCommitAdapter(DateCommitAdapter dateCommitAdapter) {
        this.dateCommitAdapter = dateCommitAdapter;
    }

    public void setDcJoborderId(int dcJoborderId) {
        this.dcJoborderId = dcJoborderId;
    }

    public SwipeButton(Context context)
    {
        super(context);
        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(21)
    public SwipeButton(Context context, AttributeSet attrs,
                       int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setItemStat(ImageView ivItemStatQC) {
        this.ivItemStatQC = ivItemStatQC;
    }

    public void setDialogContainer(Dialog dialogContainer) {
        this.dialogContainer = dialogContainer;
    }

    public void setTVSwipe(TextView tvSwipe) {
        this.tvSwipe = tvSwipe;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        RelativeLayout background = new RelativeLayout(context);

        LayoutParams layoutParamsView = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        background.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rounded));

        addView(background, layoutParamsView);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
                RelativeLayout.TRUE);
        centerText.setText("SLIDE");
        centerText.setTextColor(Color.WHITE);
        centerText.setPadding(15, 15, 15, 15);
        background.addView(centerText, layoutParams);

        final ImageView swipeButton = new ImageView(context);
        this.slidingButton = swipeButton;

        disabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play_arrow);
        enabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_check_color_white);

        slidingButton.setImageDrawable(disabledDrawable);
        slidingButton.setPadding(20, 20, 20, 20);

        LayoutParams layoutParamsButton = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL,
                RelativeLayout.TRUE);

        swipeButton.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_button));
        swipeButton.setImageDrawable(disabledDrawable);
        addView(swipeButton, layoutParamsButton);

        setOnTouchListener(getButtonTouchListener());
    }

    boolean skipped;

    private OnTouchListener getButtonTouchListener()
    {
        return  new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:

                        if (slidingButton.getWidth() >= event.getX()) {
                            skipped = false;
                        }
                        else {
                            skipped = true;
                        }

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        if (!skipped)
                        {
                            if (initialX == 0) {
                                initialX = slidingButton.getX();
                            }

                            if (event.getX() > initialX + slidingButton.getWidth() / 2 &&
                                    event.getX() + slidingButton.getWidth() / 2 < getWidth()) {

                                slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
                                centerText.setAlpha(1 - 1.3f * (slidingButton.getX() +
                                        slidingButton.getWidth()) / getWidth());
                            }

                            if (event.getX() + slidingButton.getWidth() / 2 > getWidth() &&
                                    slidingButton.getX() + slidingButton.getWidth() / 2 <
                                            getWidth()) {

                                slidingButton.setX(getWidth() - slidingButton.getWidth());
                            }

                            if (event.getX() < slidingButton.getWidth() / 2 &&
                                    slidingButton.getX() > 0) {
                                slidingButton.setX(0);
                            }
                        }

                        return true;

                    case MotionEvent.ACTION_UP:

                        if (!active)
                        {
                            initialButtonWidth = slidingButton.getWidth();

                            if ((slidingButton.getX() + slidingButton.getWidth()) > getWidth() * 0.99)
                            {
                                if (ivItemStatQC != null)
                                {
                                    new ApproveQCTask().execute(
                                            String.valueOf(joid),
                                            String.valueOf(cid),
                                            source,
                                            String.valueOf(woid)
                                    );
                                }
                                else
                                {
                                    new ApproveDCTask().execute(
                                            String.valueOf(joid),
                                            String.valueOf(cid),
                                            source
                                    );
                                }
                            }
                            else {
                                moveButtonBack();
                            }
                        }

                        return true;
                }

                return false;
            }
        };
    }

    private void expandButton()
    {
        final ValueAnimator positionAnimator = ValueAnimator.ofFloat(slidingButton.getX(), 0);

        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(x);
            }
        });

        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                getWidth());

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                active = true;
                slidingButton.setImageDrawable(enabledDrawable);
            }
        });

        animatorSet.playTogether(positionAnimator, widthAnimator);
        animatorSet.start();
    }

    private void collapseButton()
    {
        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                initialButtonWidth);

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params =  slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });

        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                active = false;
                slidingButton.setImageDrawable(disabledDrawable);
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(objectAnimator, widthAnimator);
        animatorSet.start();
    }

    private void moveButtonBack()
    {
        final ValueAnimator positionAnimator = ValueAnimator.ofFloat(slidingButton.getX(), 0);

        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(x);
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1);

        positionAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }


    private class ApproveQCTask extends AsyncTask<String, String, JSONObject>
    {
        private HttpURLConnection conn = null;
        private URL url;
        private SharedPreferences sharedPreferences;

        public ApproveQCTask() {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }

        protected void onPreExecute()
        {
            super.onPreExecute();
            dialogContainer.dismiss();
            Util.progressBarMain.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(String[] params)
        {
            JSONObject responseJson = new JSONObject();

            try
            {
                url = new URL(sharedPreferences.getString("domain", null)+"approvemcsaqc");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "Approve-Date-Commit");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1: Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder build = new Uri.Builder()
                        .appendQueryParameter("joid", params[0])
                        .appendQueryParameter("cid", params[1])
                        .appendQueryParameter("source", params[2])
                        .appendQueryParameter("woid", params[3]);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.write(build.build().getEncodedQuery());
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int resCode = conn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK)
                {
                    InputStream connInputStream = conn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connInputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    responseJson = new JSONObject(stringBuilder.toString());
                }
                else
                {
                    responseJson.put("success", false);
                    responseJson.put("reason", "Request failed.");
                }

                return responseJson;
            }
            catch (MalformedURLException | ConnectException | SocketTimeoutException e)
            {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MODEL_PACKAGE,
                        "network_exception", e.toString());

                try
                {
                    responseJson.put("success", false);

                    if (e instanceof MalformedURLException) {
                        responseJson.put("reason", "Invalid URL");
                    }
                    else if (e instanceof ConnectException) {
                        responseJson.put("reason", "Cannot connect to server. Check wifi/data connection.");
                    }
                    else if (e instanceof SocketTimeoutException) {
                        responseJson.put("reason", "Connection timed out.");
                    }
                }
                catch (JSONException je)
                {
                    Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                            "json_exception", je.toString());
                }
                finally {
                    return responseJson;
                }
            }
            catch (Exception e)
            {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MODEL_PACKAGE,
                        "exception", e.toString());

                try
                {
                    responseJson.put("success", false);
                    responseJson.put("reason", e.toString());
                }
                catch (JSONException je)
                {
                    Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                            "json_exception", je.toString());
                }
                finally {
                    return responseJson;
                }
            }
            finally
            {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        protected void onPostExecute(final JSONObject responseJson)
        {
            try
            {
                final boolean res = responseJson.getBoolean("success");

                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        Util.progressBarMain.setVisibility(View.GONE);

                        if (res)
                        {
                            Variables.attemptedApproves++;

                            expandButton();
                            ivItemStatQC.setImageResource(R.drawable.ic_action_check_colored_round);

                            TextView text = new TextView(getContext());
                            text.setText("Quality Check has been approved.");
                            text.setTextSize(17);
                            text.setPadding(20, 0, 10, 0);
                            text.setGravity(Gravity.CENTER_HORIZONTAL);

                            AlertDialog.Builder alertBox = new AlertDialog.Builder(getContext());
                            alertBox.setView(text);
                            alertBox.setTitle("Approved");
                            alertBox.setCancelable(false);

                            alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogContainer.cancel();
                                }
                            });

                            AlertDialog dialog = alertBox.create();
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                            wmlp.gravity = Gravity.TOP;
                            wmlp.y = 200;
                            dialog.show();
                        }
                        else
                        {
                            moveButtonBack();

                            String errMsg;

                            try{
                                errMsg = responseJson.getString("reason");
                            }
                            catch (JSONException e) {
                                errMsg = e.toString();
                            }

                            TextView text = new TextView(getContext());
                            text.setText(errMsg);
                            text.setTextSize(17);
                            text.setPadding(20, 0, 10, 0);
                            text.setGravity(Gravity.CENTER_HORIZONTAL);

                            AlertDialog.Builder alertBox = new AlertDialog.Builder(getContext());
                            alertBox.setView(text);
                            alertBox.setTitle("Failed");
                            alertBox.setCancelable(false);

                            alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            AlertDialog dialog = alertBox.create();
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                            wmlp.gravity = Gravity.TOP;
                            wmlp.y = 200;
                            dialog.show();
                        }
                    }
                };

                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 1000);
            }
            catch (JSONException je)
            {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                        "json_exception", je.toString());

                Toast.makeText(getContext(), je.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ApproveDCTask extends AsyncTask<String, String, JSONObject>
    {
        private HttpURLConnection conn = null;
        private URL url;
        private SharedPreferences sharedPreferences;

        public ApproveDCTask() {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }

        protected void onPreExecute()
        {
            super.onPreExecute();
            Util.progressBarMain.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(String[] params)
        {
            JSONObject responseJson = new JSONObject();

            try
            {
                url = new URL(sharedPreferences.getString("domain", null)+"approvemcsadatecommit");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setRequestProperty("Cookie", "JSESSIONID="+sharedPreferences.getString("sessionId", null));
                conn.setRequestProperty("Host", "localhost:8080");
                conn.setRequestProperty("Referer", "Approve-Quality-Check");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1: Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("X-Requested-Width", "XMLHttpRequest");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder build = new Uri.Builder()
                        .appendQueryParameter("joid", params[0])
                        .appendQueryParameter("cid", params[1])
                        .appendQueryParameter("source", params[2]);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.write(build.build().getEncodedQuery());
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int resCode = conn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK)
                {
                    InputStream connInputStream = conn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connInputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    responseJson = new JSONObject(stringBuilder.toString());
                }
                else
                {
                    responseJson.put("success", false);
                    responseJson.put("reason", "Request failed.");
                }

                return responseJson;
            }
            catch (MalformedURLException | ConnectException | SocketTimeoutException e)
            {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MODEL_PACKAGE,
                        "network_exception", e.toString());

                try
                {
                    responseJson.put("success", false);

                    if (e instanceof MalformedURLException) {
                        responseJson.put("reason", "Invalid URL");
                    }
                    else if (e instanceof ConnectException) {
                        responseJson.put("reason", "Cannot connect to server. Check wifi/data connection.");
                    }
                    else if (e instanceof SocketTimeoutException) {
                        responseJson.put("reason", "Connection timed out.");
                    }
                }
                catch (JSONException je)
                {
                    Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                            "json_exception", je.toString());
                }
                finally {
                    return responseJson;
                }
            }
            catch (Exception e)
            {
                Util.displayStackTraceArray(e.getStackTrace(), Variables.MODEL_PACKAGE,
                        "exception", e.toString());

                try
                {
                    responseJson.put("success", false);
                    responseJson.put("reason", e.toString());
                }
                catch (JSONException je)
                {
                    Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                            "exception", je.toString());
                }
                finally {
                    return responseJson;
                }
            }
            finally
            {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        protected void onPostExecute(final JSONObject responseJson)
        {
            try
            {
                final boolean res = responseJson.getBoolean("success");

                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        Util.progressBarMain.setVisibility(View.GONE);

                        if (res)
                        {
                            DCValueInfoAdapter.setIconOnApprovedDC();

                            TextView text = new TextView(getContext());
                            text.setText("Date Commit has been approved.");
                            text.setTextSize(17);
                            text.setPadding(20, 0, 10, 0);
                            text.setGravity(Gravity.CENTER_HORIZONTAL);

                            AlertDialog.Builder alertBox = new AlertDialog.Builder(getContext());
                            alertBox.setView(text);
                            alertBox.setTitle("Approved");
                            alertBox.setCancelable(false);

                            alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    for (DateCommitModel dateCommitModel : dcData)
                                    {
                                        if (dateCommitModel.getJoId() == dcJoborderId)
                                        {
                                            dateCommitModel.setCsaApproved(true);
                                            dateCommitAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }
                            });

                            alertBox.create().show();
                            expandButton();
                        }
                        else
                        {
                            moveButtonBack();

                            String errMsg;

                            try{
                                errMsg = responseJson.getString("reason");
                            }
                            catch (JSONException e) {
                                errMsg = e.toString();
                            }

                            TextView text = new TextView(getContext());
                            text.setText(errMsg);
                            text.setTextSize(17);
                            text.setPadding(20, 0, 10, 0);
                            text.setGravity(Gravity.CENTER_HORIZONTAL);

                            AlertDialog.Builder alertBox = new AlertDialog.Builder(getContext());
                            alertBox.setView(text);
                            alertBox.setTitle("Failed");
                            alertBox.setCancelable(false);

                            alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            });

                            alertBox.create().show();
                        }
                    }
                };

                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 1000);
            }
            catch (JSONException je)
            {
                Util.displayStackTraceArray(je.getStackTrace(), Variables.MODEL_PACKAGE,
                        "json_exception", je.toString());

                Toast.makeText(getContext(), je.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}