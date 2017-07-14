package com.medx.android.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by alexey on 9/23/16.
 */

public abstract class MXLiveLoadFragment extends MXBaseFragment {
    private static final int LIMIT_LIST = 5;
    private boolean isLoading;

    RecyclerView list;

    public abstract RecyclerView getList();

    public abstract void getNewPortion(int lastItem);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = getList();
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("LiveListFragment", String.valueOf(newState));
                RecyclerView.LayoutManager manager = list.getLayoutManager();
                int lastVisibleElement = 0;
                if (manager instanceof LinearLayoutManager) {
                    lastVisibleElement = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    loading(lastVisibleElement);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    private void loading(int lastVisibleElement){
        if (list.getAdapter().getItemCount() - lastVisibleElement < LIMIT_LIST && !isLoading) {
            isLoading = true;
            getNewPortion(list.getAdapter().getItemCount());
        }
    }
    public void setIsLoading(boolean loading)   {
        isLoading = loading;
    }
}
