package com.elook.client.el;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.MeasurementRecordService;
import com.elook.client.ui.PullToRefreshListView;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by haiming on 5/26/16.
 */
public class AllRecordsActivity extends Activity {
    private static final String TAG = "RecordsActivity";
    private static final String IMAGES = "IMAGE";
    private static final String NORMALUP = "normalup";
    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    EditText mStartTimeEditText, mEndTimeEditText;
    PullToRefreshListView mRecordsListView;
    LinearLayout mSearchWrapperLayout;
    ImageView mHideSearWrapperImage;

    int mDeviceId;
    ELookDatabaseHelper mDatabaseHelper;
    MeasurementInfo mMeasurementInfo = null;
    RecordAdapter mAdapter;
    DatePickerDialog mStartDatePickerDialog;
    DatePickerDialog mEndDatePickerDialog;
    String[] mCyclesPeriod = null;

    ProgressDialog mProgressDialog;

    private String mStartTimeStr, mEndTimeStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String tmpStr = getIntent().getStringExtra("deviceid");
        if(tmpStr == null || (tmpStr != null && tmpStr.equals("null")))return;
        mDeviceId = Integer.parseInt(tmpStr);
        setContentView(R.layout.activity_all_records);
        initViews();
    }

    private void initViews() {
        mSearchWrapperLayout = (LinearLayout) findViewById(R.id.search_wrapper);
        mStartTimeEditText = (EditText) findViewById(R.id.start_time);
        mEndTimeEditText = (EditText) findViewById(R.id.end_time);
        mRecordsListView = (PullToRefreshListView) findViewById(R.id.measurement_records);
        mRecordsListView.setOnRefreshListener(mOnRefreshListener);

        mHideSearWrapperImage = (ImageView) findViewById(R.id.hide_search_wrapper);
        mHideSearWrapperImage.setOnClickListener(mPackSearchViewListener);

        mStartTimeEditText.setOnClickListener(mClickListener);
        mEndTimeEditText.setOnClickListener(mClickListener);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = (c.get(Calendar.MONTH) + 1);
        int day = c.get(Calendar.DAY_OF_MONTH);
        StringBuffer sb = new StringBuffer();

        Log.d("doujun","year="+year);
        Log.d("doujun","month="+month);
        Log.d("doujun","day="+day);
        if(month == 1){
            year = year-1;
            month=12;
        }else{
            month = month -1;
        }
        sb.append(year + "-");
        if (month < 10) {
            sb.append("0" + month + "-");
        } else {
            sb.append(month + "-");
        }
        sb.append(day);
        mStartTimeStr = sb.toString();

        sb = new StringBuffer();

        if(month < 12){
            month = month + 1;
        } else if (month == 12){
            month = 1;
            year = year + 1;
        }

        sb.append(year + "-");
        if (month < 10) {
            sb.append("0" + month + "-");
        } else {
            sb.append(month + "-");
        }
        sb.append(day);
        mEndTimeStr = sb.toString();

        mStartTimeEditText.setText(mStartTimeStr);
        mEndTimeEditText.setText(mEndTimeStr);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mProgressDialog = new ProgressDialog(this);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(this);
        Intent intent = getIntent();
        mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        mAdapter = new RecordAdapter();
        mRecordsListView.setAdapter(mAdapter);

        mCyclesPeriod = getResources().getStringArray(R.array.warning_cycle);
        getContentResolver().registerContentObserver(MeasurementRecordService.MEASUREMENT_RECORDS_TABLE_URI, true, mObserver);

        mHandler.sendEmptyMessage(MSG_UPDATE_ADAPTE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    TranslateAnimation mHiddenAction = null;
    TranslateAnimation mShowAction = null;
    private void initAnimation(){
        mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        mHiddenAction.setDuration(500);

        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);
    }


    private DatePickerDialog initDatePickerDialog(int year, int month, int day, DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog dateDialog = new DatePickerDialog(this, listener, year, month, day);
        return dateDialog;
    }


    private void showMeasurementRecords(MeasurementInfo info) {
        showMeasurementRecords(info, null, null);
    }

    private void showMeasurementRecords(MeasurementInfo info, String startTime, String endTime) {
        if(info == null)return;
        List<MeasurementRecord> records;
        if (startTime == null || endTime == null ||
                startTime.isEmpty() || endTime.isEmpty()) {
            records = info.getRecords();
        } else {
            records = info.getPeriodRecords(startTime, endTime);
        }

        if (records.isEmpty()) {
            Log.e(TAG, "Has no records");
        }

        mAdapter.setRecords(records);
        mAdapter.notifyDataSetChanged();
    }

    DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            StringBuffer sb = new StringBuffer();
            sb.append(year + "-");
            if (monthOfYear < 9) {
                sb.append("0" + (monthOfYear + 1) + "-");
            } else if(monthOfYear == 12){
                monthOfYear = 1;
                year+=1;
                sb.append((monthOfYear) + "-");
            }else{
                sb.append((monthOfYear + 1) + "-");
            }
            sb.append(dayOfMonth);
            mStartTimeEditText.setText(sb.toString());
            mEndDatePickerDialog = initDatePickerDialog(year, monthOfYear, dayOfMonth, mEndtDateSetListener);
            mStartTimeStr = mStartTimeEditText.getText().toString();
            mEndTimeStr = mEndTimeEditText.getText().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                long startTimestamp = dateFormat.parse(mStartTimeStr).getTime();
                long endTimestamp = dateFormat.parse(mEndTimeStr).getTime();
                if (endTimestamp < startTimestamp) {
                    Toast.makeText(AllRecordsActivity.this, "End time shoule after start time", Toast.LENGTH_LONG).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(MSG_FETCH_RECORDS);
        }
    };

    DatePickerDialog.OnDateSetListener mEndtDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            StringBuffer sb = new StringBuffer();
            sb.append(year + "-");
            if (monthOfYear < 9) {
                sb.append("0" + (monthOfYear + 1) + "-");
            } else if(monthOfYear == 12){
                monthOfYear = 1;
                year+=1;
                sb.append((monthOfYear) + "-");
            }else{
                sb.append((monthOfYear + 1) + "-");
            }
            sb.append(dayOfMonth);
            mEndTimeEditText.setText(sb.toString());
            mStartTimeStr = mStartTimeEditText.getText().toString();
            mEndTimeStr = mEndTimeEditText.getText().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                long startTimestamp = dateFormat.parse(mStartTimeStr).getTime();
                long endTimestamp = dateFormat.parse(mEndTimeStr).getTime();
                if (endTimestamp < startTimestamp) {
                    Toast.makeText(AllRecordsActivity.this, "End time shoule after start time", Toast.LENGTH_LONG).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(MSG_FETCH_RECORDS);
        }
    };

    private PullToRefreshListView.OnRefreshListener mOnRefreshListener = new PullToRefreshListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mHandler.sendEmptyMessage(MSG_FETCH_RECORDS);
//            new GetDataTask().execute();
        }
    };

    View.OnClickListener mPackSearchViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initAnimation();
            if(mSearchWrapperLayout.isShown()){
                mSearchWrapperLayout.setVisibility(View.GONE);
                mSearchWrapperLayout.startAnimation(mHiddenAction);
                mHideSearWrapperImage.startAnimation(mHiddenAction);
                mRecordsListView.startAnimation(mHiddenAction);
                mHideSearWrapperImage.setImageResource(R.drawable.unfold_arrow);
            } else {
                mSearchWrapperLayout.setVisibility(View.VISIBLE);
                mHideSearWrapperImage.startAnimation(mShowAction);
                mSearchWrapperLayout.startAnimation(mShowAction);
                mRecordsListView.startAnimation(mShowAction);
                mHideSearWrapperImage.setImageResource(R.drawable.pack_arrow);
            }
        }
    };

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.start_time: {
                    if (mStartDatePickerDialog == null) {
                        int year, month, day;
                        Calendar calendar = Calendar.getInstance();
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);
                        day = calendar.get(Calendar.DAY_OF_MONTH);
                        mStartDatePickerDialog = initDatePickerDialog(year, month, day, mStartDateSetListener);
                    }
                    mStartDatePickerDialog.show();
                    mStartDatePickerDialog = null;
                }
                break;
                case R.id.end_time: {
                    if (mEndDatePickerDialog == null) {
                        int year, month, day;
                        Calendar calendar = Calendar.getInstance();
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);
                        day = calendar.get(Calendar.DAY_OF_MONTH);
                        mEndDatePickerDialog = initDatePickerDialog(year, month, day, mEndtDateSetListener);
                    }
                    mEndDatePickerDialog.show();
                    mEndDatePickerDialog = null;
                }
                break;
            }
        }
    };

    private static final int MSG_READY_LOAD_IMG_FROM_SERVER = 1;
    private static final int MSG_LOAD_IMG_FROM_SERVER_FINISHED = 2;
    private static final int MSG_PROGRESSING = 3;
    private static final int MSG_PROGRED = 4;
    private static final int MSG_SET_DATE_FLOW_FAILED = 5;
    private static final int MSG_UPDATE_ADAPTE = 6;
    private static final int MSG_FETCH_RECORDS = 7;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_READY_LOAD_IMG_FROM_SERVER: {

                }
                break;
                case MSG_LOAD_IMG_FROM_SERVER_FINISHED: {

                }
                break;
                case MSG_PROGRESSING: {
                    ProgressDialog dialog = (ProgressDialog) msg.obj;
                    dialog.show();
                }
                break;
                case MSG_SET_DATE_FLOW_FAILED:
                case MSG_PROGRED: {
                    ProgressDialog dialog = (ProgressDialog) msg.obj;
                    dialog.dismiss();
                }
                break;
                case MSG_FETCH_RECORDS:
                    new GetDataTask().execute();
                    break;
                case MSG_UPDATE_ADAPTE:
                    mStartTimeStr = mStartTimeEditText.getText().toString();
                    mEndTimeStr = mEndTimeEditText.getText().toString();
                    showMeasurementRecords(mMeasurementInfo, mStartTimeStr, mEndTimeStr);
                    break;
            }
        }
    };


    ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
            mHandler.sendEmptyMessage(MSG_UPDATE_ADAPTE);
        }
    };

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

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            long endTime = ELUtils.convertLocaleStringTOTimeStamp("yyyy-MM-dd", mEndTimeStr) / 1000 + DAY_IN_SECONDS;
            String endTimeStr = ELUtils.convertTimestampToLocalString("yyyy-MM-dd",  (int)endTime);
            ELServiceHelper helper = ELServiceHelper.get();
            helper.fetchMeasurementTimedRecord(mDeviceId, mStartTimeStr,endTimeStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mHandler.sendEmptyMessage(MSG_UPDATE_ADAPTE);
            mRecordsListView.onRefreshComplete();
        }
    }


    private class DowloadImageTask extends AsyncTask<String, Void, String> {
        MeasurementRecord mRecord;

        public DowloadImageTask(MeasurementRecord r) {
            this.mRecord = r;
        }

        @Override
        protected String doInBackground(String... params) {
            String newPicUrl = params[0];
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
            AlertDialog dialog = new PreviewImageDialog(AllRecordsActivity.this, targetLocalPath, mRecord);
            dialog.show();
        }
    }

    private class ItemClickedListener implements View.OnClickListener {
        MeasurementRecord record;

        public ItemClickedListener(MeasurementRecord r) {
            this.record = r;
        }

        @Override
        public void onClick(View v) {
            String newPicUrl = record.getNewPicUrl();
            if (newPicUrl != null && !newPicUrl.equals("null")) {
                showProgressDialog();
                new DowloadImageTask(record).execute(newPicUrl);
            }
        }
    }

    class PreviewImageDialog extends AlertDialog {
        Drawable mImageDrawable = null;
        ImageView mImageView;
        TextView mRecordValueTV;
        View mRootView = null;
        Context mContext;
        MeasurementRecord mRecord;

        public PreviewImageDialog(Context context, int resId, MeasurementRecord record) {
            super(context);
            this.mContext = context;
            mImageDrawable = context.getResources().getDrawable(resId);
            this.mRecord = record;
        }

        public PreviewImageDialog(Context context, String imagePath, MeasurementRecord record) {
            super(context);
            this.mContext = context;
            this.mImageDrawable = Drawable.createFromPath(imagePath);
            this.mRecord = record;
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
            mRecordValueTV.setText(mRecord.getRecordValue() + "");

            setContentView(mRootView);
        }
    }

    private class RecordAdapter extends BaseAdapter {

        List<MeasurementRecord> mMeasurementRecords = new ArrayList<>();
        public RecordAdapter() { }

        public RecordAdapter(List<MeasurementRecord> records) {
            this.mMeasurementRecords = records;
        }

        public void setRecords(List<MeasurementRecord> records) {
            this.mMeasurementRecords.clear();
            this.mMeasurementRecords = records;
        }

        @Override
        public int getCount() {
            return mMeasurementRecords.size();
        }

        @Override
        public MeasurementRecord getItem(int position) {
            return mMeasurementRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HolderView view = null;
            MeasurementRecord record = mMeasurementRecords.get(position);

            if (convertView == null) {
                view = new HolderView();
                convertView = LayoutInflater.from(AllRecordsActivity.this).inflate(R.layout.measurement_detail_record_item, null);
                view.mDateTimeTextView = (TextView) convertView.findViewById(R.id.measurement_detail_record_date);
                view.mValuesTextView = (TextView) convertView.findViewById(R.id.measurement_detail_record_value);
                view.mRecordPicImageView = (ImageView) convertView.findViewById(R.id.record_pic);
                convertView.setTag(view);
            } else {
                view = (HolderView) convertView.getTag();
            }

            view.mValuesTextView.setText(String.format(getString(R.string.record_value), record.getRecordValue()));
            view.mDateTimeTextView.setText(ELUtils.convertTimestampToLocalString(Constant.DATE_FORMAT, record.getDateTime()));
            if (record.getNewPicUrl() != null && !record.getNewPicUrl().equals("null")) {
                view.mRecordPicImageView.setImageResource(R.drawable.list_icon_pre);
            } else {
                view.mRecordPicImageView.setImageResource(R.drawable.list_icon);
            }
            convertView.setOnClickListener(new ItemClickedListener(record));
            return convertView;
        }

        private class HolderView {
            TextView mValuesTextView;
            TextView mDateTimeTextView;
            ImageView mRecordPicImageView;
        }
    }

    public void back(View v) {
        finish();
    }
}
