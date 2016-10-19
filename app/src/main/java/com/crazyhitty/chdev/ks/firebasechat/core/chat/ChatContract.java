package com.crazyhitty.chdev.ks.firebasechat.core.chat;

import android.app.Activity;

import com.crazyhitty.chdev.ks.firebasechat.models.Chat;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 11:06 AM
 * Project: FirebaseChat
 */

public interface ChatContract {
    interface View {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);

        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }

    interface Presenter {
        void sendMessage(Activity activity, Chat chat, String receiverFirebaseToken);

        void getMessage(Activity activity, String senderUid, String receiverUid);
    }

    interface Interactor {
        void sendMessageToFirebaseUser(Activity activity, Chat chat, String receiverFirebaseToken);

        void getMessageFromFirebaseUser(Activity activity, String senderUid, String receiverUid);
    }

    interface OnSendMessageListener {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);
    }

    interface OnGetMessagesListener {
        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }
}
