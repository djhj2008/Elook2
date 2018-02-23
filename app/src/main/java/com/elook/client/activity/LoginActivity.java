package com.elook.client.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.el.FindPasswordActivity;
import com.elook.client.exception.ErrorCode;
import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.exception.ExceptionCenter;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.ui.ELookActionBar;
import com.elook.client.user.AdvertInfo;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.ELUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

/**
 * Created by haiming on 5/19/16.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    ELookApplication myapp;
    EditText mLoginPhoneNumberEditText;
    EditText mLoginPasswordEditText;
    TextView mRegisteTextView;
    Button mSubmitButton;
    LinearLayout logbgLL;

    TextView mLoginTitleTV, mLoginPhoneNumberDescriptionTV, mLoginPasswdDescriptionTV,mFindPwdDescriptionTV;

    private Dialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_login);
        initViews();
        mProgressDialog = ELUtils.createLoadingDialog(this, "");
    }

    private void initViews() {
        mRegisteTextView = (TextView) findViewById(R.id.login_registe);
        mLoginPhoneNumberEditText = (EditText) findViewById(R.id.login_phone_number);
        mLoginPasswordEditText = (EditText) findViewById(R.id.login_password);
        mSubmitButton = (Button) findViewById(R.id.login_submit);
        mLoginTitleTV = (TextView)findViewById(R.id.login_login);
        logbgLL = (LinearLayout)findViewById(R.id.login_bg);

        mLoginPhoneNumberDescriptionTV = (TextView)findViewById(R.id.login_phone_number_description);
        mLoginPasswdDescriptionTV = (TextView)findViewById(R.id.login_password_description);
        mFindPwdDescriptionTV = (TextView)findViewById(R.id.login_find_pwd_description);

        myapp=(ELookApplication)getApplication();

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mLoginPhoneNumberEditText.getText().toString();
                String password = mLoginPasswordEditText.getText().toString();
                if(phoneNumber.isEmpty()||password.isEmpty()){
                    mHandler.sendEmptyMessage(MSG_VERIFY_CODE_PHONE_EMPTY);
                    return;
                }else if (!ELUtils.isPhoneNumberAvaliable(phoneNumber)) {
                    mHandler.sendEmptyMessage(MSG_VERIFY_CODE_PHONE_FORMAT);
                    return;
                }else if(password.length()<6){
                    mHandler.sendEmptyMessage(MSG_VERIFY_CODE_PASSWORD_SHORT);
                }
                new LoginTask().execute();
            }
        });
        mRegisteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisteActivity.class));
                finish();
            }
        });
    }

    private boolean isPhoneNumberAvaliable(String phoneNumber){
        boolean isAvaliable = false;
        if(phoneNumber.matches("1[34578][0-9]{9,9}")){
            isAvaliable = true;
        } else {
            isAvaliable = false;
        }
        return isAvaliable;
    }

    private static final int MSG_SHOW_PROGRESS_DIALOG = 0;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 1;
    private static final int MSG_VERIFY_CODE_PHONE_EMPTY = 2;
    private static final int MSG_VERIFY_CODE_PHONE_FORMAT = 3;
    private static final int MSG_VERIFY_CODE_PASSWORD_SHORT = 4;
    private static final int MSG_LOGIN_SUCCESS = 5;
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
                case MSG_LOGIN_SUCCESS:
                    Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
                    intent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case MSG_VERIFY_CODE_PHONE_EMPTY:
                    Toast.makeText(LoginActivity.this, "手机号或密码为空.", Toast.LENGTH_LONG).show();
                    break;
                case MSG_VERIFY_CODE_PHONE_FORMAT:
                    Toast.makeText(LoginActivity.this, "手机号码格式有误.", Toast.LENGTH_LONG).show();
                    break;
                case MSG_VERIFY_CODE_PASSWORD_SHORT:
                    Toast.makeText(LoginActivity.this, "密码格式不正确.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
            boolean isSuccessfully = false;
            isSuccessfully = login();
            if(isSuccessfully){
                downloadAdvertPic();
                ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(LoginActivity.this);
                UserInfo info = databaseHelper.getActiveUserInfo();
                myapp.setUserInfo(info);
            }

            return isSuccessfully;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessfully) {
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
            if (isSuccessfully) {
                mHandler.sendEmptyMessage(MSG_LOGIN_SUCCESS);
                Log.d(TAG, "Login successfully");
            }else{
                ExceptionCenter.process(LoginActivity.this, new ErrorCode(ErrorCodeMap.ERRNO_LOGIN_FAILED));
            }

        }
    }

    private void downloadAdvertPic(){
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(LoginActivity.this);
        List<AdvertInfo> infos = databaseHelper.getAdvertInfo();
        int banner_count = infos.size();
        for(int i=0;i<banner_count;i++){
            String picurl=infos.get(i).getAdvertPicUrl();
            if(picurl!=null){
                if(!picurl.startsWith("http://"))
                    continue;
                int index =picurl.lastIndexOf("/");
                if(index > 0) {
                    String newPicUrl = picurl.substring(index+1);
                    Log.d(TAG,"downloadAdvertPic name:"+newPicUrl);
                    final String targetLocalPath = getFilesDir().getAbsolutePath() +
                            "/" + "ADVERT" + "/" + newPicUrl;
                    File imageFile = new File(targetLocalPath);
                    File parentFile = imageFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    if(!imageFile.exists()){
                        ELServiceHelper instanceHelper = ELServiceHelper.get();
                        instanceHelper.loadImageFromeServer(picurl, targetLocalPath);
                    }
                }
            }
        }
    }

    private boolean login() {
        ELServiceHelper instanceHelper = ELServiceHelper.get();
        String phoneNumber = mLoginPhoneNumberEditText.getText().toString();
        String password = mLoginPasswordEditText.getText().toString();
        boolean isSuccessful = instanceHelper.login(phoneNumber, password);
        return isSuccessful;
    }

    public void findPassword(View v) {
        Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "on destroy");
//        BitmapDrawable bd = (BitmapDrawable)logbgLL.getBackground();
//        logbgLL.setBackgroundResource(0);
//        bd.setCallback(null);
//        bd.getBitmap().recycle();
//        logbgLL=null;
        setContentView(R.layout.activity_null);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            System.exit(0);
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void back(View v) {
        finish();
    }

}
