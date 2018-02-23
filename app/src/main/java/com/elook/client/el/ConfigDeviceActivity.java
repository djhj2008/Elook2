package com.elook.client.el;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.utils.ELUtils;

import java.util.HashMap;

/**
 * Created by haiming on 6/1/16.
 */
public class ConfigDeviceActivity extends Activity implements  View.OnClickListener{
    private static final String TAG = "ConfigDevActvity";

    FrameLayout mConfigBasicInfoWrapper, mConfigCoreInfoWrapper;

    TextView mConfigDevIdTV, mConfigDevBindStateTV;
    TextView mConfigDevBindTimeTV;
    EditText mConfigDevOfficeNumberET, mConfigDevNickNameET, mConfigDevAddressET;

    Button mNextStepButton;

    RadioGroup mDelayChooserGroup;
    ToggleButton mConfigDevNeedPicTB;
    Button mSaveConfigButton;

    Dialog mProgressDialog;

    ELookDatabaseHelper mDatabaseHelper;
    MeasurementInfo mMeasurementInfo;
    int mDeviceId;
    int mUpdateDelayMode = 0;
    boolean isNeedUpdatePic = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String tmpDevId = getIntent().getStringExtra("deviceid");
//        if(tmpDevId == null || (tmpDevId != null && tmpDevId.equals("null")))return;
        mDeviceId = Integer.parseInt("123456789");
        if(mDeviceId < 0) return;
        mDatabaseHelper = ELookDatabaseHelper.newInstance(this);

        setContentView(R.layout.activity_config_device);
        initViews();
        mProgressDialog = ELUtils.createLoadingDialog(this,"");
        mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
    }

    private void initViews(){
        mConfigBasicInfoWrapper = (FrameLayout)findViewById(R.id.config_device_basic_wrapper);
        mConfigCoreInfoWrapper = (FrameLayout)findViewById(R.id.config_device_core_wrapper);

        mConfigDevIdTV = (TextView)findViewById(R.id.config_device_deviceId);
//        mConfigDevBindStateTV = (TextView)findViewById(R.id.)
        mConfigDevBindTimeTV = (TextView)findViewById(R.id.config_device_bind_time);

        mConfigDevOfficeNumberET = (EditText)findViewById(R.id.config_dev_office_number);
        mConfigDevNickNameET = (EditText)findViewById(R.id.config_dev_nick_name);
        mConfigDevAddressET = (EditText)findViewById(R.id.config_dev_address);
        mNextStepButton = (Button)findViewById(R.id.config_dev_next_step);

        mDelayChooserGroup = (RadioGroup)findViewById(R.id.config_dev_radio_group);
        mConfigDevNeedPicTB = (ToggleButton)findViewById(R.id.toggle_button);
        mSaveConfigButton = (Button)findViewById(R.id.config_device_save_config);


        mConfigDevIdTV.setText(String.format(getString(R.string.measurement_settings_deviceid), mDeviceId));
//        mConfigDevBindTimeTV.setText();


        mNextStepButton.setOnClickListener(this);
        mSaveConfigButton.setOnClickListener(this);
        mDelayChooserGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        mConfigDevNeedPicTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mConfigDevNeedPicTB.setBackgroundResource(isChecked?R.drawable.switcher_on:R.drawable.switcher_off);
                isNeedUpdatePic = isChecked;
            }
        });
    }

    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "onCheckedChanged ");
            int index = 0;
            switch (checkedId) {
                case R.id.config_dev_fp_standar:
                    Log.d(TAG, "onClick  activity_measurement_record_day");
                    index = 1;
                    break;
                case R.id.config_dev_fp_save_battery:
                    Log.d(TAG, "onClick  activity_measurement_record_week");
                    index = 7;
                    break;
                case R.id.config_dev_fp_super_save_battery:
                    Log.d(TAG, "onClick  activity_measurement_record_month");
                    index = 30;
                    break;
            }
            mUpdateDelayMode = index;

        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.config_dev_next_step:
                mConfigBasicInfoWrapper.setVisibility(View.GONE);
                mConfigCoreInfoWrapper.setVisibility(View.VISIBLE);
                break;
            case R.id.config_device_save_config:
                new SaveDeviceConfigTask().execute();
                break;
        }
    }

    private static final int MSG_SHOW_PROGRESS_DIALOG = 0;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 1;
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
                default:
                    break;
            }
        }
    };

    class SaveDeviceConfigTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
            boolean isSuccess = saveConfig();
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
            if(isSuccess){
                Toast.makeText(ConfigDeviceActivity.this, "Add Device successfully", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean saveConfig(){
        ELServiceHelper helper = ELServiceHelper.get();
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(ConfigDeviceActivity.this);
        int devType = Character.digit((mDeviceId + "").charAt(1), 10);
        boolean isSuccess = helper.addMeasurement(databaseHelper.getActiveUserInfo().getUserPhoneName(),
                mDeviceId,devType,mUpdateDelayMode);


        if(mConfigDevNeedPicTB.isChecked()){
//            HashMap<String, String> params = new HashMap<>();
            Bundle params = new Bundle();
            if(mMeasurementInfo == null){
                params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_UPLSTATE, "1");
            } else {
                boolean currentState = mMeasurementInfo.getUplState() == 0 ? false : true;
                if (currentState ^ mConfigDevNeedPicTB.isChecked())
                    params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_UPLSTATE, (mConfigDevNeedPicTB.isChecked() ? 1 : 0) + "");
            }
            if(params.size() > 0){
                helper.setDeviceDateFlow(mDeviceId, params);
            }
        }
        return isSuccess;

    }
    public void back(View v){
        finish();
    }
}
