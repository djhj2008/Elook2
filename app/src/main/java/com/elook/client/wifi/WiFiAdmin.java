package com.elook.client.wifi;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;

import com.elook.client.R;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 4/1/16.
 */
public class WiFiAdmin {
    private static final String TAG = "WiFiAdmin";
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    private Context mContext;
    WifiManager mWiFiManager;

    List<OnCollectAPInfoFinished> mScanFinishedListeners = new ArrayList<>();
    List<ScanResult> mAllScanResults;
    List<AccessPointInfo> mAllApInfos;
    AccessPointInfo mCurrentApInfo; // connected the ap
    String mConnectedAPPasswd;

    private boolean hasConnectedWifi = false;

    public WiFiAdmin(Context context) {
        mContext = context;
        mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        initData();
    }

    private void initData() {
        mAllScanResults = new ArrayList<>();
        mAllApInfos = new ArrayList<>();
        mCurrentApInfo = new AccessPointInfo();
    }

    public void openWiFi() {
        int i = 0;
        if (!mWiFiManager.isWifiEnabled()) {
            do {
                i++;
                SystemClock.sleep(1000);
                mWiFiManager.setWifiEnabled(true);
            } while (!mWiFiManager.isWifiEnabled() && i <= 3); //check again if it is disable
        }
    }

    public void closeWiFi() {
        if (mWiFiManager.isWifiEnabled()) {
            mWiFiManager.setWifiEnabled(false);
        }
    }

    private void startScan() {
        openWiFi();
        mWiFiManager.startScan();
        synchronized (mAllScanResults){
            mAllScanResults = mWiFiManager.getScanResults();
        }
    }

    public List<ScanResult> getSacnResults() {
        return mWiFiManager.getScanResults();
    }

    public boolean connectToWiFi(ScanResult scanResult, String passwd) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        openWiFi();
        int networkId = -1;
        boolean isConnectedSuccessful = false;
        boolean configActive = false;
        AccessPointInfo connectedApInfo = hasWifiConnected();
        if (connectedApInfo == null || !connectedApInfo.getmacAdrress().equals(scanResult.BSSID)) {
            List<WifiConfiguration> configurations = mWiFiManager.getConfiguredNetworks();
            if (configurations != null && !configurations.isEmpty()) {
                for (final WifiConfiguration config : configurations) {
                    if (config.SSID.equals(scanResult.SSID)) {
                        networkId = config.networkId;
                        break;
                    }
                }
            }

            if (networkId < 0) {
                networkId = wifiManager.addNetwork(createWifiConfig(scanResult, passwd));
            }

            if (networkId == -1) {
                Log.d(TAG, "cannot get networkId");
                return false;
            }
            if (!wifiManager.saveConfiguration()) {
                Log.d(TAG, "Cannot save configuration");
                return false;
            }
            configActive = wifiManager.enableNetwork(networkId, true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

        } else {
            configActive = true;
        }

        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = null;
        int retry = 0;
        try {
            do {
                Log.d("doujun","connectToWiFi..."+isConnectedSuccessful);
                wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                isConnectedSuccessful = wifi.isConnected();
                Thread.sleep(1000);
                retry++;
            } while (!isConnectedSuccessful && retry <= 120);//timeout as 2 mins

        } catch (InterruptedException e) {
            isConnectedSuccessful = false;
            e.printStackTrace();
        }
        Log.d("doujun","connectToWiFi:"+isConnectedSuccessful);
        if (configActive && isConnectedSuccessful) {
            AccessPointInfo info = getApInfoByMAC(scanResult.BSSID);
            if(info!=null) {
                mCurrentApInfo.setconnectedflag(true);
                mCurrentApInfo.setaliveFlag(1);
                mCurrentApInfo.setName(info.getName());
                mCurrentApInfo.setIP(info.getIP());
                mCurrentApInfo.setmacAdrress(info.getmacAdrress());
                mCurrentApInfo.setsecurityType(info.getsecurityType());
                mCurrentApInfo.setimg(null);
                mCurrentApInfo.setconnectedflag(true);
            }else{
                mCurrentApInfo.setconnectedflag(false);
                mCurrentApInfo.setaliveFlag(0);
                mCurrentApInfo.setName("");
                mCurrentApInfo.setIP("");
                mCurrentApInfo.setmacAdrress("");
                mCurrentApInfo.setsecurityType(0);
                mCurrentApInfo.setimg(null);
            }
            mConnectedAPPasswd = passwd;
        } else {
            mCurrentApInfo.setconnectedflag(false);
            mCurrentApInfo.setaliveFlag(0);
            mCurrentApInfo.setName("");
            mCurrentApInfo.setIP("");
            mCurrentApInfo.setmacAdrress("");
            mCurrentApInfo.setsecurityType(0);
            mCurrentApInfo.setimg(null);
            mConnectedAPPasswd = "";
        }
        return configActive && isConnectedSuccessful;
    }

    public String getConnectedAPPasswd() {
        return mConnectedAPPasswd;
    }


    public boolean connectToAp(AccessPointInfo apInfo, String passwd) throws NetworkErrorException {
        ScanResult scanResult = getScanResultByMAC(apInfo.getmacAdrress());
        if (scanResult == null) {
            throw new NetworkErrorException();
        }
        boolean isConnectedSuccessful = connectToWiFi(scanResult, passwd);
        return isConnectedSuccessful;
    }

    public boolean connectToEasyLink(String elkSSID) throws NetworkErrorException {
        collectApInfos();
        ScanResult scanResult = getScanResultBySSID(elkSSID);
        if (scanResult == null) {
            throw new NetworkErrorException();
        }
        return connectToWiFi(scanResult, Constant.EASY_LINK_PASSWD);
    }

    private AccessPointInfo getApInfoByMAC(String macAddress) {
        AccessPointInfo apInfo = null;
        for (int i = 0; i < mAllApInfos.size(); i++) {
            apInfo = mAllApInfos.get(i);
            if (apInfo.getmacAdrress().equals(macAddress)) break;
            apInfo = null;
        }
        return apInfo;

    }

    private ScanResult getScanResultByMAC(String macAddress) {
        ScanResult scanResult = null;
        synchronized (mAllScanResults) {
            for (int i = 0; i < mAllScanResults.size(); i++) {
                scanResult = mAllScanResults.get(i);
                if (scanResult.BSSID.equals(macAddress)) break;
                scanResult = null;
            }
        }
        return scanResult;
    }

    private ScanResult getScanResultBySSID(String ssid) {
        ScanResult scanResult = null;
        synchronized (mAllScanResults) {
            for (int i = 0; i < mAllScanResults.size(); i++) {
                scanResult = mAllScanResults.get(i);
                if (scanResult.SSID.equals(ssid)) break;
                scanResult = null;
            }
        }
        return scanResult;
    }

    private WifiConfiguration createWifiConfig(ScanResult scanResult, String passwd) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        setupSecurity(wifiConfiguration, getScanResultSecurity(scanResult), passwd);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        wifiConfiguration.SSID = "\"" + scanResult.SSID + "\"";
        wifiConfiguration.priority = getMaxPriority(wifiManager) + 1;
        return wifiConfiguration;
    }

    private AccessPointInfo hasWifiConnected() {
        hasConnectedWifi = false;
        AccessPointInfo connectedApInfo = null;
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mConnectedWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        hasConnectedWifi = false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED
                || mConnectedWifiInfo.isConnected()) {
            connectedApInfo = new AccessPointInfo();
            connectedApInfo.setName(wifiInfo.getSSID());
            connectedApInfo.setmacAdrress(wifiInfo.getBSSID());
            connectedApInfo.setIP(Formatter.formatIpAddress(wifiInfo.getIpAddress()));
            hasConnectedWifi = true;
        }
        return connectedApInfo;
    }

    private boolean isScanResultCollected(ScanResult scanResult) {
        boolean isCollected = false;
        if (mAllApInfos.size() > 0) {
            for (int i = 0; i < mAllApInfos.size(); i++) {
                if (mAllApInfos.get(i).getaliveFlag() == 1) {
                    if (scanResult.SSID.equals(mAllApInfos.get(i).getName()))
                        isCollected = true;
                }
            }
        } else if (mAllApInfos.isEmpty()) {
            isCollected = false;
        }
        return isCollected;
    }

    public List<AccessPointInfo> getCollectedApInfos() {
        return mAllApInfos;
    }

    public List<AccessPointInfo> collectApInfos() {
        mAllApInfos.clear();
        startScan();
        synchronized (mAllScanResults) {
            if (mAllScanResults.size() > 0) {
                ScanResult scanResult = null;
                for (int iScan = 0; iScan < mAllScanResults.size(); iScan++) {
                    scanResult = mAllScanResults.get(iScan);
                    AccessPointInfo apInfo = new AccessPointInfo();

                    if (isScanResultCollected(scanResult)) {
                        continue;
                    } else {
                        apInfo.setsecurityType(getSecurity(scanResult));
                        apInfo.setaliveFlag(1);
                        apInfo.setName(scanResult.SSID);
                        apInfo.setmacAdrress(scanResult.BSSID);
                        apInfo.setconnectedflag(false);
                        apInfo.setIP("");

                        if (scanResult.level > -50)
                            apInfo.setimg(mContext.getResources().getDrawable(R.drawable.signal5));
                        else if (scanResult.level > -60)
                            apInfo.setimg(mContext.getResources().getDrawable(R.drawable.signal4));
                        else if (scanResult.level > -70)
                            apInfo.setimg(mContext.getResources().getDrawable(R.drawable.signal3));
                        else if (scanResult.level > -80)
                            apInfo.setimg(mContext.getResources().getDrawable(R.drawable.signal2));
                        else
                            apInfo.setimg(mContext.getResources().getDrawable(R.drawable.signal1));

                        AccessPointInfo connectedApInfo = null;
                        if ((connectedApInfo = hasWifiConnected()) != null)
                            if (connectedApInfo.getmacAdrress().equals(apInfo.getmacAdrress())) {
                                apInfo.setIP(connectedApInfo.getIP());
                                apInfo.setconnectedflag(true);
                                apInfo.setaliveFlag(1);
                                mCurrentApInfo = new AccessPointInfo(apInfo);
                            }
                    }
                    mAllApInfos.add(apInfo);
                }
            }
        }
        if (mScanFinishedListeners != null && !mScanFinishedListeners.isEmpty()) {
            synchronized (mScanFinishedListeners) {
                for (OnCollectAPInfoFinished listener : mScanFinishedListeners) {
                    listener.onCollectAPInfoFinished();
                }
            }
        }
        return mAllApInfos;
    }


    public void registeScanFinishedListener(OnCollectAPInfoFinished scanListener) {
        if (mScanFinishedListeners == null) {
            mScanFinishedListeners = new ArrayList<>();
        }
        synchronized (mScanFinishedListeners) {
            mScanFinishedListeners.add(scanListener);
        }
    }

    public void unregisteScanFinishedListener(OnCollectAPInfoFinished scanListener) {
        if (mScanFinishedListeners == null || mScanFinishedListeners.isEmpty()) {
            Log.d(TAG, "has no scanFinished listener");
            return;
        }
        synchronized (mScanFinishedListeners) {
            mScanFinishedListeners.remove(scanListener);
        }
    }

    public interface OnCollectAPInfoFinished {
        void onCollectAPInfoFinished();
    }

    private static int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        if (configurations != null) {
            for (final WifiConfiguration config : configurations) {
                if (config.priority > pri) {
                    pri = config.priority;
                }
            }
        }
        return pri;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    private static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }


    public String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {
        return String.valueOf(getSecurity(wifiConfig));
    }


    public String getScanResultSecurity(ScanResult scanResult) {
        return String.valueOf(getSecurity(scanResult));
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    public void setupSecurity(WifiConfiguration config, String security, String password) {
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        final int sec = security == null ? SECURITY_NONE : Integer.valueOf(security);
        final int passwordLen = password == null ? 0 : password.length();
        switch (sec) {
            case SECURITY_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case SECURITY_WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (passwordLen != 0) {
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((passwordLen == 10 || passwordLen == 26 || passwordLen == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case SECURITY_PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (passwordLen != 0) {
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case SECURITY_EAP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                break;
            default:
                Log.e(TAG, "Invalid security type: " + sec);
        }
    }

    public boolean isOpenNetwork(String security) {
        return String.valueOf(SECURITY_NONE).equals(security);
    }

    public String getDisplaySecirityString(final ScanResult scanResult) {
        final int security = getSecurity(scanResult);
        if (security == SECURITY_PSK) {
            switch (getPskType(scanResult)) {
                case WPA:
                    return "WPA";
                case WPA_WPA2:
                case WPA2:
                    return "WPA2";
                default:
                    return "?";
            }
        } else {
            switch (security) {
                case SECURITY_NONE:
                    return "OPEN";
                case SECURITY_WEP:
                    return "WEP";
                case SECURITY_EAP:
                    return "EAP";
            }
        }
        return "?";
    }

}
