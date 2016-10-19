package com.crazyhitty.chdev.ks.firebasechat.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.crazyhitty.chdev.ks.firebasechat.R;

/**
 * Utility class for network related queries.
 * <br>
 * Author Kartik Sharma
 * Created on: 8/7/2016 , 9:15 AM
 * Project: FinalProject
 */

public class NetworkConnectionUtil {
    public static final String ERR_DIALOG_TITLE = "No internet connection detected !";
    private static final String ERR_DIALOG_MSG = "Looks like our application is not able to detect an active internet connection, " +
            "please check your device's network settings.";
    private static final String ERR_DIALOG_POSITIVE_BTN = "Settings";
    private static final String ERR_DIALOG_NEGATIVE_BTN = "Dismiss";

    /**
     * Check if the device is connected to internet or not.
     *
     * @param context Current context of the application
     * @return <b>true</b> if device is connected to internet, otherwise <b>false</b>
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Check if the device is connected to internet via <i>wifi</i> or not.
     *
     * @param context Current context of the application
     * @return <b>true</b> if device is connected to internet via <i>wifi</i>, otherwise <b>false</b>
     */
    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null &&
                networkInfo.isConnectedOrConnecting() &&
                networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Check if the device is connected to internet via <i>mobile network</i> or not.
     *
     * @param context Current context of the application
     * @return <b>true</b> if device is connected to internet via <i>mobile network</i>, otherwise <b>false</b>
     */
    public static boolean isConnectedToMobileNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null &&
                networkInfo.isConnectedOrConnecting() &&
                networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * Show a error dialog representing that no internet connection is available currently.
     *
     * @param context Current context of the application
     */
    public static void showNoInternetAvailableErrorDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(ERR_DIALOG_TITLE)
                .setMessage(ERR_DIALOG_MSG)
                .setIcon(R.drawable.ic_error_24dp)
                .setPositiveButton(ERR_DIALOG_POSITIVE_BTN, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(ERR_DIALOG_NEGATIVE_BTN, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
