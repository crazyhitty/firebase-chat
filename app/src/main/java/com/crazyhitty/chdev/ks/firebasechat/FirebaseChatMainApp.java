package com.crazyhitty.chdev.ks.firebasechat;

import android.app.Application;

/**
 * Author: Kartik Sharma
 * Created on: 10/16/2016 , 9:35 PM
 * Project: FirebaseChat
 */

public class FirebaseChatMainApp extends Application {
    private static boolean sIsChatActivityOpen = false;

    public static boolean isChatActivityOpen() {
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        FirebaseChatMainApp.sIsChatActivityOpen = isChatActivityOpen;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
