package com.medx.android.interfaces;

import android.content.DialogInterface;

public class MXDialogClickListener implements DialogInterface.OnClickListener {

    String name;

    public MXDialogClickListener(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}