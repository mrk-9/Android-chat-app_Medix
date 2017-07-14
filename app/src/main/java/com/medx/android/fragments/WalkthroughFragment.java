package com.medx.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.medx.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alexey on 9/19/16.
 */

public class WalkthroughFragment extends Fragment {
    @Bind(R.id.head)
    TextView tvHead;

    @Bind(R.id.body)
    TextView tvBody;

    @Bind(R.id.image)
    ImageView ivPicture;

    private static final String HEAD = "head";
    private static final String BODY = "body";
    private static final String IMAGE = "image";

    private String mHead;
    private String mBody;
    private int mImage;

    public static WalkthroughFragment newInstance(String head, String body){
        WalkthroughFragment fragment = new WalkthroughFragment();
        Bundle args = new Bundle();
        args.putString(HEAD, head);
        args.putString(BODY, body);
        fragment.setArguments(args);
        return fragment;
    }

    public static WalkthroughFragment newInstance(String head, int image){
        WalkthroughFragment fragment = new WalkthroughFragment();
        Bundle args = new Bundle();
        args.putString(HEAD, head);
        args.putInt(IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            Bundle args = getArguments();
            if (args.containsKey(HEAD)){
                mHead = args.getString(HEAD);
            }
            if (args.containsKey(BODY)){
                mBody = args.getString(BODY);
            }
            if (args.containsKey(IMAGE)){
                mImage = args.getInt(IMAGE);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.walkthrough, container, false);
        ButterKnife.bind(this, view);
        tvHead.setText(mHead);
        tvHead.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        tvBody.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        if (mBody == null){
            tvBody.setVisibility(View.GONE);
            ivPicture.setVisibility(View.VISIBLE);
            ivPicture.setImageResource(mImage);
        } else {
            tvBody.setText(mBody);
        }
        return view;
    }
}
