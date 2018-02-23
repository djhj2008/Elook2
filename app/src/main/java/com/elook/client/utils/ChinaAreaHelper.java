package com.elook.client.utils;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.elook.client.R;

public class ChinaAreaHelper {
	private static final String TAG = "ChinaAreaHelper";
	private static final String TAG_PROVINCE = "province";
	private static final String TAG_CITY = "City";
	private static final String TAG_COUNTY = "Piecearea";

	private static final String ATTR_PROVINCE = "province";
	private static final String ATTR_CITY = "City";
	private static final String ATTR_COUNT = "Piecearea";

	private static final String TAG_ZONE1 = "市辖区";
	private static final String TAG_ZONE2 = "县";
	private static boolean mProvinceStarted = false;
	private static boolean mCityStarted = false;
	private static boolean mCountyStarted = false;

	private HashMap<String, Region> mProvinces = new HashMap<>();
	private HashMap<String, Region> mCities = new HashMap<>();
	private HashMap<String, Region> mCounties = new HashMap<>();

	static XmlPullParser mParser = null;

	public ChinaAreaHelper(Context context) throws XmlPullParserException, IOException {
		mProvinces.clear();
		mCities.clear();
		mCounties.clear();
		XmlPullParserFactory factory;

		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		mParser = factory.newPullParser();

		mParser.setInput(context.getResources().openRawResource(R.raw.china_area), "UTF-8");

		int eventType = mParser.getEventType();
		Log.d(TAG, "start to parser the xml");
		Region provinceRegion = null;
		Region cityRegion = null;
		Region countyRegion = null;
		String provinceName = "";
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if (mParser.getName().equals(TAG_PROVINCE)) {
					mProvinceStarted = true;
					provinceName = mParser.getAttributeValue(null, ATTR_PROVINCE);
					if (provinceName != null) {
						provinceRegion = new Region(provinceName);
						// mProvinces.add(, provinceRegion);
						mProvinces.put(provinceName, provinceRegion);
					}
				}

				if (mProvinceStarted && mParser.getName().equals(TAG_CITY)) {
					mCityStarted = true;
					String cityName = mParser.getAttributeValue(null, ATTR_CITY);
					if (cityName != null) {
						if (cityName.equals(TAG_ZONE1) || cityName.equals(TAG_ZONE2)){
							cityName = provinceName;
							if(provinceRegion.containChildRegion(cityName)) break;
						}
							
						cityRegion = new Region(cityName);
						if (provinceRegion != null) {
							provinceRegion.mChildRegions.add(cityRegion);
							mCities.put(cityName, cityRegion);
						}
					}
				}

				if (mCityStarted && mParser.getName().equals(TAG_COUNTY)) {
					mCountyStarted = true;
					String countyName = mParser.getAttributeValue(null, ATTR_COUNT);
					if (countyName != null) {
						countyRegion = new Region(countyName);
						if (cityRegion != null) {
							cityRegion.mChildRegions.add(countyRegion);
						}
					}
				}

				break;

			case XmlPullParser.END_TAG:
				if (mParser.getName().equals(TAG_COUNTY)) {
					mCountyStarted = false;
				}

				if (mParser.getName().equals(TAG_CITY)) {
					mCityStarted = false;
				}

				if (mParser.getName().equals(TAG_PROVINCE)) {
					mProvinceStarted = false;
				}
				break;

			default:
				break;
			}

			eventType = mParser.next();
		}

	}
	
	public void dumpProvinces(){
		ArrayList<String> provinces = getProvince();
		for(String provinceName: provinces){
			Log.d(TAG, provinceName);
		}
	}
	
	public void dumpCitiesOfProvince(String province){
		ArrayList<String> cities = getCitiesOfProvince(province);
		for(String citiyName: cities){
			Log.d(TAG, citiyName);
		}
	}

	public void dumpCountiesOfCity(String cityName){
		ArrayList<String> counties = getCountiesOfCity(cityName);
		for(String countyName: counties){
			Log.d(TAG, countyName);
		}
	}
	
	public ArrayList<String> getProvince() {
		ArrayList<String> provinces = new ArrayList<>();
		for(String provinceName : mProvinces.keySet()){
			provinces.add(provinceName);
		}
		return provinces;
	}
	
	public ArrayList<String> getCitiesOfProvince(String provinceName){
		ArrayList<String> cities = new ArrayList<>();
		Region provinceRegion = mProvinces.get(provinceName.trim());
		for(Region cityRegion : provinceRegion.mChildRegions){
			cities.add(cityRegion.mRegionName);
		}
		return cities;
	}

	public ArrayList<String> getCountiesOfCity(String cityName){
		ArrayList<String> counties = new ArrayList<>();
		Region cityRegion = mCities.get(cityName.trim());
		for(Region countyRegion : cityRegion.mChildRegions){
			counties.add(countyRegion.mRegionName);
		}
		return counties;
	}

}
