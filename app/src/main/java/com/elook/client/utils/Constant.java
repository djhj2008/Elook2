package com.elook.client.utils;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by xy on 16-2-2.
 */
public class Constant {
    public static final boolean DEBUG = true;
    public static final String AIP = "an";
    public static final String URL_BASE = "124.207.250.67:16000";
    public static final String URL_HEADER = "http://"+URL_BASE;
    public static final String URL_PREFIX = URL_HEADER+"/index.php/Admin";
    public static final String URL_REGISTE 		        = URL_PREFIX + "/Manager/reg";
    public static final String URL_MSG_VERIFY_CODE      = URL_PREFIX + "/Manager/sendphonemsg";
    public static final String URL_REG_EXTERN_INFO      = URL_PREFIX + "/Manager/addenduserinfo";
    public static final String URL_LOGIN 		        = URL_PREFIX + "/manager/login";
    public static final String URL_ADD_DEVICE 	        = URL_PREFIX + "/deviceadd/add";
    public static final String URL_DEL_DEVICE 	        = URL_PREFIX + "/devupdown/updown";
    public static final String URL_CHECKIN_DEV          = URL_PREFIX + "/deviceadd/checkin";//query the devices of some user
    public static final String URL_CHECK_DEVICE 	    = URL_PREFIX + "/deviceadd/checkdev";//query measurement records
    public static final String URL_FETCH_RECORD         = URL_PREFIX + "/deviceadd/devtimeselect";
    public static final String URL_FETCH_RECORD_VIEW    = URL_PREFIX + "/reportmsg/reportmsg";
    public static final String URL_DATE_FLOW            = URL_PREFIX + "/deviceadd/dateflow";
    public static final String URL_GET_DEV_INFO         = URL_PREFIX + "/deviceupd/deviceselect";
    public static final String URL_SET_NOT_USE_DAY      = URL_PREFIX + "/deviceadd/setaccess";
    public static final String URL_SET_ENGNEER_DELAY    = URL_PREFIX + "/deviceadd/setdelaysub";
    public static final String URL_DEV_SET_CONFIG       = URL_PREFIX + "/deviceadd/devmsg";
    public static final String URL_FETCH_PUSHMSG        = URL_PREFIX + "/pushmsg/pushselect";
    public static final String URL_FETCH_SIGLE_MSG      = URL_PREFIX + "/pushmsg/pushidfind";
    public static final String URL_ADVERTIS             = URL_PREFIX + "/advertis/advertis";
    public static final String URL_CHECK_DEV_STATE      = URL_PREFIX + "/deviceupd/GetDeviceStateAOS";
    public static final String URL_SET_DEV_STATE        = URL_PREFIX + "/deviceupd/SetDeviceState";
    public static final String URL_FETCH_PROBLEMMSG     = URL_PREFIX + "/problem/select";
    public static final String URL_FETCH_FIND_PWD       = URL_PREFIX + "/manager/backpwd";

    public static final String URL_UPDATE 		    = URL_PREFIX + "/Manager/eupdinfo";
    public static final String URL_CHANGEPASSWD 	= URL_PREFIX + "/Manager/eresetp";
    public static final String URL_SEND_BMP         = URL_PREFIX + "/Deviceupd/SendBmp";
    public static final String URL_SET_DELAY        = URL_PREFIX + "/addev/saveupdelay";
    public static final String URL_SET_LOCATION     = URL_PREFIX + "/Location/Location";
    public static final String URL_CHANGE_PWD_PHONEMSG  = URL_PREFIX + "/manager/phonemsgpwd";



    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String EASY_LINK_PASSWD = "12345678";
    public static final int EASY_LINK_PORT = 8008;
    public static final String SERVER_IP = "124.207.250.67";
    public static final String SERVER_PORT = "16000";

    public static final int CMD_APP_CHECK_STATE = 0x01;
    public static final int CMD_APP_WIFI_MESSAGE = 0x02;
    /*Command END*/


    public enum  DeviceRunningState {
        PREPARE_PHOTO_STATE             (0x00, "PREPARE_PHOTO_STATE"),
        SEND_PHOTO_STATE                (0x11, "SEND_PHOTO_STATE"),
        PARAMETER_REV_STATE             (0x22, "PARAMETER_REV_STATE"),
        VERIFY_STATE_STATE              (0x33, "VERIFY_STATE_STATE"),
        URL_CONFIG_ERROR_STATE          (0x44, "URL_CONFIG_ERROR_STATE"),
        WIFI_CONFIG_ERROR_STATE         (0x55, "WIFI_CONFIG_ERROR_STATE"),
        CONFIG_FINISH_STATE             (0x66, "CONFIG_FINISH_STATE"),
        RUN_STATE                       (0x77, "RUN_STATE"),
        WIFI_FAULT_ERROR_STATE          (0x88, "WIFI_FAULT_ERROR_STATE"),
        URL_FAULT_ERROR_STATE           (0x99, "URL_FAULT_ERROR_STATE"),
        DEVICE_PROCESSED_CMD_STATE      (0xff, "DEVICE_PROCESSED_CMD_STATE"),
        UNKNOWN                         (-1, "UNKNOWN");


        private int mStateValue = -1;
        private String mStateName = "UNKNOWN";

        DeviceRunningState(int state, String stateName){
            this.mStateName = stateName;
            this.mStateValue = state;
        }

        public static DeviceRunningState valueOf(int value){
            switch (value){
                case 0x00: return PREPARE_PHOTO_STATE;
                case 0x11: return SEND_PHOTO_STATE;
                case 0x22: return PARAMETER_REV_STATE;
                case 0x33: return VERIFY_STATE_STATE;
                case 0x44: return URL_CONFIG_ERROR_STATE;
                case 0x55: return WIFI_CONFIG_ERROR_STATE;
                case 0x66: return CONFIG_FINISH_STATE;
                case 0x77: return RUN_STATE;
                case 0x88: return WIFI_FAULT_ERROR_STATE;
                case 0x99: return URL_FAULT_ERROR_STATE;
                case 0xff: return DEVICE_PROCESSED_CMD_STATE;
            }
            return UNKNOWN;
        }
    }
    protected static final String LOGIN_FAILED_MESSAGE = "User name or password wrong! This account or lock!";

    public static class ErrorCode {
        public static final String LOGIN_SUCCESSFULLY_MESSAGE = "success";
        public static final String LOGIN_FAILED_MESSAGE = "User name or password wrong! This account or lock!";

        public static final String REGISTE_SUCCESSFULLY_MESSAGE = "Registration success! Please login";
        public static final String REGISTE_FAILED_MESSAGE = "Registration failure!";
        public static final String REGISTE_WRONG_PHONENUMBER_MESSAGE = "phone number format is not correct";
        public static final String REGISTE_WRONG_SALES_MESSAGE = "Sales staff can not be empty";
        public static final String REGISTE_WRONG_NAME_EXISTED_MESSAGE = "User name is already in use, please fill in again";

        public static final String UPDATE_SUCCESSFULLY_MESSAGE = "success";
        public static final String UPDATE_FAILED_MESSAGE = "update failed";
        public static final String UPDATE_FAILED_KEY_ERROR_MESSAGE = "key error";

        public static final String CHANGEPASSWD_SUCCESSFULLY_MESSAGE = "Registration success! Please login";
        public static final String CHANGEPASSWD_FAILED_PARAMETER_ERROR_MESSAGE = "update failed0!";
        public static final String CHANGEPASSWD_FAILED_MESSAGE = "update failed!";


        public static final String ADD_DEVDATA_SUCCESSFULLY_MESSAGE = "addAdd successful";
        public static final String ADD_DEVDATA_FAILED_MESSAGE = "add information failed";
        public static final String ADD_DEVDATA_WRONG_DEVVAL_MESSAGE = "content must be numeric";
        public static final String ADD_DEVDATA_DEVID_EMPTY_MESSAGE = "User cannot be empty!";
        public static final String ADD_DEVDATA_DEVID_NOT_EXISTED__MESSAGE = "User not exist";

        public static final String ADD_DEVICE_SUCCESSFULLY_MESSAGE = "Insert the data successfully";
        public static final String ADD_DEVICE_FAILED_MESSAGE = "Insert the data failure";
        public static final String ADD_DEVICE_USER_NOT_EXISTED_MESSAGE = "User not exist";
        public static final String ADD_DEVICE_DEVID_REPEATED_MESSAGE = "The instrument ID cannot be repeated";
        public static final String ADD_DEVICE_DEVID_WRONG_FORMAT_MESSAGE = "Names can only be 9 digits";
        public static final String ADD_DEVICE_DELAY_NOT_ALLOW_EMPTY_MESSAGE = "delay is not null";


        public static final String CHECK_DEVICE_SUCCESSFULLY_MESSAGE = "select ok!";
        public static final String CHECK_DEVICE_EMPTY_MESSAGE = "empty";
        public static final String CHECK_DEVICE_FAILED_MESSAGE = "The query fails";

        public static final String CHECK_DEVID_SUCCESSFULLY_MESSAGE = "success";
        public static final String CHECK_DEVID_FAILED_MESSAGE = "Insert the data failure";
        public static final String CHECK_DEVID_FAILED_NO_ID_MESSAGE = "The instrument ID no";

        public static final String GET_DEVICE_INFO_SUCCESSFULLY_MESSAGE = "OK";

        public static final String SET_DATE_FLOW_SUCCESSFULLY_MESSAGE = "ok";
        public static final String SET_DATE_FLOW_FAILED_MESSAGE = "Setting failed";

        public static final String AGENT_LOGIN_SUCCESSFULLY_MESSAGE = "success";
        public static final String AGENT_LOGIN_FAILED_MESSAGE = "User name or password wrong! This account or lock!";

        public static final String AGENT_REG_SUCCESSFULLY_MESSAGE = "Registration success! Please login";
        public static final String AGENT_REG_FAILED_MESSAGE = "Registration failure!";
        public static final String AGENT_REG_FAILED_ERROR_PHONE_MESSAGE = "phone number format is not correct";
        public static final String AGENT_REG_FAILED_ERROR_EMAIL_MESSAGE = "Please fill in the correct format";
        public static final String AGENT_REG_FAILED_NAME_REPEATED_MESSAGE = "User name is already in use, please fill in again";

        public static final String SALES_LOGIN_SUCCESSFULLY_MESSAGE = "success";
        public static final String SALES_LOGIN_FAILED_MESSAGE = "User name or password wrong! This account or lock!";

        public static final String SALES_REG_SUCCESSFULLY_MESSAGE = "Registration success! Please login";
        public static final String SALES_REG_FAILED_MESSAGE = "Registration failure!";
        public static final String SALES_REG_FAILED_ERROR_PHONE_MESSAGE = "phone number format is not correct";
        public static final String SALES_REG_FAILED_ERROR_EMAIL_MESSAGE = "Please fill in the correct format";
        public static final String SALES_REG_FAILED_NAME_REPEATED_MESSAGE = "User name is already in use, please fill in again";

        public static final String SAVE_UPDELAY_SUCCESSFULLY_MESSAGE = "ok";

        public static final String FETCH_PUSHMSG_SUCCESSFULLY_MESSAGE = "OK!";
        public static final String FETCH_PUSHMSG_FAILED_MESSAGE = "error or page is not";

        public static final String LOGOUT_MESSAGE = "login out";

        public static final int ERROR_CODE_BASE = 0;

//        public static final int LOGIN_CODE_BASE     = ERROR_CODE_BASE + 100;
//        public static final int LOGIN_SUCCESS_CODE  = LOGIN_CODE_BASE + 1;
//        public static final int LOGIN_FAILED_CODE   = LOGIN_CODE_BASE + 2;

        public static final int REGISTE_CODE_BASE                           = ERROR_CODE_BASE + 200;
        public static final int REGISTE_SUCCESSFULLY_CODE                   = REGISTE_CODE_BASE + 1;
        public static final int REGISTE_FAILED_CODE                         = REGISTE_CODE_BASE + 2;
        public static final int REGISTE_FAILED_NAME_EXISTED_CODE            = REGISTE_CODE_BASE + 3;
        public static final int REGISTE_FAILED_SALES_EMPTY_CODE             = REGISTE_CODE_BASE + 4;
        public static final int REGISTE_FAILED_PHONENUMBER_NOT_CORRECT_CODE = REGISTE_CODE_BASE + 5;

        public static final int UPDATE_CODE_BASE         = ERROR_CODE_BASE + 300;
        public static final int UPDATE_SUCCESSFULLY_CODE = UPDATE_CODE_BASE + 1;
        public static final int UPDATE_FAILED_CODE       = UPDATE_CODE_BASE + 2;
        public static final int UPDATE_FAILED_KEY_ERROR_CODE = UPDATE_CODE_BASE + 3;

        public static final int CHANGEPASSWD_CODE_BASE           = ERROR_CODE_BASE + 400;
        public static final int CHANGEPASSWD_SUCCESSFULLY_CODE   = CHANGEPASSWD_CODE_BASE + 1;
        public static final int CHANGEPASSWD_FAILED_CODE         = CHANGEPASSWD_CODE_BASE + 2;
        public static final int CHANGEPASSWD_FAILED_PARAMETER_ERROR_CODE = CHANGEPASSWD_CODE_BASE + 3;


        public static final int ADD_DEVDATA_CODE_BASE                = ERROR_CODE_BASE + 500;
        public static final int ADD_DEVDATA_SUCCESSFULLY_CODE        = ADD_DEVDATA_CODE_BASE + 1;
        public static final int ADD_DEVDATA_FAILED_CODE              = ADD_DEVDATA_CODE_BASE + 2;
        public static final int ADD_DEVDATA_WRONG_DEVVAL_CODE        = ADD_DEVDATA_CODE_BASE + 3;
        public static final int ADD_DEVDATA_DEVID_EMPTY_CODE         = ADD_DEVDATA_CODE_BASE + 4;
        public static final int ADD_DEVDATA_DEVID_NOT_EXISTED_CODE   = ADD_DEVDATA_CODE_BASE + 5;

        public static final int ADD_DEVICE_CODE_BASE             = ERROR_CODE_BASE + 600;
        public static final int ADD_DEVICE_SUCCESSFULLY_CODE     = ADD_DEVICE_CODE_BASE + 1;
        public static final int ADD_DEVICE_FAILED_CODE           = ADD_DEVICE_CODE_BASE + 2;
        public static final int ADD_DEVICE_USER_NOT_EXISTED_CODE = ADD_DEVICE_CODE_BASE + 3;
        public static final int ADD_DEVICE_DEVID_REPEATED_CODE   = ADD_DEVICE_CODE_BASE + 4;
        public static final int ADD_DEVICE_DEVID_WRONG_FORMAT_CODE = ADD_DEVICE_CODE_BASE + 5;
        public static final int ADD_DEVICE_DELAY_NOT_ALLOW_EMPTY_CODE = ADD_DEVICE_CODE_BASE + 6;

        public static final int CHECK_DEVICE_CODE_BASE           = ERROR_CODE_BASE + 700;
        public static final int CHECK_DEVICE_SUCCESSFULLY_CODE   = CHECK_DEVICE_CODE_BASE + 1;
        public static final int CHECK_DEVICE_FAILED_CODE         = CHECK_DEVICE_CODE_BASE + 2;

        public static final int CHECK_DEVID_CODE_BASE            = ERROR_CODE_BASE + 800;
        public static final int CHECK_DEVID_SUCCESSFULLY_CODE    = CHECK_DEVID_CODE_BASE + 1;
        public static final int CHECK_DEVID_FAILED_CODE          = CHECK_DEVID_CODE_BASE + 2;
        public static final int CHECK_DEVID_FAILED_NO_ID_CODE    = CHECK_DEVID_CODE_BASE + 3;
        public static final int CHECK_DEVICE_EMPTY_CODE          = CHECK_DEVID_CODE_BASE + 4;

        public static final int AGENT_LOGIN_CODE_BASE            = ERROR_CODE_BASE + 900;
        public static final int AGENT_LOGIN_SUCCESSFULLY_CODE    = AGENT_LOGIN_CODE_BASE + 1;
        public static final int AGENT_LOGIN_FAILED_CODE          = AGENT_LOGIN_CODE_BASE + 2;

        public static final int AGENT_REG_CODE_BASE                  = ERROR_CODE_BASE + 1000;
        public static final int AGENT_REG_SUCCESSFULLY_CODE          = AGENT_REG_CODE_BASE + 1;
        public static final int AGENT_REG_FAILED_CODE                = AGENT_REG_CODE_BASE + 2;
        public static final int AGENT_REG_FAILED_ERROR_PHONE_CODE    = AGENT_REG_CODE_BASE + 3;
        public static final int AGENT_REG_FAILED_ERROR_EMAIL_CODE    = AGENT_REG_CODE_BASE + 4;
        public static final int AGENT_REG_FAILED_NAME_REPEATED_CODE  = AGENT_REG_CODE_BASE + 5;


        public static final int SALES_REG_CODE_BASE                  = ERROR_CODE_BASE + 1100;
        public static final int SALES_REG_SUCCESSFULLY_CODE          = SALES_REG_CODE_BASE + 1;
        public static final int SALES_REG_FAILED_CODE                = SALES_REG_CODE_BASE + 2;
        public static final int SALES_REG_FAILED_ERROR_PHONE_CODE    = SALES_REG_CODE_BASE + 3;
        public static final int SALES_REG_FAILED_ERROR_EMAIL_CODE    = SALES_REG_CODE_BASE + 4;
        public static final int SALES_REG_FAILED_NAME_REPEATED_CODE  = SALES_REG_CODE_BASE + 5;

        public static final int SALES_LOGIN_CODE_BASE            = ERROR_CODE_BASE + 1200;
        public static final int SALES_LOGIN_SUCCESSFULLY_CODE    = SALES_LOGIN_CODE_BASE + 1;
        public static final int SALES_LOGIN_FAILED_CODE          = SALES_LOGIN_CODE_BASE + 2;

        public static final int LOGOUT_CODE_BASE         = ERROR_CODE_BASE + 1300;
        public static final int LOGOUT_SUCCESSFULLY_CODE = LOGOUT_CODE_BASE + 1;
        public static final int LOGOUT_FAILED_CODE       = LOGOUT_CODE_BASE + 2;

        public static final int SET_DATE_FLOW_BASE = ERROR_CODE_BASE + 1400;
        public static final int SET_DATE_FLOW_SUCCESSFULLY_CODE = SET_DATE_FLOW_BASE + 1 ;
        public static final int SET_DATE_FLOW_FAILED_CODE = SET_DATE_FLOW_BASE + 2;

        public static final int SAVE_UPDELAY_BASE = ERROR_CODE_BASE + 1500;
        public static final int SAVE_UPDELAY_SUCCESSFULLY_CODE = SAVE_UPDELAY_BASE + 1;

        public static final int FETCH_PUSHMSG_BASE = ERROR_CODE_BASE + 1600;
        public static final int FETCH_PUSHMSG_SUCCESSFULLY_CODE = FETCH_PUSHMSG_BASE + 1;
        public static final int FETCH_PUSHMSG_FAILED_CODE = FETCH_PUSHMSG_BASE + 2;

        public static final int GET_DEVICE_INFO_BASE_CODE = ERROR_CODE_BASE + 1700;
        public static final int GET_DEVICE_INFO_SUCCESSFULLY_CODE =  GET_DEVICE_INFO_BASE_CODE + 1;
        public static final int GET_DEVICE_INFO_FAILED_CODE = GET_DEVICE_INFO_BASE_CODE + 2;

        public static final int SAVE_NOTUSE_DAY_BASE = ERROR_CODE_BASE + 1800;
        public static final int SAVE_NOTUSE_DAY_SUCCESSFULLY_CODE = SAVE_NOTUSE_DAY_BASE + 1;
        public static final int SAVE_NOTUSE_DAY_FAILED_CODE = SAVE_NOTUSE_DAY_BASE + 2;

        public static final int DEV_SET_CONFIG_BASE = ERROR_CODE_BASE + 1900;
        public static final int DEV_SET_CONFIG_SUCCESSFULLY_CODE = DEV_SET_CONFIG_BASE + 1;
        public static final int DEV_SET_CONFIG_FAILED_CODE = DEV_SET_CONFIG_BASE + 2;

        static {
            Map<Integer, String> mErrorCodeMap = new Hashtable<>();
//            mErrorCodeMap.put(LOGIN_SUCCESS_CODE, LOGIN_SUCCESSFULLY_MESSAGE);
//            mErrorCodeMap.put(LOGIN_FAILED_CODE, LOGIN_FAILED_MESSAGE);

            mErrorCodeMap.put(REGISTE_SUCCESSFULLY_CODE, REGISTE_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(REGISTE_FAILED_CODE, REGISTE_FAILED_MESSAGE);
            mErrorCodeMap.put(REGISTE_FAILED_PHONENUMBER_NOT_CORRECT_CODE, REGISTE_WRONG_PHONENUMBER_MESSAGE);
            mErrorCodeMap.put(REGISTE_FAILED_SALES_EMPTY_CODE, REGISTE_WRONG_SALES_MESSAGE);
            mErrorCodeMap.put(REGISTE_FAILED_NAME_EXISTED_CODE, REGISTE_WRONG_NAME_EXISTED_MESSAGE);

            mErrorCodeMap.put(UPDATE_SUCCESSFULLY_CODE, UPDATE_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(UPDATE_FAILED_CODE, UPDATE_FAILED_MESSAGE);
            mErrorCodeMap.put(UPDATE_FAILED_KEY_ERROR_CODE, UPDATE_FAILED_KEY_ERROR_MESSAGE);

            mErrorCodeMap.put(CHANGEPASSWD_SUCCESSFULLY_CODE, CHANGEPASSWD_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(CHANGEPASSWD_FAILED_CODE, CHANGEPASSWD_FAILED_MESSAGE);
            mErrorCodeMap.put(CHANGEPASSWD_FAILED_PARAMETER_ERROR_CODE, CHANGEPASSWD_FAILED_PARAMETER_ERROR_MESSAGE);

            mErrorCodeMap.put(ADD_DEVDATA_SUCCESSFULLY_CODE, ADD_DEVDATA_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(ADD_DEVDATA_FAILED_CODE, ADD_DEVDATA_FAILED_MESSAGE);
            mErrorCodeMap.put(ADD_DEVDATA_WRONG_DEVVAL_CODE, ADD_DEVDATA_WRONG_DEVVAL_MESSAGE);
            mErrorCodeMap.put(ADD_DEVDATA_DEVID_EMPTY_CODE, ADD_DEVDATA_DEVID_EMPTY_MESSAGE);
            mErrorCodeMap.put(ADD_DEVDATA_DEVID_NOT_EXISTED_CODE, ADD_DEVDATA_DEVID_NOT_EXISTED__MESSAGE);

            mErrorCodeMap.put(ADD_DEVICE_SUCCESSFULLY_CODE, ADD_DEVICE_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(ADD_DEVICE_FAILED_CODE, ADD_DEVICE_FAILED_MESSAGE);
            mErrorCodeMap.put(ADD_DEVICE_USER_NOT_EXISTED_CODE, ADD_DEVICE_USER_NOT_EXISTED_MESSAGE);
            mErrorCodeMap.put(ADD_DEVICE_DEVID_REPEATED_CODE, ADD_DEVICE_DEVID_REPEATED_MESSAGE);
            mErrorCodeMap.put(ADD_DEVICE_DEVID_WRONG_FORMAT_CODE, ADD_DEVICE_DEVID_WRONG_FORMAT_MESSAGE);
            mErrorCodeMap.put(ADD_DEVICE_DELAY_NOT_ALLOW_EMPTY_CODE, ADD_DEVICE_DELAY_NOT_ALLOW_EMPTY_MESSAGE);

            mErrorCodeMap.put(CHECK_DEVICE_SUCCESSFULLY_CODE, CHECK_DEVICE_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(CHECK_DEVICE_FAILED_CODE, CHECK_DEVICE_FAILED_MESSAGE);

            mErrorCodeMap.put(CHECK_DEVID_SUCCESSFULLY_CODE, CHECK_DEVID_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(CHECK_DEVID_FAILED_CODE, CHECK_DEVID_FAILED_MESSAGE);
            mErrorCodeMap.put(CHECK_DEVID_FAILED_NO_ID_CODE, CHECK_DEVID_FAILED_NO_ID_MESSAGE);
            mErrorCodeMap.put(CHECK_DEVICE_EMPTY_CODE, CHECK_DEVICE_EMPTY_MESSAGE);

            mErrorCodeMap.put(AGENT_LOGIN_SUCCESSFULLY_CODE, AGENT_LOGIN_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(AGENT_LOGIN_FAILED_CODE, AGENT_LOGIN_FAILED_MESSAGE);

            mErrorCodeMap.put(AGENT_REG_SUCCESSFULLY_CODE, AGENT_REG_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(AGENT_REG_FAILED_CODE, AGENT_REG_FAILED_MESSAGE);
            mErrorCodeMap.put(AGENT_REG_FAILED_ERROR_PHONE_CODE, AGENT_REG_FAILED_ERROR_PHONE_MESSAGE);
            mErrorCodeMap.put(AGENT_REG_FAILED_ERROR_EMAIL_CODE, AGENT_REG_FAILED_ERROR_EMAIL_MESSAGE);
            mErrorCodeMap.put(AGENT_REG_FAILED_NAME_REPEATED_CODE, AGENT_REG_FAILED_NAME_REPEATED_MESSAGE);

            mErrorCodeMap.put(SALES_LOGIN_SUCCESSFULLY_CODE, SALES_LOGIN_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(SALES_LOGIN_FAILED_CODE, SALES_LOGIN_FAILED_MESSAGE);


            mErrorCodeMap.put(SALES_REG_SUCCESSFULLY_CODE, SALES_REG_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(SALES_REG_FAILED_CODE, SALES_REG_FAILED_MESSAGE);
            mErrorCodeMap.put(SALES_REG_FAILED_ERROR_PHONE_CODE, SALES_REG_FAILED_ERROR_PHONE_MESSAGE);
            mErrorCodeMap.put(SALES_REG_FAILED_ERROR_EMAIL_CODE, SALES_REG_FAILED_ERROR_EMAIL_MESSAGE);
            mErrorCodeMap.put(SALES_REG_FAILED_NAME_REPEATED_CODE, SALES_REG_FAILED_NAME_REPEATED_MESSAGE);

            mErrorCodeMap.put(SAVE_UPDELAY_SUCCESSFULLY_CODE, SAVE_UPDELAY_SUCCESSFULLY_MESSAGE);

            mErrorCodeMap.put(FETCH_PUSHMSG_SUCCESSFULLY_CODE, FETCH_PUSHMSG_SUCCESSFULLY_MESSAGE);
            mErrorCodeMap.put(FETCH_PUSHMSG_FAILED_CODE, FETCH_PUSHMSG_FAILED_MESSAGE);

            mErrorCodeMap.put(LOGOUT_SUCCESSFULLY_CODE, LOGOUT_MESSAGE);
        }

    }


}
