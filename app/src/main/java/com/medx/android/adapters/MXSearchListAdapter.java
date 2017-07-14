package com.medx.android.adapters;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.activities.MXDirectoryProfileActivity;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXRelationshipUtil;
import com.medx.android.utils.chat.MXUserUtil;

import java.util.List;

/**
 * Created by alexey on 9/23/16.
 */

public class MXSearchListAdapter extends RecyclerView.Adapter<MXSearchListAdapter.ViewHolder> {

    private List<MXUser> mDataset;
    private boolean isSearchByPostcode;

    public boolean isSearchByPostcode() {
        return isSearchByPostcode;
    }

    public void setIsSearchByPostcode(boolean isSearchByPostcode) {
        this.isSearchByPostcode = isSearchByPostcode;
    }

    public MXSearchListAdapter(List<MXUser> data) {
        mDataset = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //super.onBindViewHolder(holder, position);
        MXUser item = mDataset.get(position);

        holder.title.setText(new StringBuffer(item.last_name).append(", ").append(item.preferred_first_name));
        holder.subtitle.setText(item.specialty);
        holder.rightText.setText(item.address);

        if (AppUtils.isEmptyString(item.public_key)) {
            holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), R.color.unverified_text));
        } else {
            holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), android.R.color.primary_text_light));
        }


        if (isSearchByPostcode) {
            holder.distance.setVisibility(View.VISIBLE);
            float distance = 0;
            if (item != null) {
                distance = item.distance.isEmpty() ? 0 : Float.parseFloat(item.distance);
            }
            holder.distance.setText(String.format("%.2fkm", distance));


        }  else{
            holder.distance.setVisibility(View.GONE);
        }
    }

   /* @Override
    protected void onItemClick(View view, Doctor item, int itemPosition) {

    }*/

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView subtitle;
        TextView rightText;
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            rightText = (TextView) itemView.findViewById(R.id.rightText);
            distance = (TextView) itemView.findViewById(R.id.distance);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mDataset.size()) {
                MXUser doctor = mDataset.get(position);
                MXUserUtil.saveUserByInfo(doctor.dictionaryForUser());
                MXRelationshipUtil.saveRelationShipByInfo(doctor.dictionaryForUser(), MedXUser.CurrentUser().userId());
                String user_id = MXUserUtil.saveUsersByObject(doctor);
                Intent intent = new Intent(v.getContext(), MXDirectoryProfileActivity.class);
                intent.putExtra(MXDirectoryProfileActivity.USER_ID, user_id);
                v.getContext().startActivity(intent);
            }
        }
    }

    public void addAll(List<MXUser> lista){
        mDataset.addAll(lista);
        notifyDataSetChanged();
    }

    /**
     Permite limpiar todos los elementos del recycler
     **/
    public void clear(){
        mDataset.clear();
        notifyDataSetChanged();
    }
}
