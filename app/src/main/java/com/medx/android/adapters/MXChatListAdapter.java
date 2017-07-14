package com.medx.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medx.android.R;
import com.medx.android.interfaces.ChatlistLongTapListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.utils.views.MXChatListCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey on 9/23/16.
 */

public class MXChatListAdapter extends RecyclerView.Adapter<MXChatListCell> {

    private List<MXUser> mData;
    Context context;
    ChatlistLongTapListener listener;

    public MXChatListAdapter(Context context, ArrayList<MXUser> data, ChatlistLongTapListener listener) {
        mData = data;
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<MXUser> data) {
        mData = data;
    }

    @Override
    public MXChatListCell onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
        return new MXChatListCell(view);
    }

    @Override
    public void onBindViewHolder(MXChatListCell holder, int position) {
        // super.onBindViewHolder(holder, position);
        MXUser item = mData.get(position);
        holder.setupCell(item, listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public void addAll(List<MXUser> lista){
        mData.addAll(lista);
        notifyDataSetChanged();
    }

    /**
     Permite limpiar todos los elementos del recycler
     **/
    public void clear(){
        mData.clear();
    }
}
