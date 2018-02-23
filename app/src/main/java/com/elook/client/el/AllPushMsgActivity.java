package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.fragments.NotificationFragment;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.ui.PullToRefreshListView;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.user.PushMessage;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xy on 7/12/16.
 */
public class AllPushMsgActivity extends Activity {
    private static final String TAG = "AllPushMsgActivity";
    PushMsgAdapter mAdapter;
    ELookDatabaseHelper mDatabaseHelper;
    ELookApplication app;
    Context mContext;
    ListView mPushMsgsListView;
    int msgtype;
    AlertDialog mMsgBodyDialog;
    ProgressDialog m_pDialog;
    private static final int SYSTEM_MSG_TYPE = 1;
    private static final int PROPERTY_MSG_TYPE = 2;
    private static final int CONMMUNITY_MSG_TYPE = 3;
    private static final int ALARM_MSG_TYPE = 4;
    int magbody_title[]={
        R.string.notification,
        R.string.notification_system,
        R.string.notification_property,
        R.string.notification_conmmunity,
        R.string.notification_alarm
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pushmsgs);
        mAdapter = new PushMsgAdapter();
        app = (ELookApplication)getApplication();
        UserInfo info= app.getUserInfo();
        mContext = (Context)AllPushMsgActivity.this;
        //int uid = mDatabaseHelper.getActiveUserInfo().getUserId();
        int uid = info.getUserId();
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        List<PushMessage> tmpmsg = mDatabaseHelper.getMessages(uid);
        List<PushMessage> msgs = new ArrayList<>();
        msgtype = getIntent().getIntExtra("msgtype",0);
        for(int i=0; i<tmpmsg.size();i++) {
            PushMessage m = tmpmsg.get(i);
            Log.d(TAG,"id="+m.getPushMsgId()+" type="+m.getPushMsgType());
            if(m.getPushMsgType()==msgtype){
                msgs.add(m);
            }
        }
        if(msgs.size()>0) {
            mAdapter.setPushMsgs(msgs);
            mPushMsgsListView = (ListView) findViewById(R.id.all_push_msgs);
            mPushMsgsListView.setAdapter(mAdapter);
        }
    }

    private class ItemClickedListener implements View.OnClickListener {
        PushMessage mPushMsg;

        public ItemClickedListener(PushMessage msg) {
            this.mPushMsg = msg;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"onClick id:"+mPushMsg.getPushMsgId());
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_BODY_START);
            DowloadMsgTask t = new DowloadMsgTask(mPushMsg);
            t.execute();
            }
    }

    private static final int MSG_GET_MESSAGE_BODY_FINISH = 0;
    private static final int MSG_GET_MESSAGE_BODY_START = 1;

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_GET_MESSAGE_BODY_START:
                    //创建ProgressDialog对象
                    m_pDialog = new ProgressDialog(AllPushMsgActivity.this);
                    // 设置进度条风格，风格为圆形，旋转的
                    m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    // 设置ProgressDialog 提示信息
                    m_pDialog.setMessage(getString(R.string.notification_process_str));
                    // 设置ProgressDialog 的进度条是否不明确
                    m_pDialog.setIndeterminate(false);
                    // 设置ProgressDialog 是否可以按退回按键取消
                    m_pDialog.setCancelable(false);
                    m_pDialog.show();
                    break;
                case MSG_GET_MESSAGE_BODY_FINISH:
                    m_pDialog.hide();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private class DowloadMsgTask extends AsyncTask<Void, Void, Void> {
        PushMessage mPushMsg;

        public DowloadMsgTask(PushMessage msg) {
            this.mPushMsg = msg;
        }

        @Override
        protected Void doInBackground(Void... params) {
            int uid = mPushMsg.getPushMsgId();
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            instanceHelper.fetchSiglePushMessage(uid);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //dismissProgressDialog();
            PushMessage msg = mDatabaseHelper.getMessage(mPushMsg.getPushMsgId());
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_BODY_FINISH);
            mMsgBodyDialog = new PreviewMsgDialog(AllPushMsgActivity.this,msg);
            mMsgBodyDialog.show();
        }
    }

    class PreviewMsgDialog extends AlertDialog {
        TextView mPushMsgTV;
        TextView mPushMsgBodyTitleTV;
        View mRootView = null;
        Context mContext;
        PushMessage mPushMsg;

        public PreviewMsgDialog(Context context, PushMessage msg) {
            super(context);
            this.mContext = context;
            this.mPushMsg = msg ;
        }

        @Override
        public void show() {
            super.show();
            LayoutInflater li = LayoutInflater.from(mContext);
            mRootView = li.inflate(R.layout.dialog_preview_pushmsg, null);
            mPushMsgTV = (TextView) mRootView.findViewById(R.id.pushmsg_body_text);
            mPushMsgBodyTitleTV = (TextView) mRootView.findViewById(R.id.pushmsg_body_title);
            mPushMsgBodyTitleTV.setText(magbody_title[mPushMsg.getPushMsgType()]);
            mPushMsgTV.setText(mPushMsg.getPushMsgBody());
            setContentView(mRootView);
        }
    }

    public void back(View v) {
        finish();
    }

    public void back_dialog(View v) {
        mMsgBodyDialog.dismiss();
    }

    private class PushMsgAdapter extends BaseAdapter {

        List<PushMessage> mPushMessages = new ArrayList<>();
        public PushMsgAdapter() { }

        public PushMsgAdapter(List<PushMessage> msgs) {
            this.mPushMessages = msgs;
        }

        public void setPushMsgs(List<PushMessage> msgs) {
            this.mPushMessages.clear();
            this.mPushMessages = msgs;
        }

        @Override
        public int getCount() {
            return mPushMessages.size();
        }

        @Override
        public PushMessage getItem(int position) {
            return mPushMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HolderView view = null;
            PushMessage msg = mPushMessages.get(position);

            if (convertView == null) {
                view = new HolderView();
                convertView = LayoutInflater.from(AllPushMsgActivity.this).inflate(R.layout.pushmsg_item, null);
                view.mDateTimeTextView = (TextView) convertView.findViewById(R.id.pushmsg_item_time);
                view.mTextView = (TextView) convertView.findViewById(R.id.pushmsg_item_text);
                view.mTitleTextView = (TextView)convertView.findViewById(R.id.pushmsg_item_title);
                view.mRecordPicImageView = (ImageView) convertView.findViewById(R.id.record_pic);
                convertView.setTag(view);
            } else {
                view = (HolderView) convertView.getTag();
            }
            int type = msg.getPushMsgType();
            if(type <= ALARM_MSG_TYPE&&type >= 0){
                view.mTextView.setText(magbody_title[type]);
            }
            view.mDateTimeTextView.setText(msg.getPushMsgTimestamp());
            view.mTitleTextView.setText(msg.getPushMsgTitle());
            convertView.setOnClickListener(new ItemClickedListener(msg));
            return convertView;
        }

        private class HolderView {
            TextView mTextView;
            TextView mDateTimeTextView;
            TextView mTitleTextView;
            ImageView mRecordPicImageView;
        }
    }
}
