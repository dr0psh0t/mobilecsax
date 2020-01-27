package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.wmdc.com.mobilecsa.utils.Util;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.lang.ref.WeakReference;

public class DialogImageUriTask extends AsyncTask<Uri, String, Uri> {

    private WeakReference<FragmentActivity> weakReference;

    public DialogImageUriTask(FragmentActivity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Uri doInBackground(Uri[] uris) {
        return uris[0];
    }

    protected void onPostExecute(Uri uri) {
        if (uri != null) {
            PhotoView photoView = new PhotoView(weakReference.get());

            Dialog builder = new Dialog(weakReference.get());

            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

            if (builder.getWindow() != null) {
                builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            } else {
                Util.longToast(weakReference.get(),
                        "Builder Window is null. Cannot set background drawable to dialog.");
            }

            builder.addContentView(photoView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            photoView.setImageURI(uri);
            builder.show();
        } else {
            AlertDialog.Builder warningBox = new AlertDialog.Builder(weakReference.get());

            warningBox.setMessage("A problem has occured in displaying Photo.");
            warningBox.setTitle("Error");
            warningBox.setCancelable(true);

            warningBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            warningBox.create().show();
        }
    }
}