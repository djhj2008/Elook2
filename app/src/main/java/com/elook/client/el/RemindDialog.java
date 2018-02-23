package com.elook.client.el;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.service.LoadDataFromServer;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by haiming on 5/13/16.
 */
public class RemindDialog extends Dialog {
    private static final String TAG = "DateFlowDialog";
    private LinearLayout mRootView;
    EditText mThredHoldEditText;
    //EditText mDeviceUpdateDelayEditText;
    Button mSubmitConfigButton;
    Spinner mDeviceDateInterval;

    private Context mContext;
    private int mDeviceId;
    private MeasurementInfo mMeasurementInfo;
    String[] mCyclesPeriod = null;
    private int day = 0;

    private ProgressDialog mProgressDialog;

    public RemindDialog(Context context, int devId) {
        super(context);
        this.mContext = context;
        this.mDeviceId = devId;
        mProgressDialog = new ProgressDialog(context);
        mCyclesPeriod = context.getResources().getStringArray(R.array.warning_cycle);
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

        mRootView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_remind_setup, null);
        mThredHoldEditText = (EditText) mRootView.findViewById(R.id.device_usage_thredhold);
        mDeviceDateInterval = (Spinner) mRootView.findViewById(R.id.spinner2);

        mSubmitConfigButton = (Button)mRootView.findViewById(R.id.submit_device_config);
        if(mMeasurementInfo != null){
            mThredHoldEditText.setText(mMeasurementInfo.getDeviceFlow()+"");
            mDeviceDateInterval.setSelection(mMeasurementInfo.getDateInterval());
        }
        setTitle(mContext.getString(R.string.dialog_title));

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
            int flow = 1, datainterval = 0;
            boolean state = false;

            datainterval = mDeviceDateInterval.getSelectedItemPosition();
            //updateDelay = Integer.parseInt(mDeviceUpdateDelayEditText.getText().toString().trim());
            String tmp = mThredHoldEditText.getText().toString().trim();
            if (tmp.isEmpty()) {
                flow = 1;
            } else {
                try {
                    flow = Integer.parseInt(mThredHoldEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "It is not number");
                    flow = 1;
                }
            }


            if ( (mMeasurementInfo.getDeviceFlow() == flow) &&
                  (mMeasurementInfo.getDateInterval() == datainterval)) {
                //PASS
                dismiss();
            } else {
                final int tempFlow = flow;
                final int tempDateInterval = datainterval;
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle params = new Bundle();

                        if(tempFlow != mMeasurementInfo.getDeviceFlow() ||
                                tempDateInterval != mMeasurementInfo.getDateInterval()){
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_FLOW, tempFlow+"");
                            params.putString(ELookServiceImpl.HTTP_PARAMS_DEV_INTERNAL, tempDateInterval+"");
                        }

                        if(params.size() > 0){
                            ELServiceHelper helper = ELServiceHelper.get();
                            boolean ret = helper.setDeviceDateFlow(mDeviceId, params);
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
            mThredHoldEditText.setText(mMeasurementInfo.getDeviceFlow()+"");
            mDeviceDateInterval.setSelection(mMeasurementInfo.getDateInterval());
        }
    }
}
