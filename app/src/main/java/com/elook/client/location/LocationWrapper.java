package com.elook.client.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.poisearch.PoiSearch;


/**
 * Created by haiming on 5/20/16.
 */
public class LocationWrapper {
    private static final String TAG = "LocationWrapper";
    private static final int LOCATION_UPDATE_DEALY = 10 * 1000;//ONE HOUR
    private static final int MIN_LOCATION_DISTANCE = 10;// IN METERS
    LocationManager mLocationManager;
    String mLocationProvider;
    double mLatitude, mLongitude;
    GeocodeSearch mGeocoderSearch;
    LatLonPoint mLatLonPoint;
    private Context mContext;


    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;


    public LocationWrapper(Context context){
        this.mContext = context;

        locationClient = new AMapLocationClient(mContext);
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        // 设置定位监听
        locationClient.setLocationListener(mAMapLocationListener);
        initOption();
    }

    // 根据控件的选择，重新设置定位参数
    private void initOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /*
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(false);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(true);

        locationOption.setHttpTimeOut(30000);

    }

    private void startToLocate() {
        Log.d(TAG, "startToLocate");
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    private void stopLocate(){
        locationClient.stopLocation();
    }

    AMapLocationListener mAMapLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            Log.d("doujun","aMapLocation:"+aMapLocation.toString());
            if(aMapLocation.getErrorCode()!=0){
                mLocationReachedListener.onLocationChanged(null,
                        null,
                        null,
                        null,
                        null,
                        null,0,0);
                stopLocate();
            }else {
                mLocationReachedListener.onLocationChanged(aMapLocation.getProvince(),
                        aMapLocation.getCity(),
                        aMapLocation.getDistrict(),
                        aMapLocation.getStreet(),
                        aMapLocation.getStreetNum(),
                        aMapLocation.getAoiName(),
                        aMapLocation.getLatitude(),
                        aMapLocation.getLongitude());
                stopLocate();
            }
        }
    };


    private OnLocationChangedListener mLocationReachedListener;
    public void queryCurrentLocation(OnLocationChangedListener listener) {
        startToLocate();
        mLocationReachedListener = listener;
        Log.d(TAG, "queryCurrentLocation ");
    }

    public interface OnLocationChangedListener{
        void onLocationChanged(String province, String city, String township, String street, String streetNumber,String building,double latitude,double longitude);
    }
}
