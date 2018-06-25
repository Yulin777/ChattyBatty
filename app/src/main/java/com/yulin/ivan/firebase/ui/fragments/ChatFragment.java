package com.yulin.ivan.firebase.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yulin.ivan.firebase.R;
import com.yulin.ivan.firebase.core.chat.ChatContract;
import com.yulin.ivan.firebase.core.chat.ChatPresenter;
import com.yulin.ivan.firebase.events.PushNotificationEvent;
import com.yulin.ivan.firebase.models.Chat;
import com.yulin.ivan.firebase.ui.adapters.ChatRecyclerAdapter;
import com.yulin.ivan.firebase.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener {
    public static String filterChoice = "original";
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firebaseToken) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(Constants.ARG_RECEIVER_UID));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString();
        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiverFirebaseToken = getArguments().getString(Constants.ARG_FIREBASE_TOKEN);
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(),
                chat,
                receiverFirebaseToken);
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(filterMessage(chat));
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    public void setFilterChoiceOriginal() {
        filterChoice = "original";
    }

    public void setFilterChoiceCensored() {
        filterChoice = "censored";
    }

    public void setFilterChoiceAntagonized() {
        filterChoice = "antagonized";
    }

    private Chat filterMessage(Chat chat) {
        switch (filterChoice) {
            case "censored":
                return censorMessage(chat);
            case "antagonized":
                return antagonizeMessage(chat);
            default:
                return chat;
        }
    }

    private Chat censorMessage(Chat chat) {
        chat.message = chat.message.toLowerCase().replaceAll("fuck", "love");
        chat.message = chat.message.toLowerCase().replaceAll("shit", "butterfly");
        chat.message = chat.message.toLowerCase().replaceAll("cock", "horn");
        chat.message = chat.message.toLowerCase().replaceAll("cunt", "lady");
        chat.message = chat.message.toLowerCase().replaceAll("nigger", "man");
        chat.message = chat.message.toLowerCase().replaceAll("faggot", "homosexual");
        chat.message = chat.message.toLowerCase().replaceAll("motherfucker", "motherlover");
        chat.message = chat.message.toLowerCase().replaceAll("penis", "man's organ");
        chat.message = chat.message.toLowerCase().replaceAll("vagina", "woman's organ");
        chat.message = chat.message.toLowerCase().replaceAll("asshole", "back exit");
        return chat;
    }

    private Chat antagonizeMessage(Chat chat) {
        chat.message = chat.message.toLowerCase().replaceAll("love", "fuck");
        chat.message = chat.message.toLowerCase().replaceAll("butterfly", "shit");
        chat.message = chat.message.toLowerCase().replaceAll("horn", "cock");
        chat.message = chat.message.toLowerCase().replaceAll("lady", "cunt");
        chat.message = chat.message.toLowerCase().replaceAll("man", "nigger");
        chat.message = chat.message.toLowerCase().replaceAll("homosexual", "faggot");
        chat.message = chat.message.toLowerCase().replaceAll("motherlover", "motherfucker");
        return chat;
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onPushNotificationEvent(PushNotificationEvent pushNotificationEvent) {
        if (mChatRecyclerAdapter == null || mChatRecyclerAdapter.getItemCount() == 0) {
            mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    pushNotificationEvent.getUid());
        }
    }
}
