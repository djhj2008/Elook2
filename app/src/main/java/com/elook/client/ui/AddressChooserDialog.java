package com.elook.client.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;


import com.elook.client.R;
import com.elook.client.utils.ChinaAreaHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by haiming on 5/28/16.
 */
public class AddressChooserDialog extends AlertDialog implements View.OnClickListener, WheelView.OnWheelViewListener{

    protected static final int AREA_SELECTOR_DEFAULT_INDEX = 2;
    protected static final int AREA_SELECTOR_OFFSET = 2;

    private View mRootView;
    private Context mContext;
    WheelView mProvinceView, mCityView, mAreaView;
    Button mCancelButton, mConfirmButton;
    ChinaAreaHelper mAreaHelper = null;

    ArrayList<String> mProvinces, mCities, mAreas;
    private String mProvinceName, mCityName, mAreaName;
    private String mDefaultProvinceName, mDefaultCityName, mDefaultAreaName;

    private OnAddressChoosedListener mListener;

    public AddressChooserDialog(Context context) {
        super(context);
        this.mContext = context;
        initDialogUI();
    }

    private void initDialogUI(){
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_address_chooser, null);
        mProvinceView = (WheelView)mRootView.findViewById(R.id.addres_province);
        mCityView = (WheelView)mRootView.findViewById(R.id.addres_city);
        mAreaView = (WheelView)mRootView.findViewById(R.id.addres_area);

        mCancelButton = (Button)mRootView.findViewById(R.id.address_cancel);
        mConfirmButton = (Button)mRootView.findViewById(R.id.address_ok);

        mProvinceView.setOnWheelViewListener(this);
        mCityView.setOnWheelViewListener(this);
        mAreaView.setOnWheelViewListener(this);

        mCancelButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);

        mAreaHelper = null;
        try {
            mAreaHelper = new ChinaAreaHelper(mContext);
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initDefaultAddress();

        mProvinces = mAreaHelper.getProvince();
        mCities = mAreaHelper.getCitiesOfProvince(mDefaultProvinceName);
        mAreas = mAreaHelper.getCountiesOfCity(mDefaultCityName);

        mProvinceView.setOffset(AREA_SELECTOR_OFFSET);
        mProvinceView.setItems(mProvinces);
        mProvinceView.setSeletion(AREA_SELECTOR_DEFAULT_INDEX);

        mCityView.setOffset(AREA_SELECTOR_OFFSET);
        mCityView.setItems(mCities);
        mCityView.setSeletion(AREA_SELECTOR_DEFAULT_INDEX);

        mAreaView.setOffset(AREA_SELECTOR_OFFSET);
        mAreaView.setItems(mAreas);
        mAreaView.setSeletion(AREA_SELECTOR_DEFAULT_INDEX);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.address_cancel:
                dismiss();
                break;
            case R.id.address_ok:
                if(mListener != null){
                    mListener.onAddressChoosed(mProvinceName, mCityName, mAreaName);
                }
                dismiss();
                break;
        }
    }


    private void initDefaultAddress(){
        ArrayList<String> areas = null;

        String provinceName = "";
        String cityName = "";
        String countyName = "";
        areas = mAreaHelper.getProvince();
        if(areas.size() > AREA_SELECTOR_DEFAULT_INDEX){
            provinceName = areas.get(AREA_SELECTOR_DEFAULT_INDEX);
        } else {
            provinceName = areas.get(0);
        }
        mProvinceName = mDefaultProvinceName = provinceName;

        areas = mAreaHelper.getCitiesOfProvince(provinceName);
        if(areas.size() > AREA_SELECTOR_DEFAULT_INDEX){
            cityName = areas.get(AREA_SELECTOR_DEFAULT_INDEX);
        } else {
            cityName = areas.get(0);
        }
        mCityName = mDefaultCityName = cityName;

        areas = mAreaHelper.getCountiesOfCity(cityName);
        if(areas.size() > AREA_SELECTOR_DEFAULT_INDEX){
            countyName = areas.get(AREA_SELECTOR_DEFAULT_INDEX);
        } else {
            countyName = areas.get(0);
        }
        mAreaName = mDefaultAreaName = countyName;
    }

    @Override
    public void onSelected(View v, int selectedIndex, String item) {
        int id = v.getId();
        switch (id){
            case R.id.addres_province:
                mProvinceName = item;
                mCities = mAreaHelper.getCitiesOfProvince(item);
                mCityView.setItems(mCities);
                if(mCities.size() > AREA_SELECTOR_DEFAULT_INDEX){
                    mCityName = mCities.get(AREA_SELECTOR_DEFAULT_INDEX);
                } else {
                    mCityName = mCities.get(0);
                }


                mAreas = mAreaHelper.getCountiesOfCity(mCityName);
                mAreaView.setItems(mAreas);
                if(mAreas.size() > AREA_SELECTOR_DEFAULT_INDEX){
                    mAreaName = mAreas.get(AREA_SELECTOR_DEFAULT_INDEX);
                } else {
                    mAreaName = mAreas.get(0);
                }
                break;
            case R.id.addres_city:
                mCityName = item;
                mAreas = mAreaHelper.getCountiesOfCity(mCityName);
                mAreaView.setItems(mAreas);
                if(mAreas.size() > AREA_SELECTOR_DEFAULT_INDEX){
                    mAreaName = mAreas.get(AREA_SELECTOR_DEFAULT_INDEX);
                } else {
                    mAreaName = mAreas.get(0);
                }
                break;
            case R.id.addres_area:
                mAreaName = item;
                break;
        }
    }

    @Override
    public void show() {
        super.show();
        setContentView(mRootView);
    }

    public void registeAddressChoosedListener(OnAddressChoosedListener listener){
        mListener = listener;
    }

    public interface OnAddressChoosedListener{
        void onAddressChoosed(String province, String city, String area);
    }
}
