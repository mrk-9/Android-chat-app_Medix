package com.medx.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.views.MXChatCell;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alexey on 9/27/16.
 */

public class MXChatAdapter extends RecyclerView.Adapter<MXChatCell> implements StickyRecyclerHeadersAdapter<MXChatAdapter.HeaderHolder> {

    JSONArray messages;
    Context context;
    Activity activity;
    private View.OnClickListener mSpaceClickListener;

    private static final int TO = 1;
    private static final int FROM = 2;


    public MXChatAdapter(Context context, Activity activity, JSONArray messages, View.OnClickListener spaceClickListener) {
        this.messages = messages;
        this.context = context;
        this.mSpaceClickListener = spaceClickListener;
        this.activity = activity;
    }

    @Override
    public MXChatCell onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0)  {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_from, parent, false);
            view.setVisibility(View.GONE);
            return new MXChatCell(view);
        }

        if (viewType == FROM)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_from, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_to, parent, false);
        return new MXChatCell(view);
    }

    @Override
    public void onBindViewHolder(MXChatCell holder, int position) {
        if (position < messages.length()) {
            JSONObject message_info = AppUtils.getJSONFromJSONArray(messages, position);
            holder.initWithMessage(activity, message_info, mSpaceClickListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < messages.length()) {
            if (AppUtils.getStringFromJSON(AppUtils.getJSONFromJSONArray(messages, position), "sender_id").equals(MedXUser.CurrentUser().userId()))
                return FROM;
            else return TO;
        } else return 0;
    }

    @Override
    public long getItemId(int position) {
        if (position < messages.length())
            return position;
        else
            return messages.length() - 1;
    }

    @Override
    public int getItemCount() {
        return messages.length();
    }

    public void addAll(JSONArray messages){
        this.messages = messages;
        notifyDataSetChanged();
    }

    /**
     Permite limpiar todos los elementos del recycler
     **/

    public void clear(){
        messages = new JSONArray();
        notifyDataSetChanged();
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.date)
        public TextView date;

        public HeaderHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public long getHeaderId(int position) {
        if (position > messages.length() - 1)
            position = messages.length() - 1;
        JSONObject item = AppUtils.getJSONFromJSONArray(messages, position);
        String sentat = AppUtils.getStringFromJSON(item, "sent_at");
        if (AppUtils.isEmptyString(sentat))
            sentat = AppUtils.StringFromDate(new Date());
        Date date = AppUtils.DateFromString(sentat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int id = calendar.get(Calendar.DAY_OF_YEAR);
        return id;
    }

    @Override
    public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_sticky_label, parent, false);
        return new HeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderHolder holder, int position) {
        if (position > messages.length() - 1)
            position = messages.length() - 1;
        JSONObject item = AppUtils.getJSONFromJSONArray(messages, position);
        String sentat = AppUtils.getStringFromJSON(item, "sent_at");
        if (AppUtils.isEmptyString(sentat))
            sentat = AppUtils.StringFromDate(new Date());
        holder.date.setText(AppUtils.printDay(AppUtils.DateFromString(sentat)));
    }
}
