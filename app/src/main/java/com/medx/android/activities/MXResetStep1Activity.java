package com.medx.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 10/15/16.
 */

public class MXResetStep1Activity extends MXAuthActivity {

    /**
     * Properties field
     */

    @Bind(R.id.enterNumber)
    EditText number;
    @Bind(R.id.numberError)
    TextView tvErrorPhone;

    @Bind(R.id.submit)
    TextView send;
    @Bind(R.id.title)
    TextView title;

    /**
     * Init Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset1);

        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        tvErrorPhone.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.submit)
    void send(View view) {
        tvErrorPhone.setVisibility(View.INVISIBLE);

        if (!doValidation())
            return;

        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "app", "1");
        AppUtils.setJSONObjectWithObject(params, "step", "1");
        AppUtils.setJSONObjectWithObject(params, "phone", number.getText());

        showProgress(getString(R.string.progress_dialog_text));
        BackendBase.newSharedConnection().accessPublicAPIbyPost(this, ApiURLs.RESET, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (result.has("success"))   {
                    Intent intent = new Intent(MXResetStep1Activity.this, MXResetStep2Activity.class);
                    intent.putExtra("phone", String.valueOf(number.getText()));
                    pushView(intent);
                    finish();
                } else {
                    if (result.has("failure") && AppUtils.getJSONFromJSON(result, "failure").has("msg"))   {
                        tvErrorPhone.setText(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(result, "failure"), "msg"));
                    }

                    tvErrorPhone.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });
    }

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

    private boolean doValidation()  {

        return validatePhoneNumber();
    }
}
