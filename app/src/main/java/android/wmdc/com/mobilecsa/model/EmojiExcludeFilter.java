package android.wmdc.com.mobilecsa.model;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by wmdcprog on 6/1/2018.
 */

public class EmojiExcludeFilter implements InputFilter
{
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dent) {

        for (int i = start; i < end; i++)
        {
            int type = Character.getType(source.charAt(i));

            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }

        return null;
    }
}
