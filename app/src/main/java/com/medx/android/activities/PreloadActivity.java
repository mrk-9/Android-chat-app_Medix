package com.medx.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.medx.android.R;

import butterknife.Bind;

public class PreloadActivity extends MXBaseActivity {

    Handler handler = new Handler();

    @Bind(R.id.textView16)
    TextView copy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preload);
        copy.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(this::start, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(this::start);
    }

    void start() {
        Intent intent = new Intent(PreloadActivity.this, MXIntroActivity.class);
        pushView(intent);
        finish();
    }
}
