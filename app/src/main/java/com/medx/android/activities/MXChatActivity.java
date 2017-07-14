package com.medx.android.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.medx.android.R;
import com.medx.android.adapters.MXChatAdapter;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.classes.notification.anim.NotificationOpenCloseAnimation;
import com.medx.android.classes.notification.timer.ActionTimer;
import com.medx.android.classes.notification.timer.ActionTimerListener;
import com.medx.android.classes.services.ChatHelper;
import com.medx.android.classes.services.ChatService;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.interfaces.DeliverCompletionListener;
import com.medx.android.interfaces.LocalStorageCompletionListener;
import com.medx.android.interfaces.MessageSaveCompletionListener;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXMessageDBHelper;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.ImagePicker;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXUserUtil;
import com.medx.android.utils.views.CircleProgressView;
import com.medx.android.utils.views.MXDialogManager;
import com.medx.android.utils.views.MXSoftKeyboardLsnedRelativeLayout;
import com.medx.android.utils.views.PtrClassicCustomLayout;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/23/16.
 */

public class MXChatActivity extends MXAuthActivity {

    /**
     * Properties field
     */

    public static String DATA = "user_id";

    @Bind(R.id.ptr)
    PtrClassicCustomLayout ptr;

    @Bind(android.R.id.list)
    RecyclerView list;
    private MXChatAdapter adapter;

    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.send)
    TextView send;

    @Bind(R.id.info)
    ImageView info;

    @Bind(R.id.message)
    EditText messageEdt;

    @Bind(R.id.softRoot)
    MXSoftKeyboardLsnedRelativeLayout softRoot;

    MedXUser currentUser;
    MXUser recipient;
    JSONArray messages;
    boolean isDecrypting;

    String filePath;

    @Bind(R.id.notification_bar)
    RelativeLayout notificationView;

    @Bind(R.id.not_text)
    TextView notificationText;

    @Bind(R.id.not_title)
    TextView notificationTitle;

    boolean isSendingTextMessage;
    boolean isPageRefreshing;

    private ActionTimer notificationTimer = new ActionTimer();

    private final int MAX_MESSAGE_LENGTH = 500;

    public static final int ATTACH_IMAGE = 202;

    static MXChatActivity instance;

    public static MXChatActivity getInstance()  {
        return instance;
    }

    /**
     * Init mehtods
     */

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "InComing":
                    int type = intent.getIntExtra("type", -1);
                    if (type == Utils.kKnotificationChatDidReceiveDialogMessages)
                        chatDidReceiveNewDialogMessageNotification();
                    break;
                case "progress_upload":
                    setProgressBar(intent);
                    break;
                case "progress_download":
                    setProgressBar(intent);
                    break;
                case "download":
                    processDownload(intent);
                    break;
                case "upload":
                    String info_st = intent.getStringExtra("message");
                    JSONObject info = AppUtils.getJSONFromString(info_st);
                    chatCellDidUpdateMessageInfo(info);
                    break;
                case "alert":
                    String text = intent.getStringExtra("message");
                    postTimedNotification(text);
                    break;
            }
        }
    };

    private void processDownload(Intent intent) {
        int file_size = intent.getIntExtra("file_size", 100);
        if (file_size > 0)  {
            String info_st = intent.getStringExtra("message");
            JSONObject info = AppUtils.getJSONFromString(info_st);
            chatCellDidUpdateMessageInfo(info);
        }

        String appMessageId = intent.getStringExtra("app_message_id");

        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject info = AppUtils.getJSONFromJSONArray(messages, i);
            if (appMessageId.equals(AppUtils.getStringFromJSON(info, "app_message_id")))
            {
                View view2 = list.getLayoutManager().findViewByPosition(i);
                if (view2 != null) {
                    CircleProgressView progressView2 = (CircleProgressView) view2.findViewById(R.id.progressCircle);
                    progressView2.setVisibility(View.GONE);
                }
            }
        }
    }

    public void chatCellDidUpdateMessageInfo(JSONObject message_info) {
        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject message = AppUtils.getJSONFromJSONArray(messages, i);
            if (AppUtils.getStringFromJSON(message, "app_message_id").equals(AppUtils.getStringFromJSON(message_info, "app_message_id")))   {
                try {
                    messages.put(i, message_info);
                    AppUtils.setJSONObjectWithObject(currentUser.userDialogs, recipient.user_id, AppUtils.reverseObjectEnumerator(messages));
                    adapter.notifyItemChanged(i);
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setProgressBar(Intent intent)   {
        int percent = intent.getIntExtra("percent", 100);
        String appMessageId = intent.getStringExtra("app_message_id");

        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject info = AppUtils.getJSONFromJSONArray(messages, i);
            if (appMessageId.equals(AppUtils.getStringFromJSON(info, "app_message_id")))
            {
                View view2 = list.getLayoutManager().findViewByPosition(i);
                if (view2 != null) {
                    CircleProgressView progressView2 = (CircleProgressView) view2.findViewById(R.id.progressCircle);
                    if (percent < 100) {
                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressView2.setProgress(percent);
                            }
                        }, 20);
                    } else
                        progressView2.setVisibility(View.GONE);
                }
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Utils.ids = new ArrayList<>();

        instance = this;
        String user_id = getIntent().getStringExtra(DATA);
        recipient = MXUserUtil.findByUserId(user_id);

        if (recipient != null)  {
            currentUser = MedXUser.CurrentUser();
            messages = new JSONArray();

            checkRecipientInfo();
            initPage();
        }

        softRoot.addSoftKeyboardLsner(new MXSoftKeyboardLsnedRelativeLayout.SoftKeyboardLsner() {
            @Override
            public void onSoftKeyboardShow() {
                list.scrollToPosition(messages.length() - 1);
            }

            @Override
            public void onSoftKeyboardHide() {

            }
        });

        title.setTextColor(ContextCompat.getColor(this, R.color.white));
        send.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        messageEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > MAX_MESSAGE_LENGTH){
                    // trim to MAX_LENGTH
                    CharSequence trimmed = s.subSequence(0, MAX_MESSAGE_LENGTH);
                    messageEdt.setText(trimmed);
                    messageEdt.setSelection(messageEdt.getText().length());
                    MXDialogManager.showSimpleDialog(MXChatActivity.this, null, getString(R.string.long_message));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        notificationTitle.setText("New Message");

        notificationTimer.addListener(new ActionTimerListener() {
            @Override
            public void actionTimerCompleted() {
                Handler mainHandler = new Handler(getMainLooper());
                Runnable post = new Runnable() {

                    @Override
                    public void run() {
                        if(notificationView.getVisibility() == View.VISIBLE)
                            hideNotification();
                    }
                };

                mainHandler.post(post);
            }
        });

        notificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNotification();
            }
        });
    }


    /**
     * Check Recipient's info again
     */

    private void checkRecipientInfo()   {
        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", currentUser.accessToken());
        AppUtils.setJSONObjectWithObject(params, "user_id", recipient.user_id);

        BackendBase.newSharedConnection().accessAPIFastGET(ApiURLs.URL_USERS_INFO, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if(response.equals("success"))  {
                    String userId = MXUserUtil.saveUserByInfo(AppUtils.getJSONFromJSON(result, "user"));
                    recipient = MXUserDBHelper.getInstance().getUser(userId);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {

            }
        });
    }

    private void initPage() {
        setRecipient();
        setupList();
        ptr.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                isPageRefreshing = true;
                if (ptr.isRefreshing())
                    ptr.refreshComplete();
                loadMessages();
            }
        });

        loadMessages();
    }

    private void setRecipient() {
        MXUser recipient = this.recipient;

        title.setTextColor(ContextCompat.getColor(this, R.color.white));
        send.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        title.setText(new StringBuffer(recipient.salutation).append(" ").append(recipient.preferred_first_name).append(" ").append(recipient.last_name));

        ChatService.dialogRecipientId = recipient.user_id;
    }

    /**
     * Data Source Methods
     */

    public void loadMessages()  {
        new LoadMessages().execute();
    }

    // Mark status of new message in server to read
    private void markToReadOfServiceMessages(JSONArray messageList) {
        JSONObject lastTextMessage = lastTextMesasgeInMessages(messageList);
        if (lastTextMessage != null)    {
            ArrayList<String> tempIds = new ArrayList<>();
            tempIds.add(AppUtils.getStringFromJSON(lastTextMessage, "app_message_id"));
            ChatService.instance.updateReceivedMessageStatus("2", tempIds, new CompletionListener() {
                @Override
                public void complete(boolean success, String errorStatus) {

                }
            });
        }
    }

    private JSONObject lastTextMesasgeInMessages(JSONArray messageList)   {
        JSONArray newMessageList = AppUtils.reverseObjectEnumerator(messageList);
        for (int i = 0; i < newMessageList.length(); i ++) {
            JSONObject m = AppUtils.getJSONFromJSONArray(newMessageList, i);
            int messageType = Integer.parseInt(AppUtils.getStringFromJSON(m, "type"));
            if (messageType == 0)   {
                return m;
            }
        }

        return null;
    }

    /**
     * Adds or updates data source with new message
     */

    private void addNewMessages(ArrayList<MXMessage> newMessages)   {

        for (MXMessage m : newMessages) {
            addNewMessage(m);
        }

        list.scrollToPosition(adapter.getItemCount() - 1);
        currentUser.resetApplicationBadge();
        markToReadOfServiceMessages(AppUtils.JSONFromArrayWithMessage(newMessages));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void addNewMessage(MXMessage newMessage)   {
        int dataSourceIndex = indexForAppMessageId(newMessage.app_message_id);
        JSONObject new_messsage_info = MXMessageUtil.dictionaryWithValuesFromMessage(newMessage);

        if (dataSourceIndex == -1)  {
            // Adds new messages at the right place by sent_at
            if (messages.length() > 0)  {
                int targetIndex = -1;
                for (int i = messages.length() - 1 ; i > 0; i --)    {
                    JSONObject m = AppUtils.getJSONFromJSONArray(messages, i);
                    if (m.length() > 0) {
                        if (AppUtils.compareDate(newMessage.sent_at, AppUtils.DateFromString(AppUtils.getStringFromJSON(m, "sent_at"))) == 1) {
                            targetIndex = i - 1;
                        } else
                            break;
                    }
                }

                if (targetIndex == -1)  {
                    messages.put(new_messsage_info);
                    adapter.notifyItemInserted(messages.length());
                } else {
                    AppUtils.insertObjectToJSONArray(messages, targetIndex, new_messsage_info);
                    adapter.notifyItemInserted(targetIndex);
                }
            } else {
                messages.put(new_messsage_info);
                adapter.notifyItemInserted(messages.length());
            }
        } else {
            try {
                messages.put(dataSourceIndex, new_messsage_info);
                adapter.notifyItemChanged(dataSourceIndex);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * Check messge index in data source by app_message_id
     */

    private int indexForAppMessageId(String appMessageId)   {
        int count = messages.length();
        for (int i = count; i > 0; i --)    {
            JSONObject m = AppUtils.getJSONFromJSONArray(messages, i - 1);
            if (AppUtils.getStringFromJSON(m, "app_message_id").equals(appMessageId))   {
                return i-1;
            }
        }

        return -1;
    }

    /**
     * Text Message
     */

    @OnClick(R.id.send)
    public void pnClickSend(View view) {
        hideKeyboard();
        if (AppUtils.isEmptyString(messageEdt.getText().toString()))
            return;

        isSendingTextMessage = true;
        checkToPopupInvitation();
    }

    private void handleMessageDeliverStatus(int message_status, String app_message_id)  {
        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject message_info = AppUtils.getJSONFromJSONArray(messages, i);
            if (AppUtils.getStringFromJSON(message_info, "app_message_id").equals(app_message_id))  {
                AppUtils.setJSONObjectWithObject(message_info, "status", message_status);
                break;
            }
        }

        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject message = AppUtils.getJSONFromJSONArray(messages, i);
            if (app_message_id.equals(AppUtils.getStringFromJSON(message, "app_message_id")))   {
                adapter.notifyItemChanged(i);
                list.scrollToPosition(adapter.getItemCount() - 1);
                break;
            }
        }

        if (message_status == 100)
            showSimpleMessage("", getString(R.string.communication_error), "OK", null);
    }

    private void sendTextMessage()  {
        JSONObject message_info = sendingMessageInfoByType(0);
        messageEdt.setText("");

        showProgress("Encrypting...");

        ChatService.instance.sendTextMessage(message_info, new LocalStorageCompletionListener() {
            @Override
            public void complete(String app_message_id, boolean error) {
                if (AppUtils.isNotEmptyString(app_message_id))  {
                    MXMessage saveMessage = MXMessageDBHelper.getInstance().getMessage(app_message_id);
                    JSONObject new_message_info = null;
                    try {
                        new_message_info = new JSONObject(message_info.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppUtils.setJSONObjectWithObject(new_message_info, "is_encrypted", saveMessage.is_encrypted);
                    messages.put(new_message_info);
                    hideProgress();

                    adapter.notifyItemInserted(messages.length());
                    list.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        }, new DeliverCompletionListener() {
            @Override
            public void complete(String app_message_id, int message_status) {
                handleMessageDeliverStatus(message_status, app_message_id);
            }
        });
    }

    /**
     * Photo Message
     */

    public void sendAttachment()    {
        JSONObject message_info = sendingMessageInfoByType(1);

        showProgress("Encrypting...");

        MXMessageUtil.saveMessageByInfo(message_info, filePath.isEmpty() ? null : filePath, new MessageSaveCompletionListener() {
            @Override
            public void complete(String app_message_id, String sharedKeyString, Error error) {
                if (AppUtils.isNotEmptyString(app_message_id)) {
                    MXMessage saveMessage = MXMessageDBHelper.getInstance().getMessage(app_message_id);
                    JSONObject new_message_info = null;
                    try {
                        new_message_info = new JSONObject(message_info.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppUtils.setJSONObjectWithObject(new_message_info, "is_encrypted", saveMessage.is_encrypted);

                    if (AppUtils.isNotEmptyString(sharedKeyString)) {
                        AppUtils.setJSONObjectWithObject(new_message_info, "text", sharedKeyString);
                    }
                    messages.put(new_message_info);

                    ChatService.instance.sendPhotoMessage(saveMessage, new DeliverCompletionListener() {
                        @Override
                        public void complete(String app_message_id, int message_status) {
                            if (message_status == 100)
                                handleMessageDeliverStatus(100, app_message_id);

                            if (!app_message_id.isEmpty()) {
                                adapter.notifyItemInserted(messages.length());
                                list.scrollToPosition(adapter.getItemCount() - 1);
                                hideProgress();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Invitation Pop-up
     */

    public void checkToPopupInvitation()    {
        if (!recipient.hasInstallApp() && !recipient.isVerified()) {
            showSMSInvitionDlg();
        } else {
            sendMessage();
        }
    }

    private void showSMSInvitionDlg() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.sms_custom_edit, null);
        EditText etPhone = (EditText) dialoglayout.findViewById(R.id.phone);
        new AlertDialog.Builder(this)
                .setMessage(String.format(getString(R.string.SMSinvation), recipient.salutation + " " + recipient.fullName()))
                .setPositiveButton(R.string.send_sms, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inviteByPhone(etPhone.getText().toString());
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .setView(dialoglayout)
                .show();
    }

    private void inviteByPhone(String phone) {
        showProgress(getString(R.string.sending_invitation));
        MedXUser.CurrentUser().inviteUser(recipient.user_id, phone, new CompletionListener() {
            @Override
            public void complete(boolean success, String errorStatus) {
                hideProgress();
                if (success)
                    sendMessage();
                else
                    showSimpleMessage("", errorStatus, "OK", null);
            }
        });
    }

    /**
     * Send message
     */

    private void sendMessage()  {
        if (isSendingTextMessage)
            sendTextMessage();
        else
            sendAttachment();
    }

    private JSONObject sendingMessageInfoByType(int message_type) {
        String sender_id = currentUser.userId();
        Date sent_at = Calendar.getInstance().getTime();
        String app_message_id = MXMessageUtil.appMessageIdByUserId(sender_id, sent_at);

        JSONObject data = new JSONObject();

        if (message_type == 0)  {
            AppUtils.setJSONObjectWithObject(data, "text", messageEdt.getText().toString());
            AppUtils.setJSONObjectWithObject(data, "sender_id", currentUser.userId());
            AppUtils.setJSONObjectWithObject(data, "recipient_id", recipient.user_id);
            AppUtils.setJSONObjectWithObject(data, "sent_at", AppUtils.StringFromDate(sent_at));
            AppUtils.setJSONObjectWithObject(data, "app_message_id", app_message_id);
            AppUtils.setJSONObjectWithObject(data, "type", 0);
        } else {
            AppUtils.setJSONObjectWithObject(data, "sender_id", sender_id);
            AppUtils.setJSONObjectWithObject(data, "recipient_id", recipient.user_id);
            AppUtils.setJSONObjectWithObject(data, "sent_at", AppUtils.StringFromDate(sent_at));
            AppUtils.setJSONObjectWithObject(data, "app_message_id", app_message_id);
            AppUtils.setJSONObjectWithObject(data, "filename", MXMessageUtil.imageFileNameByAppMessageId(app_message_id));
            AppUtils.setJSONObjectWithObject(data, "type", 1);
            AppUtils.setJSONObjectWithObject(data, "width", AppUtils.getImageWidthFromFile(filePath));
            AppUtils.setJSONObjectWithObject(data, "height", AppUtils.getImageWidthFromFile(filePath));
        }

        return data;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("InComing");
        IntentFilter intentFilter1 = new IntentFilter("progress_upload");
        IntentFilter intentFilter2 = new IntentFilter("progress_download");
        IntentFilter intentFilter3 = new IntentFilter("download");
        IntentFilter intentFilter4 = new IntentFilter("upload");
        IntentFilter intentFilter5 = new IntentFilter("alert");
        registerReceiver(broadcastReceiver, intentFilter);
        registerReceiver(broadcastReceiver, intentFilter1);
        registerReceiver(broadcastReceiver, intentFilter2);
        registerReceiver(broadcastReceiver, intentFilter3);
        registerReceiver(broadcastReceiver, intentFilter4);
        registerReceiver(broadcastReceiver, intentFilter5);

        ChatService.dialogRecipientId = recipient.user_id;
        ChatService.dialogViewName = "CHAT";

        Intent intent = new Intent(MXChatActivity.this, ChatService.class);
        intent.putExtra("controller", "chat");
        getApplicationContext().startService(intent);
    }

    public void hideNotification() {
        // check to be sure we have a notification view for this activity
        if (notificationView != null) {

            // initiate close animation if visible
            if(notificationView.getVisibility() == View.VISIBLE) {
                NotificationOpenCloseAnimation anim = new NotificationOpenCloseAnimation(
                        notificationView, 1000);
                notificationView.startAnimation(anim);
            }
        }
    }

    public void postNotification(String message, boolean closeable) {

        // check to be sure we have a notification view for this activity
        if (notificationView != null) {

            // change the notification message
            notificationText.setText(message);

            // initiate open animation if not visible
            if(notificationView.getVisibility() != View.VISIBLE) {
                NotificationOpenCloseAnimation anim = new NotificationOpenCloseAnimation(
                        notificationView, 10000);
                notificationView.startAnimation(anim);
            }
        }
    }

    public void postTimedNotification(String message) {
        if (notificationView != null) {

            // start delayed timer
            notificationTimer.startTimer();

            // show notification
            postNotification(message, false);
        }
    }

    @Override
    protected void onPause() {
        ChatService.instance.stopRegularCheck();
        Intent intent = new Intent(this, ChatHelper.class);
        intent.putExtra("mode", "resetUser");
        getApplicationContext().startService(intent);

        super.onPause();
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.info)
    void onClickInfo(View view) {
        Intent intent = new Intent(this, MXDirectoryProfileActivity.class);
        intent.putExtra(MXDirectoryProfileActivity.USER_ID, recipient.user_id);
        intent.putExtra(MXDirectoryProfileActivity.FROM_CHAT, true);
        pushView(intent);
    }

    @OnClick(R.id.attach)
    void onClickAttach(View view) {
        Intent intentAvatar = ImagePicker.getPickImageIntent(this);
        startActivityForResult(intentAvatar, ATTACH_IMAGE);
    }

    private void chatDidReceiveNewDialogMessageNotification()   {
        ArrayList<String> incomingsAppMessageIds =  AppUtils.getArrayFromJSON(Utils.notificationData, "incomings");

        // Update message status with read
        ArrayList<String> readAppMessageIds = AppUtils.getArrayFromJSON(Utils.notificationData, "reads");
        for (int i = 0; i < messages.length(); i ++)    {
            JSONObject message_info = AppUtils.getJSONFromJSONArray(messages, i);
            if (readAppMessageIds.indexOf(AppUtils.getStringFromJSON(message_info, "app_message_id")) != -1)    {
                AppUtils.setJSONObjectWithObject(message_info, "status", 2);
            }
        }

        if(incomingsAppMessageIds.size() > 0 || readAppMessageIds.size() > 0)   {
            if (incomingsAppMessageIds.size() > 0)  {
                MXMessageUtil.updateMessagesInAppMessagesIds(AppUtils.JSONFromArray(incomingsAppMessageIds), 2, true, new CompletionListener() {
                    @Override
                    public void complete(boolean success, String errorStatus) {
                        ArrayList<MXMessage> newMessages = new ArrayList<>();
                        for (String app_message_id : incomingsAppMessageIds)    {
                            MXMessage message = MXMessageDBHelper.getInstance().getMessage(app_message_id);
                            newMessages.add(message);
                        }

                        addNewMessages(newMessages);
                        resetUserDialog();
                    }
                });
            } else {
                for (String id : readAppMessageIds) {
                    for (int i = 0; i < messages.length(); i ++)    {
                        JSONObject message = AppUtils.getJSONFromJSONArray(messages, i);
                        if (id.equals(AppUtils.getStringFromJSON(message, "app_message_id")))   {
                            adapter.notifyItemChanged(i);
                        }
                    }
                }
                resetUserDialog();
            }
        }
    }

    private void resetUserDialog()  {
        AppUtils.setJSONObjectWithObject(currentUser.userDialogs, recipient.user_id, AppUtils.reverseObjectEnumerator(messages));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // get attach uri
            Uri uri = ImagePicker.getImageUriFromResult(this, resultCode, data);
            if (uri != null) {
                isSendingTextMessage = false;

                filePath = AppUtils.copyImageToTempFolder(this, uri, "temp.jpg");
                checkToPopupInvitation();
            }
        }
    }

    private void setupList()    {
        if (list.getAdapter() != null)  {
            adapter.clear();
            adapter.addAll(messages);
        } else {
            adapter = new MXChatAdapter(this, this, messages, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if no view has focus:
                    // messageEdt = messageEdt.getCurrentFocus();
                    if (messageEdt != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
                    }
                }
            });

            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            list.setLayoutManager(llm);
            list.setAdapter(adapter);
            list.addItemDecoration(new StickyRecyclerHeadersDecoration(adapter));
        }
    }

    public void callUploadService(String filePath, JSONObject messageInfo)  {
        Intent intent = new Intent(MXChatActivity.this, ChatHelper.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("type", "upload");
        intent.putExtra("message", messageInfo.toString());
        getApplicationContext().startService(intent);
    }

    public void callDownloadService(String filePath, JSONObject messageInfo)    {
        Intent intent = new Intent(MXChatActivity.this, ChatHelper.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("type", "download");
        intent.putExtra("message", messageInfo.toString());
        getApplicationContext().startService(intent);
    }

    class LoadMessages extends AsyncTask<Void, Void, Void>  {
        JSONArray emptyStatusAppMessageIds = new JSONArray();
        JSONArray notMarkReadAppMessageIds = new JSONArray();
        ArrayList<MXMessage> originalMessages = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isDecrypting = true;

            String currentUserId = currentUser.userId();
            int nDecryptingCount = 0;

            if (messages == null)
                messages = new JSONArray();
            if ((messages.length() == 0) && AppUtils.getJSONArrayFromJSON(currentUser.userDialogs, recipient.user_id).length() > 0)    {
                JSONArray originalMessgesJSON = AppUtils.getJSONArrayFromJSON(currentUser.userDialogs, recipient.user_id);

                ArrayList<MXMessage> messages1 = MXMessageUtil.findMessagesBetweenUsers(currentUserId, recipient.user_id, AppUtils.DateFromString(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSONArray(originalMessgesJSON, 0), "sent_at")));

                assert originalMessgesJSON != null;
                for (int i = 0; i < originalMessgesJSON.length(); i ++) {
                    JSONObject messageInfo = AppUtils.getJSONFromJSONArray(originalMessgesJSON, i);
                    MXMessage message = new MXMessage();
                    message.updateMessageWithInfo(messageInfo);
                    originalMessages.add(message);
                }

                for (MXMessage m : messages1)   {
                    originalMessages.add(0, m);
                }

                nDecryptingCount = messages1.size();
            } else {
                Date lastSentAt = null;
                if (messages.length() > 0)    {
                    lastSentAt = AppUtils.DateFromString(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSONArray(messages, 0), "sent_at"));
                }

                originalMessages = MXMessageUtil.findMessagesBetweenUsers(currentUserId, recipient.user_id, lastSentAt, 10);

                nDecryptingCount = originalMessages.size();
            }

            if (nDecryptingCount > 0)
                showProgress("Decrypting...");
        }
        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<MXMessage> finalOriginalMessages = originalMessages;
            for (MXMessage message : finalOriginalMessages)  {
                JSONObject message_info = new JSONObject();
                message_info = MXMessageUtil.dictionaryWithValuesFromMessage(message);

                if (AppUtils.isEmptyString(AppUtils.getStringFromJSON(message_info, "status"))) {
                    emptyStatusAppMessageIds.put(AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    AppUtils.setJSONObjectWithObject(message_info, "status", 100);
                }

                boolean receiveMessage = AppUtils.getStringFromJSON(message_info, "recipient_id").equals(currentUser.userId());
                if (receiveMessage) {
                    if (AppUtils.isEmptyString(AppUtils.getStringFromJSON(message_info, "status")) || Integer.parseInt(AppUtils.getStringFromJSON(message_info, "status")) != 2)    {
                        notMarkReadAppMessageIds.put(AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    }
                }

                messages = AppUtils.insertObjectToJSONArray(messages, 0, message_info);
            }

            // Marks some of sent/received messages to NOT_DELIVERED/READ
            MXMessageUtil.updateMessagesInAppMessagesIds(emptyStatusAppMessageIds, 100, false, new CompletionListener() {
                @Override
                public void complete(boolean success, String errorStatus) {
                    MXMessageUtil.updateMessagesInAppMessagesIds(notMarkReadAppMessageIds, 2, false, new CompletionListener() {
                        @Override
                        public void complete(boolean success, String errorStatus) {
                            if (errorStatus == null)    {

                            }
                        }
                    });
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread

            AppUtils.setJSONObjectWithObject(currentUser.userDialogs, recipient.user_id, AppUtils.reverseObjectEnumerator(messages));
            AppUtils.setJSONObjectWithObject(currentUser.userDialogs, recipient.user_id, AppUtils.reverseObjectEnumerator(messages));

            currentUser.resetApplicationBadge();
            markToReadOfServiceMessages(messages);
            hideProgress();

            isDecrypting = false;
            for (int i = 0; i < messages.length(); i ++)    {
                JSONObject info = AppUtils.getJSONFromJSONArray(messages, i);
            }

            setupList();
            if (!isPageRefreshing)
                list.scrollToPosition(adapter.getItemCount() - 1);

            isPageRefreshing = false;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            if (ptr.isRefreshing())
                ptr.refreshComplete();
        }
    }

    @Override
    protected void onDestroy()  {
        super.onDestroy();
        getApplicationContext().stopService(new Intent(MXChatActivity.this, ChatService.class));
        getApplicationContext().stopService(new Intent(MXChatActivity.this, ChatHelper.class));
        unregisterReceiver(broadcastReceiver);
    }
}
