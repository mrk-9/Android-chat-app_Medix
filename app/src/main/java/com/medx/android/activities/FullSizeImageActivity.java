package com.medx.android.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.medx.android.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alexey on 9/28/16.
 */

public class FullSizeImageActivity extends AppCompatActivity {
    private float initialScale;
    private static final String TAG = "FullSizeImageActivity";

    @Bind(R.id.root)
    View root;
    @Bind(R.id.imageView)
    SubsamplingScaleImageView scaleImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_full_size_image);

        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().hasExtra("image")){
            String imagePath = getIntent().getStringExtra("image");
            if (imagePath != null){
                File file = new File(imagePath);
                if (file.exists()){
                    Uri uri = Uri.fromFile(file);
                    scaleImageView.setImage(ImageSource.uri(uri));


                    scaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                        @Override
                        public void onReady() {

                        }

                        @Override
                        public void onImageLoaded() {
                            initialScale = scaleImageView.getScale();
                        }

                        @Override
                        public void onPreviewLoadError(Exception e) {

                        }

                        @Override
                        public void onImageLoadError(Exception e) {

                        }

                        @Override
                        public void onTileLoadError(Exception e) {

                        }
                    });
                    scaleImageView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            float scale = scaleImageView.getScale();
                            if (scale > initialScale){
                                root.setBackgroundColor(Color.BLACK);
                            } else{
                                root.setBackgroundColor(Color.TRANSPARENT);
                            }
                            Log.d(TAG, String.valueOf(scale));
                            return false;
                        }
                    });
                }
            }
        }
        scaleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                FullSizeImageActivity.this.overridePendingTransition(R.anim.zoom_out, R.anim.zoom_out);
            }
        });

    }
}
