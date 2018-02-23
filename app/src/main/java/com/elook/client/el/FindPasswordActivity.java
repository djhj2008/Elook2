package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.activity.LoginActivity;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.utils.ELUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by haiming on 3/25/16.
 */
public class FindPasswordActivity extends Activity {
    private static final String TAG = "FindPasswordWithPhone";
    private static final int VERIFY_CODE_TIMEOUT = 5 * 60 * 1000;
    private static final int GET_VERIFY_CODE_TIMEOUT = 60 * 1000;

    FrameLayout mNewPasswordWrapper,mVerifyCodeWrapper;

//    LinearLayout mVerifyCodeWrapper, mNewPasswdWrapper;

    EditText mPhoneNumberEditText;
    EditText mVerifyCodeEditText;
    Button mGetVerifyCodeButton;
    Button mNextStepButton;

    EditText mNewPasswdEditText;
    Button mShowPasswordButton, mSubmitNewPasswordButton;

    Dialog mProgressDialog = null;

    private int mVerifyCode = 0;
    private int mVerifyCodeTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ELookApplication application = (ELookApplication)getApplication();
        application.addActivity(this);
        setContentView(R.layout.activity_find_passwd);
        initViews();
        mTimer = new Timer();
        mProgressDialog = ELUtils.createLoadingDialog(this,"");
    }

    private void initViews() {
        mVerifyCodeWrapper = (FrameLayout)findViewById(R.id.findpwd_get_verify_code_wrapper);
        mPhoneNumberEditText = (EditText)findViewById(R.id.findpwd_phone_number);
        mVerifyCodeEditText = (EditText)findViewById(R.id.findpwd_phone_verify_code);
        mGetVerifyCodeButton = (Button) findViewById(R.id.findpwd_get_phone_verify_code);
        mNextStepButton = (Button)findViewById(R.id.findpwd_next);

        mGetVerifyCodeButton.setOnClickListener(mGetVerifyCodeListener);
        mNextStepButton.setOnClickListener(mVerifyCodeListener);

        mNewPasswordWrapper = (FrameLayout)findViewById(R.id.findpwd_newpasswd_wrapper);
        mNewPasswdEditText = (EditText)findViewById(R.id.findpwd_new_password);
        mShowPasswordButton = (Button)findViewById(R.id.findpwd_show_password);
        mSubmitNewPasswordButton = (Button)findViewById(R.id.findpwd_submit);

        mShowPasswordButton.setOnTouchListener(mShowPasswdListener);
        mSubmitNewPasswordButton.setOnClickListener(mSubmitNewPasswdListener);
    }

    View.OnClickListener mGetVerifyCodeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String phoneNumberStr = mPhoneNumberEditText.getText().toString().trim();
            if(!isPhoneNumberAvaliable(phoneNumberStr)){
                //Toast.makeText(FindPasswordActivity.this, "请填写正确的手机号 ", Toast.LENGTH_LONG).show();
                return;
            }
            mGetVerifyCodeButton.setEnabled(false);
            mGetVerifyCodeButton.setClickable(false);

            mTimer = new Timer();
            mTimer.schedule(mTimerTask, 0, 1000);
            new Thread(verifyable).start();

        }
    };

    Runnable verifyable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            final String phoneNumber = mPhoneNumberEditText.getEditableText().toString();
            if (!ELUtils.isPhoneNumberAvaliable(phoneNumber)) {
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_PHONE_FORMAT);
                return;
            }
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            String verifyCodeInfo = instanceHelper.getMessageVerifyCode(phoneNumber);
            String results[] = verifyCodeInfo.split(",");
            if(results.length <= 0){
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_FAIL);
                return;
            }
            try {
                mVerifyCode = Integer.parseInt(results[0]);
                mVerifyCodeTimestamp = Integer.parseInt(results[1]);
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_OK);
            } catch (NumberFormatException e){
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_ERROR);
            }
        }
    };


    private boolean isPhoneNumberAvaliable(String phoneNumber){
        boolean isAvaliable = false;
        if(phoneNumber.matches("1[34578][0-9]{9,9}")){
            isAvaliable = true;
        } else {
            isAvaliable = false;
        }
        return isAvaliable;
    }

    View.OnClickListener mVerifyCodeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String phoneNumberStr = mPhoneNumberEditText.getText().toString().trim();
            if(!isPhoneNumberAvaliable(phoneNumberStr)){
                //Toast.makeText(FindPasswordActivity.this, "请填写正确的手机号 ", Toast.LENGTH_LONG).show();
                return;
            }
            long current = SystemClock.currentThreadTimeMillis();
            if(current - mVerifyCodeTimestamp > VERIFY_CODE_TIMEOUT){
                //Toast.makeText(FindPasswordActivity.this, "verify Code time out", Toast.LENGTH_LONG).show();
                return;
            }

            String code = mVerifyCodeEditText.getText().toString();
            if(code == null ||
                    (code != null && code.isEmpty())){
                return;
            }

            if(!code.equals(mVerifyCode+"")){
                //Toast.makeText(FindPasswordActivity.this, "Verify code is wrong", Toast.LENGTH_LONG).show();
            } else {
                mVerifyCodeWrapper.setVisibility(View.GONE);
                mNewPasswordWrapper.setVisibility(View.VISIBLE);

                //Toast.makeText(FindPasswordActivity.this, "Correct", Toast.LENGTH_LONG).show();
                if(mTimer!=null) {
                    mTimer.cancel();
                    mTimer = null;
                    mCounter = 60;
                }
                mHandler.sendEmptyMessage(MSG_RESET_BUTTON);
            }
        }
    };

    View.OnTouchListener mShowPasswdListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mNewPasswdEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    break;
                case MotionEvent.ACTION_UP:
                    mNewPasswdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    break;
            }
            return true;
        }
    };

    View.OnClickListener mSubmitNewPasswdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String phoneNumber = "";
            String newPasswd = "";
            phoneNumber = mPhoneNumberEditText.getText().toString();
            newPasswd = mNewPasswdEditText.getText().toString();

            if(phoneNumber.isEmpty() || phoneNumber.length() != 11){
                Toast.makeText(FindPasswordActivity.this,
                        "Phone number is not correct",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if(mProgressDialog != null && !mProgressDialog.isShowing()) mProgressDialog.show();
            new ChangePasswordTask().execute(phoneNumber, newPasswd);
        }
    };

    private class ChangePasswordTask extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0];
            String newPasswd = params[1];
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            boolean ret = instanceHelper.changPasswdWithPhoneMsg(phoneNumber, newPasswd);
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean ret) {
            if(mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if(ret) {
                mHandler.sendEmptyMessage(MSG_CHANGE_PWD_SUCCESS);
            }else{
                mHandler.sendEmptyMessage(MSG_CHANGE_PWD_FAIL);
            }
        }
    }

    private void showSuccessDialog(boolean ret) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FindPasswordActivity.this);
        if(ret) {
            builder.setMessage(getString(R.string.change_pass_word_success));
        }else{
            builder.setMessage(getString(R.string.change_pass_word_fail));
        }
        builder.setTitle(R.string.find_password);
        builder.setPositiveButton(R.string.config_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logout();
            }
        });

        builder.setNegativeButton(R.string.config_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void logout(){
        ELServiceHelper serviceHelper = ELServiceHelper.get();
        boolean isSuccessfully = serviceHelper.logout();
        if(isSuccessfully){
            startActivity(new Intent(FindPasswordActivity.this, LoginActivity.class));
            finish();
            ELookApplication application = (ELookApplication) getApplication();
            application.removeMainActivity();
        }
    }

    static int mCounter = 60;
    Timer mTimer;
    TimerTask mTimerTask = new ELTimerTask();
    class ELTimerTask extends TimerTask{
        public void run() {
            mCounter--;
            if (mCounter > 0) {
                Message msg = mHandler.obtainMessage(MSG_UPDATE_BUTTON);
                msg.arg1 = mCounter;
                mHandler.sendMessage(msg);
            } else {
                if(mTimer!=null) {
                    mTimer.cancel();
                    mTimer = null;
                    mCounter = 60;
                }
                mHandler.sendEmptyMessage(MSG_RESET_BUTTON);
            }
        }
    }

    private static final int MSG_UPDATE_BUTTON = 0;
    private static final int MSG_RESET_BUTTON = 1;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 2;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 3;
    private static final int MSG_VERIFY_CODE_OK = 100;
    private static final int MSG_VERIFY_CODE_FAIL = 101;
    private static final int MSG_VERIFY_CODE_ERROR = 102;
    private static final int MSG_VERIFY_CODE_PHONE_FORMAT = 103;
    private static final int MSG_CHANGE_PWD_SUCCESS = 104;
    private static final int MSG_CHANGE_PWD_FAIL = 105;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BUTTON:
                    int counter = msg.arg1;
                    mGetVerifyCodeButton.setText(String.format(getString(R.string.verify_code_timeout), counter));
                    break;
                case MSG_RESET_BUTTON:
                    mTimerTask = new ELTimerTask();
                    mGetVerifyCodeButton.setText(getString(R.string.msg_verify_code));
                    mGetVerifyCodeButton.setEnabled(true);
                    mGetVerifyCodeButton.setClickable(true);
                    break;
                case MSG_SHOW_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                }
                break;
                case MSG_DIMISS_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
                break;
                case MSG_VERIFY_CODE_OK: {
                    Toast.makeText(FindPasswordActivity.this,
                            "verify code:"+mVerifyCode, Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_FAIL: {
                    Toast.makeText(FindPasswordActivity.this,
                            "Cannot get verify code", Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_ERROR: {
                    Toast.makeText(FindPasswordActivity.this,
                            "Cannot parser", Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_PHONE_FORMAT: {
                    Toast.makeText(FindPasswordActivity.this,
                            "Phone number format is error", Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_CHANGE_PWD_SUCCESS:
                    showSuccessDialog(true);
                    break;
                case MSG_CHANGE_PWD_FAIL:
                    showSuccessDialog(false);
                    break;
                default:
                    break;
            }
        }
    };

    public void back(View v){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ELookApplication application = (ELookApplication)getApplication();
//        application.removeActivity(this);
    }
}
