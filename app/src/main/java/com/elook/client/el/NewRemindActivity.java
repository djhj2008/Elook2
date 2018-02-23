package com.elook.client.el;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.user.MeasurementInfo;

/**
 * Created by haiming on 5/27/16.
 */
public class NewRemindActivity extends Activity {
    private int mDeviceId;
    private MeasurementInfo mMeasurementInfo;
    private ProgressDialog mProgressDialog;
    int mHour,mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_remind);
        mDeviceId=getIntent().getIntExtra("deviceid",0);
        mProgressDialog = new ProgressDialog(NewRemindActivity.this);
    }

    public void showDeviceConfig(View v){
        RemindDialog dialog = new RemindDialog(NewRemindActivity.this, mDeviceId);
        dialog.show();
    }

    public void showNotUseNotify(View v){
        NotUseNotifyDialog dialog = new NotUseNotifyDialog(NewRemindActivity.this, mDeviceId);
        dialog.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog != null){
            mProgressDialog.setMessage(getString(R.string.refreshing));
            mProgressDialog.show();
        }

    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.d("NewRemindActivity","mTimeSetListener hourOfDay:"+hourOfDay+" minute"+minute);
                int delay = Integer.parseInt(""+hourOfDay+""+minute);
                showProgressDialog();
                final int tempdelay = delay;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle params = new Bundle();
                        if(tempdelay != mMeasurementInfo.getmUpdelaySub()){
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEVICE_SETACCESS,tempdelay+"");
                        }

                        if(params.size() > 0){
                            ELServiceHelper helper = ELServiceHelper.get();
                            boolean ret = helper.setEngneerDelay(mDeviceId, params);
                        }
                        dismissProgressDialog();
                    }
                }).start();
            }
        };

    public void showEngneerDelay(View v){
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(NewRemindActivity.this);
        mMeasurementInfo = databaseHelper.getMeasurementInfo(mDeviceId);
        int delay = mMeasurementInfo.getmUpdelaySub();
        String str_delay = String.format("%04d", delay);
        Log.d("NewRemindActivity","showEngneerDelay delay:"+str_delay);
        String str_hour = str_delay.substring(0,2);
        String str_min = str_delay.substring(2,4);
        mHour = Integer.parseInt(str_hour);
        mMinute = Integer.parseInt(str_min);
        Log.d("NewRemindActivity","showEngneerDelay hour:"+str_hour+" str_min:"+str_min);
        TimePickerDialog dialog = new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true);
        dialog.setTitle(getString(R.string.remind_engneer_delay_notice));
        dialog.show();
        //EngneerDelayNotifyDialog dialog = new EngneerDelayNotifyDialog(NewRemindActivity.this, mDeviceId);
        //dialog.show();
    }

    public void back(View v){
        finish();
    }
}
