package com.elook.client.location;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;

/**
 * Created by xy on 7/25/16.
 */
public class PoiAroundSearch implements PoiSearch.OnPoiSearchListener {
    private static final String TAG = "PoiAroundSearch";
    private Context mContext;
    double mLititude;
    double mLongitude;
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;
    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据
    int currentPage = 0;

    public  PoiAroundSearch(Context context ,double lititude,double longitude){
        this.mContext = context;
        this.mLititude = lititude;
        this.mLongitude = longitude;
    }

    public void doSearchQuery() {
        query = new PoiSearch.Query("", "商务住宅", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(100);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        if (mLititude != 0&& mLongitude!=0) {
            poiSearch = new PoiSearch(mContext, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(mLititude, mLongitude), 1000, true));//
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {
                // 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        for(PoiItem item:poiItems){
                            Log.d(TAG,item.toString());
                        }
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        for(SuggestionCity city:suggestionCities){
                            Log.d(TAG,city.toString());
                        }
                    } else {
                        Log.d(TAG,"Search fail.");
                    }
                }
                if (poiResult.getPageCount() - 1 > currentPage) {
                    currentPage++;
                    query.setPageNum(currentPage);// 设置查后一页
                    poiSearch.searchPOIAsyn();
                }
            } else {

            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
