package com.elook.client.exception;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.elook.client.R;
import com.elook.client.el.ErrorPageActivity;
import com.elook.client.user.MeasurementInfo;

/**
 * Created by haiming on 5/28/16.
 */
public  class ExceptionCenter {
    private static final String TAG = "ExceptionCenter";
    private static Context mContext;
    private static final int MSG_PROCESSED_ERROR = 1;
    private static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "show toast");
                    Toast.makeText(mContext, solution, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    static String solution = "";
    public static void process(final Context context,ErrorCode e){
        if(context != mContext)mContext = context;
//        e.printStackTrace();
        switch (e.status_code){
            case ErrorCodeMap.ERROR_CANNOT_CONNECT_TO_SERVER:
                solution = context.getString(R.string.cannot_connect_to_server);
                break;
            case ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON:
                solution = context.getString(R.string.cannot_get_result_from_server);
                break;
            case ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON:
                solution = context.getString(R.string.cannot_parser_result_from_server);
                break;
            case ErrorCodeMap.ERROR_OPERATION_TIMEOUT:
                solution = context.getString(R.string.operation_timeout);
                break;
            case ErrorCodeMap.ERRNO_REG_FAILED_ERROR_PHONENUMBER:
                solution = context.getString(R.string.errno_reg_phonenumber_format);
                break;
            case ErrorCodeMap.ERRNO_REG_FAILED_USER_EXISTED:
                solution = context.getString(R.string.errno_reg_user_existed);
                break;
            case ErrorCodeMap.ERRNO_REG_FAILED:
                solution = context.getString(R.string.errno_reg_failed);
                break;
            case ErrorCodeMap.ERRNO_GET_VERIFY_CODE_FAILED:
                solution = context.getString(R.string.cannot_send_verify_code);
                break;
            case ErrorCodeMap.ERRNO_LOGIN_FAILED:
                solution = context.getString(R.string.login_failed);
                break;
            case ErrorCodeMap.ERRNO_ADD_USER_INFO_ERROR_EAMIL_FORMAT:
                solution = context.getString(R.string.email_error_format);
                break;
            case ErrorCodeMap.ERRNO_ADD_USER_INFO_ERROR:
                solution = context.getString(R.string.add_user_info_failed);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_NAME_WRONG:
                solution = context.getString(R.string.device_name_should_be_9_digits);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_DELAY_WRONG:
                solution = context.getString(R.string.device_should_not_be_null);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_USER_NOT_EXISTED:
                solution = context.getString(R.string.device_user_not_existed);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_NOT_CONFIGED:
                solution = context.getString(R.string.device_not_configed);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_INSERT_ID_REPATED:
                solution = context.getString(R.string.device_id_should_not_be_repeated);
                break;
            case ErrorCodeMap.ERRNO_ADD_DEVICE_FAILED:
                solution = context.getString(R.string.cannont_add_device_to_server);
                break;
            case ErrorCodeMap.ERROR_REG_USER_CAN_NOT_FIND:
                solution = context.getString(R.string.add_user_info_debug_database);
                break;
            default:
                solution = e.message;
                break;
        }

        handler.sendEmptyMessage(MSG_PROCESSED_ERROR);
//        Intent intent = new Intent(context, ErrorPageActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(ErrorPageActivity.ERROR_CODE_TAG, e.status_code);
//        intent.putExtra(ErrorPageActivity.ERROR_MSG_TAG, solution);
//        context.startActivity(intent);15298242661
    }
}
