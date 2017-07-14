package com.medx.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.medx.android.R;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXUserUtil;
import com.medx.android.utils.views.MXDialogManager;
import com.medx.android.utils.views.MXSoftKeyboardLsnedRelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/21/16.
 */

public class MXSettingsFragment extends MXBaseFragment {

    /**
     * Properties field
     */

    @Bind(R.id.root)
    LinearLayout root;
    @Bind(R.id.prefixName)
    EditText prefixName;
    @Bind(R.id.name)
    EditText name;
    @Bind(R.id.surname)
    EditText surname;
    @Bind(R.id.specialty)
    EditText specialty;
    @Bind(R.id.about)
    EditText about;
    @Bind(R.id.location)
    TextView location;

    @Bind(R.id.locationBtn)
    TextView locationBtn;

    @Bind(R.id.textView29)
    TextView textView29;

    @Bind(R.id.doneLayout)
    View doneLayout;

    @Bind(R.id.done)
    View done;

    @Bind(R.id.rootSettings)
    MXSoftKeyboardLsnedRelativeLayout rootSettings;

    private String[] prefixArray;
    private NumberPicker picker;

    JSONArray offices;

    MedXUser currentUser;

    static MXSettingsFragment fragment;

    /**
     * static methods
     */

    public static MXSettingsFragment newInstance() {

        if (fragment == null) {
            Bundle args = new Bundle();

            MXSettingsFragment fragment = new MXSettingsFragment();
            fragment.setArguments(args);
        }

        return fragment;
    }

    /**
     * Init mehtods
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootSettings.addSoftKeyboardLsner(new MXSoftKeyboardLsnedRelativeLayout.SoftKeyboardLsner() {
            @Override
            public void onSoftKeyboardShow() {
                //Log.d("SoftKeyboard", "Soft keyboard shown");
                textView29.setVisibility(View.INVISIBLE);
                doneLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSoftKeyboardHide() {
                textView29.setVisibility(View.VISIBLE);
                doneLayout.setVisibility(View.GONE);
            }
        });
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        locationBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

        prefixArray = getResources().getStringArray(R.array.prefix_name);
        prefixName.setText(prefixArray[0]);

        if (currentUser == null)    {
            currentUser = MedXUser.CurrentUser();
            initContent();
        }
    }

    private void initContent()  {
        MXUser user = MXUserUtil.findByUserId(currentUser.userId());

        if (user == null)
            return;
        prefixName.setText(user.salutation);
        name.setText(user.preferred_first_name);
        surname.setText(user.last_name);
        specialty.setText(user.specialty);

        if (AppUtils.isNotEmptyString(user.about))  {
            about.setText(user.about);
        } else {
            about.setText("");
            about.setVisibility(View.INVISIBLE);
        }

        offices = new JSONArray();
        if (AppUtils.isNotEmptyObject(AppUtils.getJSONArrayFromJSON(currentUser.info, "offices")))  {
            JSONArray officeList = AppUtils.getJSONArrayFromJSON(currentUser.info, "offices");
            for (int i = 0; i < officeList.length(); i ++)  {
                JSONObject officeInfo = AppUtils.getJSONFromJSONArray(officeList, i);
                addLocation(officeInfo);
            }
        }

        // prefixName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        name.setImeOptions(EditorInfo.IME_ACTION_DONE);
        surname.setImeOptions(EditorInfo.IME_ACTION_DONE);
        specialty.setImeOptions(EditorInfo.IME_ACTION_DONE);
        about.setImeOptions(EditorInfo.IME_ACTION_DONE);


        // prefixName.setOnEditorActionListener(onEditorActionListener);
        name.setOnEditorActionListener(onEditorActionListener);
        surname.setOnEditorActionListener(onEditorActionListener);
        specialty.setOnEditorActionListener(onEditorActionListener);

        // Done action for username and about
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (validate()) {
                    updateAfterCheckingTextFieldsChanged();
                }
            }
        });
    }

    private void addLocation(JSONObject officeInfo) {
        offices.put(officeInfo);
        updateLocation();
    }

    TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN) {
                hideKeyboard();

                if (validate()) {
                    updateAfterCheckingTextFieldsChanged();
                }
                return true;
            }
            return false;
        }
    };

    private void updateAfterCheckingTextFieldsChanged() {
        boolean bChanged = !(AppUtils.getStringFromJSON(currentUser.info, "salutation").equals(prefixName.getText().toString()) && AppUtils.getStringFromJSON(currentUser.info, "preferred_first_name").equals(prefixName.getText().toString()) && AppUtils.getStringFromJSON(currentUser.info, "about").equals(about.getText().toString()));

        if(bChanged)
            onUpdate();
    }

    private void onUpdate() {
        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "token", currentUser.accessToken());
        AppUtils.setJSONObjectWithObject(params, "salutation", prefixName.getText().toString());
        AppUtils.setJSONObjectWithObject(params, "preferred_first_name", name.getText().toString());
        AppUtils.setJSONObjectWithObject(params, "about", about.getText());

        String jsonString = offices.toString();
        AppUtils.setJSONObjectWithObject(params, "offices", jsonString);

        showProgress("Updating...");
        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_USERS_UPDATE, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (AppUtils.getStringFromJSON(result, "response").equals("success"))   {
                    AppUtils.setJSONObjectWithObject(currentUser.info, "offices", offices);
                    AppUtils.setJSONObjectWithObject(currentUser.info, "salutation", prefixName.getText().toString());
                    AppUtils.setJSONObjectWithObject(currentUser.info, "preferred_first_name", name.getText().toString());
                    AppUtils.setJSONObjectWithObject(currentUser.info, "about", about.getText().toString());

                    MXUserUtil.updateUserDefaults(currentUser.info, new Date());

                    JSONObject info = new JSONObject();
                    AppUtils.setJSONObjectWithObject(info, "user_id", currentUser.userId());
                    AppUtils.setJSONObjectWithObject(info, "about", about.getText().toString());
                    AppUtils.setJSONObjectWithObject(info, "salutation", prefixName.getText().toString());
                    AppUtils.setJSONObjectWithObject(info, "preferred_first_name", name.getText().toString());

                    MXUserUtil.saveUserByInfo(info);

                } else {
                    showSimpleMessage("", getString(R.string.connection_error), "OK", null);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });
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

    private void updateLocation() {
        for (int i = 5; i < root.getChildCount() - 1; i++)
            root.removeView(root.getChildAt(i));
        System.out.println("SettingsFragment.updateLocation = " + offices.length());
        for (int i = 0; i < offices.length(); i ++) {
            JSONObject item = AppUtils.getJSONFromJSONArray(offices, i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_location, root, false);
            int finalI1 = i;
            view.findViewById(R.id.close).setOnClickListener(v -> {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage(R.string.confirm_delete_location);
                builder1.setCancelable(true);

                int finalI = finalI1;
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                root.removeView(view);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    offices.remove(finalI);
                                }
                                onUpdate();
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            });


            TextView tw = (TextView) view.findViewById(R.id.text);
           /* TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);*/
            // tw.setLayoutParams(rowLayout);
            tw.setGravity(Gravity.LEFT);
            String address = AppUtils.getStringFromJSON(item, "address");

            tw.setText(MXUserUtil.refinOfficePhoneNumberInLocation(address));
            root.addView(view, root.getChildCount() - 1);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(name.getApplicationWindowToken(), 0);
    }

    @OnClick(R.id.prefixName)
    void onClickPrefix(View view) {
        MXDialogManager.showCustomDialog(getContext(), createPicker(), null, this::onPrefixSelect);
    }

    public void onPrefixSelect(DialogInterface dialog, int which) {
        prefixName.setText(prefixArray[picker.getValue()]);
    }

    NumberPicker createPicker() {
        picker = new NumberPicker(getContext());
        picker.setMinValue(0);
        picker.setMaxValue(prefixArray.length - 1);
        picker.setDisplayedValues(prefixArray);
        return picker;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean validate() {
        if (AppUtils.isEmptyString(name.getText().toString())) {
            name.setError(getString(R.string.name_blank));
            return false;
        }
        return true;
    }

    @OnClick(R.id.locationBtn)
    void onClickLocation(View view) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        View locView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_location, null, false);


        Dialog dialog = MXDialogManager.createBottomSheetDialog(getContext(), locView);
        TextView postcode_loct = (TextView) locView.findViewById(R.id.postcode);
        TextView street_loct = (TextView) locView.findViewById(R.id.street);
        TextView number_loct = (TextView) locView.findViewById(R.id.number);
        TextView console_loct = (TextView) locView.findViewById(R.id.console);
        ImageView close_loct = (ImageView) locView.findViewById(R.id.close);
        TextView send_loct = (TextView) locView.findViewById(R.id.send);
        TextView title_loct = (TextView) locView.findViewById(R.id.title);
        title_loct.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        send_loct.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        close_loct.setOnClickListener(v1 -> dialog.cancel());

        console_loct.setText("");

        send_loct.setOnClickListener((v -> {
            if (!TextUtils.isEmpty(postcode_loct.getText()) && !TextUtils.isEmpty(street_loct.getText()) && validatePhoneNumber(String.valueOf(number_loct.getText()))) {

                console_loct.setText("");
                JSONObject params = new JSONObject();

                AppUtils.setJSONObjectWithObject(params, "token", currentUser.accessToken());
                AppUtils.setJSONObjectWithObject(params, "postcode", postcode_loct.getText());

                showProgress("Updating...");

                BackendBase.newSharedConnection().accessAPIFastGET(ApiURLs.URL_USERS_GEOCODE, params, new MXWebServiceListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        hideProgress();
                        if (AppUtils.getStringFromJSON(result, "response").equals("success"))   {
                            if (number_loct.getText().toString().length() == 9)  {
                                number_loct.setText("0" + number_loct.getText().toString());
                            }

                            JSONObject dict = new JSONObject();
                            AppUtils.setJSONObjectWithObject(dict, "postcode", postcode_loct.getText().toString());
                            AppUtils.setJSONObjectWithObject(dict, "phone", number_loct.getText().toString());
                            AppUtils.setJSONObjectWithObject(dict, "address", street_loct.getText().toString() + "\n" + AppUtils.getStringFromJSON(result, "location") + "\n" + number_loct.getText().toString());
                            AppUtils.setJSONObjectWithObject(dict, "latitude", AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(result, "coordinate"), "latitude"));
                            AppUtils.setJSONObjectWithObject(dict, "longitude", AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(result, "coordinate"), "longitude"));

                            addLocation(dict);
                            onUpdate();

                            dialog.cancel();
                        }
                        else
                            console_loct.setText("Address could not found/Invalid. Please review");
                    }

                    @Override
                    public void onFailed(JSONObject erroreResult) {
                        console_loct.setText("Address could not found/Invalid. Please review");
                        hideProgress();
                    }
                });
            } else {
                if (TextUtils.isEmpty(postcode_loct.getText()) || TextUtils.isEmpty(street_loct.getText()) || TextUtils.isEmpty(number_loct.getText()))
                    MXDialogManager.showSimpleDialog(getContext(), getString(R.string.warning), getString(R.string.enter_all_fields));
                else
                    MXDialogManager.showSimpleDialog(getContext(), getString(R.string.warning), getString(R.string.invalid_number_text));
            }

        }));
        dialog.show();
        locView.setClickable(true);
        locView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locView.getApplicationWindowToken(), 0);
            }
        });

    }

    /**
     * Notification methods
     */

    @Override
    public void onResume()  {
        super.onResume();
        hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
