package com.medx.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.medx.android.R;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.views.LinkUtils;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/19/16.
 */

public class MXLoginActivity extends MXAuthActivity {

    /**
     * Properties field
     */

    @Bind(R.id.number)
    EditText number;
    @Bind(R.id.tvErrorPhone)
    TextView tvErrorPhone;


    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.tvErrorPassword)
    TextView tvErrorPassword;


    @Bind(R.id.send)
    TextView send;
    @Bind(R.id.title)
    TextView title;
    private ProgressDialog dialog;

    @Bind(R.id.tvForgot)
    TextView tvForgot;

    /**
     * Init Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        LinkUtils.stripUnderlines(tvForgot);
        tvForgot.setMovementMethod(LinkMovementMethod.getInstance());

        tvErrorPhone.setVisibility(View.INVISIBLE);
    }

    /**
     * Button Methods
     */

    @OnClick(R.id.tvForgot)
    void forgotPassword(View view)  {
        Intent intent = new Intent(MXLoginActivity.this, MXResetStep1Activity.class);
        pushView(intent);
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.send)
    void send(View view) {
        tvErrorPassword.setVisibility(View.INVISIBLE);
        tvErrorPhone.setVisibility(View.INVISIBLE);

        if (!doValidation())
            return;

        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "phone", number.getText());
        AppUtils.setJSONObjectWithObject(params, "password", password.getText());

        showProgress(getString(R.string.progress_dialog_text));
        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_AUTH_PIN, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (AppUtils.getStringFromJSON(result, "response").equals("success"))   {
                    Intent intent = new Intent(MXLoginActivity.this, MXVerifyPinActivity.class);
                    intent.putExtra(MXVerifyPinActivity.PHONE, String.valueOf(number.getText()));
                    pushView(intent);
                    finish();
                } else {
                    if (AppUtils.getStringFromJSON(result, "status").equals("phone"))   {
                        tvErrorPhone.setText(R.string.unregister_number);
                        tvErrorPhone.setVisibility(View.VISIBLE);
                    } else {
                        tvErrorPassword.setText(R.string.invalid_password);
                        tvErrorPassword.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });
    }

    /**
     * Validation Methods
     */

    private boolean validatePhoneNumber()  {
        tvErrorPhone.setText(getString(R.string.number_blank));
        if (number.getText().length() == 0)   {
            tvErrorPhone.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvErrorPhone.setVisibility(View.INVISIBLE);

            boolean anError = false;

            if (!AppUtils.isValidPhone(number.getText()))    {
                anError = true;
            }

            if (anError)    {
                tvErrorPhone.setText(R.string.invalid_number_text);
            }

            return !anError && isValidCountryNumber();
        }
    }

    private boolean isValidCountryNumber()  {
        boolean isValidCountryNumber = false;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number.getText().toString(), "AU");
            isValidCountryNumber = phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        if (!isValidCountryNumber) {
            tvErrorPhone.setText(R.string.invalid_number_text);
            tvErrorPhone.setVisibility(View.VISIBLE);
        }

        return isValidCountryNumber;
    }

    private boolean validatePassword() {
         if (password.getText().length() == 0)  {
             tvErrorPassword.setVisibility(View.VISIBLE);
             return false;
         } else {
             tvErrorPassword.setVisibility(View.INVISIBLE);
             return true;
         }
    }

    private boolean doValidation()  {

        return validatePhoneNumber() && validatePassword();
    }
}
