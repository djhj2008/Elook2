package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.utils.Constant;

import java.io.File;

/**
 * Created by guoguo on 4/26/2016.
 */
public class InitializeDeviceActivity extends Activity {
    private static final String TAG = "InitDeviceActivity";
    private static final String IMAGES = "IMAGE";
    private static final boolean DEBUG_SKIP_INITIALIZED = false;
    LinearLayout mInitializeDeviceContainer;
    View mRootView;
    InitializedDevice mInitDevTool;
    int mDeviceId;
    ImageView mInitDevImageView;
    TextView mInitDevTextView;
    ProgressDialog mProgressDialog;
    ELookDatabaseHelper mDatabaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);
        mInitializeDeviceContainer = (LinearLayout)findViewById(R.id.initialize_device_container);
        mDeviceId = getIntent().getIntExtra("deviceid", 0);
        int state = getIntent().getIntExtra("state",0);
        mInitDevTool = new InitializedDevice(this, mDeviceId,state);
        mRootView = mInitDevTool.getInitDeviceMainView();
        mInitDevImageView = (ImageView)mRootView.findViewById(R.id.init_dev_layer);
        mInitDevTextView = (TextView)mRootView.findViewById(R.id.init_dev_text);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;

        mInitializeDeviceContainer.addView(mRootView, params);

        mDatabaseHelper = ELookDatabaseHelper.newInstance(this);

        mInitDevTool.setInitializedListener(new InitializedDevice.OnInitializedDeviceListener() {
            @Override
            public void onInitializedDevice(int initializeStage, boolean isSuccessfully) {
                if(initializeStage == InitializedDevice.MSG_FINISHED_VERIFY_INITIALIZED_STATE
                        && isSuccessfully){
                    Log.d(TAG, "initialized Successfully");
                    showSucessDialog(true);
                }else if(initializeStage == InitializedDevice.MSG_FINISHED_VERIFY_ERROR_STATE
                        && isSuccessfully){
                    mInitDevTextView.setText(R.string.device_not_existed);
                    mInitDevTool.restart();
                }else if(initializeStage == InitializedDevice.MSG_FINISHED_VERIFY_INITIALIZED_STATE
                        && !isSuccessfully){
                    Log.d(TAG, "initialized Failed");
                    String newPicUrl="null";
                    showProgressDialog();
                    new DowloadImageTask().execute(newPicUrl);
                }
                else if(initializeStage == InitializedDevice.MSG_FINISHED_CONFIRM_ERROR_STATE
                        && !isSuccessfully){
                    Log.d(TAG, "initialized Failed");
                    String newPicUrl="null";
                    showProgressDialog();
                    new DowloadImageTask2().execute(newPicUrl);
                }else if (initializeStage == InitializedDevice.MSG_FINISHED_CONNECT_DEV){
                    showFailDialog(getString(R.string.device_state_dev_ap_error));
                }else if (initializeStage == InitializedDevice.MSG_TIMEOUT){
                    showFailDialog(getString(R.string.device_state_dev_timeout));
                }else if (initializeStage == InitializedDevice.MSG_FINISHED_RECONNECT_WIFI
                        ||initializeStage ==InitializedDevice.MSG_FINISHED_CONNECT_WIFI){
                    showFailDialog(getString(R.string.device_state_dev_wifi_error));
                }
            }
        });

    }

    private void updateMeasurementInfosInBackground() {
        ELServiceHelper helper = ELServiceHelper.get();
        helper.getDeviceInfo(mDeviceId);
        //mHandler.sendEmptyMessage(MSG_UPDATED_MEASUREMENT_INFOS);
    }

    class PreviewImageDialog extends AlertDialog {
        Drawable mImageDrawable = null;
        ImageView mImageView;
        TextView mRecordValueTV;
        View mRootView = null;
        Context mContext;
        int value = 0;
        String result;

        public PreviewImageDialog(Context context, int resId) {
            super(context);
            this.mContext = context;
            mImageDrawable = context.getResources().getDrawable(resId);
        }

        public PreviewImageDialog(Context context, String imagePath,String ret) {
            super(context);
            this.mContext = context;
            this.mImageDrawable = Drawable.createFromPath(imagePath);
            this.value = 0;
            this.result = ret;
        }

        public PreviewImageDialog(Context context, String imagePath ,String ret ,int value) {
            super(context);
            this.mContext = context;
            this.mImageDrawable = Drawable.createFromPath(imagePath);
            this.value = value;
            this.result = ret;
        }

        @Override
        public void show() {
            super.show();
            LayoutInflater li = LayoutInflater.from(mContext);
            mRootView = li.inflate(R.layout.dialog_preview_image, null);
            mImageView = (ImageView) mRootView.findViewById(R.id.preview_big_pic);
            if (this.mImageDrawable != null) {
                mImageView.setImageDrawable(this.mImageDrawable);
            }
            mRecordValueTV = (TextView) mRootView.findViewById(R.id.record_value);
            mRecordValueTV.setText(result);
            setContentView(mRootView);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog != null) {
            Log.d(TAG, "show Dialog");
            mProgressDialog.setMessage(getString(R.string.refreshing));
            mProgressDialog.show();
        }

    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            Log.d(TAG, "dismiss Dialog");
            mProgressDialog.dismiss();
        }
    }


    private class DowloadImageTask extends AsyncTask<String, Void, String> {
        MeasurementRecord mRecord;
        public DowloadImageTask() {

        }

        @Override
        protected String doInBackground(String... params) {
            updateMeasurementInfosInBackground();
            MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
            String newPicUrl = mMeasurementInfo.getDeviceUrlErrorPic();
            Log.d(TAG,"DowloadImageTask newPicUrl="+newPicUrl);
            final String targetLocalPath = getFilesDir().getAbsolutePath() +
                    "/" + IMAGES + "/" + newPicUrl;
            File imageFile = new File(targetLocalPath);
            File parentFile = imageFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!imageFile.exists()) {
                final String imgUrlInServer = Constant.URL_HEADER + "/" +newPicUrl;
                ELServiceHelper instanceHelper = ELServiceHelper.get();
                instanceHelper.loadImageFromeServer(imgUrlInServer, targetLocalPath);
            }
            return targetLocalPath;
        }

        @Override
        protected void onPostExecute(String targetLocalPath) {
            dismissProgressDialog();
            String ret = getString(R.string.init_device_cannot_parse_str);
            AlertDialog dialog = new PreviewImageDialog( InitializeDeviceActivity.this, targetLocalPath,ret);
            dialog.show();
        }
    }

    private class DowloadImageTask2 extends AsyncTask<String, Void, String> {
        MeasurementInfo mMeasurementInfo;
        public DowloadImageTask2() {

        }

        @Override
        protected String doInBackground(String... params) {
            updateMeasurementInfosInBackground();
            mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
            String newPicUrl = mMeasurementInfo.getDeviceUrlPic();
            Log.d(TAG,"DowloadImageTask newPicUrl="+newPicUrl);
            final String targetLocalPath = getFilesDir().getAbsolutePath() +
                    "/" + IMAGES + "/" + newPicUrl;
            File imageFile = new File(targetLocalPath);
            File parentFile = imageFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!imageFile.exists()) {
                final String imgUrlInServer = Constant.URL_HEADER + "/" +newPicUrl;
                ELServiceHelper instanceHelper = ELServiceHelper.get();
                instanceHelper.loadImageFromeServer(imgUrlInServer, targetLocalPath);
            }
            return targetLocalPath;
        }

        @Override
        protected void onPostExecute(String targetLocalPath) {
            dismissProgressDialog();
            int value = mMeasurementInfo.getTmpValue();
            String ret = getString(R.string.init_device_parse_error_str,value);
            AlertDialog dialog = new PreviewImageDialog( InitializeDeviceActivity.this, targetLocalPath,ret,value);
            dialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(DEBUG_SKIP_INITIALIZED){
            startConfigDevice();
        }

    }
    private void showFailDialog(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InitializeDeviceActivity.this);

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

    private void showSucessDialog(boolean result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InitializeDeviceActivity.this);
        if(result) {
            builder.setMessage(getString(R.string.device_state_config_success));
        }else{
            builder.setMessage(getString(R.string.config_failed));
        }
        builder.setTitle(R.string.config_title);
        builder.setPositiveButton(R.string.config_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startConfigDevice();
            }
        });

        builder.setNegativeButton(R.string.config_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startConfigDevice();
            }
        });
        builder.create().show();
    }

    private void startConfigDevice(){
        //Intent intent = new Intent(InitializeDeviceActivity.this, ConfigDeviceActivity.class);
        //intent.putExtra("deviceid", mDeviceId);
        //startActivity(intent);
        Log.d("DeviceAdapter", "onClick deviceId = "+mDeviceId );
        Intent intent = new Intent(InitializeDeviceActivity.this, MeasurementSettingsActivity.class);
        intent.putExtra("deviceid",mDeviceId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitDevTool.stopThread();
        mInitDevTool.clean();
        mInitDevTool=null;
    }

    public void back(View v){
        finish();
    }
}
