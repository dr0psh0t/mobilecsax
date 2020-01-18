package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;

import com.github.chrisbanes.photoview.PhotoView;

public class DialogImageUriTask extends AsyncTask<Uri, String, Uri> {
    private Context context;
    PhotoView imageView;
    Dialog builder;

    public DialogImageUriTask(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        imageView = new PhotoView(context);
        builder = new Dialog(context);

        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected Uri doInBackground(Uri[] uris) {
        return uris[0];
    }

    protected void onPostExecute(Uri uri) {
        if (uri != null) {
            imageView.setImageURI(uri);
            builder.show();
        } else {
            AlertDialog.Builder warningBox = new AlertDialog.Builder(context);

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