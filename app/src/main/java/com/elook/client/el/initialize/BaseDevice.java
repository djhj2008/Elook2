package com.elook.client.el.initialize;

/**
 * Created by xy on 6/21/16.
 */
public class BaseDevice {
    public static final String DEVICE_WATER = "1";
    public static final String DEVICE_ELEC = "2";
    public static final String DEVICE_WATER_WIFI = "1";
    public static final String DEVICE_WATER_GPRS = "2";
    public static final int DEVSTATE_ERROR = -1;
    public static final int DEVSTATE_PRE_INIT = 0;
    public static final int DEVSTATE_HAS_CONNECT_SERVER = 1;
    public static final int DEVSTATE_START_TO_CONFIG = 2;
    public static final int DEVSTATE_ERROR_NORMAL = 3;
    public static final int DEVSTATE_DIG_PARSE_FAIL = 4;
    public static final int DEVSTATE_CONFIG_PASS = 5;
    public static final int DEVSTATE_CONFIRM_FAIL = 6;
    public static final int DEVSTATE_ERROR_OFFLINE = 7;
    public static final int DEVSTATE_DEVID_NOT_EXISTED = 8;
    public static final int DEVSTATE_DEV_CONFIG_MIS = 9;
    public static final int DEVSTATE_DEV_LED_CORRECT = 10;

    public static final int DEVSTATE_AP_SCAN = 1;
    public static final int DEVSTATE_AP_SCAN_OK = 2;
    public static final int DEVSTATE_AP_SCAN_FAIL = 3;
    public static final int DEVSTATE_AP_CONNECT = 4;
    public static final int DEVSTATE_AP_CONNECT_OK = 5;
    public static final int DEVSTATE_AP_CONNECT_FAIL = 6;
    public static final int DEVSTATE_AP_CONFIRM_AP = 7;
    public static final int DEVSTATE_AP_COMUNICATE = 8;
    public static final int DEVSTATE_AP_COMUNICATE_OK = 9;
    public static final int DEVSTATE_AP_COMUNICATE_FAIL = 10;

}
