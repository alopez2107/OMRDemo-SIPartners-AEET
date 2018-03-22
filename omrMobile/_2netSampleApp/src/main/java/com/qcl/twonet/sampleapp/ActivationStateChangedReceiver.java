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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.qcl.twonet.sampleapp.R;
import com.qcl.vh.content.BroadcastNotifier;

public class ActivationStateChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BroadcastNotifier.ACTION_ACTIVATION_STATE_CHANGED.equals(action)) {
            boolean isActive = intent.getBooleanExtra(BroadcastNotifier.EXTRA_IS_ACTIVE, false);
            boolean wasActive = Utility.isHubActive(context);

            if (isActive != wasActive) {
                createNotification(context, isActive);
            }

            Utility.setHubActive(context, isActive);
        }
    }

    void createNotification(Context context, boolean isActive) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ParentAppActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.activation_notification_title))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(context.getString(
                        isActive ? R.string.activation_notification_message_active :
                                R.string.activation_notification_message_inactive))
                .setLights(0xff00ff00, 500, 500); // Green LED with 500 ms ON / 500 ms OFF pattern

        // Display notification
        notificationManager.notify(1, mBuilder.build());
    }
}
