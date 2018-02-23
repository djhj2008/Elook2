package com.elook.client.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.MainActivity;
import com.elook.client.R;
import com.elook.client.activity.LoginActivity;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.el.AllNormalQuestionActivity;
import com.elook.client.el.ChangePasswordActivity;
import com.elook.client.el.FindPasswordActivity;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.ui.RecordsLayout;
import com.elook.client.user.MeasurementInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by haiming on 5/24/16.
 */
public class MineInfoFragment extends Fragment implements View.OnClickListener{
    final static String TAG = "MineInfoFragment";
    View mRootView;
    Context mContext;
    ImageView mUserPhotoIV;
    TextView mPhoneNumberTV;
    ELookDatabaseHelper mDatabaseHelper;
    LinearLayout mReportAdviseIV, mChangePwdIV, mAboutUsIV, mServicePhoneNumberIV, mNormalQuesionIV;
    RelativeLayout mPhotoWapper;
    Button mLogoutButton;
    String mPhoneNumber;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = (Context)getActivity();
        mRootView = inflater.inflate(R.layout.fragment_mine, container, false);
        initViews();
        return mRootView;
    }

    private void initViews(){
        mPhotoWapper = (RelativeLayout)mRootView.findViewById(R.id.fragment_mine_person_photo_wrapper);
        mUserPhotoIV = (ImageView)mRootView.findViewById(R.id.fragment_mine_person_photo);
        mPhoneNumberTV = (TextView)mRootView.findViewById(R.id.fragment_mine_phone_number);
        mReportAdviseIV = (LinearLayout)mRootView.findViewById(R.id.fragment_mine_report_advise_wrapper);
        mChangePwdIV = (LinearLayout)mRootView.findViewById(R.id.fragment_mine_change_passwd_wrapper);
        mAboutUsIV = (LinearLayout)mRootView.findViewById(R.id.fragment_mine_about_us_wrapper);
        mServicePhoneNumberIV = (LinearLayout)mRootView.findViewById(R.id.fragment_mine_service_phone_wrapper);
        mNormalQuesionIV = (LinearLayout)mRootView.findViewById(R.id.fragment_mine_normal_quession_wrapper);
        mLogoutButton = (Button)mRootView.findViewById(R.id.fragment_mine_logout);

        mReportAdviseIV.setOnClickListener(this);
        mChangePwdIV.setOnClickListener(this);
        mAboutUsIV.setOnClickListener(this);
        mServicePhoneNumberIV.setOnClickListener(this);
        mNormalQuesionIV.setOnClickListener(this);
        mLogoutButton.setOnClickListener(this);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        mPhoneNumber = mDatabaseHelper.getActiveUserInfo().getUserPhoneName();
        mPhoneNumberTV.setText(mPhoneNumber);

        //mReportAdviseIV.setVisibility(View.GONE);
    }

    private void cleanMem(){
        Log.d(TAG,"cleanMem");
        mDatabaseHelper=null;
        //mRootView=null;
    }

    public void recycle(){
        if(mPhotoWapper!=null) {
            BitmapDrawable bd = (BitmapDrawable) mPhotoWapper.getBackground();
            mPhotoWapper.setBackgroundResource(0);
            bd.setCallback(null);
            bd.getBitmap().recycle();
            mPhotoWapper = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanMem();
    }

    private class GetMeasuremInfoTask extends AsyncTask<Void, Void, MeasurementInfo> {
        @Override
        protected MeasurementInfo doInBackground(Void... params) {
            mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
            MeasurementInfo info=null;// = mDatabaseHelper.getMeasurementInfo(mDeviceId);
            return info;
        }

        @Override
        protected void onPostExecute(MeasurementInfo info) {
            if(info == null){
                return;
            }
        }
    }

    public void startFindPassWord(){
        Intent intent = new Intent(mContext, ChangePasswordActivity.class);
        intent.putExtra("phonenumber",mPhoneNumber);
        startActivity(intent);
    }

    public void startAllNormalQuestions(){
        Intent intent = new Intent(mContext, AllNormalQuestionActivity.class);
        startActivity(intent);
    }

    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.center_about_us));
        builder.setTitle(R.string.about_us);
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

    private void showCallNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.center_call_number));
        builder.setTitle(R.string.service_phone_number);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fragment_mine_report_advise_wrapper:
                break;
            case R.id.fragment_mine_change_passwd_wrapper:
                startFindPassWord();
                break;
            case R.id.fragment_mine_about_us_wrapper:
                showAboutUsDialog();
                break;
            case R.id.fragment_mine_service_phone_wrapper:
                showCallNumberDialog();
                break;
            case R.id.fragment_mine_normal_quession_wrapper:
                startAllNormalQuestions();
                break;
            case R.id.fragment_mine_logout:
                ELServiceHelper serviceHelper = ELServiceHelper.get();
                boolean isSuccessfully = serviceHelper.logout();
                if(isSuccessfully){
                    Intent it = new Intent(mContext, LoginActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    getActivity().finish();
                }
                break;
        }
    }
}
