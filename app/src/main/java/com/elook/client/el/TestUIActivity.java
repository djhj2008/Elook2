package com.elook.client.el;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.elook.client.R;
import com.elook.client.ui.RecordWrapper;
import com.elook.client.ui.RecordsLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 6/14/16.
 */
public class TestUIActivity extends Activity {

    ViewPager mCheckWayViewPager;
    MyViewPagerAdapter mViewPagerAdapter;
    LinearLayout[] mAllCheckWayLayouts = null;
    RecordsLayout[] mAllRecordsLayouts = null;
    RecordsLayout mSelectedRecordLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        RecordsLayout weekLayout = new RecordsLayout(this);
//        weekLayout.setIsDotPattern(false);
//        weekLayout.setRecordWrapper(dummyRecordWrappers());
//        RecordValueSurface recordValueSurface = new RecordValueSurface(this, false);
//        recordValueSurface.setDotCoord(new Point(200,200));
//        recordValueSurface.setRecordDate("5/1");
        setContentView(R.layout.activity_measurement_records);
        initViews();
    }


    private void initViews() {
//        mCheckWayViewPager = (ViewPager) findViewById(R.id.activity_measurement_record_check_way_viewpager);


        LinearLayout dayLayoutWrapper, weekLayoutWrapper, monthLayoutWrapper, yearLayoutWrapper;
//        dayLayoutWrapper = (LinearLayout) LayoutInflater.from(TestUIActivity.this).inflate(R.layout.day_layout, null);
        weekLayoutWrapper = (LinearLayout) LayoutInflater.from(TestUIActivity.this).inflate(R.layout.week_layout, null);
//        monthLayoutWrapper = (LinearLayout) LayoutInflater.from(TestUIActivity.this).inflate(R.layout.month_layout, null);
//        yearLayoutWrapper = (LinearLayout) LayoutInflater.from(TestUIActivity.this).inflate(R.layout.day_layout, null);

        mAllCheckWayLayouts = new LinearLayout[]{
//                dayLayoutWrapper,
                weekLayoutWrapper,
//                monthLayoutWrapper,
//                yearLayoutWrapper
        };

        mAllRecordsLayouts = new RecordsLayout[]{
//                (RecordsLayout) dayLayoutWrapper.findViewById(R.id.activity_measurement_record_day),
                (RecordsLayout) weekLayoutWrapper.findViewById(R.id.activity_measurement_record_week),
//                (RecordsLayout) monthLayoutWrapper.findViewById(R.id.activity_measurement_record_month),
//                (RecordsLayout) yearLayoutWrapper.findViewById(R.id.activity_measurement_record_day),
        };



        mSelectedRecordLayout = mAllRecordsLayouts[0];

        List<RecordWrapper> recordWrappers = null;
        recordWrappers = dummyRecordWrappers();
        Log.d("TestUI", "recordWrappers count = "+recordWrappers.size());

        mSelectedRecordLayout.setIsDotPattern(false);
        if (recordWrappers != null) mSelectedRecordLayout.setRecordWrapper(recordWrappers);

        mViewPagerAdapter = new MyViewPagerAdapter();
        mCheckWayViewPager.setAdapter(mViewPagerAdapter);
        mCheckWayViewPager.setCurrentItem(0);
    }

    private class MyViewPagerAdapter extends PagerAdapter {
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    private List<RecordWrapper> dummyRecordWrappers() {
        List<RecordWrapper> recordWrappers = new ArrayList<>();
        RecordWrapper wrapper = new RecordWrapper();

        wrapper.setValue(435);
        wrapper.setDate("5/1-5/7");
        recordWrappers.add(wrapper);

        wrapper = new RecordWrapper();
        wrapper.setValue(480);
        wrapper.setDate("5/8-5/14");
        recordWrappers.add(wrapper);

        wrapper = new RecordWrapper();
        wrapper.setValue(520);
        wrapper.setDate("5/15-5/21");
        recordWrappers.add(wrapper);

        wrapper = new RecordWrapper();
        wrapper.setValue(520);
        wrapper.setDate("5/22-5/28");
        recordWrappers.add(wrapper);

        return recordWrappers;
    }
}
