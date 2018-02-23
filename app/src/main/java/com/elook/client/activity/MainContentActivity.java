package com.elook.client.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.fragments.MainFragment;
import com.elook.client.fragments.MineInfoFragment;
import com.elook.client.fragments.NotificationFragment;
import com.elook.client.fragments.ServiceFragment;
import com.elook.client.ui.MyViewPager;

/**
 * Created by haiming on 5/21/16.
 */
public class MainContentActivity extends FragmentActivity {
    private static final String TAG = "MainContentActivity";
    public static final String ACTION_LOCATION = "com.elook.client.LOCATION";

    MyViewPager mViewPager;
    FragmentPagerAdapter mFragmentAdapter;
    int mCurNavIndex = -1;

    private MainFragment mMainFragment;
    private NotificationFragment mNotificationFragment;
    private ServiceFragment mServiceFragment;
    private MineInfoFragment mMineInfoFragment;

    TextView[] mNavigations;
    Fragment[] mFragments;
    String[] mFragmentTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ELookApplication application = (ELookApplication) getApplication();
        application.addActivity(this);
        setContentView(R.layout.activity_main_content);
        initViews();
        setupViewPager();

    }

    private void initViews() {

        mNavigations = new TextView[]{
                (TextView)findViewById(R.id.navigation_home),
                (TextView)findViewById(R.id.navigation_message),
                (TextView)findViewById(R.id.navigation_serve),
                (TextView)findViewById(R.id.navigation_my),
        };

        mMainFragment = new MainFragment();
        mNotificationFragment = new NotificationFragment();
        mServiceFragment = new ServiceFragment();
        mMineInfoFragment = new MineInfoFragment();

        mFragments = new Fragment[] {
                mMainFragment,
                mNotificationFragment,
                mServiceFragment,
                mMineInfoFragment};

        mFragmentTags = new String[]{
                getString(R.string.main_fragment_tag),
                getString(R.string.notification_tag),
                getString(R.string.service_tag),
                getString(R.string.mine_tag),
        };

        mNavigations[0].setSelected(true);
        mCurNavIndex = 0;
    }


    private void setupViewPager() {
        mViewPager = (MyViewPager) findViewById(R.id.fragment);

        mViewPager.setScrollble(false);

        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, previousIndex = "+mCurNavIndex+",  clickedIndex = "+position);
                mNavigations[mCurNavIndex].setSelected(false);
                mNavigations[position].setSelected(true);
                mCurNavIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });
    }

    public void onTabClicked(View v) {
        Log.d(TAG, "onTabClicked");
        int id = v.getId();
        int index = -1;
        switch (id) {
            case R.id.navigation_home:
                index = 0;
                break;
            case R.id.navigation_message:
                index = 1;
                break;
            case R.id.navigation_serve:
                index = 2;
                break;
            case R.id.navigation_my:
                index = 3;
                break;
        }
        mViewPager.setCurrentItem(index,false);
    }

    class FragmentAdapter extends FragmentPagerAdapter {

        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }

    public void back(){
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        mMainFragment.recyle();
        //mMineInfoFragment.recycle();
        mMainFragment=null;
        mNotificationFragment=null;
        mServiceFragment=null;
        mMineInfoFragment=null;
        mViewPager=null;
        mFragments=null;
        mNavigations=null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            System.exit(0);
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
