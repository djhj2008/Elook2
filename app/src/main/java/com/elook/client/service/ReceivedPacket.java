package com.elook.client.service;

import android.util.Log;

import com.elook.client.utils.Constant.DeviceRunningState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 3/3/16.
 */
public class ReceivedPacket extends Packet {
    public ReceivedPacket() {
    }


    public int initFirstPartPacktet(byte[] packet) {
        mHeadHighByte = (byte) (packet[0] & 0xFF);
        mHeadLowByte = (byte) (packet[1] & 0xFF);
        mHead = (short) ((packet[0] & 0xFF) << 8 | (packet[1] & 0xFF));
        mDataLength = ((packet[2] & 0xFF) << 24 |
                (packet[3] & 0xFF) << 16 |
                (packet[4] & 0xFF) << 8 |
                (packet[5] & 0xFF));
        return mDataLength;
    }

    public void initSecondPartPacket(byte[] packet) {
        mCommand = (byte) (packet[0] & 0xFF);
        mPacketLength = mDataLength + 6;

        /* because
        mDataLength include the length of command(on byte)  + real length of Data + length of Checksum(two byte).
        So, mDataLength - 3 is real length of Data
        */
        mData = new byte[mDataLength - 3];
        System.arraycopy(packet, 1, mData, 0, mDataLength - 3);

        mCheckSum = (short) ((packet[packet.length - 2] & 0xFF) << 8 |
                (packet[packet.length - 1] & 0xFF));
        processData();
    }

    public boolean isValidPacket() {
        short tempSum = calculateCheckSum();
        return tempSum == mCheckSum ? true : false;
    }

    private String mSSID = null;
    private String mPASSWD = null;

    private void processData() {
        if (mData.length > 0) {
            int ssidLength = mData[0] & 0xFF;
            byte[] ssidBytes = new byte[ssidLength];
            System.arraycopy(mData, 1, ssidBytes, 0, ssidLength);
            mSSID = new String(ssidBytes);

            int passWdLength = mData[ssidLength + 1] & 0xFF;
            byte[] passWD = new byte[passWdLength];
            System.arraycopy(mData, ssidLength + 1, passWD, 0, passWdLength);
            mPASSWD = new String(passWD);
        }
    }

    public String getSSIDFromPacket() {
        return mSSID;

    }

    public String getPasswordFromPacket() {
        return mPASSWD;
    }

}
