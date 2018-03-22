/**
 * Copyright (C) 2013-2014 Qualcomm Life, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Qualcomm
 * Life, Inc.
 *
 * The following sample code illustrates various aspects of the 2net Mobile SDK.
 *
 * The sample code herein is provided for your convenience, and has not been
 * tested or designed to work on any particular system configuration. It is
 * provided AS IS and your use of this sample code, whether as provided or with
 * any modification, is at your own risk. Neither Qualcomm Life, Inc. nor any
 * affiliate takes any liability nor responsibility with respect to the sample
 * code, and disclaims all warranties, express and implied, including without
 * limitation warranties on merchantability, fitness for a specified purpose,
 * and against infringement.
 */

package com.qcl.twonet.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.qcl.vh.service.NetworkService;

/**
 * Utility methods used by the Parent App
 */
public class Utility {

    public final static String TAG = "SampleAppUtility";
    public final static String KEY_CERTIFICATE_EXPIRED = "cert_expired";
    public final static String KEY_HUB_ACTIVE = "hub_active";

    static boolean sVhStarted = false;

    /**
     * Creates a unique key to be used with {@link SharedPreferences}
     *
     * @param prefix  Static prefix for this key
     * @param address Unique address such as BT MAC address
     * @return key String
     */
    public static String keyForAddress(String prefix, String address) {
        String suffix = address.replace(':', '_');
        return prefix + "_" + suffix;
    }

    /**
     * Instructs the 2net Mobile Core to start its services if they are not already running.
     * <p/>
     * <p/>
     * <p/>
     * This should be called upon Parent Application start to ensure the framework is operational.
     *
     * @param context an instance of {@link Context}
     */
    public static void startVhFramework(Context context) {
        if (!sVhStarted) {
            sVhStarted = true;
            Intent intent = new Intent("com.qcl.vh.action.START_SERVICES");
            context.sendBroadcast(intent);
        }
    }


    /**
     * Instructs the 2net Mobile Core to Initiates network connection to the 2net server
     * <p/>
     * <p/>
     * <p/>
     * This should be called when 2net Mobile core need to sync with the 2net server.
     *
     * @param context an instance of {@link Context}
     */
    public static void callHome(Context context) {
        Intent intent = new Intent("com.qcl.vh.action.CALL_HOME");
        intent.setClass(context, NetworkService.class);
        context.startService(intent);

    }

    /**
     * Initializes and authenticates 2net Mobile Core with the 2net Platform.
     * <p/>
     * <p/>
     * <p/>
     * During authentication, security credentials are exchanged between the 2net Mobile Core and the 2net Platform. These credentials are
     * <p/>
     * used for securing network communication path and stored device data.
     * <p/>
     * <p/>
     * <p/>
     * This should be called only once during Parent Application's initial start up when the 2net Mobile Core's Id is not
     * <p/>
     * available.
     *
     * @param context  an instance of {@link Context}
     * @param authCode
     * @see getVirtualHubId(Context context)
     */
    public static void authenticate(Context context, String appId, String authCode) {
        Log.d(TAG, "    auth call:" );
        Intent intent = new Intent(NetworkService.ACTION_AUTHENTICATE);
        intent.setClass(context, NetworkService.class);
        intent.putExtra(NetworkService.EXTRA_APP_ID, appId);
        intent.putExtra(NetworkService.EXTRA_AUTH_CODE, authCode);
        context.startService(intent);

    }

    /**
     * Returns 2net Mobile Core unique Id in the 2net system.
     * <p/>
     * <p/>
     * <p/>
     * This should be called on Parent Application's launch. If it is not available, Parent Application must
     * <p/>
     * authenticate the 2net Mobile Core with the 2net Platform using {@link authenticate} API.
     *
     * @param context an instance of {@link Context}
     * @return 2net Mobile Core's Id or null.
     * @see authenticate()
     */
    public static String getVirtualHubId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("VhId", null);
    }


    public static boolean hasUserAcceptedEula(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("acceptedEula", false);
    }


    public static void setUserAcceptedEula(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("acceptedEula", true).commit();
    }

    /**
     * When the application hasn't been used for a while and the SSL certificate could not be automatically renewed,
     * this should be called to let the app know that the user has to re-authenticate.
     *
     * @param context
     */
    public static void setCertExpired(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_CERTIFICATE_EXPIRED, value)
                .commit();
    }

    /**
     * Checks if the SSL certificate used to communicate with SP is expired and the app needs to re-authenticate with
     * the server.
     *
     * @param context
     */
    public static boolean isCertExpired(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_CERTIFICATE_EXPIRED, false);
    }

    /**
     * When the 2net Mobile Core has been deactivated, this should be called to let the app know that application and
     * devices can not be used.
     *
     * @param context
     */
    public static void setHubActive(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_HUB_ACTIVE, value)
                .commit();
    }

    /**
     * Check if the 2net Mobile Core has been deactivated or not.
     *
     * @param context
     * @return
     */
    public static boolean isHubActive(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_HUB_ACTIVE, false);
    }
}
