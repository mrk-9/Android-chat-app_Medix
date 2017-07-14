package com.medx.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class MXResetStep2Activity extends MXAuthActivity {

    /**
     * Properties field
     */

    @Bind(R.id.enterPin)
    EditText pin;
    @Bind(R.id.pinError)
    TextView tvErrorPin;

    @Bind(R.id.submit)
    TextView send;
    @Bind(R.id.title)
    TextView title;

    String phone;

    /**
     * Init Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset2);

        phone = getIntent().getStringExtra("phone");
        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        pin.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(4)});

        tvErrorPin.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.submit)
    void send(View view) {
        tvErrorPin.setVisibility(View.INVISIBLE);

        if (!doValidation())
            return;

        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "app", "1");
        AppUtils.setJSONObjectWithObject(params, "step", "2");
        AppUtils.setJSONObjectWithObject(params, "phone", phone);
        AppUtils.setJSONObjectWithObject(params, "pin", pin.getText().toString());

        showProgress(getString(R.string.progress_dialog_text));
        BackendBase.newSharedConnection().accessPublicAPIbyPost(this, ApiURLs.RESET, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (result.has("success"))   {
                    Intent intent = new Intent(MXResetStep2Activity.this, MXResetStep3Activity.class);
                    intent.putExtra("phone", phone);
                    intent.putExtra("pin", String.valueOf(pin.getText()));
//                    startActivityForResult(intent, CLOSE_CODE);
                    pushView(intent);
                    finish();
                } else {
                    if (result.has("failure") && AppUtils.getJSONFromJSON(result, "failure").has("msg"))   {
                        tvErrorPin.setText(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(result, "failure"), "msg"));
                    }

                    tvErrorPin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });
    }

    private boolean validatePin() {
        if (pin.getText().length() == 0)  {
            tvErrorPin.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvErrorPin.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private boolean doValidation()  {
        return validatePin();
    }

}
