package com.elook.client.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class Region {
	public String mRegionName = "";
//	public String mRegionLevel = "";
//	public HashMap<String, ArrayList<Region>> mChildRegionMap = new HashMap<>();
	public ArrayList<Region> mChildRegions = new ArrayList<>();
	
	public void addChildRegion(Region region){
		if(region !=null){
			mChildRegions.add(region);
		}
	}
	
	public Region (String regionName) {
		mRegionName = regionName;
	}
	
	public boolean containChildRegion(String childRegionName){
		boolean isContained = false;
		for(Region region : mChildRegions){
			if(region.mRegionName.equals(childRegionName))return true;
		}
		return false;
	}

}
