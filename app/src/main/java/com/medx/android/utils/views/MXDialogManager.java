package com.medx.android.utils.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.medx.android.R;
import com.medx.android.interfaces.MXDialogClickListener;

public class MXDialogManager {

    static AlertDialog dialog;

    public static AlertDialog showSimpleDialog(Context context, String title, String message) {
        return showSimpleDialog(context, title, message, null);
    }

    public static AlertDialog showCustomDialog(Context context, View view, String title, DialogInterface.OnClickListener... callbacks) {
        return showCustomDialog(context, view, title, (Object[]) callbacks);
    }

    public static AlertDialog showCustomDialog(Context context, View view, String title, MXDialogClickListener... callbacks) {
        return showCustomDialog(context, view, title, (Object[]) callbacks);
    }

    public static AlertDialog showCustomDialog(Context context, View view, String title, Object... callbacks) {
        AlertDialog.Builder adb = buildAlertDialog(context, title);
        addCallbacks(context, adb, callbacks);
        adb.setView(view);
        return show(context, adb);
    }

    private static AlertDialog show(Context context, AlertDialog.Builder adb) {
        if (context instanceof Activity)
            if (!((Activity) context).isFinishing())
                return dialog = adb.show();
        return dialog = adb.create();
    }

    private static AlertDialog.Builder buildAlertDialog(Context context, String title) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        if (!TextUtils.isEmpty(title))
            adb.setTitle(title);
        return adb;
    }

    private static void addCallbacks(Context context, AlertDialog.Builder adb, Object... callbacks) {
        if (callbacks != null && callbacks.length > 0) {
            String title = null;
            DialogInterface.OnClickListener callback = null;
            for (int i = 0; i < callbacks.length; i++) {
                Object obj = callbacks[i];
                if (obj instanceof MXDialogClickListener) {
                    title = ((MXDialogClickListener) obj).getName();
                    callback = ((MXDialogClickListener) obj);
                } else {
                    callback = (DialogInterface.OnClickListener) obj;
                    switch (i) {
                        case 0:
                            title = context.getString(R.string.positive_btn);
                            break;
                        case 1:
                            title = context.getString(R.string.negative_btn);
                            break;
                        case 2:
                            title = context.getString(R.string.neutral_btn);
                            break;
                    }
                }
                adb.setPositiveButton(title, callback);
            }
        } else adb.setPositiveButton(context.getString(R.string.positive_btn), null);
    }



    public static AlertDialog showSimpleDialog(Context context, String title, String message, DialogInterface.OnClickListener... callbacks) {
        if (dialog != null)
            dialog.cancel();
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(title);
        if (callbacks != null)
            for (int i = 0; i < callbacks.length; i++) {
                DialogInterface.OnClickListener callback = callbacks[i];
                switch (i) {
                    case 0:
                        adb.setPositiveButton(context.getString(R.string.positive_btn), callback);
                        break;
                    case 1:
                        adb.setNegativeButton(context.getString(R.string.negative_btn), callback);
                        break;
                    case 2:
                        adb.setNeutralButton(context.getString(R.string.neutral_btn), callback);
                        break;
                }
            }
        else adb.setPositiveButton(context.getString(R.string.positive_btn), null);
        adb.setMessage(message);
        if (context instanceof Activity)
            if (!((Activity) context).isFinishing())
                return dialog = adb.show();
        return dialog = adb.create();
    }

    public static AlertDialog showSimpleDialog(Context context, String title, String message, MXDialogClickListener... callbacks) {
        if (dialog != null)
            dialog.cancel();
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(title);
        if (callbacks != null)
            for (int i = 0; i < callbacks.length; i++) {
                MXDialogClickListener callback = callbacks[i];
                switch (i) {
                    case 0:
                        adb.setPositiveButton(callback == null ? context.getString(R.string.positive_btn) : callback.getName(), callback);
                        break;
                    case 1:
                        adb.setNegativeButton(callback == null ? context.getString(R.string.negative_btn) : callback.getName(), callback);
                        break;
                    case 2:
                        adb.setNeutralButton(callback == null ? context.getString(R.string.neutral_btn) : callback.getName(), callback);
                        break;
                }
            }
        else adb.setPositiveButton(context.getString(R.string.positive_btn), null);
        adb.setMessage(message);
        if (context instanceof Activity)
            if (!((Activity) context).isFinishing())
                return dialog = adb.show();
        return null;
    }


    public static Dialog createBottomSheetDialog(Context context, View view) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context, R.style.MaterialDialogSheet);
        float dpi = context.getResources().getDisplayMetrics().density;
        int pad = (int)-dpi*1;
        adb.setView(view,pad,0,pad,0);

        adb.setCancelable(true);

        /*adb.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);*/

        Dialog dialog = adb.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
        wmlp.width = 10000;
        wmlp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
        //wmlp.x = 100;   //x position
       // wmlp.y = 100;   //y position

       // dialog.show();


        return dialog;
    }

    public static void hideDialog(Dialog dialog) {
        if (dialog != null)
            dialog.cancel();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        return ProgressDialog.show(context, null, context.getString(R.string.progress_dialog_text));
    }

    public static ProgressDialog showProgressDialog(Context context, String text) {
        return ProgressDialog.show(context, null, text);
    }



}