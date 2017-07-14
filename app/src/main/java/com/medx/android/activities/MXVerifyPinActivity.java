package com.medx.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.EncryptionUtil;
import com.medx.android.utils.chat.MXUserUtil;
import com.medx.android.utils.views.MXDialogManager;

import org.json.JSONObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by alexey on 9/19/16.
 */

public class MXVerifyPinActivity extends MXAuthActivity {

    /**
     * Properties field
     */

    public static final String PHONE = "phone";

    MedXUser currentUser;

    @Bind(R.id.logoutTime)
    EditText logoutTime;
    @Bind(R.id.enterPin)
    EditText enterPin;

    String phone;
    String[] timeArray;
    NumberPicker picker;

    @Bind(R.id.send)
    TextView send;
    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.pinError)
    TextView pinError;

    /**
     * Init Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pin);

        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        if (getIntent() != null)
            phone = getIntent().getStringExtra(PHONE);
        timeArray = getResources().getStringArray(R.array.logout_time);

        enterPin.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(4)});

        createPicker();
        onLogoutTimeSelect(null, 0);

        initUI();
    }

    private void initUI()   {
        currentUser = MedXUser.CurrentUser();

        // Init expire-period picker
        picker.setValue(3);
    }

    NumberPicker createPicker() {
        picker = new NumberPicker(this);
        picker.setMinValue(0);
        int maxValue = timeArray.length - 1;
        picker.setMaxValue(maxValue);
        picker.setDisplayedValues(timeArray);
        picker.setValue(maxValue);
        picker.setOnValueChangedListener(this::onLogoutTimeChange);
        return picker;
    }

    public void onLogoutTimeSelect(DialogInterface dialog, int which) {
        logoutTime.setText(String.format(getString(R.string.logout_time_formetter), timeArray[picker.getValue()]));
    }

    void onLogoutTimeChange(NumberPicker picker, int oldVal, int newVal) {
    }

    /**
     * Validation Methods
     */

    private boolean validatePIN()   {
        boolean isValid = false;

        if (enterPin.getText().toString().isEmpty()) {
            pinError.setText(getString(R.string.pin_blank));
            isValid = false;
        } else {
            isValid = true;
        }

        if (isValid)
            pinError.setVisibility(View.INVISIBLE);
        else
            pinError.setVisibility(View.VISIBLE);

        return isValid;
    }

    private boolean doValidation()  {
        return validatePIN();
    }

    /**
     * Authenticate Methods
     */

    private void onAuthenticationSuccess(JSONObject userInfo)   {
        KeyPair keyPair = MXUserUtil.getKeyPair();
        boolean bHasToRegisterEncryptionKeys = AppUtils.isEmptyobject(keyPair) || AppUtils.isEmptyString(AppUtils.getStringFromJSON(userInfo, "public_key")) || !AppUtils.convertPublicKeyToString(keyPair.getPublic()).equals(AppUtils.getStringFromJSON(userInfo, "public_key"));

        if (bHasToRegisterEncryptionKeys)  {
            if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(userInfo, "public_key")))  {
                showConfirmmessage(getString(R.string.before_generate_key_text), "Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doRegisterEncryptionKeys();
                    }
                });
            } else
                doRegisterEncryptionKeys();
        } else {
            doAfterSignInWithKeyPair(keyPair);
            Intent intent = new Intent(MXVerifyPinActivity.this, MXRootViewActivity.class);
            pushView(intent);
            finish();
        }
    }

    private void doRegisterEncryptionKeys() {
        showProgress(getString(R.string.please_wait));
        final boolean[] isSuccess = new boolean[1];
        Observable.create(subscriber -> {
            try {
                KeyPair keyPair = EncryptionUtil.generateKeyPair();
                currentUser.registerPublicKey(AppUtils.convertPublicKeyToString(keyPair.getPublic()), new CompletionListener() {
                    @Override
                    public void complete(boolean success, String errorStatus) {
                        isSuccess[0] = success;
                        if (success) {
                            doAfterSignInWithKeyPair(keyPair);
                            Intent intent = new Intent(MXVerifyPinActivity.this, MXRootViewActivity.class);
                            pushView(intent);
                            finish();
                        } else {
                            showSimpleMessage("", getString(R.string.connection_error), "OK", null);
                        }

                        hideProgress();
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe((v) -> {

        });
    }

    /**
     * Button Methods
     */

    @OnClick(R.id.send)
    void onClickSend(View view) {
        hideKeyboard();
        pinError.setVisibility(View.GONE);

        if (doValidation()) {
            String period = timeArray[picker.getValue()];

            JSONObject params = new JSONObject();

            AppUtils.setJSONObjectWithObject(params, "phone", phone);
            AppUtils.setJSONObjectWithObject(params, "pin", enterPin.getText());
            AppUtils.setJSONObjectWithObject(params, "expire_period", period);



            showProgress(getString(R.string.progress_dialog_text));

            BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_AUTH_VERIFY_PIN, params, new MXWebServiceListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    hideProgress();
                    String response = AppUtils.getStringFromJSON(result, "response");

                    if (response.equals("success")) {
                        MXUserUtil.updateUserDefaultsWithLoginExpirePeriod(period);
                        currentUser.setUserInfo(AppUtils.getJSONFromJSON(result, "user"));
                        MXUserUtil.saveUserByInfo(AppUtils.getJSONFromJSON(result, "user"));
                        onAuthenticationSuccess(AppUtils.getJSONFromJSON(result, "user"));
                    } else {
                        pinError.setText(getString(R.string.invalid_pin));
                        pinError.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailed(JSONObject erroreResult) {
                    hideProgress();
                    showSimpleMessage("", getString(R.string.connection_error), "OK", null);
                }
            });
        }
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.logoutTime)
    void onClickLogoutTome(View view) {
        MXDialogManager.showCustomDialog(this, picker, null, this::onLogoutTimeSelect);
    }
}
