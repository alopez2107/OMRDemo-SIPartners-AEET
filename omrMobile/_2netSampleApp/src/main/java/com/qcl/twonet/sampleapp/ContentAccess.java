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
import android.database.Cursor;
import android.net.Uri;

import com.qcl.vh.content.VirtualHubProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to access the 2net Mobile Core Content Provider
 */
public class ContentAccess {

    public final static String ATTR_ID = "_id";
    public final static String ATTR_MAC_ADDRESS = "address";
    public final static String ATTR_SERIAL_NUMBER = "serialNumber";
    public final static String ATTR_MODEL = "deviceModel";
    public final static String ATTR_TYPE = "deviceType";
    public final static String ATTR_LAST_READING_TIME = "lastReadingTime";
    public final static String ATTR_LAST_SEEN_TIME = "lastSeenTime";
    public final static String ATTR_IS_SLAVE = "isSlave";
    public final static String ATTR_IS_MASTER = "isMaster";
    public final static String ATTR_CHANNEL_NUMBER = "channelNumber";
    public final static String ATTR_CHANNEL_MODE = "channelMode";

    private Context mContext;

    final static String[] PROJECTION_DEVICE = { "devices." + ATTR_ID, ATTR_MAC_ADDRESS, ATTR_SERIAL_NUMBER,
            ATTR_MODEL, ATTR_TYPE, ATTR_LAST_READING_TIME, ATTR_LAST_SEEN_TIME, ATTR_IS_MASTER, ATTR_IS_SLAVE,
            ATTR_CHANNEL_MODE, ATTR_CHANNEL_NUMBER
    };

    public ContentAccess(Context context) {
        mContext = context;
    }

    /**
     * Finds and populates a MedicalDevice instance by its MAC Address
     * 
     * @param address Bluetooth MAC Address of a device to find
     * @return an instance of MedicalDevice or null if the device is not found
     */
    public MedicalDevice findByAddress(String address) {
        String selection = ATTR_MAC_ADDRESS + "=?";
        String[] selectionArgs = new String[] { address };

        return queryDevices(selection, selectionArgs);
    }

    private MedicalDevice queryDevices(String selection, String[] selectionArgs) {
        MedicalDevice md = null;
        final Uri URI_DEVICES = VirtualHubProvider.getDevicesUri(mContext);
        Cursor c = mContext.getContentResolver().query(URI_DEVICES, PROJECTION_DEVICE, selection, selectionArgs, null);
        if (c == null) {
            return md;
        }

        if (c.moveToFirst()) {
            md = fromCursor(c);
        }
        c.close();
        return md;
    }

    /**
     * Finds all Medical Devices configured in the 2net Mobile Core.
     * 
     * @return a Set of MedicalDevice instances (Empty Set when nothing is found)
     */
    public Set<MedicalDevice> findAll() {
        HashSet<MedicalDevice> mdSet = new HashSet<MedicalDevice>();
        final Uri URI_DEVICES = VirtualHubProvider.getDevicesUri(mContext);
        Cursor c = mContext.getContentResolver().query(URI_DEVICES, PROJECTION_DEVICE, null, null, null);
        if (c == null) {
            return mdSet;
        }
        if (c.moveToFirst()) {
            do {
                mdSet.add(fromCursor(c));
            } while (c.moveToNext());
        }
        c.close();
        return mdSet;
    }

    /**
     * Populates an instance of MedicalDevice with attributes supplied by a Cursor
     */
    static MedicalDevice fromCursor(Cursor c) {
        MedicalDevice md = new MedicalDevice();
        md._id = c.getLong(c.getColumnIndex(ATTR_ID));
        md.mMacAddress = c.getString(c.getColumnIndex(ATTR_MAC_ADDRESS));
        md.mSerialNumber = c.getString(c.getColumnIndex(ATTR_SERIAL_NUMBER));
        md.mModel = c.getString(c.getColumnIndex(ATTR_MODEL));
        md.mType = c.getString(c.getColumnIndex(ATTR_TYPE));
        md.mLastReadingTime = c.getLong(c.getColumnIndex(ATTR_LAST_READING_TIME));
        md.mLastSeenTime = c.getLong(c.getColumnIndex(ATTR_LAST_SEEN_TIME));
        md.mChannel = c.getInt(c.getColumnIndex(ATTR_CHANNEL_NUMBER));
        md.mChannelMode = c.getInt(c.getColumnIndex(ATTR_CHANNEL_MODE));
        md.mMaster = c.getInt(c.getColumnIndex(ATTR_IS_MASTER)) > 0 ? true : false;
        md.mSlave = c.getInt(c.getColumnIndex(ATTR_IS_SLAVE)) > 0 ? true : false;
        md.mLastReadingTime = c.getLong(c.getColumnIndex(ATTR_LAST_READING_TIME));
        md.mLastSeenTime = c.getLong(c.getColumnIndex(ATTR_LAST_SEEN_TIME));
        return md;
    }
}
