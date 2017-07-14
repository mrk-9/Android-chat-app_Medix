package com.medx.android.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by alexey on 9/21/16.
 */

public class MXBaseFragment extends Fragment {

    /**
     * Properties field
     */

    public ProgressDialog dialog;

    /**
     * View created methods
     */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    /**
     * Utility methods
     */

    public void showSimpleMessage(String title, String message, String okBtTitle, DialogInterface.OnClickListener listener)   {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, okBtTitle, listener != null ? listener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * manage progress bar
     */

    public void showProgress(String message)  {
        dialog = ProgressDialog.show(getContext(), "", message, true);
    }

    public void hideProgress()  {
        if (dialog != null)
            dialog.dismiss();
    }
}
