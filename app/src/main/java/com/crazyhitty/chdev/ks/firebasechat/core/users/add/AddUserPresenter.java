package com.crazyhitty.chdev.ks.firebasechat.core.users.add;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;

/**
 * Author: Kartik Sharma
 * Created on: 9/2/2016 , 10:05 PM
 * Project: FirebaseChat
 */

public class AddUserPresenter implements AddUserContract.Presenter, AddUserContract.OnUserDatabaseListener {
    private AddUserContract.View mView;
    private AddUserInteractor mAddUserInteractor;

    public AddUserPresenter(AddUserContract.View view) {
        this.mView = view;
        mAddUserInteractor = new AddUserInteractor(this);
    }

    @Override
    public void addUser(Context context, FirebaseUser firebaseUser) {
        mAddUserInteractor.addUserToDatabase(context, firebaseUser);
    }

    @Override
    public void onSuccess(String message) {
        mView.onAddUserSuccess(message);
    }

    @Override
    public void onFailure(String message) {
        mView.onAddUserFailure(message);
    }
}
