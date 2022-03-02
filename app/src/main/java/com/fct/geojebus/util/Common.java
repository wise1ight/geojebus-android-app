package com.fct.geojebus.util;

import android.content.Context;
import android.widget.Toast;

import com.fct.geojebus.R;

/**
 * Created by KUvH on 2015-07-15.
 */
public class Common {

    public Common() {

    }

    public void showErrorToast(Context context, int code) {
        switch (code) {
            case 1:
                Toast.makeText(context, context.getString(R.string.toast_error_1), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(context, context.getString(R.string.toast_error_2), Toast.LENGTH_SHORT).show();
                break;
            case -101:
                Toast.makeText(context, context.getString(R.string.toast_error_101), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, context.getString(R.string.toast_error_not_define) + String.valueOf(code), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
