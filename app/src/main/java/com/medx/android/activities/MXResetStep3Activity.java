package com.medx.android.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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

public class MXResetStep3Activity extends MXAuthActivity {

    /**
     * Properties field
     */

    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.confirm)
    EditText confirm;
    @Bind(R.id.passwordError)
    TextView tvErrorPassword;

    @Bind(R.id.submit)
    TextView send;
    @Bind(R.id.title)
    TextView title;

    String pin;
    String phone;

    /**
     * Init Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset3);

        phone = getIntent().getStringExtra("phone");
        pin = getIntent().getStringExtra("pin");

        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        tvErrorPassword.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.submit)
    void send(View view) {
        tvErrorPassword.setVisibility(View.INVISIBLE);

        if (!doValidation())
            return;

        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "app", "1");
        AppUtils.setJSONObjectWithObject(params, "step", "3");
        AppUtils.setJSONObjectWithObject(params, "phone", phone);
        AppUtils.setJSONObjectWithObject(params, "pin", pin);
        AppUtils.setJSONObjectWithObject(params, "password", password.getText());
        AppUtils.setJSONObjectWithObject(params, "password_confirmation", confirm.getText());

        showProgress(getString(R.string.progress_dialog_text));
        BackendBase.newSharedConnection().accessPublicAPIbyPost(this, ApiURLs.RESET, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (result.has("success"))   {
                    finish();
                } else {
                    if (result.has("failure") && AppUtils.getJSONFromJSON(result, "failure").has("msg"))   {
                        tvErrorPassword.setText(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(result, "failure"), "msg"));
                    } else tvErrorPassword.setText("passwords don't match");

                    tvErrorPassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });
    }

    private boolean validatePasswords() {
        boolean hasPass = true;
        boolean hasConfirmPass = true;

        if (TextUtils.isEmpty(password.getText())) {
            password.setError(getString(R.string.password_blank));
            hasPass = false;
        } else if (password.getText().length() <= 5) {
            password.setError(getString(R.string.small_password));
            hasPass = false;
        }

        if (!String.valueOf(confirm.getText()).equals(String.valueOf(password.getText()))) {
            confirm.setError(getString(R.string.confirm_password_not_match));
            hasConfirmPass = false;
        }

        return hasConfirmPass && hasPass;
    }

    private boolean doValidation()  {
        return validatePasswords();
    }
}
