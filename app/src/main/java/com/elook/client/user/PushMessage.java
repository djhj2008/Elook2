package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.elook.client.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by guoguo on 4/23/2016.
 */
/*
* {"idpushmsg":"70","type":"1","msg":"","time":"2016-04-19 15:30:20"}*/
public class PushMessage implements Parcelable {

    private int mPushMsgId;
    private int mState;
    private int mPushMsgType;
    private String mPushMsgTitle; //msg
    private String mPushMsgBody;// body
    private String mPushMsgTimestamp;
    private int mPushMsgUserId = -1;

    public static final Parcelable.Creator<PushMessage> CREATOR = new ClassLoaderCreator<PushMessage>() {
        @Override
        public PushMessage createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public PushMessage createFromParcel(Parcel source) {
            return new PushMessage(source);
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };
    public  PushMessage(){
    }

    public PushMessage(Parcel source){
        readFromParcel(source);
    }

    public PushMessage(JSONObject dataJson, int userId){
        int msgId = -1, msgType = -1 ,msgState = -1;
        String msgTitle= "", msgBody = "", msgTimestamp = "";
        if(dataJson != null){
            try {
                String tmpStr = dataJson.getString("pushmsg_autoid");
                if(isAvaliableInteger(tmpStr)) msgId = Integer.parseInt(tmpStr);

                tmpStr = dataJson.getString("pushmsg_state");
                if(isAvaliableInteger(tmpStr)) msgState = Integer.parseInt(tmpStr);

                tmpStr = dataJson.getString("pushmsg_type");
                if(isAvaliableInteger(tmpStr)) msgType = Integer.parseInt(tmpStr);

                msgTimestamp = dataJson.getString("pushmsg_time");

                msgTitle = dataJson.getString("pushmsg_title");

                msgBody = dataJson.getString("pushmsg_msg");
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        this.mPushMsgId = msgId;
        this.mState = msgState;
        this.mPushMsgType = msgType;
        this.mPushMsgTitle = msgTitle;
        this.mPushMsgBody = msgBody;
        this.mPushMsgTimestamp = msgTimestamp;
        this.mPushMsgUserId = userId;
    }

    public PushMessage(int msgId,int msgState, int msgType, String msgTitle, String msgBody, String msgTimestamp, int msgUser){
        this.mPushMsgId = msgId;
        this.mState = msgState;
        this.mPushMsgType = msgType;
        this.mPushMsgTitle = msgTitle;
        this.mPushMsgBody = msgBody;
        this.mPushMsgTimestamp = msgTimestamp;
        this.mPushMsgUserId = msgUser;
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

    public int getPushMsgId() {
        return mPushMsgId;
    }

    public void setPushMsgId(int pushMsgId) {
        this.mPushMsgId = pushMsgId;
    }

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public int getPushMsgType() {
        return mPushMsgType;
    }

    public void setPushMsgType(int pushMsgType) {
        this.mPushMsgType = pushMsgType;
    }

    public String getPushMsgTitle() {
        return mPushMsgTitle;
    }

    public void setPushMsgTitle(String pushMsgTitle) {
        this.mPushMsgTitle = pushMsgTitle;
    }

    public String getPushMsgBody() {
        return mPushMsgBody;
    }

    public void setPushMsgBody(String pushMsgBody) {
        this.mPushMsgBody = pushMsgBody;
    }

    public String getPushMsgTimestamp() {
        return mPushMsgTimestamp;
    }

    public void setPushMsgTimestamp(String pushMsgTimestamp) {
        this.mPushMsgTimestamp = pushMsgTimestamp;
    }

    public int getPushMsgUserId() {
        return mPushMsgUserId;
    }

    public void setPushMsgUserId(int pushMsgUserId) {
        this.mPushMsgUserId = pushMsgUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPushMsgId);
        dest.writeInt(this.mState);
        dest.writeInt(this.mPushMsgType);
        dest.writeString(this.mPushMsgTitle);
        dest.writeString(this.mPushMsgBody);
        dest.writeString(this.mPushMsgTimestamp);
        dest.writeInt(this.mPushMsgUserId);
    }

    public void readFromParcel(Parcel source){
        this.mPushMsgId = source.readInt();
        this.mState = source.readInt();
        this.mPushMsgType = source.readInt();
        this.mPushMsgTitle = source.readString();
        this.mPushMsgBody = source.readString();
        this.mPushMsgTimestamp = source.readString();
        this.mPushMsgUserId = source.readInt();
    }
}
