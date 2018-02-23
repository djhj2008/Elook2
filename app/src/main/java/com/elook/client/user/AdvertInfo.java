package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by haiming on 3/18/16.
 */
/*
      "data": {
        "enduser_enduser_id": "19",
        "enduser_phone_name": "18201168378",
        "enduser_alias_name": null,
        "enduser_mail": null,
        "enduser_manager_pwd": "test",
        "enduser_register_time": "2016-05-30 15:07:10",
        "enduser_login_status": "1",
        "enduser_community_id": null,
        "enduser_region": null,
        "enduser_province": null,
        "enduser_city": null,
        "enduser_area": null,
        "enduser_addr": null
      }

*/
public class AdvertInfo implements Parcelable {
    private static final String TAG = "AdvertInfo";

    private int mAdvertId;
    private String mAdvertPicUrl = "";
    private String mAdvertUrl = "";
    private int mAdvertOpen;

    public static final Creator<AdvertInfo> CREATOR = new ClassLoaderCreator<AdvertInfo>(){
        @Override
        public AdvertInfo createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public AdvertInfo createFromParcel(Parcel source) {
            return new AdvertInfo(source);
        }

        @Override
        public AdvertInfo[] newArray(int size) {
            return new AdvertInfo[size];
        }
    };

    public AdvertInfo(){};

    public AdvertInfo(Parcel parcel){
        readFromParcel(parcel);
    }

    public int parserUserInfoJson(JSONObject userinfoJson){
        int parserStatus = -1;
        try {
            String tmpStr = "";
            tmpStr = userinfoJson.getString("easy_adv_autoid");
            if(tmpStr == null || tmpStr.isEmpty() ||
                    !isAvaliableInteger(tmpStr)){

            } else {
                mAdvertId = Integer.parseInt(tmpStr);
            }

            tmpStr = userinfoJson.getString("easy_adv_picurl");
            if(tmpStr == null || tmpStr.isEmpty() ){

            } else {
                mAdvertPicUrl = tmpStr;
            }

            tmpStr = userinfoJson.getString("easy_adv_advurl");
            if(tmpStr == null || tmpStr.isEmpty() ){

            } else {
                mAdvertUrl = tmpStr;
            }

            tmpStr = userinfoJson.getString( "easy_adv_open");
            if(tmpStr == null || tmpStr.isEmpty() ||
                    !isAvaliableInteger(tmpStr)){

            } else {
                mAdvertOpen = Integer.parseInt(tmpStr);
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
        return parserStatus;
    }


    private boolean isAvaliableInteger(String integerStr){
        boolean isAvaliable = true;
        if(integerStr == null || (integerStr != null && integerStr.length() <= 0)){
            return false;
        }
        for (int i = 0; i < integerStr.length(); i++){
            if(!Character.isDigit(integerStr.charAt(i))){
                isAvaliable = false;
                break;
            }
        }
        return isAvaliable;
    }

    public int getAdvertId() {
        return mAdvertId;
    }

    public String getAdvertPicUrl() {
        return mAdvertPicUrl;
    }

    public String getAdvertUrl() {
        return mAdvertUrl;
    }

    public int getAdvertOpen() {
        return mAdvertOpen;
    }

    public void setAdvertId(int mAdvertId) {
        this.mAdvertId = mAdvertId;
    }

    public void setAdvertPicUrl(String mAdvertPicUrl) {
        this.mAdvertPicUrl = mAdvertPicUrl;
    }

    public void setAdvertUrl(String mAdvertUrl) {
        this.mAdvertUrl = mAdvertUrl;
    }

    public void setAdvertOpen(int mAdvertOpen) {
        this.mAdvertOpen = mAdvertOpen;
    }

    public void readFromParcel(Parcel parcel){
        Log.d(TAG, "readFromParcel");
        this.mAdvertId = parcel.readInt();
        this.mAdvertPicUrl = parcel.readString();
        this.mAdvertUrl = parcel.readString();
        this.mAdvertOpen = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel");
        dest.writeInt(this.mAdvertId);
        dest.writeString(this.mAdvertPicUrl);
        dest.writeString(this.mAdvertUrl);
        dest.writeInt(this.mAdvertOpen);

    }
}
