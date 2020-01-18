package android.wmdc.com.mobilecsa.asynchronousclasses;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.github.chrisbanes.photoview.PhotoView;

public class DialogSignatureTask extends AsyncTask<String, String, Bitmap> {

    private Context context;

    public DialogSignatureTask(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Bitmap doInBackground(String[] unpureBase64)
    {
        String pureBase64 = unpureBase64[0].substring(unpureBase64[0].indexOf(",")+1);

        byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,
                decodedString.length);

        return decodedByte;
    }

    protected void onPostExecute(Bitmap result)
    {
        if (result != null)
        {
            PhotoView signPhotoView = new PhotoView(context);
            signPhotoView.setImageBitmap(result);

            Dialog builder = new Dialog(context);
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
            builder.addContentView(signPhotoView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            builder.show();
        }
    }
}