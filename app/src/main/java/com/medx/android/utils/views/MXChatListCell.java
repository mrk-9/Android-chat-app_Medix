package com.medx.android.utils.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.activities.MXChatActivity;
import com.medx.android.adapters.MXChatListAdapter;
import com.medx.android.interfaces.ChatlistLongTapListener;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.models.chat.MXRelationship;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.chat.EncryptionUtil;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXRelationshipUtil;

/**
 * Created by alexey on 10/21/16.
 */

public class MXChatListCell extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    MXUser item;
    ChatlistLongTapListener listener;

    TextView image;
    TextView title;
    TextView subtitle;
    TextView date;
    ImageView avBackground;
    ImageView greenIndicator;
    RelativeLayout tabView;

    public MXChatListCell(View itemView) {
        super(itemView);

        image = (TextView) itemView.findViewById(R.id.image);
        title = (TextView) itemView.findViewById(R.id.title);
        subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        date = (TextView) itemView.findViewById(R.id.date);
        avBackground = (ImageView) itemView.findViewById(R.id.avBackground);
        greenIndicator = (ImageView) itemView.findViewById(R.id.greenIndicator);
        tabView = (RelativeLayout) itemView.findViewById(R.id.tabView);

        tabView.setOnClickListener(this);
        tabView.setOnLongClickListener(this);
    }

    public void setupCell(MXUser mUser, ChatlistLongTapListener cellListener)   {
        item = mUser;
        listener = cellListener;

        MedXUser currentUser = MedXUser.CurrentUser();

        MXRelationship rel = MXRelationshipUtil.findRelationshipByUserId(currentUser.userId(), item.user_id);
        if (rel != null)
            rel.updateUser();
        // First & last name
        title.setText(item.fullName());
        if (!item.hasInstallApp() && !item.isVerified())    {
            title.setTextColor(ContextCompat.getColor(title.getContext(), R.color.unverified_text));
        } else {
            title.setTextColor(ContextCompat.getColor(title.getContext(), android.R.color.primary_text_light));
        }

        // Last message
        subtitle.setTextColor(Color.BLACK);
        MXMessage lastMessage = MXMessageUtil.findLastMessagesBetweenUsers(currentUser.userId(), item.user_id);
        if(lastMessage != null) {
            if (lastMessage.type == 0) {
                String decruptedText = lastMessage.text;
                if (lastMessage.is_encrypted.equals("1")) {
                    try {
                        decruptedText = EncryptionUtil.decrypt(lastMessage.text, currentUser.privateKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                        decruptedText = "";
                    }
                    if (AppUtils.isEmptyString(decruptedText)) {
                        decruptedText = "Message sent to difference device";
                        subtitle.setTypeface(subtitle.getTypeface(), Typeface.ITALIC);
                    } else {
                        subtitle.setTypeface(subtitle.getTypeface(), Typeface.NORMAL);
                    }
                }

                if (decruptedText.length() > 60) {
                    subtitle.setText(decruptedText.substring(0, 60));
                } else {
                    subtitle.setText(decruptedText);
                }
            } else {
                subtitle.setText("Photo");
            }

            if (lastMessage.status == 2) {
                subtitle.setTextColor(Color.GRAY);
            } else {
                subtitle.setTextColor(Color.RED);
            }
        }   else {
            subtitle.setText("");
        }

        // Unread message count
        if (MXMessageUtil.countOfUnreadMessageRecipientBySender(rel.user, item) == 0)   {
            greenIndicator.setVisibility(View.GONE);
        } else {
            greenIndicator.setVisibility(View.VISIBLE);
        }

        // Last messaged time
        long lastStamp = rel.last_message_date.getTime() / 1000;
        long timeStamp = System.currentTimeMillis() / 1000;
        if (timeStamp - lastStamp < 60) {
            date.setText(((int) timeStamp - lastStamp) + " secs");
        } else if (timeStamp - lastStamp < 3600)    {
            date.setText(((int) timeStamp - lastStamp) / 60 + " mins ");
        } else if (timeStamp - lastStamp < 86400)   {
            date.setText(AppUtils.DateStringWithFormatter(rel.last_message_date, "hh:mm a"));
        } else if (timeStamp - lastStamp < 604800)  {
            date.setText(AppUtils.DateStringWithFormatter(rel.last_message_date, "EEE hh:mm a"));
        } else {
            date.setText(AppUtils.DateStringWithFormatter(rel.last_message_date, "MMM dd YYYY"));

            if (lastStamp == 0) {
                subtitle.setText(subtitle.getContext().getString(R.string.auto_destored));
                subtitle.setTextColor(Color.GRAY);
            }
        }

        date.setTextColor(Color.GRAY);

        int index = avtarBGColorIndex(item);

        Resources res = avBackground.getResources();
        int[] colorArray = res.getIntArray(R.array.avatar_colors);

        int color = colorArray[index];
        avBackground.setColorFilter(color);

        image.setTextColor(ContextCompat.getColor(image.getContext(), R.color.white));

        StringBuffer imageText = new StringBuffer();

        String firstName = item.first_name;
        if (item.preferred_first_name != null && item.preferred_first_name.length() > 0){
            firstName = item.preferred_first_name;
        }
        imageText.append(String.valueOf(firstName).charAt(0))
                .append(String.valueOf(item.last_name).charAt(0));
        image.setText(imageText.toString().toUpperCase());
    }

    private int avtarBGColorIndex(MXUser item){
        return (Integer.parseInt(item.user_id) * 29 * contactIdentifier(item).length()) %21;
    }

    private String contactIdentifier(MXUser item){
        String contact = item.first_name + " " + item.last_name + " " + item.specialty;
        return contact;
    }

    @Override
    public void onClick(View v) {
        Utils.isDataLoaded = false;
        Intent intent = new Intent(v.getContext(), MXChatActivity.class);
        intent.putExtra(MXChatActivity.DATA, item.user_id);
        image.getContext().startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        //Toast.makeText(view.getContext(), "long click " + this.getAdapterPosition(), Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(v.getContext())
                .setTitle("")
                .setMessage(String.format(v.getResources().getString(R.string.confirm_delete_conversation), item.salutation + " " + item.fullName()))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.longTap(item.user_id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
        return true;
    }
}
