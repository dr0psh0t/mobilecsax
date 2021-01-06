package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.lang.ref.WeakReference;

/**
 * Created by wmdcprog on 7/2/2018.
 */

public class DialogSignatureTask extends AsyncTask<String, String, Bitmap> {

    private WeakReference<FragmentActivity> weakReference;

    public DialogSignatureTask(FragmentActivity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Bitmap doInBackground(String[] unpureBase64) {
        String pureBase64 = unpureBase64[0].substring(unpureBase64[0].indexOf(",")+1);

        byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            PhotoView signPhotoView = new PhotoView(weakReference.get());
            signPhotoView.setImageBitmap(result);

            Dialog builder = new Dialog(weakReference.get());
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

            if (builder.getWindow() != null) {
                builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            } else {
                Log.e("Null", "Builder Window is null. Cannot set background drawable to dialog.");
            }

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {}
            });
            builder.addContentView(signPhotoView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            builder.show();
        }
    }
}