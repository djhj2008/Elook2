package com.elook.client.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.location.LocationWrapper;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.ui.AddressChooserDialog;
import com.elook.client.ui.ELookActionBar;
import com.elook.client.utils.ELUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by haiming on 5/19/16.
 */
public class RegisteActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RegisteActivity";
    private static final boolean DEBUG = false;
    private static final int VERIFY_CODE_TIMEOUT = 5 * 60 * 1000;


    ELookActionBar mActionBar;
    FrameLayout mRegisteMustInfoWrapper;
    EditText mPhoneNumberEditText;
    EditText mVerifyCodeEditText;
    EditText mPasswordEditText;
    CheckBox mAgreedWithLicenceCheckBox;

    Button mGetPhoneVerifyCodeButton;
    Button mShowPasswdButton;
    Button mSubmitButton;

    FrameLayout mRegisteExtInfoWrapper;
    EditText mRegistEmailEditText;
    EditText mRegisteAddressEditText;
    EditText mDetailAddressEditText;
    Button mGetLocationButton;
    Button mSubmit2Button;

    private int mVerifyCode;
    private int mVerifyCodeTimestamp;

    private Dialog mProgressDialog = null;

    private String mProvinceName = "", mCityName = "", mAreaName = "";
    private String mDetailAddress;

    LocationWrapper mLocationWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ELookApplication application = (ELookApplication) getApplication();
        application.addActivity(this);
        setContentView(R.layout.activity_registe);
        initViews();
        mProgressDialog = ELUtils.createLoadingDialog(this, "");
    }

    private void initViews() {
        mActionBar = (ELookActionBar)findViewById(R.id.registe_actionbar);
        mRegisteMustInfoWrapper = (FrameLayout) findViewById(R.id.registe_must_info_wrapper);
        mPhoneNumberEditText = (EditText) findViewById(R.id.registe_phone_number);
        mVerifyCodeEditText = (EditText) findViewById(R.id.registe_phone_verify_code);
        mPasswordEditText = (EditText) findViewById(R.id.registe_password);
        mAgreedWithLicenceCheckBox = (CheckBox) findViewById(R.id.registe_agreed_with_licence);
        mGetPhoneVerifyCodeButton = (Button) findViewById(R.id.registe_get_phone_verify_code);
        mShowPasswdButton = (Button) findViewById(R.id.registe_show_password);
        mSubmitButton = (Button) findViewById(R.id.registe_submit);

        mRegisteExtInfoWrapper = (FrameLayout) findViewById(R.id.registe_extension_info_wrapper);
        mRegistEmailEditText = (EditText) findViewById(R.id.registe_email);
        mRegisteAddressEditText = (EditText) findViewById(R.id.registe_user_address);
        mDetailAddressEditText = (EditText)findViewById(R.id.registe_detail_address);
        mGetLocationButton = (Button) findViewById(R.id.registe_locate);
        mSubmit2Button = (Button) findViewById(R.id.registe_submit2);

        mRegisteAddressEditText.setOnClickListener(this);
        mGetPhoneVerifyCodeButton.setOnClickListener(this);
        mShowPasswdButton.setOnTouchListener(mShowPasswdListener);
        mSubmitButton.setOnClickListener(this);
        mGetLocationButton.setOnClickListener(this);
        mSubmit2Button.setOnClickListener(this);
        mActionBar.setActionBarListener(new ELookActionBar.ActionBarListener() {
            @Override
            public void onMenuClicked(View v) {
                String title = ((TextView)v).getText().toString();
                if(title.equals(getString(R.string.login))){
                    startActivity(new Intent(RegisteActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
            }
        });
    }


    private void showExtensionInfo() {
        mRegisteMustInfoWrapper.setVisibility(View.GONE);
        mRegisteExtInfoWrapper.setVisibility(View.VISIBLE);
    }

    private boolean isNeedToAddExternInfo(){
        boolean isNeed = false;
        String email = mRegistEmailEditText.getText().toString();
        mDetailAddress = mDetailAddressEditText.getText().toString();
        String phoneNumber = mPhoneNumberEditText.getText().toString();
        if(!email.isEmpty() && !mProvinceName.isEmpty() &&
                !mCityName.isEmpty() && !mAreaName.isEmpty()){
            isNeed = true;
            if(!ELUtils.isEmailAvaliable(email)){
                isNeed = false;
                Toast.makeText(RegisteActivity.this,
                        "邮箱格式错误.", Toast.LENGTH_LONG).show();
            }
        } else if(email.isEmpty() && mProvinceName.isEmpty() &&
                mCityName.isEmpty() && mAreaName.isEmpty()){
            isNeed = true;
        }else {
            isNeed = false;
            Toast.makeText(RegisteActivity.this,
                    "邮箱或地址不能为空.", Toast.LENGTH_LONG).show();
        }

        return isNeed;
    }

    private boolean mustInfoIsCorrect() {
        String phoneNumber = mPhoneNumberEditText.getText().toString().trim();
        if(!ELUtils.isPhoneNumberAvaliable(phoneNumber)){
            Toast.makeText(RegisteActivity.this, "手机号码格式有误.", Toast.LENGTH_LONG).show();
        }
        boolean isCorrect = false;
        long current = SystemClock.currentThreadTimeMillis();
        if (current - mVerifyCodeTimestamp > VERIFY_CODE_TIMEOUT) {
            Toast.makeText(RegisteActivity.this, "验证码超时.", Toast.LENGTH_LONG).show();
            return false;
        }

        String code = mVerifyCodeEditText.getText().toString();
        if (code == null ||
                (code != null && code.isEmpty())) {
            return false;
        }

        if (!code.equals(mVerifyCode + "")) {
            Toast.makeText(RegisteActivity.this, "验证码错误.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if(mTimer!=null) {
                mTimer.cancel();
                mTimer = null;
                mCounter = 60;
            }
            mHandler.sendEmptyMessage(MSG_RESET_BUTTON);
        }
        String verifCode = mVerifyCodeEditText.getText().toString().trim();
        if ( mAgreedWithLicenceCheckBox.isChecked()) {
            isCorrect = true;
        } else {
            Toast.makeText(RegisteActivity.this, "请勾选用户注册协议", Toast.LENGTH_LONG).show();
            isCorrect = false;
        }
        return isCorrect;
    }

    View.OnTouchListener mShowPasswdListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    break;
                case MotionEvent.ACTION_UP:
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    break;
            }
            return true;
        }
    };

    Timer mTimer ;
    static int mCounter = 60;
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

    Runnable verifyable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            final String phoneNumber = mPhoneNumberEditText.getEditableText().toString();
            if (!ELUtils.isPhoneNumberAvaliable(phoneNumber)) {
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_PHONE_FORMAT);
                //Toast.makeText(RegisteActivity.this,
                //        "Phone number format is error", Toast.LENGTH_LONG).show();
                return;
            }
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            String verifyCodeInfo = instanceHelper.getMessageVerifyCode(phoneNumber);
            String results[] = verifyCodeInfo.split(",");
            if(results.length <= 0){
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_FAIL);
                //Toast.makeText(RegisteActivity.this,
                //        "Cannot get verify code", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                mVerifyCode = Integer.parseInt(results[0]);
                mVerifyCodeTimestamp = Integer.parseInt(results[1]);
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_OK);
            } catch (NumberFormatException e){
                mHandler.sendEmptyMessage(MSG_VERIFY_CODE_ERROR);
                //Toast.makeText(RegisteActivity.this,
                //        "Cannot parser", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onclick");
        int id = v.getId();
        switch (id) {
            case R.id.registe_get_phone_verify_code: {

                mGetPhoneVerifyCodeButton.setEnabled(false);
                mGetPhoneVerifyCodeButton.setClickable(false);

                mTimer = new Timer();
                mTimer.schedule(mTimerTask, 0, 1000);
                new Thread(verifyable).start();

            }

            break;
            case R.id.registe_submit:
                if (mustInfoIsCorrect()) {
                    showExtensionInfo();
                } else {
                    Toast.makeText(this, "phone number or verify code is wrong, or please check user Licence", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.registe_user_address:
                AddressChooserDialog dialog = new AddressChooserDialog(this);
                dialog.registeAddressChoosedListener(new AddressChooserDialog.OnAddressChoosedListener() {
                    @Override
                    public void onAddressChoosed(String province, String city, String area) {
                        mProvinceName = province;
                        mCityName = city;
                        mAreaName = area;
                        Log.d(TAG, "province: "+province+", city = "+city+", area = "+area);
                        mRegisteAddressEditText.setText(province+city+area);
                    }
                });
                dialog.show();
                break;

            case R.id.registe_locate:
                mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
                mLocationWrapper = new LocationWrapper(RegisteActivity.this);
                new QueryLocationTask().execute();
                break;
            case R.id.registe_submit2:
                if(!isNeedToAddExternInfo()){
                    return;
                }
                mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
                new RegisteTask().execute();
                break;
            default:
                Log.d(TAG, "default");
                break;
        }

    }

    String[] mLocationString = null;
    class QueryLocationTask extends AsyncTask<Void, Void, String[]>{
        @Override
        protected String[] doInBackground(Void... params) {

            final Object lock = new Object();
            LocationWrapper.OnLocationChangedListener mLocationListener = new LocationWrapper.OnLocationChangedListener() {
                @Override
                public void onLocationChanged(String province, String city, String township, String street, String streetNumber , String building,double latitude,double longitude) {
                    synchronized (lock){
                        if(mLocationString == null){
                            mLocationString = new String[6];
                            mLocationString[0] = province;
                            mLocationString[1] = city;
                            mLocationString[2] = township;
                            mLocationString[3] = street;
                            mLocationString[4] = streetNumber;
                            mLocationString[5] = building;
                            lock.notify();
                        }
                    }
                }
            };

            mLocationWrapper.queryCurrentLocation(mLocationListener);
            synchronized (lock){
                try {
                    if (mLocationString == null) lock.wait();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            return mLocationString;
        }

        @Override
        protected void onPostExecute(String[] address) {
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
            if(address != null && address.length == 6){
                mProvinceName = address[0];
                mCityName = address[1];
                mAreaName = address[2];
                mDetailAddress = address[3] + address[4] + address[5];

                mRegisteAddressEditText.setText(mProvinceName+mCityName+mAreaName);
                mDetailAddressEditText.setText(mDetailAddress);
            } else {
                Toast.makeText(RegisteActivity.this, "获取位置失败.", Toast.LENGTH_LONG).show();
            }
            mLocationString = null;
        }
    }

    class RegisteTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccessfully = submitMustInfo();
            if(isSuccessfully) {
                submitExternInfo();
            }
            return isSuccessfully;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessfully) {
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
            if(isSuccessfully){
                //pop1
                Toast.makeText(RegisteActivity.this, "Registe successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisteActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }else{
                //pop3
                Toast.makeText(RegisteActivity.this, "Registe error", Toast.LENGTH_LONG).show();
            }

        }
    }

    private boolean submitMustInfo() {
        ELServiceHelper instanceHelper = ELServiceHelper.get();
        String phoneNumber = mPhoneNumberEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        boolean isSuccessful = instanceHelper.registe(phoneNumber, password);
        return isSuccessful;
    }


    private void submitExternInfo() {
        String email = mRegistEmailEditText.getText().toString();
        mDetailAddress = mDetailAddressEditText.getText().toString();
        String phoneNumber = mPhoneNumberEditText.getText().toString();
        if(!email.isEmpty() && ELUtils.isEmailAvaliable(email) && !mProvinceName.isEmpty() &&
                !mCityName.isEmpty() && !mAreaName.isEmpty()){
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            instanceHelper.addUserExternInfo(phoneNumber, email, mProvinceName, mCityName, mAreaName, mDetailAddress);

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

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BUTTON:
                    int counter = msg.arg1;
                    mGetPhoneVerifyCodeButton.setText(String.format(getString(R.string.verify_code_timeout), counter));
                    break;
                case MSG_RESET_BUTTON:
                    mTimerTask = new ELTimerTask();
                    mGetPhoneVerifyCodeButton.setText(getString(R.string.msg_verify_code));
                    mGetPhoneVerifyCodeButton.setEnabled(true);
                    mGetPhoneVerifyCodeButton.setClickable(true);
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
                    Toast.makeText(RegisteActivity.this,
                            "verify code:"+mVerifyCode, Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_FAIL: {
                    Toast.makeText(RegisteActivity.this,
                            "Cannot get verify code", Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_ERROR: {
                    Toast.makeText(RegisteActivity.this,
                            "Cannot parser", Toast.LENGTH_LONG).show();
                }
                break;
                case MSG_VERIFY_CODE_PHONE_FORMAT: {
                    Toast.makeText(RegisteActivity.this,
                            "Phone number format is error", Toast.LENGTH_LONG).show();
                }
                break;
                default:
                    break;
            }
        }
    };


    public void showUserLicence(View v) {
        Toast.makeText(this, "show user Licence", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ELookApplication application = (ELookApplication) getApplication();
        //application.removeActivity(this);
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
}
