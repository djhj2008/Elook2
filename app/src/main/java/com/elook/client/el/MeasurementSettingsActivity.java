package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.elook.client.R;
import com.elook.client.activity.LoginActivity;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.location.LocationWrapper;
import com.elook.client.location.PoiAroundSearch;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.user.MeasurementInfo;
import com.google.zxing.common.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by haiming on 5/26/16.
 */
public class MeasurementSettingsActivity extends Activity {
    private static final String TAG = "MeasurementSettings";
    private static final int UPDELAY_TYPE_DAY_1 = 1;
    private static final int UPDELAY_TYPE_DAY_2 = 3;
    private static final int UPDELAY_TYPE_DAY_3 = 6;
    TextView mTextViewDevId;
    TextView mTextViewBindTime;
    EditText mNickNameView;
    EditText mPayIdView;
    EditText mLoactionView;
    ImageView mLocationButton;
    ToggleButton mPicUpStateView;
    Spinner mUpdelayView;
    Button mSaveButton;

    int mDeviceId=0;
    private ProgressDialog mProgressDialog;
    ELookDatabaseHelper mDatabaseHelper;
    private MeasurementInfo mMeasurementInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_settings);
        mDeviceId = getIntent().getIntExtra("deviceid",0);
        initView();

    }

    @Override
    protected void onStart() {
        //mDatabaseHelper = ELookDatabaseHelper.newInstance(MeasurementSettingsActivity.this);
        super.onStart();
    }

    private void initView(){
        mTextViewDevId = (TextView)findViewById(R.id.measurement_settings_deviceId);
        mTextViewBindTime = (TextView)findViewById(R.id.measurement_settings_bind_time);
        mPicUpStateView = (ToggleButton)findViewById(R.id.remind_usage_up_switcher);
        mNickNameView = (EditText)findViewById(R.id.measurement_settings_nick_name);
        mPayIdView = (EditText)findViewById(R.id.measurement_settings_payid);
        mLoactionView = (EditText)findViewById(R.id.measurement_settings_location);
        mLocationButton = (ImageView)findViewById(R.id.measurement_settings_location_button);
        mUpdelayView = (Spinner)findViewById(R.id.measurement_settings_updelay);
        mSaveButton = (Button)findViewById(R.id.measurement_settings_save);
        mSaveButton.setOnClickListener(mSaveButtonListener);
        String deviceString = getString(R.string.measurement_settings_deviceid,mDeviceId);
        mTextViewDevId.setText(deviceString);
        mProgressDialog = new ProgressDialog(MeasurementSettingsActivity.this);
        new GetMeasuremInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mPicUpStateView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPicUpStateView.setBackgroundResource(isChecked ? R.drawable.switcher_on : R.drawable.switcher_off);
            }
        });
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new QueryLocationTask().execute();
                Intent intent = new Intent(MeasurementSettingsActivity.this,LocationModeSourceActivity.class);
                //startActivity(intent);
                startActivityForResult(intent,1000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1001){
            String ret = data.getStringExtra("result");
            mLoactionView.setText(ret);
        }
    }

    View.OnClickListener mSaveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String payid= mPayIdView.getText().toString().trim();
            String nickname = mNickNameView.getText().toString().trim();
            int uplstate = mPicUpStateView.isChecked()?1:0;
            String location = mLoactionView.getText().toString().trim();
            int upldelay = getUpdelayDay(mUpdelayView.getSelectedItemPosition());
            Bundle params = new Bundle();

            Log.d(TAG,"payid="+payid);
            Log.d(TAG,"nickname="+nickname);
            Log.d(TAG,"uplstate="+uplstate);
            Log.d(TAG,"location="+location);
            Log.d(TAG,"upldelay="+upldelay);

            if(!payid.isEmpty()&&!payid.equals(mMeasurementInfo.getPayId())){
                params.putString(ELookServiceImpl.HTTP_PARAMS_PAY_ID, payid);
            }
            if(!nickname.isEmpty()&&!nickname.equals(mMeasurementInfo.getAlisaName())){
                params.putString(ELookServiceImpl.HTTP_PARAMS_ALIAS, nickname);
            }
            if(uplstate!=mMeasurementInfo.getUplState()){
                params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_UPLSTATE, uplstate+"");
            }
            if(upldelay!=mMeasurementInfo.getUpdelay()){
                params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_DELAY, upldelay+"");
            }
            if(!location.isEmpty()&&!location.equals(mMeasurementInfo.getLocation())){
                params.putString(ELookServiceImpl.HTTP_PARAMS_LOCATION, location);
            }
            Log.d(TAG,"params="+params.toString());
            if(params.size()==0){
                mHandler.sendEmptyMessage(MSG_SHOW_CONFIG_FAIL_DIALOG);
            }else{
                params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
                showProgressDialog();
                final Bundle mparams = params;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ELServiceHelper helper = ELServiceHelper.get();
                        boolean ret = helper.setDeviceConfig(mDeviceId, mparams);
                        dismissProgressDialog();
                        if(ret){
                            Log.d(TAG,"SET SECUESSFUL!");
                            mHandler.sendEmptyMessage(MSG_SHOW_CONFIG_SUCCESS_DIALOG);
                        }else{
                            Log.d(TAG,"SET FAILED!");
                            mHandler.sendEmptyMessage(MSG_SHOW_CONFIG_FAIL_DIALOG);
                        }
                    }
                }).start();
            }

        }
    };

    class QueryLocationTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
            LocationWrapper mLocationWrapper = new LocationWrapper(MeasurementSettingsActivity.this);
            mLocationWrapper.queryCurrentLocation(mLocationListener);
            return null;
        }

    }

    LocationWrapper.OnLocationChangedListener mLocationListener = new LocationWrapper.OnLocationChangedListener() {
        @Override
        public void onLocationChanged(String province,
                                      String city,
                                      String township,
                                      String street,
                                      String streetNumber,
                                      String building,
                                      double latitude,
                                      double longitude) {
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);

            if(building!=null){
                mLoactionView.setText(building);
            } else {
                //Toast.makeText(MeasurementSettingsActivity.this, "Cannot locate", Toast.LENGTH_LONG).show();
            }
            PoiAroundSearch pserch = new PoiAroundSearch(MeasurementSettingsActivity.this,latitude,longitude);
            pserch.doSearchQuery();
        }

    };

    private static final int MSG_SHOW_CONFIG_SUCCESS_DIALOG = 0;
    private static final int MSG_SHOW_CONFIG_FAIL_DIALOG = 1;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 2;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 3;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_SHOW_CONFIG_SUCCESS_DIALOG:
                    showSucessDialog(true);
                    break;
                case MSG_SHOW_CONFIG_FAIL_DIALOG:
                    showSucessDialog(false);
                    break;
                case MSG_SHOW_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                        mProgressDialog.setMessage(getString(R.string.refreshing));
                        mProgressDialog.show();
                    }
                }
                break;
                case MSG_DIMISS_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }

            }
        }
    };
    public void startRemind(View v){
        //Intent intent = new Intent(MeasurementSettingsActivity.this, NewRemindActivity.class);
        //startActivity(intent);
    }

    public void back(View v){
        finish();
    }

    private void showProgressDialog() {
        if (mProgressDialog != null){
            Log.d(TAG, "show Dialog");
            mProgressDialog.setMessage(getString(R.string.refreshing));
            mProgressDialog.show();
        }

    }

    private void showSucessDialog(boolean result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementSettingsActivity.this);
        if(result) {
            builder.setMessage(getString(R.string.config_success));
        }else{
            builder.setMessage(getString(R.string.config_failed));
        }
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
         }
        });
        builder.create().show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            Log.d(TAG, "dismiss Dialog");
            mProgressDialog.dismiss();
        }
    }

    public int getUpdelayType(int delay){
        int type = -1;
        if(delay==UPDELAY_TYPE_DAY_1){
            type = 0;
        }else if(delay==UPDELAY_TYPE_DAY_2){
            type = 1;
        }else if(delay==UPDELAY_TYPE_DAY_3){
            type = 2;
        }
        return type;
    }

    public int getUpdelayDay(int type){
        int day = -1;
        if(type==0){
            type = UPDELAY_TYPE_DAY_1;
        }else if(type==1){
            type = UPDELAY_TYPE_DAY_2;
        }else if(type==2){
            type = UPDELAY_TYPE_DAY_3;
        }
        return type;
    }

    private class GetMeasuremInfoTask extends AsyncTask<Void, Void, MeasurementInfo> {
        @Override
        protected MeasurementInfo doInBackground(Void... params) {
            mDatabaseHelper = ELookDatabaseHelper.newInstance(MeasurementSettingsActivity.this);
            MeasurementInfo info = mDatabaseHelper.getMeasurementInfo(mDeviceId);
            return info;
        }

        @Override
        protected void onPostExecute(MeasurementInfo info) {
            dismissProgressDialog();
            if(info == null){
                return;
            }
            mMeasurementInfo = info;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String tmptime= format.format(info.getRegTimestamp());
            String bindtime=getString(R.string.measurement_settings_bind_time, tmptime);
            Log.d(TAG,"bindtime:"+bindtime);
            mTextViewBindTime.setText(bindtime);
            Log.d(TAG, "alisaname:" + info.getAlisaName());
            if(info.getAlisaName()!=null){
                mNickNameView.setText(info.getAlisaName());
            }
            if(info.getPayId()!=null){
                mPayIdView.setText(info.getPayId());
            }
            if(info.getLocation()!=null){
                mLoactionView.setText(info.getLocation());
            }
            int type = getUpdelayType(info.getUpdelay());
            if(type == -1){

            }else{
                mUpdelayView.setSelection(type);
            }
            if(info.getUplState()==1){
                mPicUpStateView.setChecked(true);
            }else{
                mPicUpStateView.setChecked(false);
            }
        }
    }
}
