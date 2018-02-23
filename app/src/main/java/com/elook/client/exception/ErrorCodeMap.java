package com.elook.client.exception;

/**
 * Created by haiming on 5/19/16.
 */
public class ErrorCodeMap {
    public static final int ERROR_CANNOT_CONNECT_TO_SERVER = -1;
    public static final int ERROR_CANNOT_GET_RESULT_JSON = 0;
    public static final int ERROR_CANNOT_PARSER_RESULT_JSON = 1;
    public static final int ERROR_OPERATION_TIMEOUT = 2;

    public static final int ERROR_REG_USER_CAN_NOT_FIND = 3;

    public static final int ERRNO_REG_FAILED_ERROR_PHONENUMBER = 10000100;
    public static final int ERRNO_REG_FAILED_USER_EXISTED = 10000111;
    public static final int ERRNO_REG_FAILED = 10000101;
    public static final int ERRNO_REG_SUCCESSFULLY = 10000102;
    public static final int ERRNO_ADD_USER_INFO_ERROR_EAMIL_FORMAT = 10000103;
    public static final int ERRNO_ADD_USER_INFO_SUCCESSFULLY = 10000106;
    public static final int ERRNO_ADD_USER_INFO_ERROR = 10000107;

    public static final int ERRNO_LOGIN_SUCCESSFULLY = 10000200;
    public static final int ERRNO_LOGIN_FAILED = 10000201;

    public static final int ERRNO_GET_VERIFY_CODE_SUCCESSFULLY = 10000202;
    public static final int ERRNO_GET_VERIFY_CODE_FAILED = 10000203;

    public static final int ERRNO_FIND_PWD_SUCCESSFULLY = 10000300;

    public static final int ERRNO_ADD_DEVICE_NAME_WRONG = 10000303;
    public static final int ERRNO_ADD_DEVICE_DELAY_WRONG = 10000301;
    public static final int ERRNO_ADD_DEVICE_USER_NOT_EXISTED = 10000305;
    public static final int ERRNO_ADD_DEVICE_NOT_CONFIGED = 10000307;
    public static final int ERRNO_ADD_DEVICE_INSERT_ID_REPATED = 10000309;
    public static final int ERRNO_ADD_DEVICE_FAILED = 10000311;
    public static final int ERRNO_ADD_DEVICE_SUCCESSFULLY = 10000312;
    public static final int ERRNO_DEL_DEVICE_FAILED = 10005001;
    public static final int ERRNO_DEL_DEVICE_SUCCESSFULLY = 10005003;
    public static final int ERRNO_DEL_DEVICE_SUCCESSFULLY2 = 10005005;

    public static final int ERRNO_CHECKIN_DEVICE_FAIL = 10000901;
    public static final int ERRNO_CHECKIN_DEVICE_SUCCESSFULLY = 10000904;
    public static final int ERRNO_SELECT_DEVICE_SUCCESSFULLY = 10001110;
    public static final int ERRNO_DEVTIMESELECT_SUCCESSFULLY = 10001112;
    public static final int ERRNO_DATAFLOW_FAIL = 10001117;
    public static final int ERRNO_DATAFLOW_SUCCESSFULLY = 10001118;

    public static final int ERRNO_FETCH_RECORD_SUCCESSFULLY = 10004000;

    public static final int ERRNO_NOTUSE_DAY_SUCCESSFULLY = 10004001;

    public static final int  ERRNO_FETCH_PROBLEMMSG_SUCCESSFULLY = 10004204;

    public static final int ERRNO_FETCH_PUSHMSG_SUCCESSFULLY = 10000500;
    public static final int ERRNO_FETCH_PUSHMSGID_SUCCESSFULLY = 10000504;




}
