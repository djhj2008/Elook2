package com.elook.client.adapter;

import android.widget.BaseAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.el.AddMeasurementActivity;
import com.elook.client.el.MeasurementRecordsActivity;
import com.elook.client.el.UnBindDeviceActivity;
import com.elook.client.ui.RecordPanel;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementInfo.MeasurementType;

import java.util.List;

/**
 * Created by haiming on 5/25/16.
 */
public class DeviceListAdapter extends BaseAdapter {
    private List<MeasurementInfo> mAllMeasurementInfo;
    private Context mContext;
    private static final String TAG = "DeviceListAdapter";

    public DeviceListAdapter(Context context, List<MeasurementInfo> infos) {
        mContext = context;
        mAllMeasurementInfo = infos;
    }

    public void setAdapterList(List<MeasurementInfo> infos) {
        mAllMeasurementInfo = null;
        mAllMeasurementInfo = infos;
    }


    public void setAllMeasurementInfo(List<MeasurementInfo> allMeasurementInfo) {
        this.mAllMeasurementInfo = allMeasurementInfo;
    }

    @Override
    public int getCount() {
        return mAllMeasurementInfo.size();
    }

    @Override
    public MeasurementInfo getItem(int position) {
        return mAllMeasurementInfo.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return MeasurementType.TYPES_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        int type = -1;
        MeasurementInfo measurementInfo = mAllMeasurementInfo.get(position);
        return measurementInfo.getType();
    }


    private void initCommonInfo(MeasurementInfo info, HolderView view) {
        //MeasurementRecord record = info.getLastRecord();
        int CurValue = info.getCurValue();
        view.mMeasurementIdTV.setText(String.format(mContext.getString(R.string.home_measurement_id), info.getDeviceId()));
        if (CurValue >= 0) {
            view.mMeasurementRecordValueRP.setRecordValue(CurValue);
        }else{
            view.mMeasurementRecordValueRP.setRecordValue(0);
        }
        view.mMeasurementUsageTV.setText(String.format(mContext.getString(R.string.home_measurement_usage),
                info.getYedValue(), info.getWeekValue()));
        view.mMeasurementNickNameTV.setText(info.getAlisaName());
        view.mMeasurementAddressTV.setText(info.getLocation());
        if (info.getDeviceState() != MeasurementInfo.MEASUREMENT_STATE_CONFIG_SUCCESS) {
            view.mMeasurementWarningWrapperLL.setVisibility(View.VISIBLE);
            view.mMeasurementWarningTV.setText(R.string.device_state_error);
        }else{
            view.mMeasurementWarningWrapperLL.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView view = null;
        AddNewDeviceView addDeviceView = null;
        final MeasurementInfo measurementInfo = mAllMeasurementInfo.get(position);
        if(measurementInfo == null) return convertView;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case MeasurementType.WATER_METER:// WATER_METER
                case MeasurementType.ELECTRIC_METER://ELECTRIC_METER
                    long start_time = System.currentTimeMillis();
                    view = new HolderView();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.measurement_item, null);
                    view.mMeasurementCoreInfoRL = (RelativeLayout)convertView.findViewById(R.id.home_measurement_core);
                    view.mMeasurementIconIV = (ImageView)convertView.findViewById(R.id.home_measurement_core_device_icon);
                    view.mMeasurementInfoWrapperLL = (LinearLayout)convertView.findViewById(R.id.home_measurement_info);
                    view.mMeasurementIdTV = (TextView)convertView.findViewById(R.id.home_measurement_id);
                    view.mMeasurementUsageTV = (TextView)convertView.findViewById(R.id.home_measurement_usage);
                    view.mMeasurementRecordValueRP = (RecordPanel)convertView.findViewById(R.id.home_measurement_record_panel);
                    view.mMeasurementNickNameTV = (TextView)convertView.findViewById(R.id.home_measurement_nick_name);
                    view.mMeasurementAddressTV = (TextView)convertView.findViewById(R.id.home_measurement_address);
                    view.mMeasurementWarningWrapperLL = (LinearLayout)convertView.findViewById(R.id.home_measurement_warning_wrapper);
                    view.mMeasurementWarningTV = (TextView)convertView.findViewById(R.id.home_measurement_warning);
                    view.mMeasurementEnterIV = (ImageView)convertView.findViewById(R.id.home_measurement_enter);
                    long end_time = System.currentTimeMillis();
                    long spend_time = end_time - start_time;
                    Log.i(TAG, "mView.draw: spend_time = " + spend_time);
                    convertView.setTag(view);
                    break;
                case MeasurementType.MOCK_ADD_DEVICE: //MOCK_ADD_DEVICE
                    addDeviceView = new AddNewDeviceView();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.add_device_view, null);
                    TextView addDeviceTV = (TextView)convertView.findViewById(R.id.home_measurement_add_device);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, AddMeasurementActivity.class);
                            mContext.startActivity(intent);
                        }
                    });
                    convertView.setTag(addDeviceView);
                    break;
                case -1: //UNKNOWN
                    break;
            }
        } else {
            switch (type) {
                case MeasurementType.WATER_METER:// WATER_METER
                case MeasurementType.ELECTRIC_METER://ELECTRIC_METER
                    view = (HolderView) convertView.getTag();
                    break;
                case MeasurementType.MOCK_ADD_DEVICE: //MOCK_ADD_DEVICE
                    addDeviceView = (AddNewDeviceView) convertView.getTag();
                    break;
                default:
                    break;
            }
        }




        if (type == MeasurementType.WATER_METER || type == MeasurementType.ELECTRIC_METER) {
            initCommonInfo(measurementInfo, view);
            view.mMeasurementCoreInfoRL.setOnClickListener(new MeasurementDetailClickListener(measurementInfo.getDeviceId()));
            view.mMeasurementCoreInfoRL.setOnLongClickListener(new MeasurementDetailLongClickListener(measurementInfo.getDeviceId()));
        }
        return convertView;
    }

    private class HolderView {
        RelativeLayout mMeasurementCoreInfoRL;// home_measurement_core
        ImageView mMeasurementIconIV;
        LinearLayout mMeasurementInfoWrapperLL;
        TextView mMeasurementIdTV;
        TextView mMeasurementUsageTV;
        RecordPanel mMeasurementRecordValueRP;
        TextView mMeasurementNickNameTV;
        TextView mMeasurementAddressTV;
        ImageView mMeasurementEnterIV;

        LinearLayout mMeasurementWarningWrapperLL;
        TextView mMeasurementWarningTV;
    }

    private class MeasurementDetailClickListener implements View.OnClickListener {
        private int mClickedDeviceId;

        public MeasurementDetailClickListener(int deviceId) {
            mClickedDeviceId = deviceId;
        }

        @Override
        public void onClick(View v) {
            Log.d("DeviceAdapter", "onClick deviceId = "+mClickedDeviceId);
            Intent intent = new Intent(mContext, MeasurementRecordsActivity.class);
            intent.putExtra("deviceid", mClickedDeviceId+"");
            mContext.startActivity(intent);
        }
    }

    private class MeasurementDetailLongClickListener implements View.OnLongClickListener {
        private int mClickedDeviceId;

        public MeasurementDetailLongClickListener(int deviceId) {
            mClickedDeviceId = deviceId;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("DeviceAdapter", "onClick deviceId = "+mClickedDeviceId);
            Intent intent = new Intent(mContext, UnBindDeviceActivity.class);
            intent.putExtra("deviceid", mClickedDeviceId+"");
            mContext.startActivity(intent);
            return true;
        }
    }

    private class AddNewDeviceView {
        TextView mAddNewDeviceTextView;
    }

}
