package com.elook.client.el;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.user.MeasurementInfo;

/**
 * Created by haiming on 5/13/16.
 */
public class EngneerDelayNotifyDialog extends Dialog {
    private static final String TAG = "DateFlowDialog";
    private LinearLayout mRootView;
    EditText mThredHoldEditText;
    Button mSubmitConfigButton;

    private Context mContext;
    private int mDeviceId;
    private MeasurementInfo mMeasurementInfo;

    private ProgressDialog mProgressDialog;

    public EngneerDelayNotifyDialog(Context context, int devId) {
        super(context);
        this.mContext = context;
        this.mDeviceId = devId;
        mProgressDialog = new ProgressDialog(context);
        initViews();
    }

    private void startToUpdateMeasurementInfo() {
        showProgressDialog();
        new GetMeasuremInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showProgressDialog() {
        if (mProgressDialog != null){
            Log.d(TAG, "show Dialog");
            mProgressDialog.setMessage(mContext.getString(R.string.refreshing));
            mProgressDialog.show();
        }

    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            Log.d(TAG, "dismiss Dialog");
            mProgressDialog.dismiss();
        }
    }

    private void initViews() {
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(mContext);
        mMeasurementInfo = databaseHelper.getMeasurementInfo(mDeviceId);

        mRootView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.engneerdelay_notify_dialog, null);
        mThredHoldEditText = (EditText) mRootView.findViewById(R.id.device_usage_thredhold);
        //mDeviceUpdateDelayEditText = (EditText)mRootView.findViewById(R.id.device_update_delay);
        mSubmitConfigButton = (Button)mRootView.findViewById(R.id.submit_device_config);
        if(mMeasurementInfo != null){
            mThredHoldEditText.setText(mMeasurementInfo.getmUpdelaySub() + "");
        }
        setTitle(mContext.getString(R.string.remind_engneer_delay_notice));

        mSubmitConfigButton.setOnClickListener(mSetFlowClickListener);

    }

    @Override
    public void show() {
        super.show();
        startToUpdateMeasurementInfo();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        window.setContentView(mRootView);
    }

    private View.OnClickListener mSetFlowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int day = 0, datainterval = 0;
            boolean state = false;

            //updateDelay = Integer.parseInt(mDeviceUpdateDelayEditText.getText().toString().trim());
            String tmp = mThredHoldEditText.getText().toString().trim();
            if (tmp.isEmpty()) {
                day = 0;
            } else {
                try {
                    day = Integer.parseInt(mThredHoldEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "It is not number");
                    day = 0;
                }
            }

            if ( (mMeasurementInfo.getDevNotUseDay() == day)) {
                //PASS
                dismiss();
            } else {
                final int tempday = day;
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle params = new Bundle();
                        if(tempday != mMeasurementInfo.getmUpdelaySub()){
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEVICE_SETACCESS,tempday+"");
                        }

                        if(params.size() > 0){
                            ELServiceHelper helper = ELServiceHelper.get();
                            boolean ret = helper.setEngneerDelay(mDeviceId, params);
                        }
                        dismissProgressDialog();
                        dismiss();
                    }
                }).start();
            }
        }
    };


    private class GetMeasuremInfoTask extends AsyncTask<Void, Void, MeasurementInfo> {
        @Override
        protected MeasurementInfo doInBackground(Void... params) {
            ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(mContext);
            MeasurementInfo info = databaseHelper.getMeasurementInfo(mDeviceId);
            return info;
        }

        @Override
        protected void onPostExecute(MeasurementInfo info) {
            dismissProgressDialog();
            if(info == null){
                Toast.makeText(mContext, "There are errors when update the device info from server", Toast.LENGTH_LONG).show();
                return;
            }
            mMeasurementInfo = info;
            mThredHoldEditText.setText(mMeasurementInfo.getmUpdelaySub() + "");
        }
    }
}
