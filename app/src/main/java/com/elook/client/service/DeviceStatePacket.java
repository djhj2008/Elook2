package com.elook.client.service;

import android.graphics.Point;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by guoguo on 4/28/2016.
 */
public class DeviceStatePacket {
    private static final String TAG = "DeviceStatePacket";
    public static final int DEVSTATE_DEVID_NOT_EXISTED = 8;
    private static final String STATE_OK = "OK";

    private static final int MINS_IN_SECONDS = 60;
    private static final int HOUR_IN_SECONDS = 60 * 60;
    private static final int DAY_IN_SECONDS = 24 * HOUR_IN_SECONDS;

    String mRetStr;
    int mDevState;
    int mDevStateSubCount;
    long mTimestamp;
    long mUpdateDelayInSeconds;


    public DeviceStatePacket(String data) {
        parserData(data);
    }

    private void parserData(String data) {
        StringBuffer tempData = new StringBuffer();
        String temp = "";
        if(data.isEmpty()){
            this.mDevState = -1;
            Log.e(TAG, "Error Empty");
            return;
        }
        tempData.append(data.charAt(0));
        tempData.append(data.charAt(1));
        temp = tempData.toString();
        if (!temp.equals(STATE_OK)) {
            this.mDevState = -1;
            Log.e(TAG, "Error input data");
            return;
        }
        this.mRetStr = STATE_OK;

        char tempDevState = data.charAt(2);
        if (Character.isDigit(tempDevState)) {
            ;
            this.mDevState = Character.digit(tempDevState, 10);
        } else if (tempDevState == 'E') {
            this.mDevState = DEVSTATE_DEVID_NOT_EXISTED;
            Log.e(TAG, "Device Id is not existed!");
            return;
        }else if (tempDevState == 'A') {
            this.mDevState = 10;
        }else if (tempDevState == 'B') {
            this.mDevState = 11;
        }

        this.mDevStateSubCount = parserStateSubCount(data.substring(3, 7));
        Log.d(TAG,"mDevStateSubCount:"+mDevStateSubCount);
        this.mTimestamp = parserTimestamp(data.substring(3+4, 15+4));
        this.mUpdateDelayInSeconds = parseUpdateDelayInSeconds(data.substring(15+4, 21+4));
    }

    private int parseUpdateDelayInSeconds(String updateDelay) {
        int timeInSeconds;
        int days = Integer.parseInt(updateDelay.substring(0, 2));
        int hours = Integer.parseInt(updateDelay.substring(2, 4));
        int mins = Integer.parseInt(updateDelay.substring(4, 6));
        timeInSeconds = days * DAY_IN_SECONDS + hours * HOUR_IN_SECONDS + mins * MINS_IN_SECONDS;
        return timeInSeconds;
    }

    private int parserStateSubCount(String str) {
        int count = 0;
        Log.d(TAG,"parserStateSubCount str:"+str);
        count = Integer.parseInt(str);
        return count;
    }

    private long parserTimestamp(String time) {
        long timestamp = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmm");
        try {
            timestamp = (format.parse(time).getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public int getDevState() {
        return mDevState;
    }

    public int getDevStateSubCout() {
        return mDevStateSubCount;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public long getUpdateDelayInSeconds() {
        return mUpdateDelayInSeconds;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DeviceStatePacket:\t");
        sb.append("\tDeviceState: " + mDevState + "\n");
        sb.append("\tTimestamp: " + mTimestamp + "\n");
        sb.append("\tUpdateDelayInSeconds: " + mUpdateDelayInSeconds + "\n");
        return super.toString();
    }
}
