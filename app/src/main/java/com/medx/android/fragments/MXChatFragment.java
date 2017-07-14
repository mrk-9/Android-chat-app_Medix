package com.medx.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.medx.android.App;
import com.medx.android.R;
import com.medx.android.activities.MXAuthActivity;
import com.medx.android.adapters.MXChatListAdapter;
import com.medx.android.classes.services.ChatHelper;
import com.medx.android.classes.services.ChatService;
import com.medx.android.interfaces.ChatlistLongTapListener;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXRelationshipUtil;
import com.medx.android.utils.views.MXDialogManager;
import com.medx.android.utils.views.PtrClassicCustomLayout;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * Created by alexey on 9/21/16.
 */

public class MXChatFragment extends MXBaseFragment {
    /**
     * Properties field
     */

    private static final String TAG = "MXChatListFragment";
    @Bind(android.R.id.list)
    RecyclerView list;
    MXChatListAdapter adapter;

    @Bind(R.id.ptr)
    PtrClassicCustomLayout ptr;

    @Bind(R.id.empty)
    TextView lblNomore;

    MedXUser currentUser;
    ArrayList<MXUser> recipientts;

    int nFailedCount;

    boolean isLoadMore;

    static MXChatFragment fragment;

    /**
     * static methods
     */

    public static MXChatFragment newInstance() {
        if (fragment == null) {
            Bundle args = new Bundle();
            MXChatFragment fragment = new MXChatFragment();
            fragment.setArguments(args);
        }

        return fragment;
    }

    /**
     * Init methods
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        // fillData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser == null)    {
            currentUser = MedXUser.CurrentUser();

            recipientts = new ArrayList<>();
            setupList();

            initializeTableView();

            Utils.isDataLoaded = true;

            loadDataSource(false);

            Intent intent = new Intent(getContext(), ChatService.class);
            intent.putExtra("controller", "root");
            App.DefaultContext.startService(intent);
        }
    }

    private void initializeTableView()  {
        nFailedCount = 0;

        ptr.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (!((MXAuthActivity)getActivity()).isOnline()){
                    MXDialogManager.showSimpleDialog(getContext(), null, getString(R.string.communication_error));
                    if (ptr.isRefreshing())
                        ptr.refreshComplete();
                    return;
                }
                Log.d(TAG, "Loading...");

                loadDataSource(false);

                Intent intent = new Intent(getContext(), ChatService.class);
                intent.putExtra("controller", "root");
                App.DefaultContext.startService(intent);
            }
        });

        recipientts = new ArrayList<>();

        IntentFilter intentFilter = new IntentFilter("InComing");
        IntentFilter intentFilter1 = new IntentFilter("loadHistory");

        App.DefaultContext.registerReceiver(broadcastReceiver, intentFilter);
        App.DefaultContext.registerReceiver(broadcastReceiver, intentFilter1);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Utils.isDataLoaded) {
            Utils.isDataLoaded = true;

            loadDataSource(false);
            Intent intent = new Intent(getContext(), ChatService.class);
            intent.putExtra("controller", "root");
            App.DefaultContext.startService(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.isDataLoaded = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.DefaultContext.unregisterReceiver(broadcastReceiver);
        App.DefaultContext.stopService(new Intent(getContext(), ChatService.class));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "InComing":
                    int type = intent.getIntExtra("type", -1);
                    if (type == Utils.kNotificationChatDidReceiveNewMessage)
                        chatDidReceiveNewMessageNotification();
                    else if (type == Utils.kNotificationChatDidFailInCheckAllDialogs)
                        chatDidFailInCheckAllDialogsNotification();
                    break;
                case "loadHistory":
                    loadHistory();
            }

        }
    };

    /**
     * load history
     */

    private void loadDataSource(boolean load_more)  {
        isLoadMore = load_more;

        Intent intent = new Intent(getActivity(), ChatHelper.class);
        intent.putExtra("mode", "loadHistory");
        intent.putExtra("user_id", currentUser.userId());
        App.DefaultContext.startService(intent);
    }

    private void loadHistory()  {
        if (ptr.isRefreshing())
            ptr.refreshComplete();

        if (Utils.partners.size() > 0)  {
            recipientts = Utils.partners;
            setupList();
        }

        if (recipientts.size() > 0)
            lblNomore.setVisibility(View.GONE);
        else
            lblNomore.setVisibility(View.VISIBLE);

    }

    /**
     * Notification Methods
     */

    public void chatDidReceiveNewMessageNotification()  {

        JSONArray incomingAppMessageIds = AppUtils.getJSONArrayFromJSON(Utils.notificationData, "incomings");
        JSONArray readAppMessageIds = AppUtils.getJSONArrayFromJSON(Utils.notificationData, "reads");

        if (incomingAppMessageIds.length() > 0 || readAppMessageIds.length() > 0)   {
            loadDataSource(true);
            isLoadMore = true;
        } else {
            if (ptr.isRefreshing())
                ptr.refreshComplete();
        }
    }

    public void chatDidFailInCheckAllDialogsNotification()  {
        if (ptr.isRefreshing())
            ptr.refreshComplete();
        if (nFailedCount ++ > 0 && ChatService.dialogRecipientId == null)    {
            showSimpleMessage("", getString(R.string.connection_error), "OK", null);
        }
    }

    /**
     * set up list
     */

    private void setupList()    {
        if (list.getAdapter() != null) {
            adapter.clear();
            adapter.addAll(recipientts);
        } else {
            adapter = new MXChatListAdapter(getContext(), recipientts, new ChatlistLongTapListener() {
                @Override
                public void longTap(String user_id) {
                    MXMessageUtil.deleteConversationBetweenUsers(currentUser.userId(), user_id);
                    AppUtils.setJSONObjectWithObject(currentUser.userDialogs, user_id, new JSONArray());
                    int position = 0;

                    for (MXUser m : recipientts)    {
                        if (m.user_id.equals(user_id))
                        {
                            position = recipientts.indexOf(m);
                            break;
                        }
                    }

                    if (recipientts.size() > position)
                        recipientts.remove(position);
                    setupList();
                }
            });
            list.setAdapter(adapter);
            list.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }
}
