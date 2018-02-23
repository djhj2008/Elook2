package com.elook.client.fragments;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elook.client.ELookApplication;
import com.elook.client.R;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.user.UserInfo;

/**
 * Created by haiming on 5/24/16.
 */
public class ServiceFragment extends Fragment {
    private static final String TAG = "ServiceFragment";
    private static final boolean DEBUG = true;
    private Context mContext;
    View mRootView;
    private boolean isInitDataed;
    private boolean isInitUIed;
    TextView service_sub_title_TV;
    ELookApplication app;
    String mLocation = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_service, container, false);
        service_sub_title_TV = (TextView) mRootView.findViewById(R.id.service_action_bar_sub_title);
        app = (ELookApplication)getActivity().getApplication();
        mLocation = app.getLocation();
        if(mLocation==null) {
            UserInfo info = app.getUserInfo();
            if(info!=null)
            service_sub_title_TV.setText(info.getAddress());
        }else{
            service_sub_title_TV.setText(mLocation);
        }
        return mRootView;
    }

    private void cleanMem(){
        Log.d(TAG,"cleanMem");
        app=null;
    }


    public void setLocation(String location){
        mLocation = location;
        if(service_sub_title_TV!=null)
        service_sub_title_TV.setText(mLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanMem();
    }
}
