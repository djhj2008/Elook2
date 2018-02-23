package com.elook.client.user;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by haiming on 3/21/16.
 */
/*         {
          "device_auto_id": "37",
          "device_device_id": "123456789",
          "device_reg_time": "2016-06-01 17:08:31",
          "device_device_type": "2",
          "device_pay_id": null,
          "device_bettery_lev": null,
          "device_mob_url_pic": null,
          "device_dev_url_pic": null,
          "device_dev_url_errpic": null,
          "device_dev_config": null,
          "device_mob_state": null,
          "device_dev_error": null,
          "device_dev_save_time": null,
          "device_upl_state": null,
          "device_up_delay": "0",
          "device_dev_state": "0",
          "device_tmp_value": null,
          "device_ssid": null,
          "device_ssid_pwd": null,
          "device_date_interval": null,
          "device_device_flow": null,
          "device_dev_isaccess": "0"
        }

*/
public class MeasurementInfo implements Parcelable {
    private static final String TAG = "MeasurementInfo";
    private static final String NULL = "null";
    private static final int CONFIG_LENGHT = 59;

    private int mDeviceId;
    private long mRegTimestamp;
    private int mDeviceType;
    private String mPayId;
    private int mDeviceBatteryLevel;
    private String mMobUrlPic;
    private String mDeviceUrlPic;
    private String mDeviceUrlErrorPic;
    private String mConfig;
    private int mMobState;
    private int mError;
    private long mSaveTimestamp;
    private int mUplState;
    private int mUpdelay;
    private int mUpdelaySub;
    private int mDeviceState;
    private int mTmpValue;
    private String mSSID;
    private String mSSIDPWD;
    private int mDateInterval;
    private int mDeviceFlow;
    private int mIsAccess;
    private int mDevNotUseDay;
    private int mDevNotUseDayCount;
    private String mUserPhoneName;
    private String mAlisaName;
    private String mLocation;
    private int mLastValue;
    private int mYedValue;
    private int mWeekValue;

    public static final int MEASUREMENT_STATE_PREINIT = 0;
    public static final int MEASUREMENT_STATE_CONNECT= 1;
    public static final int MEASUREMENT_STATE_START_CONFIG = 2;
    public static final int MEASUREMENT_STATE_NORMAL_ERROR = 3;
    public static final int MEASUREMENT_STATE_CANNOT_PARSE = 4;
    public static final int MEASUREMENT_STATE_CONFIG_SUCCESS = 5;
    public static final int MEASUREMENT_STATE_PARSE_FAIL = 6;
    public static final int MEASUREMENT_STATE_NET_ERROR = 7;
    public static final int MEASUREMENT_STATE_ERROR = 8;
    public static final int MEASUREMENT_STATE_DEV_MIS = 9;
    public static final int MEASUREMENT_STATE_CONFIG_LED = 10;
    public static final int MEASUREMENT_STATE_MAX = 11;

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private int mType = MeasurementType.WATER_METER; //the meaning of mType is same to mDeviceType. Just different: mType is int, mDeviceType is String
    private CameraPara mCameraPara;
    private List<MeasurementRecord> mRecords = new ArrayList<>();

    public static final Parcelable.Creator<MeasurementInfo> CREATOR = new ClassLoaderCreator<MeasurementInfo>() {
        @Override
        public MeasurementInfo createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public MeasurementInfo createFromParcel(Parcel source) {
            return new MeasurementInfo(source);
        }

        @Override
        public MeasurementInfo[] newArray(int size) {
            return new MeasurementInfo[size];
        }
    };


    public MeasurementInfo() {
    }

    public MeasurementInfo(Parcel parcel) {
        readFromParcelable(parcel);
    }

    public MeasurementInfo(JSONObject data) {
        init(data);
    }

    private void init(JSONObject data) {
        if (data != null) {
            try {
                String tmpStr = "";
                this.mDeviceId = data.getInt("device_device_id");
                String timeStr = data.getString("device_reg_time");
                if (timeStr != null && !timeStr.isEmpty())
                    this.mRegTimestamp = ELUtils.convertLocaleStringTOTimeStamp(Constant.DATE_FORMAT, timeStr);
                this.mDeviceType = data.getInt("device_device_type");

                tmpStr = data.getString("device_pay_id");
                if (!tmpStr.equals(NULL)) this.mPayId = tmpStr;

                tmpStr = data.getString("device_bettery_lev");
                if (isAvaliableInteger(tmpStr)) this.mDeviceBatteryLevel = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_mob_url_pic");
                if (!tmpStr.equals(NULL)) this.mMobUrlPic = tmpStr;

                tmpStr = data.getString("device_dev_url_pic");
                if (!tmpStr.equals(NULL)) this.mDeviceUrlPic = tmpStr;

                tmpStr = data.getString("device_dev_url_errpic");
                if (!tmpStr.equals(NULL)) this.mDeviceUrlErrorPic = tmpStr;

                tmpStr = data.getString("device_dev_config");
                if (isConfigAvaliable(tmpStr)) {
                    this.mConfig = tmpStr;
                    parserConfig(mConfig);
                }

                tmpStr = data.getString("device_mob_state");
                if (isAvaliableInteger(tmpStr)) this.mMobState = Integer.parseInt(tmpStr);


                tmpStr = data.getString("device_dev_error");
                if (isAvaliableInteger(tmpStr)) this.mError = Integer.parseInt(tmpStr);

                timeStr = data.getString("device_dev_save_time");
                if (timeStr != null && !timeStr.equals(NULL))
                    this.mSaveTimestamp = ELUtils.convertLocaleStringTOTimeStamp(Constant.DATE_FORMAT, timeStr);

                tmpStr = data.getString("device_upl_state");
                if (isAvaliableInteger(tmpStr)) this.mUplState = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_up_delay");
                if (isAvaliableInteger(tmpStr)) this.mUpdelay = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_up_delay_sub");
                if (isAvaliableInteger(tmpStr)) this.mUpdelaySub = Integer.parseInt(tmpStr);
                Log.d(TAG, "startTime = "+mUpdelaySub+", endTime = "+mUpdelaySub);

                tmpStr = data.getString("device_dev_state");
                if (isAvaliableInteger(tmpStr)) this.mDeviceState = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_tmp_value");
                if (isAvaliableInteger(tmpStr)) this.mTmpValue = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_ssid");
                if (!tmpStr.equals(NULL)) this.mSSID = tmpStr;

                tmpStr = data.getString("device_ssid_pwd");
                if (!tmpStr.equals(NULL)) this.mSSIDPWD = tmpStr;

                tmpStr = data.getString("device_date_interval");
                if (isAvaliableInteger(tmpStr)) this.mDateInterval = Integer.parseInt(tmpStr);


                tmpStr = data.getString("device_device_flow");
                if (isAvaliableInteger(tmpStr)) this.mDeviceFlow = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_dev_isaccess");
                if (isAvaliableInteger(tmpStr)) this.mIsAccess = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_set_access");
                if (isAvaliableInteger(tmpStr)) this.mDevNotUseDay = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_record_day");
                if (isAvaliableInteger(tmpStr)) this.mDevNotUseDayCount = Integer.parseInt(tmpStr);

                tmpStr = data.getString("device_alias");
                if (!tmpStr.equals(NULL)) this.mAlisaName= tmpStr;

                tmpStr = data.getString("device_location");
                if (!tmpStr.equals(NULL)) this.mLocation= tmpStr;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        this.mType = this.mDeviceType;     // getMeasurementTypeWithDevTy(this.mDeviceId);
    }

    public void updateMeasurementInfo(JSONObject data) {
        init(data);
    }

    public MeasurementInfo(int devid, long time, int state) {
        this.mDeviceId = devid;
        this.mRegTimestamp = time;
        this.mDeviceState = state;
        this.mType = getMeasurementTypeWithDevTy(devid);
    }

    private boolean isAvaliableInteger(String integerStr){
        boolean isAvaliable = true;
        if(integerStr.length() <= 0){
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

    private boolean isConfigAvaliable(String config){
        boolean isAvaliable = true;
        if(config.length() != CONFIG_LENGHT){
            return false;
        }
        if(!isAvaliableInteger(config)){
            isAvaliable = false;
        }
        return isAvaliable;
    }

    private void parserConfig(String config) {
        this.mCameraPara = new CameraPara(config);

    }

    public void addRecord(MeasurementRecord record) {
        if (mRecords != null) {
            synchronized (mRecords){
                mRecords.add(record);
            }
        }
    }

    public void setCurValue(int value) {
        this.mLastValue = value;
    }

    public int getCurValue() {
        return this.mLastValue;
    }

    public void setYedValue(int mYedValue) {
        this.mYedValue = mYedValue;
    }

    public int getYedValue() {
        return mYedValue;
    }

    public void setWeekValue(int mWeekValue) {
        this.mWeekValue = mWeekValue;
    }

    public int getWeekValue() {
        return mWeekValue;
    }

    public List<MeasurementRecord> getPeriodRecords(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return mRecords;
        }
        //Log.d(TAG, "startTime = "+startTime+", endTime = "+endTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int start = -1, end = -1;
        try {
            start = (int) (format.parse(startTime.trim()).getTime() / 1000);
            end = (int) (format.parse(endTime.trim()).getTime() / 1000);
            end = end + DAY_IN_SECONDS;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (start < 0 || end < 0) {
            //Log.w(TAG, "the time format has error, you will get all records");
        }

        return getPeriodRecords(start, end);
    }

    public List<MeasurementRecord> getPeriodRecords(int startTime, int endTime) {
        if (startTime <= 0 || endTime <= 0) {
            return mRecords;
        }

        //Log.d(TAG, "mRecords count = "+mRecords.size());
        List<MeasurementRecord> records = new ArrayList<>();
        synchronized (mRecords){
            for (MeasurementRecord record : mRecords) {
                if (record.getDateTime() >= startTime && record.getDateTime() < endTime) {
                    records.add(record);
                }
            }
        }

        Collections.sort(records, mSortByTime);
        //Log.d(TAG, "records count = " + records.size());
        return records;
    }

    Comparator<MeasurementRecord> mSortByTime = new Comparator<MeasurementRecord>() {
        @Override
        public int compare(MeasurementRecord lhs, MeasurementRecord rhs) {
            return rhs.getDateTime() - lhs.getDateTime();
        }
    };

    public List<MeasurementRecord> getRecords() {
        return mRecords;
    }

    public MeasurementRecord getLastRecord() {
        if (mRecords != null && mRecords.isEmpty()) {
            //Log.d(TAG, "has no records");
            return null;
        }

        MeasurementRecord lastRecord = mRecords.get(0);
        synchronized (mRecords){
            for (MeasurementRecord record : mRecords) {
                if (record.getDateTime() > lastRecord.getDateTime()) {
                    lastRecord = record;
                }
            }
        }
        return lastRecord;
    }

    public boolean isNeedWiFi(){
        boolean isNeed = true;
        String tempDevId = this.mDeviceId + "";
        char firstChar = tempDevId.charAt(0);
        int tempDevty = Character.digit(firstChar, 10);
        if(tempDevty == 1){
            isNeed = true;
        } else if( tempDevty == 2){
            isNeed = false;
        }
        return isNeed;
    }

    public int getMeasurementTypeWithDevTy(int devId) {
        String tempDevId = devId + "";
        char firstChar = tempDevId.charAt(1);
        int tempDevty = Character.digit(firstChar, 10);
        int type = MeasurementType.WATER_METER;
        switch (tempDevty) {
            case 1:
                type = MeasurementType.WATER_METER;
                break;
            case 2:
                type = MeasurementType.ELECTRIC_METER;
                break;
            case 3:
            default:
                type = MeasurementType.UNKNOWN;
                break;
        }
        return type;
    }

    public class MeasurementType {
        public static final int MOCK_ADD_DEVICE = 0;
        public static final int WATER_METER = 1;
        public static final int ELECTRIC_METER = 2;
        public static final int UNKNOWN = 3;



        public static final int TYPES_COUNT = 4;
    }

    public void readFromParcelable(Parcel parcel) {
        this.mDeviceId = parcel.readInt();
        this.mRegTimestamp = parcel.readLong();
        this.mDeviceType = parcel.readInt();
        this.mPayId = parcel.readString();
        this.mDeviceBatteryLevel = parcel.readInt();
        this.mMobUrlPic = parcel.readString();
        this.mDeviceUrlPic = parcel.readString();
        this.mDeviceUrlErrorPic = parcel.readString();
        ;
        this.mConfig = parcel.readString();
        ;
        this.mMobState = parcel.readInt();
        this.mError = parcel.readInt();
        this.mSaveTimestamp = parcel.readLong();
        this.mUplState = parcel.readInt();
        this.mUpdelay = parcel.readInt();
        this.mUpdelaySub = parcel.readInt();
        this.mDeviceState = parcel.readInt();
        this.mTmpValue = parcel.readInt();
        this.mSSID = parcel.readString();
        ;
        this.mSSIDPWD = parcel.readString();
        ;
        this.mDateInterval = parcel.readInt();
        this.mDeviceFlow = parcel.readInt();
        this.mIsAccess = parcel.readInt();
        this.mDevNotUseDay = parcel.readInt();
        this.mDevNotUseDayCount = parcel.readInt();
        this.mUserPhoneName = parcel.readString();
        this.mAlisaName = parcel.readString();
        this.mLocation = parcel.readString();
        this.mLastValue = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDeviceId);
        dest.writeLong(this.mRegTimestamp);
        dest.writeInt(this.mDeviceType);
        dest.writeString(this.mPayId);
        dest.writeInt(this.mDeviceBatteryLevel);
        dest.writeString(this.mMobUrlPic);
        dest.writeString(this.mDeviceUrlPic);
        dest.writeString(this.mDeviceUrlErrorPic);
        dest.writeString(this.mConfig);
        dest.writeInt(this.mMobState);
        dest.writeInt(this.mError);
        dest.writeLong(this.mSaveTimestamp);
        dest.writeInt(this.mUplState);
        dest.writeInt(this.mUpdelay);
        dest.writeInt(this.mUpdelaySub);
        dest.writeInt(this.mDeviceState);
        dest.writeInt(this.mTmpValue);
        dest.writeString(this.mSSID);
        dest.writeString(this.mSSIDPWD);
        dest.writeInt(this.mDateInterval);
        dest.writeInt(this.mDeviceFlow);
        dest.writeInt(this.mIsAccess);
        dest.writeInt(this.mDevNotUseDay);
        dest.writeInt(this.mDevNotUseDayCount);
        dest.writeString(this.mUserPhoneName);
        dest.writeString(this.mAlisaName);
        dest.writeString(this.mLocation);
        dest.writeInt(this.mLastValue);
    }

    public class CameraPara {
        int mType;
        int mStartAngle;
        int mRadius;
        Point[] mCoords = new Point[8];
        int mValue;

        public CameraPara(String data) {
            this.mType = Character.digit(data.charAt(0), 10);
            this.mStartAngle = Integer.parseInt(data.substring(1, 4));
            this.mRadius = Integer.parseInt(data.substring(4, 7));
            for (int i = 0; i < 8; i++) {
                int index = i * 6 + 7;
                int pointx = Integer.parseInt(data.substring(index, index + 3));
                int pointy = Integer.parseInt(data.substring(index + 3, index + 6));
                Point p = new Point();
                p.x = pointx;
                p.y = pointy;
                mCoords[i] = p;
            }
            mValue = Integer.parseInt(data.substring(data.length() - 4, data.length()));
        }

        public  Point[] getCoords(){
            return mCoords;
        }

        public int getType() {
            return mType;
        }

        public int getValue() {
            return mValue;
        }

        public int getRadius() {
            return mRadius;
        }

        public int getStartAngle() {
            return mStartAngle;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("CameraParameters:\n");
            sb.append("\tType: " + mType + "\n");
            sb.append("\tStartAngle: " + mStartAngle + "\n");
            sb.append("\tRadius: " + mRadius + "\n");
            sb.append("\tCoords:\n");
            for (int i = 0; i < 8; i++) {
                sb.append("\t\t Coord[" + i + "] = (" + mCoords[i].x + ", " + mCoords[i].y + ")\n");
            }
            sb.append("\tValue: " + mValue);
            return sb.toString();
        }
    }

    public CameraPara getCameraPara() {
        return mCameraPara;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public long getRegTimestamp() {
        return mRegTimestamp;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public String getPayId() {
        return mPayId;
    }

    public int getDeviceBatteryLevel() {
        return mDeviceBatteryLevel;
    }

    public String getMobUrlPic() {
        return mMobUrlPic;
    }

    public String getDeviceUrlPic() {
        return mDeviceUrlPic;
    }

    public String getDeviceUrlErrorPic() {
        return mDeviceUrlErrorPic;
    }

    public String getConfig() {
        return mConfig;
    }

    public int getError() {
        return mError;
    }

    public int getMobState() {
        return mMobState;
    }

    public long getSaveTimestamp() {
        return mSaveTimestamp;
    }

    public int getUplState() {
        return mUplState;
    }

    public int getUpdelay() {
        return mUpdelay;
    }

    public int getmUpdelaySub() {
        return mUpdelaySub;
    }

    public void setUpdelaySub(int mUpdelaySub) {
        this.mUpdelaySub = mUpdelaySub;
    }

    public int getDeviceState() {
        return mDeviceState;
    }

    public int getTmpValue() {
        return mTmpValue;
    }

    public String getSSID() {
        return mSSID;
    }

    public String getWordpass() {
        return mSSIDPWD;
    }

    public int getDateInterval() {
        return mDateInterval;
    }

    public int getDeviceFlow() {
        return mDeviceFlow;
    }

    public int getIsAccess() {
        return mIsAccess;
    }

    public String getUserPhoneName() {
        return mUserPhoneName;
    }

    public int getType() {
        return mType;
    }

    public int getDevNotUseDay() {
        return mDevNotUseDay;
    }

    public int getDevNotUseDayCount() {
        return mDevNotUseDayCount;
    }

    public String getAlisaName() {
        return mAlisaName;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setDeviceType(int type) {
        this.mDeviceType = type;
    }

    public void setPayId(String mPayId) {
        this.mPayId = mPayId;
    }

    public void setDeviceBatteryLevel(int mDeviceBatteryLevel) {
        this.mDeviceBatteryLevel = mDeviceBatteryLevel;
    }

    public void setMobUrlPic(String mMobUrlPic) {
        this.mMobUrlPic = mMobUrlPic;
    }

    public void setDeviceUrlPic(String mDeviceUrlPic) {
        this.mDeviceUrlPic = mDeviceUrlPic;
    }

    public void setDeviceUrlErrorPic(String mDeviceUrlErrorPic) {
        this.mDeviceUrlErrorPic = mDeviceUrlErrorPic;
    }

    public void setConfig(String config) {
        if(config != null && !config.isEmpty()){
            parserConfig(config);
        }
        this.mConfig = config;
    }

    public void setMobState(int mMobState) {
        this.mMobState = mMobState;
    }

    public void setError(int mError) {
        this.mError = mError;
    }

    public void setSaveTimestamp(long mSaveTimestamp) {
        this.mSaveTimestamp = mSaveTimestamp;
    }

    public void setUplState(int mUplState) {
        this.mUplState = mUplState;
    }

    public void setUpdelay(int mUpdelay) {
        this.mUpdelay = mUpdelay;
    }

    public void setTmpValue(int mTmpValue) {
        this.mTmpValue = mTmpValue;
    }

    public void setSSID(String mSSID) {
        this.mSSID = mSSID;
    }

    public void setWordpass(String mWordpass) {
        this.mSSIDPWD = mWordpass;
    }

    public void setDeviceFlow(int mDeviceFlow) {
        this.mDeviceFlow = mDeviceFlow;
    }

    public void setDateInterval(int mDateInterval) {
        this.mDateInterval = mDateInterval;
    }

    public void setIsAccess(int mIsAccess) {
        this.mIsAccess = mIsAccess;
    }

    public void setUserPhoneName(String userphoneName) {
        this.mUserPhoneName = userphoneName;
    }

    public void setDeviceId(int mDeviceId) {
        this.mDeviceId = mDeviceId;
    }

    public void setRegTimestamp(long mTimestamp) {
        this.mRegTimestamp = mTimestamp;
    }

    public void setDeviceState(int mDeviceState) {
        this.mDeviceState = mDeviceState;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setRecords(List<MeasurementRecord> records) {
        this.mRecords = records;
    }

    public void setDevNotUseDay(int mDevNotUseDay) {
        this.mDevNotUseDay = mDevNotUseDay;
    }

    public void setDevNotUseDayCount(int mDevNotUseDayCount) {
        this.mDevNotUseDayCount = mDevNotUseDayCount;
    }

    public void setAlisaName(String mAlisaName) {
        this.mAlisaName = mAlisaName;
    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }
}
