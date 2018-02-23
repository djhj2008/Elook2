package com.elook.client.service;

import com.elook.client.utils.Constant;

import java.util.ArrayList;

/**
 * Created by haiming on 3/3/16.
 */
public class SendPacket extends Packet {
    private static final String TAG = "SendPacket";
    public SendPacket(){}

    public void setCommonData(){
        mHead = (short)(((0xeb << 8) | 0x90) & 0xFFFF);
        mReserved = 0x00;
        mDataLength = (0x03 & 0xFFFFFFFF);
        mData = null;
    }

    public void initCheckServerStatePacket(){
        resetPacket();
        setCommonData();
        mCommand = (short)(Constant.CMD_APP_CHECK_STATE & 0xFFFF);
        mCheckSum = calculateCheckSum();
    }

    private byte[] getShareWiFiAndServerInfo(String ssid, String passwd){
        ArrayList<Byte> byteArray = new ArrayList<>();
        byte[] ssidBytes = ssid.getBytes();
        int ssidLength = ssidBytes.length;
        byteArray.add((byte)(ssidLength & 0xFF));
        for (int i =0; i < ssidLength; i++){
            byteArray.add((byte)(ssidBytes[i] &0xFF));
        }

        byte[] passwdBytes = passwd.getBytes();
        int passwdLength = passwdBytes.length;
        byteArray.add((byte)(passwdLength & 0xFF));
        for (int i =0; i < passwdLength; i++){
            byteArray.add((byte)(passwdBytes[i] &0xFF));
        }

        byte[] serverIpBytes = Constant.SERVER_IP.getBytes();
        int serverIpLength = serverIpBytes.length;
        byteArray.add((byte)(serverIpLength & 0xFF));
        for (int i = 0; i < serverIpLength; i++){
            byteArray.add((byte)(serverIpBytes[i] & 0xFF));
        }

        byte[] serverPortBytes = Constant.SERVER_PORT.getBytes();
        int serverPortLength = serverPortBytes.length;
        byteArray.add((byte)(serverPortLength & 0xFF));
        for (int i = 0; i < serverPortLength; i++){
            byteArray.add((byte)(serverPortBytes[i] & 0xFF));
        }

        byte[] retBytes = new byte[byteArray.size()];
        for (int i = 0; i < byteArray.size(); i++){
            retBytes[i] = (byte)(byteArray.get(i) & 0xFF);
        }
        return retBytes;
    }

    public void initWifiConfigPacket(String ssid, String passwd){
        resetPacket();
        mCommand = (short)(Constant.CMD_APP_WIFI_MESSAGE & 0xFFFF);
        mData = getShareWiFiAndServerInfo(ssid, passwd);
        mHead = (short)(((0xeb << 8) | 0x90) & 0xFFFF);
        mReserved = 0x00;
        mDataLength = (( 1 + mData.length + 2) & 0xFFFFFFFF);
        mCheckSum = calculateCheckSum();
    }
}
