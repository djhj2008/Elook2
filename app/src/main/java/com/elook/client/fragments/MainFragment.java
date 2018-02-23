package com.elook.client.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.adapter.DeviceListAdapter;
import com.elook.client.el.AddMeasurementActivity;
import com.elook.client.exception.ErrorCode;
import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.exception.ExceptionCenter;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.MeasurementInfoService;
import com.elook.client.ui.ScollerTextView;
import com.elook.client.user.AdvertInfo;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.ELUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by haiming on 5/24/16.
 */
public class MainFragment extends Fragment implements  View.OnClickListener{
    private static final String TAG = "MainFragment";
    private static final boolean DEBUG = true;
    private Context mContext;

    View mRootView;

    TextView mActionBarTitleTV, mActionBarSubTitleTV;
    ImageView mAddNewMeasurmentIV;
    ImageView mCloseNotificationIV;

    FrameLayout mFragmentHeader;
    ViewPager mViewPager;
    LinearLayout mBannderIndicatorWrapper;
    ImageView[] mBannerIndicators;
    ImageView[] mBannerImgs;
    int[] mBannerImgIds;

    LinearLayout mNotificationContainerWrapper;
    ScollerTextView mNotificationContainerTV;
    ELookDatabaseHelper mDatabaseHelper;
    MyViewPaperAdapter mViewPagerAdapter;
    ListView mRecordsListView;
    DeviceListAdapter mDeviceAdapter;
    List<MeasurementInfo> newlist;
    int mFragmentHeaderHeight;
    private boolean isInitDataed=false;
    private boolean isInitUI=false;
    ELookApplication app;
    String mLocation = null;
    String mPhoneNumber=null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        this.mContext = (Context) getActivity();
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        initViews();
        initBanner();
        Log.d(TAG,"onCreateView: isInitDataed:"+isInitDataed);
        Log.d(TAG,"onCreateView: getUserVisibleHint():"+getUserVisibleHint());
        if (getUserVisibleHint()&&!isInitDataed) {
            mGetDataTask = new GetDataTask();
            mGetDataTask.execute();
            isInitDataed=true;
        }
        mContext.getContentResolver().registerContentObserver(MeasurementInfoService.MEASUREMENT_TABLE_URI,
                true, mObserver);
        getFragmentHeaderLocation();

        app = (ELookApplication)getActivity().getApplication();
        mLocation = app.getLocation();
        mPhoneNumber = app.getUserInfo().getUserPhoneName();
        if(mLocation==null) {
            UserInfo info = app.getUserInfo();
            if(info!=null)
            mActionBarSubTitleTV.setText(info.getAddress());
        }else{
            mActionBarSubTitleTV.setText(mLocation);
        }
        return mRootView;
    }


    private void initViews(){
        mAddNewMeasurmentIV = (ImageView)mRootView.findViewById(R.id.main_fragment_add_device_icon);
        mActionBarTitleTV = (TextView)mRootView.findViewById(R.id.main_fragment_title);
        mActionBarSubTitleTV = (TextView)mRootView.findViewById(R.id.home_measurement_action_bar_sub_title);
        mCloseNotificationIV = (ImageView)mRootView.findViewById(R.id.home_close_notification);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.main_fragment_viewpaper);
        mFragmentHeader = (FrameLayout) mRootView.findViewById(R.id.main_fragment_header);
        mBannderIndicatorWrapper = (LinearLayout) mRootView.findViewById(R.id.main_fragment_banner_indicator_wrapper);
        mNotificationContainerWrapper  = (LinearLayout)mRootView.findViewById(R.id.notification_container_wrapper);
        mNotificationContainerTV = (ScollerTextView)mRootView.findViewById(R.id.notification_container);
        mRecordsListView = (ListView) mRootView.findViewById(R.id.main_fragment_device_list);

        mAddNewMeasurmentIV.setOnClickListener(this);
        mCloseNotificationIV.setOnClickListener(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: isInitDataed:" + isInitDataed);
        Log.d(TAG, "setUserVisibleHint: getUserVisibleHint():" + getUserVisibleHint());
        if (getUserVisibleHint()) {
            if (!isInitDataed ) {
                mGetDataTask = new GetDataTask();
                mGetDataTask.execute();
                isInitDataed=true;
            }
        }else{
            isInitDataed = false;
        }
    }

    public void setLocation(String location){
        mLocation = location;
        if(mActionBarSubTitleTV!=null)
        mActionBarSubTitleTV.setText(mLocation);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDatabaseHelper  == null) {
            mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        }
        Log.d(TAG,"onResume:"+getUserVisibleHint());
        if ((getUserVisibleHint()&&!isInitDataed)) {
            mGetDataTask = new GetDataTask();
            mGetDataTask.execute();
//            if (mDeviceAdapter != null) {
//                if (newlist != null)
//                    newlist.clear();
//                newlist = initDisplayMeasurements();
//                mDeviceAdapter.setAllMeasurementInfo(newlist);
//            }
//            mRecordsListView.setAdapter(mDeviceAdapter);
        }
        showNotificationInMainWindow();
    }

    @Override
    public void onPause() {
        super.onPause();
        isInitDataed=false;
        //mRecordsListView.setAdapter(null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.main_fragment_add_device_icon:
                addNewMeasurement();
                break;
            case R.id.home_close_notification:
                closeNotification();
                break;
        }
    }

    private List<MeasurementInfo> initDisplayMeasurements() {
        if(mPhoneNumber==null||mPhoneNumber.isEmpty()){
            mPhoneNumber = mDatabaseHelper.getActiveUserInfo().getUserPhoneName();
        }
        List<MeasurementInfo> measurementInfos =
                mDatabaseHelper.getMeasurementsOfUser(mPhoneNumber);

        if (measurementInfos.isEmpty()) {
            //Toast.makeText(mContext, "Has no measurements", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Has no measurements ");
        } else {
            Log.d(TAG, "measurement count: "+measurementInfos.size());
        }

        MeasurementInfo tempMeasurementInfo = new MeasurementInfo();
        tempMeasurementInfo.setType(MeasurementInfo.MeasurementType.MOCK_ADD_DEVICE);
        measurementInfos.add(tempMeasurementInfo);
        Log.d(TAG, "measurement count: " + measurementInfos.size());
        return measurementInfos;
    }

    private static final int MSG_READY_TO_UPDATE_MEASUREMENT_INFOS = 0;
    private static final int MSG_UPDATED_MEASUREMENT_INFOS = 1;
    private static final int MSG_UPDATED_MEASUREMENT_INFOS_ERROR = 2;
    private static final int MSG_UPDATED_TIME_OUT = 3;
    private static final int UPDATED_TIME_OUT = 10000;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 1002;
    private static final int MSG_DIMISS_PROGRESS_DIALOG = 1003;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_READY_TO_UPDATE_MEASUREMENT_INFOS:
                    break;
                case MSG_UPDATED_MEASUREMENT_INFOS:
                    break;
                case MSG_UPDATED_MEASUREMENT_INFOS_ERROR:
                    ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_CONNECT_TO_SERVER));
                    break;
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
                case MSG_UPDATED_TIME_OUT: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    showNotificationInMainWindow();
                }
                break;
                case MSG_DIMISS_PROGRESS_DIALOG: {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog=null;
                    }
                    showNotificationInMainWindow();
                }
                break;
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
            Log.d(TAG, "onChange mMeasurementAdapter = " + mDeviceAdapter);
            if (mDeviceAdapter != null) {

            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange uri = " + uri);
            if (mDeviceAdapter != null) {

            }
        }
    };

    private void getFragmentHeaderLocation(){
        ViewTreeObserver viewTreeObserver = mFragmentHeader.getViewTreeObserver();
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFragmentHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mFragmentHeaderHeight = mFragmentHeader.getHeight();

            }
        };
        viewTreeObserver.addOnGlobalLayoutListener(listener);
    }

    String getLowBatDevString(List<MeasurementInfo> devs){
        String info = null,ret = null;
        if(devs==null||devs.size()==0)
            return null;
        for(int i=0;i<devs.size();i++ ){
            MeasurementInfo dev = devs.get(i);
            if(dev.getType()==MeasurementInfo.MeasurementType.MOCK_ADD_DEVICE){
                continue;
            }
            Log.d(TAG,"getLowBatDev ID:"+dev.getDeviceId()+"  bl:"+dev.getDeviceBatteryLevel());
            if(dev.getDeviceBatteryLevel()==0){
                if(info==null){
                    info = dev.getDeviceId() + "";
                }else {
                    info += ","+dev.getDeviceId();
                }
            }
        }
        if(info!=null) {
            ret = String.format(getString(R.string.lowbat_device_id), info);
        }
        return ret;
    }



    private void showNotificationInMainWindow() {
        if(mNotificationContainerTV !=null && mNotificationContainerWrapper != null){
            String devstr = getLowBatDevString(newlist);
            if(devstr!=null){
                mNotificationContainerTV.setText(devstr);
                mNotificationContainerWrapper.setVisibility(View.VISIBLE);
            }else{
                mNotificationContainerWrapper.setVisibility(View.GONE);
            }
        }
    }

    public void closeNotification(){
        if(mNotificationContainerWrapper != null){
            mNotificationContainerWrapper.setVisibility(View.GONE);
        }
    }

     /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initBanner() {
        List <AdvertInfo> infos = mDatabaseHelper.getAdvertInfo();
        int banner_count = infos.size();
        String[] mBannerImgUrls = new String[banner_count];


        for(int i=0;i<infos.size();i++){
            AdvertInfo info = infos.get(i);
            Log.d(TAG,"initBanner +picurl:"+info.getAdvertPicUrl());
            Log.d(TAG, "initBanner +url:" + info.getAdvertUrl());
        }

        mBannerImgIds = new int[]{
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3,
                R.drawable.banner_4,
                R.drawable.banner_5,
        };

        mBannerIndicators = new ImageView[banner_count];
        mBannerImgs = new ImageView[banner_count];

        for (int i = 0; i < banner_count; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
            mBannerIndicators[i] = imageView;
            if (i == 0) {
                mBannerIndicators[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else {
                mBannerIndicators[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 5;
            layoutParams.rightMargin = 5;
            mBannderIndicatorWrapper.addView(imageView, layoutParams);
        }

        for (int i = 0; i < banner_count; i++) {
            ImageView imageView = new ImageView(mContext);
            mBannerImgs[i] = imageView;
            String picurl = infos.get(i).getAdvertPicUrl();
            int index = picurl.lastIndexOf("/");
            String newPicUrl = picurl.substring(index + 1);
            Log.d(TAG, "newPicUrl name:" + newPicUrl);
            final String targetLocalPath = mContext.getFilesDir().getAbsolutePath() +
                    "/" + "ADVERT" + "/" + newPicUrl;
            Bitmap bitmap = getLoacalBitmap(targetLocalPath);
            if(bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }else {
                imageView.setBackgroundResource(mBannerImgIds[i]);
            }
        }
        mViewPagerAdapter = new MyViewPaperAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mOnPagerChangeListener);
        mViewPager.setCurrentItem(0);
    }

    private void setImageBackground(int selectItems) {
        for (int i = 0; i < mBannerIndicators.length; i++) {
            if (i == selectItems) {
                mBannerIndicators[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else {
                mBannerIndicators[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    private void updateMeasurementInfosInBackground() {
        ELookApplication app = (ELookApplication)getActivity().getApplication();
        int uid = app.getUserInfo().getUserId();
        ELServiceHelper helper = ELServiceHelper.get();
        boolean ret = helper.updateMeasurements(uid);
        if(ret == false){
            mHandler.sendEmptyMessage(MSG_UPDATED_MEASUREMENT_INFOS_ERROR);
        }
        //mHandler.sendEmptyMessage(MSG_UPDATED_MEASUREMENT_INFOS);
    }


    public void addNewMeasurement(){
        Intent intent = new Intent(mContext, AddMeasurementActivity.class);
        mContext.startActivity(intent);
    }


    private ViewPager.OnPageChangeListener mOnPagerChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setImageBackground(position % mBannerImgs.length);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    private GetDataTask mGetDataTask;
    private Dialog mProgressDialog = null;

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATED_TIME_OUT,UPDATED_TIME_OUT);
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
            updateMeasurementInfosInBackground();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mDeviceAdapter != null) {
                if(newlist!=null)
                newlist.clear();
                newlist = initDisplayMeasurements();
                mDeviceAdapter.setAllMeasurementInfo(newlist);
            }else{
                newlist=initDisplayMeasurements();
                mDeviceAdapter = new DeviceListAdapter(getActivity(), newlist);
            }
            mRecordsListView.setAdapter(mDeviceAdapter);
            isInitDataed=true;
            mHandler.removeMessages(MSG_UPDATED_TIME_OUT);
            mHandler.sendEmptyMessage(MSG_DIMISS_PROGRESS_DIALOG);
        }
    }

    class MyViewPaperAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mBannerImgs.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(mBannerImgs[position % mBannerImgs.length]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(mBannerImgs[position % mBannerImgs.length], 0);
            return mBannerImgs[position % mBannerImgs.length];
        }
    }

    public static void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    public void recyle(){
        if(mBannerImgs==null)
            return;
        for(int i=0;i<mBannerImgs.length;i++){
            releaseImageViewResouce(mBannerImgs[i]);
        }
    }

    private void cleanMem(){
        Log.d(TAG,"cleanMem");
        for(int i=0;i<mBannerImgs.length;i++){
            releaseImageViewResouce(mBannerImgs[i]);
        }
        mBannerImgs=null;
        newlist=null;
        app=null;
        mDatabaseHelper=null;
        //mRootView=null;
    }

    @Override
    public void onDestroyView() {
        cleanMem();
        super.onDestroyView();
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }
}
