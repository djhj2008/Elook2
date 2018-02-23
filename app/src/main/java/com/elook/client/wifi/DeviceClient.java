package com.elook.client.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.elook.client.service.ReceivedPacket;
import com.elook.client.service.SendPacket;
import com.elook.client.utils.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by haiming on 3/10/16.
 */
public class DeviceClient {
    private static final String TAG = "DeviceClient";

    Context mContext;
    Socket mSocket;
    boolean hasConnectedWifi = false;

    public DeviceClient(Context context){
        this.mContext = context;
    }

    private boolean hasWifiConnected(){
        hasConnectedWifi = false;
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mConnectedWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED
                || mConnectedWifiInfo.isConnected()) {
            hasConnectedWifi = true;
        }
        return hasConnectedWifi;
    }


    public String getEasyLinkLanIp(){
        String lanIp = null;
        WifiManager wifi_service = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
        int myIp =  wifiinfo.getIpAddress();
        int routerLanIp = dhcpInfo.gateway;
        int mask = dhcpInfo.netmask;

        if( (myIp & mask) != (routerLanIp & mask)){
            Log.d(TAG, "There is an error! Cannot in same lan");
        }

        lanIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.d(TAG, "getEasyLinkLanIp: "+lanIp);
        return  lanIp;
    }

    /*TODO if not connected to easy link, not allow to send command*/
    public ReceivedPacket sendCommand(SendPacket cmdPacket){
        if(cmdPacket == null) {
            Log.e(TAG, "This command is not correctly");
            return null;
        }
        Log.d(TAG, "sendCommand");
        InetAddress serverAddr = null;
        ReceivedPacket mRecPacket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            serverAddr  = InetAddress.getByName(getEasyLinkLanIp());
            mSocket = new Socket(serverAddr, Constant.EASY_LINK_PORT);
            out = mSocket.getOutputStream();

            out.write(cmdPacket.toByte());
            out.flush();

            in = mSocket.getInputStream();

            mRecPacket = new ReceivedPacket();
            int dataLength = -1;
            byte[] headBuffer = new byte[6];
            while ( in.available() < 6){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e){}
            }

            in.read(headBuffer, 0, 6);
            dataLength = mRecPacket.initFirstPartPacktet(headBuffer);
            Log.d(TAG, "dataLength = "+dataLength);
            byte[] packetBuffer;
            if( dataLength >= 3){
                packetBuffer = new byte[dataLength];
                in.read(packetBuffer, 0, dataLength);
                mRecPacket.initSecondPartPacket(packetBuffer);
            } else {
                Log.d(TAG, "Packet is not enough length");
            }
            if(!mRecPacket.isValidPacket()){
                Log.e(TAG, "checksum error");
            }
        } catch (UnknownHostException e){
            e.printStackTrace();

        } catch (IOException e){
            e.printStackTrace();

        } finally {
            try {
                if (out != null) out.close();
                if (in != null)  in.close();
                if (mSocket != null)mSocket.close();
            } catch (IOException e){

            }
        }
        return  mRecPacket;
    }

    private void receiveResult(){

    }
    
}
