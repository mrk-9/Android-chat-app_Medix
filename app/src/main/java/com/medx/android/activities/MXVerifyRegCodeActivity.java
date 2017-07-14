package com.medx.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.views.URLSpanNoUnderline;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/19/16.
 */

public class MXVerifyRegCodeActivity extends MXAuthActivity {

    @Bind(R.id.code)
    EditText code;

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.send)
    TextView send;

    @Bind(R.id.registerCodeNotify)
    TextView registerCodeNotify;

    @Bind(R.id.textView6)
    TextView textView6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_reg_code);
        code.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(6)});
        title.setTextColor(ContextCompat.getColor(this, R.color.white));
        send.setTextColor(ContextCompat.getColor(this, R.color.white));

        stripUnderlines(textView6);
        textView6.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.send)
    void onClickSend(View view) {
        registerCodeNotify.setVisibility(View.INVISIBLE);
        hideKeyboard();
        if (doValidation()) {
            JSONObject params = new JSONObject();
            AppUtils.setJSONObjectWithObject(params, "code", code.getText().toString());
            showProgress("waiting...");
            BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_AUTH_VERIFY_REGISTRATION_CODE, params, new MXWebServiceListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    hideProgress();
                    String response = AppUtils.getStringFromJSON(result, "response");
                    if (response.equals("fail"))    {
                        registerCodeNotify.setVisibility(View.VISIBLE);
                        registerCodeNotify.setText(getString(R.string.invalid_register_code));
                    } else {
                        Intent intent = new Intent(MXVerifyRegCodeActivity.this, MXRegisterActivity.class);
                        intent.putExtra(MXRegisterActivity.DATA, AppUtils.getJSONFromJSON(result, "user").toString());
                        intent.putExtra(MXRegisterActivity.CODE, String.valueOf(code.getText()));
                        pushView(intent);
                        finish();
                    }
                }

                @Override
                public void onFailed(JSONObject erroreResult) {
                    hideProgress();

                }
            });
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private boolean doValidation()  {
        if (code.getText().toString().isEmpty()) {
            registerCodeNotify.setVisibility(View.VISIBLE);
            registerCodeNotify.setText(getString(R.string.register_code_blank));
            return false;
        }

        return true;
    }
}
