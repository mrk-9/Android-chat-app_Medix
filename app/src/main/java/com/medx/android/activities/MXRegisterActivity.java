package com.medx.android.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.loopj.android.http.RequestParams;
import com.medx.android.App;
import com.medx.android.R;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.EncryptionUtil;
import com.medx.android.utils.chat.MXUserUtil;
import com.medx.android.utils.views.MXDialogManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by alexey on 9/27/16.
 */

public class MXRegisterActivity extends MXAuthActivity  {
    public static final String CODE = "code";
    public static final String DATA = "data";

    @Bind(R.id.console)
    TextView console;

    @Bind(R.id.send)
    TextView send;

    @Bind(R.id.prefixName)
    EditText prefixName;
    @Bind(R.id.name)
    EditText name;
    @Bind(R.id.surname)
    EditText surname;
    @Bind(R.id.specialty)
    EditText specialty;
    @Bind(R.id.number)
    EditText number;
    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.confirmPassword)
    EditText confirmPassword;
    @Bind(R.id.security)
    EditText security;


    @Bind(R.id.headLayour)
    View headLauyout;
    @Bind(R.id.errorLayout)
    View errorLayout;

    @Bind(R.id.textView23)
    TextView textView23;

    @Bind(R.id.scrollView)
    ScrollView scrollView;

    String[] prefixArray;
    NumberPicker picker;

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    JSONObject userInfo;
    String registerationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_OK);
        setContentView(R.layout.activity_register);
        stripUnderlines(textView23);
        textView23.setMovementMethod(LinkMovementMethod.getInstance());
        send.setTextColor(ContextCompat.getColor(this, R.color.white));

        if (getIntent() != null) {
            String infoStr = getIntent().getStringExtra(DATA);
            try {
                userInfo = new JSONObject(infoStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            registerationCode = getIntent().getStringExtra(CODE);
        }

        prefixArray = getResources().getStringArray(R.array.prefix_name);
        prefixName.setText(prefixArray[0]);

        if (!TextUtils.isEmpty(AppUtils.getStringFromJSON(userInfo, "preferred_first_name")))
            name.setText(AppUtils.getStringFromJSON(userInfo, "preferred_first_name"));
        if (!TextUtils.isEmpty(AppUtils.getStringFromJSON(userInfo, "last_name")))
            surname.setText(AppUtils.getStringFromJSON(userInfo, "last_name"));
        if (!TextUtils.isEmpty(AppUtils.getStringFromJSON(userInfo, "salutation")))
            prefixName.setText(AppUtils.getStringFromJSON(userInfo, "salutation"));
        if (!TextUtils.isEmpty(AppUtils.getStringFromJSON(userInfo, "specialty")))
            specialty.setText(AppUtils.getStringFromJSON(userInfo, "specialty"));

        hideKeyboard();

    }

    NumberPicker createPicker() {
        picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(prefixArray.length - 1);
        picker.setDisplayedValues(prefixArray);
        return picker;
    }

    @OnClick(R.id.prefixName)
    void onClickPrefix(View view) {
        MXDialogManager.showCustomDialog(this, createPicker(), null, this::onPrefixSelect);
    }

    public void onPrefixSelect(DialogInterface dialog, int which) {
        prefixName.setText(prefixArray[picker.getValue()]);
    }

    /**
     * Registeration Methods
     */

    @OnClick(R.id.send)
    void send(View view) {
        hideProgress();
        if (doValidation()) {
            showProgress("Registering & Generating Encryption Keys. \r\n(This may take up to a minutes.)");

            final boolean[] isSuccess = new boolean[1];
            Observable.create(subscriber -> {
                try {
                    PublicKey publicKey = null;
                    KeyPair keyPair = null;
                    keyPair = EncryptionUtil.generateKeyPair();
                    publicKey = keyPair.getPublic();

                    RequestParams params = new RequestParams();
                    params.put("first_name", AppUtils.getStringFromJSON(userInfo, "first_name"));
                    params.put("last_name", surname.getText().toString());
                    params.put("specialty", specialty.getText().toString());
                    params.put("preferred_first_name", name.getText().toString());
                    params.put("salutation", prefixName.getText().toString());
                    params.put("phone", number.getText().toString());
                    params.put("public_key", AppUtils.convertPublicKeyToString(publicKey));
                    params.put("password", password.getText().toString());
                    params.put("code", registerationCode);

                    KeyPair finalKeyPair = keyPair;
                    BackendBase.newSharedConnection().accessAPIbyPostWithSync(App.DefaultContext, ApiURLs.URL_AUTH_REGISTER, params, new MXWebServiceListener() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            hideProgress();
                            String response = AppUtils.getStringFromJSON(result, "response");
                            if (response.equals("fail"))    {
                                String status = AppUtils.getStringFromJSON(result, "status");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        headLauyout.setVisibility(View.INVISIBLE);
                                        errorLayout.setVisibility(View.VISIBLE);
                                        scrollView.fullScroll(ScrollView.FOCUS_UP);

                                        if (status.equals("phone"))
                                            console.setText(getString(R.string.register_invalid_number));
                                        else if (status.equals("user"))
                                            console.setText(getString(R.string.register_invalid_user));
                                        else if (status.equals("verified"))
                                            console.setText(getString(R.string.register_invalid_verified));
                                        else
                                            console.setText(getString(R.string.register_error_text));
                                    }
                                });

                            } else {
                                onRegisterationSuccessWithUserInfo(AppUtils.getJSONFromJSON(result, "user"), finalKeyPair);
                            }
                        }

                        @Override
                        public void onFailed(JSONObject erroreResult) {
                            hideProgress();
                        }
                    });
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe((v) -> {

            });

        } else {
            headLauyout.setVisibility(View.INVISIBLE);
            errorLayout.setVisibility(View.VISIBLE);
            return;
        }
    }

    private void onRegisterationSuccessWithUserInfo(JSONObject userInfo, KeyPair keyPair)   {
        MedXUser.CurrentUser().setUserInfo(userInfo);
        MXUserUtil.saveUserByInfo(userInfo);
        doAfterSignInWithKeyPair(keyPair);

        Intent intent = new Intent(MXRegisterActivity.this, MXRootViewActivity.class);
        pushView(intent);
        finish();
    }

    /**
     * Validation Methods
     */

    private boolean doValidation() {
        boolean hasName = true, hasNumber = true, hasPass = true, hasConfirmPass = true;
        if (TextUtils.isEmpty(name.getText())) {
            name.setError(getString(R.string.name_blank));
            hasName = false;
        }

        if (TextUtils.isEmpty(number.getText())) {
            number.setError(getString(R.string.number_blank));
            hasNumber = false;
        } else if (!validatePhoneNumber(number.getText().toString())) {
            number.setError(getString(R.string.invalid_number));
            hasNumber = false;
        }


        if (TextUtils.isEmpty(password.getText())) {
            password.setError(getString(R.string.password_blank));
            hasPass = false;
        } else if (password.getText().length() <= 5) {
            password.setError(getString(R.string.small_password));
            hasPass = false;
        }

        if (!String.valueOf(confirmPassword.getText()).equals(String.valueOf(password.getText()))) {
            confirmPassword.setError(getString(R.string.confirm_password_not_match));
            hasConfirmPass = false;
        }


        return hasName && hasNumber && hasPass && hasConfirmPass;
    }

    private boolean validatePhoneNumber(String number)  {
        if (number.length() == 0)   {
            return false;
        } else {

            boolean anError = false;

            if (!AppUtils.isValidPhone(number))    {
                anError = true;
            }

            return !anError && isValidCountryNumber(number);
        }
    }

    private boolean isValidCountryNumber(String number)  {
        boolean isValidCountryNumber = false;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number.toString(), "AU");
            isValidCountryNumber = phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        if (!isValidCountryNumber) {
        }

        return isValidCountryNumber;
    }

    public class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
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

}
