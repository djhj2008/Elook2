package com.elook.client.el;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ELookServiceImpl;
import com.elook.client.service.MeasurementInfoService;
import com.elook.client.service.MeasurementListService;
import com.elook.client.service.MeasurementRecordService;
import com.elook.client.ui.RecordWrapper;
import com.elook.client.ui.RecordsLayout;
import com.elook.client.user.MeasurementCountData;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.utils.ELUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by haiming on 5/25/16.
 */
public class MeasurementRecordsActivity extends Activity {
    private static final String TAG = "RecordsActivity";
    private static final boolean DEBUG = true;
    private static final int LIST_DAY_MAX = 5;
    private static final int LIST_WEEK_MAX = 4;
    private static final int LIST_MONTH_MAX = 4;
    private static final int LIST_YEAR_MAX = 3;

    RadioGroup mCheckWayGroup;
    int[] mRadioButtonIds;
    ViewPager mCheckWayViewPager;
    MyViewPagerAdapter mViewPagerAdapter;

    RelativeLayout[] mAllCheckWayLayouts = null;
    RecordsLayout[] mAllRecordsLayouts = null;
    RecordsLayout mSelectedRecordLayout = null;
    int mCurrentSelectedIndex = 0;

    ToggleButton mUsageUpLimitSwitcher, mNoUsageSticher;
    LinearLayout mStateLayoutLL;
    LinearLayout mNofityUsageUpLL,mNotifyNoUsageLL;
    TextView mNofityUsageUpTV,mNotifyNoUsageTV;
    TextView mRemindUsageUPTV,mNotUseTV;
    TextView mTitleTV;
    Button mRecButton;

    TextView mStateTextTV;
//    MeasurementInfo mMeasurementInfo;
    ELookDatabaseHelper mDatabaseHelper;

    List<RecordWrapper> recorddayWrappers = new ArrayList<>();
    List<RecordWrapper> recordweekWrappers = new ArrayList<>();
    List<RecordWrapper> recordmonthWrappers = new ArrayList<>();
    List<RecordWrapper> recordyearWrappers = new ArrayList<>();

    private Dialog mProgressDialog = null;
    private int mDeviceId;
    private int mState;
    private int dev_state_str[]={
            R.string.device_state_preinit,
            R.string.device_state_connect,
            R.string.device_state_start_config,
            R.string.device_state_normal_error,
            R.string.device_state_cannot_parse,
            R.string.device_state_config_success,
            R.string.device_state_parse_fail,
            R.string.device_state_network_error,
            R.string.device_state_error,
            R.string.device_state_dev_disconnect,
            R.string.device_state_dev_led_error
    };
    private int dev_flow;
    private int dev_interval;
    private int dev_notuse_day;
    private int dev_notuse_day_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tmpStr = getIntent().getStringExtra("deviceid");
        //if(DEBUG) tmpStr = "123456789";
        if(tmpStr == null || (tmpStr != null && tmpStr.equals("null")))return;
        mDeviceId = Integer.parseInt(tmpStr);
        setContentView(R.layout.activity_measurement_records);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(this);
        initViews();
        getContentResolver().registerContentObserver(MeasurementListService.MEASUREMENT_LIST_TABLE_URI,
                true, mObserver);
        Log.d(TAG,"onCreate!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //new FetchRecordsTask().execute();
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume!");
        mHandler.sendEmptyMessageDelayed(MSG_UPDATED_TIME_OUT,UPDATED_TIME_OUT);
        mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
        new FetchRecordsTask().execute();
        //updateRecordsView();
        //updateMeasurementInfo();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mAllRecordsLayouts[0].removePopDetailRecordView();
        super.onPause();
    }


    public void startAllRecords(View v) {
        Intent intent = new Intent(MeasurementRecordsActivity.this, AllRecordsActivity.class);
        intent.putExtra("deviceid", mDeviceId + "");
        startActivity(intent);
    }

    private void initViews() {
        Log.d(TAG, "initViews");
//        mRecordPatternWrapper = (FrameLayout)findViewById(R.id.activity_measurement_record_pattern_wrapper);
        mStateLayoutLL = (LinearLayout)findViewById(R.id.measurement_detail_record_state);
        mStateTextTV = (TextView)findViewById(R.id.measurement_detail_record_text);
        mCheckWayViewPager = (ViewPager) findViewById(R.id.activity_measurement_record_check_way_viewpager);
        mCheckWayGroup = (RadioGroup)findViewById(R.id.activity_measurement_record_radio_group);
        mUsageUpLimitSwitcher = (ToggleButton)findViewById(R.id.remind_usage_up_switcher);
        mNoUsageSticher = (ToggleButton)findViewById(R.id.no_usage_switcher);
        mRemindUsageUPTV = (TextView)findViewById(R.id.remind_usage_up_text);
        mNotUseTV = (TextView)findViewById(R.id.no_usage_text);
        mRecButton = (Button)findViewById(R.id.activity_measurement_record_reconnect_device);
        mTitleTV = (TextView)findViewById(R.id.login_login);

        mRecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeasurementRecordsActivity.this, InitializeDeviceActivity.class);
                intent.putExtra("deviceid", mDeviceId);
                intent.putExtra("state",mState);
                startActivity(intent);
            }
        });

        mNofityUsageUpLL = (LinearLayout)findViewById(R.id.measurement_notify_usageup_warpper);
        mNotifyNoUsageLL = (LinearLayout)findViewById(R.id.measurement_notify_nousage_warpper);
        mNofityUsageUpTV = (TextView)findViewById(R.id.measurement_notify_usageup_text);
        mNotifyNoUsageTV = (TextView)findViewById(R.id.measurement_notify_nousage_text);

        mRadioButtonIds = new int[]{
                R.id.activity_measurement_record_day,
                R.id.activity_measurement_record_week,
                R.id.activity_measurement_record_month,
                R.id.activity_measurement_record_year
        };

        RelativeLayout dayLayoutWrapper, weekLayoutWrapper,monthLayoutWrapper,yearLayoutWrapper;
        dayLayoutWrapper = (RelativeLayout)LayoutInflater.from(MeasurementRecordsActivity.this).inflate(R.layout.day_layout, null);
        weekLayoutWrapper = (RelativeLayout)LayoutInflater.from(MeasurementRecordsActivity.this).inflate(R.layout.week_layout, null);
        monthLayoutWrapper = (RelativeLayout)LayoutInflater.from(MeasurementRecordsActivity.this).inflate(R.layout.month_layout, null);
        yearLayoutWrapper = (RelativeLayout)LayoutInflater.from(MeasurementRecordsActivity.this).inflate(R.layout.year_layout, null);

        TextView checkAllRecordsTV = (TextView)dayLayoutWrapper.findViewById(R.id.measurement_detail_check_all_records);

        mAllCheckWayLayouts = new RelativeLayout[]{
                dayLayoutWrapper,
                weekLayoutWrapper,
                monthLayoutWrapper,
                yearLayoutWrapper
        };

        mAllRecordsLayouts = new RecordsLayout[]{
                (RecordsLayout)dayLayoutWrapper.findViewById(R.id.activity_measurement_record_day),
                (RecordsLayout)weekLayoutWrapper.findViewById(R.id.activity_measurement_record_week),
                (RecordsLayout)monthLayoutWrapper.findViewById(R.id.activity_measurement_record_month),
                (RecordsLayout)yearLayoutWrapper.findViewById(R.id.activity_measurement_record_year),
        };

        mSelectedRecordLayout = mAllRecordsLayouts[0];

        mCheckWayGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        mViewPagerAdapter = new MyViewPagerAdapter();
        mCheckWayViewPager.setAdapter(mViewPagerAdapter);
        mCheckWayViewPager.setCurrentItem(0);

        mUsageUpLimitSwitcher.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUsageUpLimitSwitcher.isChecked()) {
                    new CloseUsageUpTask().execute();
                } else {
                    showDeviceConfig();
                }
            }
        });
        mUsageUpLimitSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUsageUpLimitSwitcher.setBackgroundResource(isChecked ? R.drawable.switcher_on : R.drawable.switcher_off);
            }
        });
        mNoUsageSticher.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNoUsageSticher.isChecked()) {
                    new CloseNoUsageTask().execute();
                } else {
                    showNotUseNotify();
                }
            }
        });
        mNoUsageSticher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNoUsageSticher.setBackgroundResource(isChecked ? R.drawable.switcher_on : R.drawable.switcher_off);
            }
        });

        mCheckWayViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != mCurrentSelectedIndex) {
                    //if(mCurrentSelectedIndex == 0){
                    mSelectedRecordLayout.removePopDetailRecordView();
                    //}
                    mCurrentSelectedIndex = position;
                    mSelectedRecordLayout = mAllRecordsLayouts[mCurrentSelectedIndex];
                    mCheckWayGroup.check(mRadioButtonIds[position]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        updateMeasurementInfo();
    }

    private void updateMeasurementInfo(){
        Log.d(TAG,"updateMeasurementInfo");
        MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        mState = mMeasurementInfo.getDeviceState();
        Log.d(TAG,"updateMeasurementInfo mState="+mState);
        if(mState>=MeasurementInfo.MEASUREMENT_STATE_PREINIT&&mState<MeasurementInfo.MEASUREMENT_STATE_MAX) {
            if (mState == MeasurementInfo.MEASUREMENT_STATE_CONFIG_SUCCESS) {
                mStateLayoutLL.setVisibility(View.GONE);
            } else {
                mStateLayoutLL.setVisibility(View.VISIBLE);
                mStateTextTV.setText(dev_state_str[mState]);
            }
        }else{
            mStateLayoutLL.setVisibility(View.VISIBLE);
            mStateTextTV.setText(dev_state_str[MeasurementInfo.MEASUREMENT_STATE_ERROR]);
        }
        dev_interval = mMeasurementInfo.getDateInterval();
        dev_flow = mMeasurementInfo.getDeviceFlow();
        dev_notuse_day = mMeasurementInfo.getDevNotUseDay();
        dev_notuse_day_count = mMeasurementInfo.getDevNotUseDayCount();

        mTitleTV.setText(mMeasurementInfo.getLocation());

        if(dev_flow==0){
            mRemindUsageUPTV.setText(R.string.measurement_settings_add_notify);
        }else{
            mUsageUpLimitSwitcher.setChecked(true);
            if(dev_interval==0) {
                mRemindUsageUPTV.setText(String.format(getString(R.string.measurement_settings_usage_day), dev_flow));
            }else if(dev_interval==1){
                mRemindUsageUPTV.setText(String.format(getString(R.string.measurement_settings_usage_week), dev_flow));
            }else if(dev_interval==2){
                mRemindUsageUPTV.setText(String.format(getString(R.string.measurement_settings_usage_month), dev_flow));
            }
        }

        if(dev_notuse_day==0){
            mNotUseTV.setText(R.string.measurement_settings_add_notify);
        }else{
            mNotUseTV.setText(String.format(getString(R.string.measurement_settings_nouse), dev_notuse_day));
            mNoUsageSticher.setChecked(true);
            if(dev_notuse_day_count>=dev_notuse_day){
                mNotifyNoUsageLL.setVisibility(View.VISIBLE);
                mNotifyNoUsageTV.setText(String.format(getString(R.string.measurement_notify_nouse),dev_notuse_day_count));
            }else{
                mNotifyNoUsageLL.setVisibility(View.GONE);
            }
        }

        if(recorddayWrappers.size()==0||recordweekWrappers.size()==0||recordmonthWrappers.size()==0)
            return;
        int value = 0;
        if(dev_interval==0) {
            if(recorddayWrappers.size()>0) {
                value = recorddayWrappers.get(recorddayWrappers.size()-1).getValue();
            }
        }else if(dev_interval==1) {
            if(recordweekWrappers.size()>0) {
                value = recordweekWrappers.get(recordweekWrappers.size()-1).getValue();
            }
        }else if(dev_interval==2) {
            if(recordmonthWrappers.size()>0) {
                value = recordmonthWrappers.get(recordmonthWrappers.size()-1).getValue();
            }
        }
        if(value > dev_flow) {
            mNofityUsageUpLL.setVisibility(View.VISIBLE);
            String msg = String.format(getString(R.string.measurement_notify_usageup),value-dev_flow);
            mNofityUsageUpTV.setText(msg);
        }else{
            mNofityUsageUpLL.setVisibility(View.GONE);
        }

    }

    private Date getFirstDayOfWeek(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        return c.getTime ();
    }

    private Date getLastDayOfWeek(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6);
        return c.getTime();
    }

    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "onCheckedChanged ");
            Calendar c;
            int index = 0;
            List<RecordWrapper> recordWrappers = null;
            switch (checkedId) {
                case R.id.activity_measurement_record_day:
                    index = 0;
                    break;
                case R.id.activity_measurement_record_week:
                    index = 1;
                    break;
                case R.id.activity_measurement_record_month:
                    index = 2;
                    break;
                case R.id.activity_measurement_record_year:
                    index = 3;
                    break;
            }
            mCheckWayViewPager.setCurrentItem(index);
            updateRecordsView();
            //new FetchRecordsTask().execute();

        }
    };

    private static final int MSG_READY_TO_UPDATE_MEASUREMENT_RECORDS = 0;
    private static final int MSG_UPDATED_MEASUREMENT_RECORDS = 1;
    private static final int MSG_UPDATED_TIME_OUT = 2;
    private static final int UPDATED_TIME_OUT = 10000;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 1002;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 1003;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_READY_TO_UPDATE_MEASUREMENT_RECORDS:
                    break;
                case MSG_UPDATED_MEASUREMENT_RECORDS:
                    break;
                case MSG_SHOW_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog=null;
                    }
                    mProgressDialog = ELUtils.createLoadingDialog(MeasurementRecordsActivity.this, "");
                    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                }
                break;
                case MSG_DIMISS_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }
                case MSG_UPDATED_TIME_OUT:{
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }

            }
        }
    };

    ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            if(selfChange){
                //updateRecordsView();
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

        }
    };

    public List<MeasurementCountData> getSortRecords(List<MeasurementCountData> mRecords) {
        Log.d(TAG, "mRecords count = "+mRecords.size());
        List<MeasurementCountData> records = new ArrayList<>();
        synchronized (mRecords){
            for (MeasurementCountData record : mRecords) {
                records.add(record);
            }
        }
        Collections.sort(records, mSortByTime);
        Log.d(TAG, "records count = " + records.size());
        return records;
    }

    Comparator<MeasurementCountData> mSortByTime = new Comparator<MeasurementCountData>() {
        @Override
        public int compare(MeasurementCountData lhs, MeasurementCountData rhs) {
            return lhs.getDateTime() - rhs.getDateTime();
        }
    };
    private List<RecordWrapper> getFiveDaysRecordWrapper(){
        //MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        List<RecordWrapper> recordWrappers = new ArrayList<>();
        //if(mMeasurementInfo == null) return  recordWrappers;
        List<MeasurementCountData> tmpdatas = mDatabaseHelper.getMestDatas(mDeviceId,MeasurementListService.TABLE_TYPE_DAY);
        List<MeasurementCountData> datas = getSortRecords(tmpdatas);
        for (MeasurementCountData data: datas){
            RecordWrapper wrapper = new RecordWrapper();
            int value = 0;
            Log.d(TAG, "record value: "+data.getRecordValue());
            value = data.getRecordValue();
            wrapper.setValue(value);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd");
            long ltime = data.getDateTime()*1000L;
            String tmpdata= format.format(ltime);
            String tmpdata2= format.format(System.currentTimeMillis());
            if(tmpdata.equals(tmpdata2)){
                wrapper.setDate(getString(R.string.record_today));
            }else {
                wrapper.setDate(tmpdata);
            }
            wrapper.setType(MeasurementListService.TABLE_TYPE_DAY);
            recordWrappers.add(wrapper);
        }
        int size = recordWrappers.size();
        if(size>LIST_DAY_MAX){
            for(int i=0;i<size-LIST_DAY_MAX;i++)
                recordWrappers.remove(0);
        }
        Log.d(TAG, "record count: " +recordWrappers.size());
        return recordWrappers;
    }

    private List<RecordWrapper> getFourWeeksRecordWrapper(){
        //MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        List<RecordWrapper> recordWrappers = new ArrayList<>();
        //if(mMeasurementInfo == null) return  recordWrappers;
        List<MeasurementCountData> tmpdatas = mDatabaseHelper.getMestDatas(mDeviceId,MeasurementListService.TABLE_TYPE_WEEK);
        List<MeasurementCountData> datas = getSortRecords(tmpdatas);
        for (MeasurementCountData data: datas){
            RecordWrapper wrapper = new RecordWrapper();
            int value = 0;
            Log.d(TAG, "record value: "+data.getRecordValue());
            value = data.getRecordValue();
            wrapper.setValue(value);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd");
            long ltime = data.getDateTime()*1000L;
            long end_time = ltime+86400*6*1000L;
            wrapper.setDate(format.format(ltime)+"-"+format.format(end_time));
            wrapper.setType(MeasurementListService.TABLE_TYPE_WEEK);
            recordWrappers.add(wrapper);
        }
        int size = recordWrappers.size();
        if(size>LIST_WEEK_MAX){
            for(int i=0;i<size-LIST_WEEK_MAX;i++)
                recordWrappers.remove(0);
        }
        Log.d(TAG, "record count: " +recordWrappers.size());
        return recordWrappers;
    }

    private List<RecordWrapper> getSixMonthsRecordWrapper(){
        //MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        List<RecordWrapper> recordWrappers = new ArrayList<>();
        //if(mMeasurementInfo == null) return  recordWrappers;
        List<MeasurementCountData> tmpdatas = mDatabaseHelper.getMestDatas(mDeviceId, MeasurementListService.TABLE_TYPE_MONTH);
        List<MeasurementCountData> datas = getSortRecords(tmpdatas);
        for (MeasurementCountData data: datas){
            RecordWrapper wrapper = new RecordWrapper();
            int value = 0;
            Log.d(TAG, "record value: "+data.getRecordValue());
            value = data.getRecordValue();
            wrapper.setValue(value);
            SimpleDateFormat format = new SimpleDateFormat("MM");
            long ltime = data.getDateTime()*1000L;
            wrapper.setDate(format.format(ltime)+"月");
            wrapper.setType(MeasurementListService.TABLE_TYPE_MONTH);
            recordWrappers.add(wrapper);
        }
        int size = recordWrappers.size();
        if(size>LIST_MONTH_MAX){
            for(int i=0;i<size-LIST_MONTH_MAX;i++)
                recordWrappers.remove(0);
        }
        Log.d(TAG, "record count: " +recordWrappers.size());
        return recordWrappers;
    }

    private List<RecordWrapper> getThreeYearsRecordWrapper(){
        //MeasurementInfo mMeasurementInfo = mDatabaseHelper.getMeasurementInfo(mDeviceId);
        List<RecordWrapper> recordWrappers = new ArrayList<>();
        //if(mMeasurementInfo == null) return  recordWrappers;
        List<MeasurementCountData> tmpdatas = mDatabaseHelper.getMestDatas(mDeviceId, MeasurementListService.TABLE_TYPE_YEAR);
        List<MeasurementCountData> datas = getSortRecords(tmpdatas);
        for (MeasurementCountData data: datas){
            RecordWrapper wrapper = new RecordWrapper();
            int value = 0;
            Log.d(TAG, "record value: "+data.getRecordValue());
            value = data.getRecordValue();
            wrapper.setValue(value);
            SimpleDateFormat format = new SimpleDateFormat("yyyy");
            long ltime = data.getDateTime()*1000L;
            wrapper.setDate(format.format(ltime));
            wrapper.setType(MeasurementListService.TABLE_TYPE_YEAR);
            recordWrappers.add(wrapper);
        }
        int size = recordWrappers.size();
        if(size>LIST_YEAR_MAX){
            for(int i=0;i<size-LIST_YEAR_MAX;i++)
                recordWrappers.remove(0);
        }
        Log.d(TAG, "record count: " + recordWrappers.size());
        return recordWrappers;
    }

    private void updateRecordsView(){
        Log.d(TAG, "updateRecordsView");
        List<RecordWrapper> recordWrappers = null;

        switch (mCurrentSelectedIndex){
            case 0:
                recordWrappers = recorddayWrappers;//getFiveDaysRecordWrapper();
                break;
            case 1:
                recordWrappers = recordweekWrappers;//getFourWeeksRecordWrapper();
                break;
            case 2:
                recordWrappers = recordmonthWrappers; //getSixMonthsRecordWrapper();
                break;
            case 3:
                recordWrappers = recordyearWrappers; //getThreeYearsRecordWrapper();
                break;
        }
        mSelectedRecordLayout = mAllRecordsLayouts[mCurrentSelectedIndex];
        if(mCurrentSelectedIndex > 0)
            mSelectedRecordLayout.setIsDotPattern(false);
        else
            mSelectedRecordLayout.setIsDotPattern(true);

        if(recordWrappers != null) {
            mSelectedRecordLayout.setRecordWrapper(recordWrappers);
            mSelectedRecordLayout.showTodayRecordsView();
        }

        mCheckWayViewPager.setCurrentItem(mCurrentSelectedIndex);

        int value = 0;
        if(dev_interval==0) {
            if(recorddayWrappers.size()>0) {
                value = recorddayWrappers.get(recorddayWrappers.size()-1).getValue();
            }
        }else if(dev_interval==1) {
            if(recordweekWrappers.size()>0) {
                value = recordweekWrappers.get(recordweekWrappers.size()-1).getValue();
            }
        }else if(dev_interval==2) {
            if(recordmonthWrappers.size()>0) {
                value = recordmonthWrappers.get(recordmonthWrappers.size()-1).getValue();
            }
        }
        if(value > dev_flow) {
            mNofityUsageUpLL.setVisibility(View.VISIBLE);
            String msg = String.format(getString(R.string.measurement_notify_usageup),value-dev_flow);
            mNofityUsageUpTV.setText(msg);
        }else{
            mNofityUsageUpLL.setVisibility(View.GONE);
        }

    }


    class FetchRecordsTask extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            ELServiceHelper helper = ELServiceHelper.get();
            helper.getDeviceInfo(mDeviceId);
            helper.fetchMeasurementList(mDeviceId);
            recorddayWrappers = getFiveDaysRecordWrapper();
            recordweekWrappers = getFourWeeksRecordWrapper();
            recordmonthWrappers = getSixMonthsRecordWrapper();
            recordyearWrappers = getThreeYearsRecordWrapper();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            updateRecordsView();
            updateMeasurementInfo();
            mHandler.removeMessages(MSG_UPDATED_TIME_OUT);
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
        }
    }

    private class MyViewPagerAdapter extends PagerAdapter{
        @Override
        public int getCount() {
            return mAllCheckWayLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(mAllCheckWayLayouts[position % mAllCheckWayLayouts.length]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(mAllCheckWayLayouts[position % mAllCheckWayLayouts.length], 0);
            return mAllCheckWayLayouts[position % mAllCheckWayLayouts.length];
        }
    }

    public void startSettings(View v){
        Intent intent = new Intent(MeasurementRecordsActivity.this, MeasurementSettingsActivity.class);
        intent.putExtra("deviceid",mDeviceId);
        startActivity(intent);
    }

    public void startaddNotify(View v){
        Intent intent = new Intent(MeasurementRecordsActivity.this, NewRemindActivity.class);
        intent.putExtra("deviceid",mDeviceId);
        startActivity(intent);
    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private class CloseUsageUpTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_CLOSE_START);
            Bundle param = new Bundle();
            param.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
            param.putString(ELookServiceImpl.HTTP_PARAMS_DEV_FLOW, "0");
            param.putString(ELookServiceImpl.HTTP_PARAMS_DEV_INTERNAL, "0");
            ELServiceHelper helper = ELServiceHelper.get();
            Boolean ret = helper.setDeviceDateFlow(mDeviceId, param);
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean resule) {
            mNofityUsageUpLL.setVisibility(View.GONE);
            mRemindUsageUPTV.setText(R.string.measurement_settings_add_notify);
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_CLOSE_FINISH);
        }
    }
    private class CloseNoUsageTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_CLOSE_START);
            Bundle param = new Bundle();
            param.putString(ELookServiceImpl.HTTP_PARAMS_DEV_ID, mDeviceId+"");
            param.putString(ELookServiceImpl.HTTP_PARAMS_DEVICE_SETACCESS,"0");
            ELServiceHelper helper = ELServiceHelper.get();
            Boolean ret = helper.setDeviceNotUseDay(mDeviceId, param);
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean resule) {
            mNotifyNoUsageLL.setVisibility(View.GONE);
            mNotUseTV.setText(R.string.measurement_settings_add_notify);
            mhandler.sendEmptyMessage(MSG_GET_MESSAGE_CLOSE_FINISH);
        }
    }

    private static final int MSG_GET_MESSAGE_CLOSE_START = 0;
    private static final int MSG_GET_MESSAGE_CLOSE_FINISH = 1;
    ProgressDialog m_pDialog;
    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_GET_MESSAGE_CLOSE_START:
                    //创建ProgressDialog对象
                    m_pDialog = new ProgressDialog(MeasurementRecordsActivity.this);
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
                case MSG_GET_MESSAGE_CLOSE_FINISH:
                    m_pDialog.hide();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public void showDeviceConfig(){
        RemindDialog dialog = new RemindDialog(MeasurementRecordsActivity.this, mDeviceId);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateMeasurementInfo();
            }
        });
        dialog.show();
    }

    public void showNotUseNotify(){
        NotUseNotifyDialog dialog = new NotUseNotifyDialog(MeasurementRecordsActivity.this, mDeviceId);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateMeasurementInfo();
            }
        });
        dialog.show();
    }
}
