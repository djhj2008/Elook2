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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.activity.LoginActivity;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.utils.ELUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by haiming on 3/25/16.
 */
public class ChangePasswordActivity extends Activity {
    private static final String TAG = "FindPasswordWithPhone";

    EditText mPasswdEditText,mNewPasswdEditText,mNew2PasswdEditText;
    Button mShowPasswordButton, mSubmitNewPasswordButton;

    Dialog mProgressDialog = null;
    String phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phonenumber = getIntent().getStringExtra("phonenumber");
        if(phonenumber.isEmpty() || phonenumber.length() != 11){
            Toast.makeText(ChangePasswordActivity.this,
                    "Phone number is not correct",
                    Toast.LENGTH_LONG).show();
        }
        setContentView(R.layout.activity_change_passwd);
        initViews();
        mProgressDialog = ELUtils.createLoadingDialog(this,"");
    }

    private void initViews() {
        mPasswdEditText = (EditText)findViewById(R.id.change_password);
        mNewPasswdEditText = (EditText)findViewById(R.id.change_new_password);
        mNew2PasswdEditText = (EditText)findViewById(R.id.confirm_password);

        mShowPasswordButton = (Button)findViewById(R.id.change_show_password);
        mSubmitNewPasswordButton = (Button)findViewById(R.id.changepwd_submit);

        mShowPasswordButton.setOnTouchListener(mShowPasswdListener);
        mSubmitNewPasswordButton.setOnClickListener(mSubmitNewPasswdListener);
    }


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
            String newPasswd = mNewPasswdEditText.getText().toString();
            String newPasswd2 = mNew2PasswdEditText.getText().toString();
            ELookDatabaseHelper mDatabaseHelper = ELookDatabaseHelper.newInstance(ChangePasswordActivity.this);
            String oldpwd = mDatabaseHelper.getActiveUserInfo().getUserPasswd();
            if(oldpwd.equals(mPasswdEditText.getText().toString())) {
                if(newPasswd.equals(newPasswd2)) {
                    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                    new ChangePasswordTask().execute(phonenumber, newPasswd);
                }else{
                    showChangePWDDialog(getString(R.string.confirm_pass_word_fail));
                }
            }else{
                showChangePWDDialog(getString(R.string.old_pass_word_fail));
            }
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

    private void showChangePWDDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
        builder.setMessage(message);
        builder.setTitle(R.string.change_password);
        builder.setPositiveButton(R.string.config_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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

    private void showSuccessDialog(boolean ret) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
        if(ret) {
            builder.setMessage(getString(R.string.change_pass_word_success));
        }else{
            builder.setMessage(getString(R.string.change_pass_word_fail));
        }
        builder.setTitle(R.string.change_password);
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
                logout();
            }
        });
        builder.create().show();
    }

    public void logout(){
        ELServiceHelper serviceHelper = ELServiceHelper.get();
        boolean isSuccessfully = serviceHelper.logout();
        if(isSuccessfully){
            startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
            finish();
            ELookApplication application = (ELookApplication) getApplication();
            application.removeMainActivity();
        }
    }

    private static final int MSG_SHOW_PROGRESS_DIALOG = 2;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 3;
    private static final int MSG_CHANGE_PWD_SUCCESS = 104;
    private static final int MSG_CHANGE_PWD_FAIL = 105;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
        ELookApplication application = (ELookApplication)getApplication();
        application.removeActivity(this);
    }
}
