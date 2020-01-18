package android.wmdc.com.mobilecsa.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;

/**
 * Created by wmdcprog on 1/2/2018.
 */

public class Signature extends View
{
    private static final float STROKE_WIDTH = 5f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private Paint paint = new Paint();
    private Path path = new Path();

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();

    private LinearLayout mContent;

    public Signature(Context context, AttributeSet attrs, LinearLayout mContent)
    {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);

        this.mContent = mContent;
    }

    public String save(View v)
    {
        Bitmap signatureBitmap = null;

        if (signatureBitmap == null) {
            signatureBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
        }

        //  import for saving signature
        final Canvas canvas = new Canvas(signatureBitmap);
        draw(canvas);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);     //  bm is the bitmap object

        byte[] bytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float eventX = event.getX();
        float eventY = event.getY();
        //mGetSign.setEnabled(true);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                return true;

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:
                resetDirtyRect(eventX, eventY);
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }
                path.lineTo(eventX, eventY);
                break;
            default:
                debug("Ignored touch event: " + event.toString());
                return false;
        }

        invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouchX = eventX;
        lastTouchY = eventY;

        return true;
    }

    private void debug(String string) {
        Log.v("log_tag", string);
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }

        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }
}