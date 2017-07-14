package com.medx.android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.medx.android.R;
import com.medx.android.adapters.MXSearchListAdapter;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.views.MXDialogManager;
import com.medx.android.utils.views.PtrClassicCustomLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/21/16.
 */

public class MXSearchFragment extends MXLiveLoadFragment implements RadioGroup.OnCheckedChangeListener {

    @Bind(R.id.name)
    EditText name;
    @Bind(R.id.specialty)
    EditText specialty;

    @Bind(R.id.postcode)
    EditText postcode;

    @Bind(R.id.orderGroup)
    RadioGroup orderGroup;

    @Bind(android.R.id.list)
    RecyclerView list;
    @Bind(android.R.id.empty)
    TextView empty;

    @Bind(R.id.send)
    TextView send;

    @Bind(R.id.ptr)
    PtrClassicCustomLayout ptr;

    ProgressDialog progressDialog;

    private MXSearchListAdapter adapter;
    private NumberPicker picker;
    private String[] specialtyArray;

    ArrayList<MXUser> doctorList;
    MedXUser currentUser;

    static MXSearchFragment fragment;

    public static MXSearchFragment newInstance() {
        if (fragment == null) {
            Bundle args = new Bundle();

            MXSearchFragment fragment = new MXSearchFragment();
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;
    }

    @Override
    public RecyclerView getList() {
        return list;
    }

    @Override
    public void getNewPortion(int lastItem) {
        if (lastItem > doctorList.size() - 2)
            loadDoctors(true);
        else
            loadDoctors(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ptr.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadDoctors(false);
            }
        });

        send.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

        specialtyArray = getResources().getStringArray(R.array.specialty);
        specialty.setText(specialtyArray[0]);
        orderGroup.setOnCheckedChangeListener(this);

        empty.setVisibility(View.GONE);

        if (doctorList == null) {
            doctorList = new ArrayList<>();
            currentUser = MedXUser.CurrentUser();
        }

        postcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    if (!adapter.isSearchByPostcode())
                        orderGroup.check(R.id.d);
            }
        });
        postcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setupList();
        loadDoctors(false);
    }

    @OnClick(R.id.send)
    void onClickSearch(View view) {
        search();
        // Check if no view has focus:
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(name.getApplicationWindowToken(), 0);
    }

    private void search()   {
        showProgress("Searching...");
        String order = getOrder();
        if (order.equals("D") && postcode.getText().toString().isEmpty())  {
            hideProgress();
            showSimpleMessage("", getString(R.string.invalid_postcode), "OK", null);
            return;
        }

        loadDoctors(false);
    }

    private void loadDoctors(boolean load_more) {

        if (load_more && doctorList.size() < 10)    {
            if (ptr.isRefreshing())
                ptr.refreshComplete();
        }

        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "token", currentUser.accessToken());
        AppUtils.setJSONObjectWithObject(params, "size", "25");
        AppUtils.setJSONObjectWithObject(params, "keyword", name.getText().toString().equals("*") ? "" : name.getText().toString());
        AppUtils.setJSONObjectWithObject(params, "specialty", specialty.getText().toString().equals("All Specialties") ? "" : specialty.getText().toString());

        String order = getOrder();
        if (order.equals("D"))  {
            AppUtils.setJSONObjectWithObject(params, "order", "D");
            AppUtils.setJSONObjectWithObject(params, "postcode", postcode.getText().toString());
        } else {
            AppUtils.setJSONObjectWithObject(params, "order", "C");
        }

        AppUtils.setJSONObjectWithObject(params, "start", load_more ? doctorList.size() + "" : "0");

        BackendBase.newInstance().accessAPIFastGET(ApiURLs.URL_USERS_SEARCH, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                hideProgress();
                if (ptr.isRefreshing())
                    ptr.refreshComplete();

                if (AppUtils.getStringFromJSON(result, "response").equals("success"))   {
                    ArrayList<MXUser> doctors = new ArrayList<>();
                    JSONArray doctor_infos = AppUtils.getJSONArrayFromJSON(result, "doctors");
                    if (doctor_infos != null) {
                        for (int i = 0; i < doctor_infos.length(); i++) {
                            JSONObject info = AppUtils.getJSONFromJSONArray(doctor_infos, i);
                            MXUser doctor = new MXUser();
                            doctor.updateUserWithInfo(info);
                            doctors.add(doctor);
                        }
                    }

                    if (load_more)  {
                        doctorList.addAll(doctors);
                        setupList();
                        setIsLoading(false);
                    } else {
                        doctorList.clear();
                        doctorList = doctors;
                        setupList();
                    }
                } else {
                    if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(result, "status")))    {
                        showSimpleMessage("", AppUtils.getStringFromJSON(result, "status"), "OK", null);
                    } else {
                        showSimpleMessage("", getString(R.string.connection_error), "OK", null);
                    }

                    adapter.notifyItemRangeRemoved(0, doctorList.size());
                }

                emptyCheckList();
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                hideProgress();
                if (ptr.isRefreshing())
                    ptr.refreshComplete();
                setupList();
                emptyCheckList();
                showSimpleMessage("", getString(R.string.connection_error), "OK", null);
            }
        });

    }

    void emptyCheckList() {
        if (doctorList.size() > 0) {
            empty.setVisibility(View.GONE);
        } else {
            empty.setText(getString(R.string.empty_search_list));
            empty.setVisibility(View.VISIBLE);
        }
    }

    String getOrder() {
        switch (orderGroup.getCheckedRadioButtonId()) {
            case R.id.d:
                return "D";
            default: return "C";
        }
    }

    @OnClick(R.id.specialty)
    void onClickSpecialty(View view) {
        MXDialogManager.showCustomDialog(getContext(), createPicker(), null, this::onSpecialtySelect);
    }

    public void onSpecialtySelect(DialogInterface dialog, int which) {
        specialty.setText(specialtyArray[picker.getValue()]);
    }

    NumberPicker createPicker() {
        picker = new NumberPicker(getContext());
        picker.setMinValue(0);
        picker.setMaxValue(specialtyArray.length - 1);
        picker.setDisplayedValues(specialtyArray);
        return picker;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        postcode.setError(null);
        if (checkedId == R.id.d)
            adapter.setIsSearchByPostcode(true);
        else
            adapter.setIsSearchByPostcode(false);
    }

    private void setupList()    {
        if (list.getAdapter() != null) {
            adapter.clear();
            adapter.addAll(doctorList);
        } else {
            adapter = new MXSearchListAdapter(doctorList);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            list.setLayoutManager(llm);
            list.setAdapter(adapter);
            empty.setVisibility(View.GONE);
        }
    }
}