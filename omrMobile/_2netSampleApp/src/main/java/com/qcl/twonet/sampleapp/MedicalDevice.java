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

/**
 * Represents an instance of a configured Medical Device
 */
public class MedicalDevice {
    public final static int CHANNEL_MODE_NORMAL = 0;
    public final static int CHANNEL_MODE_STICKY = 1;

    public long _id;
    String mMacAddress;
    String mSerialNumber;

    String mModel; // device model's name as provisioned on 2net Server
    String mType;
    long mLastReadingTime; // time in ms when last reading was received from the sensor
    long mLastSeenTime; // time in ms when sensor was seen (connected to) last time
    boolean mMaster;
    boolean mSlave;
    int mChannel;
    int mChannelMode;

    /**
     * Returns BT MAC address of the device
     *
     * @return
     */
    public String getMacAddress() {
        return mMacAddress;
    }


    /**
     * Returns serial number of the device
     * 
     * @return
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * Returns the last time when a successful reading was captured by VH for this device
     *
     * @return time in milliseconds
     */
    public long getLastReadingTime() {
        return mLastReadingTime;
    }

    /**
     * Returns the last time this device successfully connected to VH
     *
     * @return time in milliseconds
     */
    public long getLastSeenTime() {
        return mLastSeenTime;
    }

    /**
     * Returns true if the device is a BT "Master"
     *
     * @return true for master devices
     */
    public boolean isMaster() {
        return mMaster;
    }

    /**
     * Returns true if the device is a BT "Slave"
     *
     * @return true for slave devices
     */
    public boolean isSlave() {
        return mSlave;
    }

    /**
     * Returns BT channel number stored for this device. This attribute is only for "sticky" master devices.
     *
     * @return integer value between 1-30 or 0 when channel is not stored
     */
    public int getChannel() {
        return mChannel;
    }

    /**
     * Returns BT channel mode. Only applicable for master devices.
     *
     * @return {@link #CHANNEL_MODE_NORMAL} or {@link #CHANNEL_MODE_STICKY}
     */
    public int getChannelMode() {
        return mChannelMode;
    }

    /**
     * Returns device's model name as provisioned on 2net Server.
     * 
     * @return
     */
    public String getModel() {
        return mModel;
    }

    /**
     * Returns device's type.
     * 
     * @return
     */
    public String getType() {
        return mType;
    }
}

