package com.elook.client.service;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by haiming on 3/3/16.
 */

/*
* packet format
*
* +---------+------------+------------------+---------+----------+
*|  Heade  | DataLength | Command or State  |   Data  | Checksum |
*+---------+------------+------------------+---------+----------+
*|  2 byte |  4 byte    |       1 byte     |many byte|   2byte  |
*+---------+------------+------------------+---------+----------+
*Data length =  the length of command  +  the length of Reserved + real length of Data + length of Checksum.
*
*So, the length of the packet is DataLength  + 6(e.g : the length of head + the length of DataLength)
*
* */


public  abstract  class Packet {
    protected int  mPacketLength;
    protected short mHead;
    protected int mDataLength;
    protected byte mCommand;
    protected byte mReserved = 0x0;
    protected byte[] mData = null; // just include Data Segment
    protected short mCheckSum ;

    protected byte mHeadHighByte;
    protected byte mHeadLowByte;

    public Packet(){  }

    protected short calculateCheckSum(){
        short sum = 0;
        sum = (short)((sum + (mCommand & 0xFF)) & 0xFFFF);
        if(mData != null){
            for (int i = 0; i < mData.length; i++){
                sum = (short)((sum + ((mData[i]) & 0xFF)) & 0xFFFF);
            }
        }
        return sum;
    }

    public byte[] toByte(){
        ArrayList<Byte> allBytes = new ArrayList<>();
        allBytes.add((byte)(( mHead >> 8) & 0xFF));
        allBytes.add((byte)(( mHead     ) & 0xFF));
        allBytes.add((byte)((mDataLength >> 24) &0xFF));
        allBytes.add((byte)((mDataLength >> 16) &0xFF));
        allBytes.add((byte)((mDataLength >>  8) &0xFF));
        allBytes.add((byte)((mDataLength      ) &0xFF));

        allBytes.add((byte)((mCommand         ) &0xFF));

        if(mData != null && mData.length > 0){
            for (int i = 0; i < mData.length; i++)
                allBytes.add(mData[i]);
        }
        allBytes.add( (byte)((mCheckSum >> 8) & 0xFF));
        allBytes.add( (byte)((mCheckSum ) & 0xFF));

        byte[] temp = new byte[allBytes.size()];
        for (int i = 0; i < allBytes.size(); i++){
            temp[i] = (byte)(allBytes.get(i) & 0xFF) ;
        }
        return temp;
    }

    public void resetPacket(){
        mPacketLength = -1;
        mHead = -1;
        mDataLength = -1;
        mCommand = -1;
        mReserved = 0x0;
        mData = null; // just include Data Segment
        mCheckSum = -1 ;
        mHeadHighByte = 0x00;
        mHeadLowByte = 0x00;
    }

    public int getDataLength() {
        return mDataLength;
    }

    public byte getCommand() {
        return mCommand;
    }

    public byte[] getData() {
        return mData;
    }
}
