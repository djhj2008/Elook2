package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

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
public class UserInfo implements Parcelable {
    private static final String TAG = "UserInfo";
    private static final int FLAG_INVALIDA_ID = 1 << 1;
    private static final int FLAG_INVALIDA_USER_NAME = 1 << 2;
    private static final int FLAG_INVALIDA_ALIAS_NAME = 1 << 3;
    private static final int FLAG_INVALIDA_USER_EMAIL = 1 << 4;
    private static final int FLAG_INVALIDA_PWD = 1 << 5;
    private static final int FLAG_INVALIDA_REGISTE_TIME = 1 << 6;
    private static final int FLAG_INVALIDA_LOGIN_STATUS = 1 << 7;
    private static final int FLAG_INVALIDA_COMMUNITY_ID = 1 << 8;
    private static final int FLAG_INVALIDA_REGION = 1 << 9;
    private static final int FLAG_INVALIDA_PROVINCE = 1 << 10;
    private static final int FLAG_INVALIDA_CITY = 1 << 11;
    private static final int FLAG_INVALIDA_AREA = 1 << 12;
    private static final int FLAG_INVALIDA_ADDRESS = 1 << 13;


    private int mUserId;
    private String mUserPhoneName = "";
    private String mUserAliasName = "";
    private String mUserEmail = "";
    private String mUserPasswd = "";
    private int mUserRegisteTime;
    private int mUserLoginStatus;
    private int mUserCommunityId;
    private String mUserRegion = ""; //for GPS
    private String mProvince = "";
    private String mCity = "";
    private String mArea = ""; //area
    private String mAddress = "";

    private boolean isLastLogin = false;


    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.ClassLoaderCreator<UserInfo>(){
        @Override
        public UserInfo createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public UserInfo(){};

    public UserInfo(Parcel parcel){
        readFromParcel(parcel);
    }

    public int parserUserInfoJson(JSONObject userinfoJson){
        int parserStatus = -1;
        try {
            String tmpStr = "";
            tmpStr = userinfoJson.getString("enduser_enduser_id");
            if(tmpStr == null || (tmpStr != null && tmpStr.isEmpty()) ||
                    !isAvaliableInteger(tmpStr)){
                parserStatus |= FLAG_INVALIDA_ID;
            } else {
                mUserId = Integer.parseInt(tmpStr);
            }

            tmpStr = userinfoJson.getString("enduser_phone_name");
            if(tmpStr == null || (tmpStr != null && !ELUtils.isPhoneNumberAvaliable(tmpStr))){
                parserStatus |= FLAG_INVALIDA_USER_NAME;
            } else {
                mUserPhoneName = tmpStr;
            }

            mUserAliasName = userinfoJson.getString("enduser_alias_name");
            mUserEmail = userinfoJson.getString("enduser_mail");
            mUserPasswd = userinfoJson.getString("enduser_manager_pwd");


            tmpStr = userinfoJson.getString("enduser_register_time");
            if(tmpStr == null || (tmpStr != null && tmpStr.isEmpty())){
                parserStatus |= FLAG_INVALIDA_REGISTE_TIME;
            } else {
                mUserRegisteTime = (int)ELUtils.convertLocaleStringTOTimeStamp(Constant.DATE_FORMAT, tmpStr);
            }

            tmpStr = userinfoJson.getString("enduser_login_status");
            if(tmpStr == null ||  !isAvaliableInteger(tmpStr)){
                parserStatus |= FLAG_INVALIDA_LOGIN_STATUS;
            } else {
                mUserLoginStatus = Integer.parseInt(tmpStr);
            }

            tmpStr = userinfoJson.getString("enduser_community_id");
            if(tmpStr == null ||  !isAvaliableInteger(tmpStr)){
                parserStatus |= FLAG_INVALIDA_COMMUNITY_ID;
            } else {
                mUserCommunityId = Integer.parseInt(tmpStr);
            }

            mUserRegion = userinfoJson.getString("enduser_region");
            mProvince = userinfoJson.getString("enduser_province");
            mCity = userinfoJson.getString("enduser_city");
            mArea = userinfoJson.getString("enduser_area");
            mAddress = userinfoJson.getString("enduser_addr");

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

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public void setUserPhoneName(String mUserPhoneName) {
        this.mUserPhoneName = mUserPhoneName;
    }

    public void setUserAliasName(String mUserAliasName) {
        this.mUserAliasName = mUserAliasName;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public void setUserPasswd(String mUserPasswd) {
        this.mUserPasswd = mUserPasswd;
    }

    public void setUserRegisteTime(int mUserRegisteTime) {
        this.mUserRegisteTime = mUserRegisteTime;
    }

    public void setUserLoginStatus(int mUserLoginStatus) {
        this.mUserLoginStatus = mUserLoginStatus;
    }

    public void setUserCommunityId(int mUserCommunityId) {
        this.mUserCommunityId = mUserCommunityId;
    }

    public void setUserRegion(String mUserRegion) {
        this.mUserRegion = mUserRegion;
    }

    public void setProvince(String mProvince) {
        this.mProvince = mProvince;
    }

    public void setCity(String mCity) {
        this.mCity = mCity;
    }

    public void setArea(String mArea) {
        this.mArea = mArea;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public void setLastLogin(boolean islastlogin){
        this.isLastLogin = islastlogin;
    }

    public boolean getIsLastLogin(){
        return this.isLastLogin;
    }

    public int getUserId() {
        return mUserId;
    }

    public String getUserPhoneName() {
        return mUserPhoneName;
    }

    public String getUserAliasName() {
        return mUserAliasName;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public String getUserPasswd() {
        return mUserPasswd;
    }

    public int getUserRegisteTime() {
        return mUserRegisteTime;
    }

    public int getUserLoginStatus() {
        return mUserLoginStatus;
    }

    public int getUserCommunityId() {
        return mUserCommunityId;
    }

    public String getUserRegion() {
        return mUserRegion;
    }

    public String getProvince() {
        return mProvince;
    }

    public String getCity() {
        return mCity;
    }

    public String getArea() {
        return mArea;
    }

    public String getAddress() {
        return mAddress;
    }

    public void readFromParcel(Parcel parcel){
        Log.d(TAG, "readFromParcel");
        this.mUserId = parcel.readInt();
        this.mUserPhoneName = parcel.readString();
        this.mUserAliasName = parcel.readString();;
        this.mUserEmail = parcel.readString();;
        this.mUserPasswd = parcel.readString();;
        this.mUserRegisteTime = parcel.readInt();
        this.mUserLoginStatus = parcel.readInt();
        this.mUserCommunityId = parcel.readInt();
        this.mUserRegion = parcel.readString();;
        this.mProvince = parcel.readString();;
        this.mCity = parcel.readString();;
        this.mArea = parcel.readString();;
        this.mAddress = parcel.readString();;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel");
        dest.writeInt(this.mUserId);
        dest.writeString(this.mUserPhoneName);
        dest.writeString(this.mUserAliasName);
        dest.writeString(this.mUserEmail);
        dest.writeString(this.mUserPasswd);
        dest.writeInt(this.mUserRegisteTime);
        dest.writeInt(this.mUserLoginStatus);
        dest.writeInt(this.mUserCommunityId);
        dest.writeString(this.mUserRegion);
        dest.writeString(this.mProvince);
        dest.writeString(this.mCity);
        dest.writeString(this.mArea);
        dest.writeString(this.mAddress);
    }
}
