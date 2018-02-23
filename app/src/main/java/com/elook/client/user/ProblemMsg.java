package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by doudou on 7/16/2016.
 */

public class ProblemMsg implements Parcelable {

    private int mProblemMsgId;
    private String mProblemMsgTitle; //msg
    private String mProblemMsgBody;// body

    public static final Creator<ProblemMsg> CREATOR = new ClassLoaderCreator<ProblemMsg>() {
        @Override
        public ProblemMsg createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public ProblemMsg createFromParcel(Parcel source) {
            return new ProblemMsg(source);
        }

        @Override
        public ProblemMsg[] newArray(int size) {
            return new ProblemMsg[size];
        }
    };
    public ProblemMsg(){
    }

    public ProblemMsg(Parcel source){
        readFromParcel(source);
    }

    public ProblemMsg(JSONObject dataJson, int userId){
        int msgId = -1;
        String msgTitle= "", msgBody = "";
        if(dataJson != null){
            try {
                String tmpStr = dataJson.getString("autoid");
                if(isAvaliableInteger(tmpStr)) msgId = Integer.parseInt(tmpStr);

                msgTitle = dataJson.getString("title");

                msgBody = dataJson.getString("content");
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        this.mProblemMsgId = msgId;
        this.mProblemMsgTitle = msgTitle;
        this.mProblemMsgBody = msgBody;
    }

    public ProblemMsg(int msgId, String msgTitle, String msgBody){
        this.mProblemMsgId = msgId;
        this.mProblemMsgTitle = msgTitle;
        this.mProblemMsgBody = msgBody;
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


    @Override
    public int describeContents() {
        return 0;
    }

    public int getProblemMsgId() {
        return mProblemMsgId;
    }

    public String getProblemMsgTitle() {
        return mProblemMsgTitle;
    }

    public String getProblemMsgBody() {
        return mProblemMsgBody;
    }

    public void setProblemMsgId(int mProblemMsgId) {
        this.mProblemMsgId = mProblemMsgId;
    }

    public void setProblemMsgTitle(String mProblemMsgTitle) {
        this.mProblemMsgTitle = mProblemMsgTitle;
    }

    public void setProblemMsgBody(String mProblemMsgBody) {
        this.mProblemMsgBody = mProblemMsgBody;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProblemMsgId);
        dest.writeString(this.mProblemMsgTitle);
        dest.writeString(this.mProblemMsgBody);
    }

    public void readFromParcel(Parcel source){
        this.mProblemMsgId = source.readInt();
        this.mProblemMsgTitle = source.readString();
        this.mProblemMsgBody = source.readString();

    }
}
