package com.elook.client.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.el.AllPushMsgActivity;
import com.elook.client.el.AllRecordsActivity;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.MeasurementListService;
import com.elook.client.ui.BadgeView;
import com.elook.client.user.MeasurementCountData;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.PushMessage;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.ELUtils;

import java.util.List;

/**
 * Created by haiming on 5/24/16.
 */
public class NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";
    private static final boolean DEBUG = true;

    private static final int SYSTEM_MSG_TYPE = 1;
    private static final int PROPERTY_MSG_TYPE = 2;
    private static final int CONMMUNITY_MSG_TYPE = 3;
    private static final int ALARM_MSG_TYPE = 4;

    private Context mContext;
    View mRootView;
    ELookApplication app;
    ELookDatabaseHelper mDatabaseHelper;
    private boolean isInitDataed=false;
    TextView notification_title_sub_TV;
    TextView system_msg_sub_TV;
    TextView property_msg_sub_TV;
    TextView conmmunity_msg_sub_TV;
    TextView alarm_msg_sub_TV;
    TextView system_msg_count_TV;
    TextView property_msg_count_TV;
    TextView conmmunity_msg_count_TV;
    TextView alarm_msg_count_TV;
    int unreadmsg_system = 0;
    int unreadmsg_property = 0;
    int unreadmsg_conmmunity = 0;
    int unreadmsg_alarm = 0;
    BadgeView number_systemV;
    BadgeView number_propertyV;
    BadgeView number_conmmunityV;
    BadgeView number_alarmV;
    private Dialog mProgressDialog = null;
    ImageView system_msg_IV;
    ImageView property_msg_IV;
    ImageView conmmunity_msg_IV;
    ImageView alarm_msg_IV;
    int userId=0;
    String mLocation = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_notification, container, false);
        app = (ELookApplication)getActivity().getApplication();
        UserInfo info= app.getUserInfo();
        userId = info.getUserId();
        mContext = (Context) getActivity();
        notification_title_sub_TV = (TextView)mRootView.findViewById(R.id.notification_action_bar_sub_title);
        system_msg_sub_TV = (TextView)mRootView.findViewById(R.id.system_msg_sub_text);
        property_msg_sub_TV = (TextView)mRootView.findViewById(R.id.property_msg_sub_text);
        conmmunity_msg_sub_TV = (TextView)mRootView.findViewById(R.id.conmmunity_msg_sub_text);
        alarm_msg_sub_TV = (TextView)mRootView.findViewById(R.id.alarm_msg_sub_text);

        system_msg_count_TV = (TextView)mRootView.findViewById(R.id.system_msg_count);
        property_msg_count_TV = (TextView)mRootView.findViewById(R.id.property_msg_count);
        conmmunity_msg_count_TV = (TextView)mRootView.findViewById(R.id.conmmunity_msg_count);
        alarm_msg_count_TV = (TextView)mRootView.findViewById(R.id.alarm_msg_count);

        number_systemV = new BadgeView(mContext,system_msg_count_TV);
        number_propertyV = new BadgeView(mContext,property_msg_count_TV);
        number_conmmunityV = new BadgeView(mContext,conmmunity_msg_count_TV);
        number_alarmV = new BadgeView(mContext,alarm_msg_count_TV);

        system_msg_IV = (ImageView)mRootView.findViewById(R.id.system_msg_button);
        property_msg_IV = (ImageView)mRootView.findViewById(R.id.property_msg_button);
        conmmunity_msg_IV = (ImageView)mRootView.findViewById(R.id.conmmunity_msg_button);
        alarm_msg_IV = (ImageView)mRootView.findViewById(R.id.alarm_msg_button);

        system_msg_IV.setOnClickListener(clickhandler);
        property_msg_IV.setOnClickListener(clickhandler);
        conmmunity_msg_IV.setOnClickListener(clickhandler);
        alarm_msg_IV.setOnClickListener(clickhandler);
        Log.d(TAG, "onCreateView: isInitDataed:" + isInitDataed);
        Log.d(TAG, "onCreateView: getUserVisibleHint():" + getUserVisibleHint());
        if (getUserVisibleHint()&&!isInitDataed) {
            mGetMsgTask = new GetMsgTask();
            mGetMsgTask.execute();
            isInitDataed=true;
        }

        mLocation = app.getLocation();
        if(mLocation==null) {
            info = app.getUserInfo();
            if(info!=null)
            notification_title_sub_TV.setText(info.getAddress());
        }else{
            notification_title_sub_TV.setText(mLocation);
        }
        return mRootView;
    }

    private void cleanMem(){
        Log.d(TAG,"cleanMem");
        mGetMsgTask=null;
        mDatabaseHelper=null;
        //mRootView=null;
        app=null;
    }

    public void setLocation(String location){
        mLocation = location;
        if(notification_title_sub_TV!=null)
        notification_title_sub_TV.setText(mLocation);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Resume :"+getUserVisibleHint());
        if(getUserVisibleHint()) {
            updateScreen();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        cleanMem();
        super.onDestroyView();
    }

    View.OnClickListener clickhandler = new View.OnClickListener() {
        public void onClick(View v) {
            int type = 0;
            switch (v.getId()) {
                case R.id.system_msg_button:
                    type = SYSTEM_MSG_TYPE;
                    Log.d(TAG, " type=" + SYSTEM_MSG_TYPE);
                    break;
                case R.id.property_msg_button:
                    type = PROPERTY_MSG_TYPE;
                    Log.d(TAG, " type=" + PROPERTY_MSG_TYPE);
                    break;
                case R.id.conmmunity_msg_button:
                    type = CONMMUNITY_MSG_TYPE;
                    Log.d(TAG, " type=" + CONMMUNITY_MSG_TYPE);
                    break;
                case R.id.alarm_msg_button:
                    type = ALARM_MSG_TYPE;
                    Log.d(TAG, " type=" + ALARM_MSG_TYPE);
                    break;
            }
            Intent intent = new Intent(mContext, AllPushMsgActivity.class);
            intent.putExtra("msgtype", type);
            startActivity(intent);
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG,"setUserVisibleHint: isInitDataed:"+isInitDataed);
        Log.d(TAG,"setUserVisibleHint: getUserVisibleHint():" + getUserVisibleHint());
        if (getUserVisibleHint()) {
            if (!isInitDataed ) {
                mGetMsgTask = new GetMsgTask();
                mGetMsgTask.execute();
                isInitDataed=true;
            }
        }else{
            isInitDataed = false;
        }
    }

    private GetMsgTask mGetMsgTask;

    private class GetMsgTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATED_TIME_OUT,UPDATED_TIME_OUT);
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
            ELServiceHelper helper = ELServiceHelper.get();
            helper.fetchPushMessage(userId);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            updateScreen();
            mHandler.removeMessages(MSG_UPDATED_TIME_OUT);
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
        }
    }

    public  void updateScreen(){
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        List<PushMessage> tmpmsg = mDatabaseHelper.getMessages(userId);
        Log.d(TAG, "pushsmg :" + tmpmsg.toString());
        if(tmpmsg.size()==0)
            return;
        system_msg_sub_TV.setText(getPushMsg(tmpmsg, SYSTEM_MSG_TYPE));
        property_msg_sub_TV.setText(getPushMsg(tmpmsg, PROPERTY_MSG_TYPE));
        conmmunity_msg_sub_TV.setText(getPushMsg(tmpmsg, CONMMUNITY_MSG_TYPE));
        alarm_msg_sub_TV.setText(getPushMsg(tmpmsg, ALARM_MSG_TYPE));
        unreadmsg_system = getUnReadMsg(tmpmsg, SYSTEM_MSG_TYPE);
        unreadmsg_property = getUnReadMsg(tmpmsg, PROPERTY_MSG_TYPE);
        unreadmsg_conmmunity = getUnReadMsg(tmpmsg, CONMMUNITY_MSG_TYPE);
        unreadmsg_alarm = getUnReadMsg(tmpmsg, ALARM_MSG_TYPE);

        if(unreadmsg_system > 0) {
            number_systemV.setText(unreadmsg_system+"");
            number_systemV.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            number_systemV.show();
        }else{
            number_systemV.hide();
        }
        if(unreadmsg_property > 0) {
            number_propertyV.setText(unreadmsg_property+"");
            number_propertyV.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            number_propertyV.show();
        }else{
            number_propertyV.hide();
        }
        if(unreadmsg_conmmunity > 0) {
            number_conmmunityV.setText(unreadmsg_conmmunity+"");
            number_conmmunityV.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            number_conmmunityV.show();
        }else{
            number_conmmunityV.hide();
        }
        if(unreadmsg_alarm > 0) {
            number_alarmV.setText(unreadmsg_alarm + "");
            number_alarmV.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            number_alarmV.show();
        }else{
            number_alarmV.hide();
        }
    }


    public String getPushMsg(List<PushMessage> msg ,int type){
        for(int i=0;i<msg.size();i++){
            if(msg.get(i).getPushMsgType()== type)
                return msg.get(i).getPushMsgTitle();
        }
        return null;
    }

    public int getUnReadMsg(List<PushMessage> msg ,int type){
        int count = 0;
        for(int i=0;i<msg.size();i++){
            PushMessage tmpmsg = msg.get(i);
            if(tmpmsg.getPushMsgType()== type)
                if(tmpmsg.getState()==0){
                    count++;
                }
        }
        return count;
    }

    private static final int MSG_SHOW_PROGRESS_DIALOG = 3002;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 3003;
    private static final int MSG_UPDATED_TIME_OUT = 2;
    private static final int UPDATED_TIME_OUT = 10000;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_SHOW_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog=null;
                    }
                    mProgressDialog = ELUtils.createLoadingDialog(mContext, "");
                    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                }
                break;
                case MSG_DIMISS_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog=null;
                    }
                }
                break;
                case MSG_UPDATED_TIME_OUT: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog=null;
                    }
                }
            }
        }
    };
}
