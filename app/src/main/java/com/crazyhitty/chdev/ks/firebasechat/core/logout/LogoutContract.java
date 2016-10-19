package com.crazyhitty.chdev.ks.firebasechat.core.logout;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 11:06 AM
 * Project: FirebaseChat
 */

public interface LogoutContract {
    interface View {
        void onLogoutSuccess(String message);

        void onLogoutFailure(String message);
    }

    interface Presenter {
        void logout();
    }

    interface Interactor {
        void performFirebaseLogout();
    }

    interface OnLogoutListener {
        void onSuccess(String message);

        void onFailure(String message);
    }
}
