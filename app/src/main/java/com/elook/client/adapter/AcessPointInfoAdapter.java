package com.elook.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.user.AccessPointInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 3/7/16.
 */
public class AcessPointInfoAdapter extends BaseAdapter {
    private List<AccessPointInfo> mAllApInfos = new ArrayList<>();
    private Context mContext;
    public AcessPointInfoAdapter(Context context){
        mContext = context;
    }

    public AcessPointInfoAdapter(Context context, List<AccessPointInfo> infos){
        mContext = context;
        mAllApInfos = infos;
    }

    public void setAdapterList(List<AccessPointInfo> infos){
        mAllApInfos = infos;
    }

    @Override
    public int getCount() {
        return mAllApInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mAllApInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WifiResultView view = null;
        AccessPointInfo apInfo = mAllApInfos.get(position);

        if (convertView == null){
            view = new WifiResultView();
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_scan_result_item, null);
            view.mWifiSignalLevelView = (ImageView)convertView.findViewById(R.id.wifi_level);
            view.mWifiSSIDView = (TextView)convertView.findViewById(R.id.wifi_ssid);
            view.mWifiConnectedStateView = (TextView)convertView.findViewById(R.id.wifi_connected_state);
            convertView.setTag(view);
        } else {
            view = (WifiResultView)convertView.getTag();
        }
        view.mWifiSignalLevelView.setImageDrawable(apInfo.getimg());
        view.mWifiSSIDView.setText(apInfo.getName());
        if(apInfo.getconnectedflag()){
            view.mWifiConnectedStateView.setVisibility(View.VISIBLE);
        } else {
            view.mWifiConnectedStateView.setVisibility(View.GONE);
        }

        return convertView;
    }

    class WifiResultView {
        ImageView mWifiSignalLevelView;
        TextView mWifiSSIDView;
        TextView mWifiConnectedStateView;
    }
}
