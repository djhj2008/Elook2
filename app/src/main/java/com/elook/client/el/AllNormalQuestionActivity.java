package com.elook.client.el;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.ProblemMsg;
import com.elook.client.user.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xy on 7/16/16.
 */
public class AllNormalQuestionActivity extends Activity {
    NormalQuestionAdapter mAdapter;
    ELookDatabaseHelper mDatabaseHelper;
    ELookApplication app;
    Context mContext;
    ListView mProblemMsgListView;
    int mUserId;
    ProgressDialog m_pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_questions);
        app = (ELookApplication)getApplication();
        UserInfo info= app.getUserInfo();
        mContext = (Context)AllNormalQuestionActivity.this;
        //int uid = mDatabaseHelper.getActiveUserInfo().getUserId();
        mUserId = info.getUserId();
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        mProblemMsgListView = (ListView)findViewById(R.id.all_normal_questions);
        new DowloadMsgTask().execute();
    }

    private static final int MSG_GET_MESSAGE_BODY_FINISH = 0;
    private static final int MSG_GET_MESSAGE_BODY_START = 1;

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_GET_MESSAGE_BODY_START:
                    //创建ProgressDialog对象
                    m_pDialog = new ProgressDialog(AllNormalQuestionActivity.this);
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

        @Override
        protected Void doInBackground(Void... params) {
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_BODY_START);
            ELServiceHelper instanceHelper = ELServiceHelper.get();
            instanceHelper.fetchProblemMessage(mUserId);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //dismissProgressDialog();
            List<ProblemMsg> msgs = new ArrayList<>();
            msgs = mDatabaseHelper.getProblemMessages();
            if(msgs.size()>0) {
                if(mAdapter==null){
                    mAdapter = new NormalQuestionAdapter(msgs);
                    mProblemMsgListView.setAdapter(mAdapter);
                }
            }
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_BODY_FINISH);
        }
    }

    private class NormalQuestionAdapter extends BaseAdapter {

        List<ProblemMsg> mProblemMsgs = new ArrayList<>();
        public NormalQuestionAdapter() { }

        public NormalQuestionAdapter(List<ProblemMsg> msgs) {
            this.mProblemMsgs = msgs;
        }

        public void setProblemMsgs(List<ProblemMsg> msgs) {
            this.mProblemMsgs.clear();
            this.mProblemMsgs = msgs;
        }

        @Override
        public int getCount() {
            return mProblemMsgs.size();
        }

        @Override
        public ProblemMsg getItem(int position) {
            return mProblemMsgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HolderView view = null;
            ProblemMsg msg = mProblemMsgs.get(position);

            if (convertView == null) {
                view = new HolderView();
                convertView = LayoutInflater.from(AllNormalQuestionActivity.this).inflate(R.layout.normal_quession_item, null);
                view.mNQTitleView = (TextView) convertView.findViewById(R.id.normal_questions_item_title);
                view.mNAskView = (TextView)convertView.findViewById(R.id.normal_questions_item_ask);
                convertView.setTag(view);
            } else {
                view = (HolderView) convertView.getTag();
            }
            view.mNQTitleView.setText(" Q"+(position+1)+" "+msg.getProblemMsgTitle());
            view.mNAskView.setText(msg.getProblemMsgBody());
            return convertView;
        }

        private class HolderView {
            TextView mNQTitleView;
            TextView mNAskView;
        }
    }
    public void back(View v) {
        finish();
    }
}
