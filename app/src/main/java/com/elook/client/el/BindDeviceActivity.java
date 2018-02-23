package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.R;
import com.elook.client.el.initialize.BaseDevice;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.UserInfo;

/**
 * Created by haiming on 5/27/16.
 */
public class BindDeviceActivity extends Activity {
    public  static final String TAG = "BindDeviceActivity";
    private static final int MSG_ADD_DEV_OK = 1;
    private static final int MSG_ADD_DEV_FAIL = 2;
    int mDeviceId;

    TextView mAddDeviceIdTV;
    TextView mDevTypeView;
    String mDevTypeStr;
    UserInfo mUserInfo;
    //声明进度条对话框
    ProgressDialog m_pDialog;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        mDeviceId = Integer.parseInt(getIntent().getStringExtra("deviceid"));

        if (mDeviceId < 0) {
            Toast.makeText(this, "Device id " + mDeviceId + " is not correct", Toast.LENGTH_LONG).show();
            return;
        }

        initViews();
    }

    private void initViews() {
        mDevTypeView = (TextView) findViewById(R.id.add_device_type_name);
        mDevTypeStr = String.valueOf(mDeviceId).substring(1, 2);
        if (mDevTypeStr.equals(BaseDevice.DEVICE_WATER)) {
            if (String.valueOf(mDeviceId).startsWith(BaseDevice.DEVICE_WATER_WIFI)) {
                mDevTypeView.setText(R.string.measurement_water_wifi);
            } else if (String.valueOf(mDeviceId).startsWith(BaseDevice.DEVICE_WATER_GPRS)) {
                mDevTypeView.setText(R.string.measurement_water_gprs);
            } else {
                mDevTypeView.setText(R.string.measurement_water_wifi);
            }
        } else if (mDevTypeStr.equals(BaseDevice.DEVICE_ELEC)) {
            mDevTypeView.setText(R.string.measurement_electric);
        } else {
            mDevTypeView.setText(R.string.measurement_unknow);
        }
        mAddDeviceIdTV = (TextView) findViewById(R.id.add_device_id);
        mAddDeviceIdTV.setText(String.format(getString(R.string.add_device_id), mDeviceId));
    }


    public void startToBindDevice(View v) {
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(BindDeviceActivity.this);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage(getString(R.string.add_device_process_str));
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
        m_pDialog.show();
        //m_pDialog.hide();
        new Thread(adddevable).start();

    }

    private void showFailDialog(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BindDeviceActivity.this);

        builder.setMessage(result);

        builder.setTitle(R.string.config_title);
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

    private void showSuccessDialog(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BindDeviceActivity.this);

        builder.setMessage(result);

        builder.setTitle(R.string.config_title);
        builder.setPositiveButton(R.string.config_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton(R.string.config_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    Runnable adddevable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            ELookDatabaseHelper helper = ELookDatabaseHelper.newInstance(BindDeviceActivity.this);
            mUserInfo = helper.getActiveUserInfo();
            ELServiceHelper bindhelper = ELServiceHelper.get();
            boolean devaddState = bindhelper.addMeasurement(mUserInfo.getUserPhoneName(), mDeviceId, Integer.parseInt(mDevTypeStr), 1);
            if (devaddState) {
                ELookDatabaseHelper mDatabaseHelper;
                mDatabaseHelper = ELookDatabaseHelper.newInstance(BindDeviceActivity.this);
                MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
                int mState = mMeasurementInfo.getDeviceState();
                Message msg = new Message();
                msg.what = MSG_ADD_DEV_OK;
                msg.arg1 = mState;
                mhandler.sendMessage(msg);
            } else {
                mhandler.sendEmptyMessage(MSG_ADD_DEV_FAIL);
            }
        }
    };

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ADD_DEV_OK:
                    int state = msg.arg1;
                    Log.d(TAG,"MSG_ADD_DEV_OK state:"+state );
                    m_pDialog.hide();
                    if(state != MeasurementInfo.MEASUREMENT_STATE_CONFIG_SUCCESS) {
                        BindDevice(state);
                    }else{
                        showSuccessDialog(getString(R.string.add_device_ok_str));
                    }
                    break;
                case MSG_ADD_DEV_FAIL:
                    m_pDialog.hide();
                    showFailDialog(getString(R.string.add_device_fail_str));
                    Log.d("doujun", "add device fail!");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void BindDevice(int state) {
        Intent intent = new Intent(BindDeviceActivity.this, InitializeDeviceActivity.class);
        intent.putExtra("deviceid", mDeviceId);
        intent.putExtra("state", state);
        startActivity(intent);
        finish();
    }

    public void rescanQRCode(View v) {
        Intent intent = new Intent(BindDeviceActivity.this, ScanQRActivity.class);
        startActivity(intent);
        finish();

    }

    public void back(View v) {
        Intent intent = new Intent(BindDeviceActivity.this, ScanQRActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
