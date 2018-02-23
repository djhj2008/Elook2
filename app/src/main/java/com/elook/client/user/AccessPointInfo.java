package com.elook.client.user;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by guoguo on 2016/3/6.
 */
public class AccessPointInfo implements Parcelable{
    private int aliveFlag;
    private String name;
    private String IP;
    private String macAdrress;
    private int securityType;
    private Drawable img;
    private boolean connectedflag;

    public static final Parcelable.Creator<AccessPointInfo> CREATOR = new ClassLoaderCreator<AccessPointInfo>() {
        @Override
        public AccessPointInfo createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public AccessPointInfo createFromParcel(Parcel source) {
            return new AccessPointInfo(source);
        }

        @Override
        public AccessPointInfo[] newArray(int size) {
            return new AccessPointInfo[size];
        }
    };

    public AccessPointInfo(Parcel parcel){
        readFromParcel(parcel);
    }

    public AccessPointInfo(AccessPointInfo info){
        this.aliveFlag = info.getaliveFlag();
        this.name = info.getName();
        this.IP = info.getIP();
        this.macAdrress = info.getmacAdrress();
        this.securityType = info.getsecurityType();
        this.img = info.getimg();
        this.connectedflag = info.getconnectedflag();
    }

    public AccessPointInfo(){}

    public int getaliveFlag() {
        return this.aliveFlag;
    }

    public void setaliveFlag(int aliveFlag) {
        this.aliveFlag = aliveFlag;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIP() {
        return this.IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getmacAdrress() {
        return this.macAdrress;
    }

    public void setmacAdrress(String macAdrress) {
        this.macAdrress = macAdrress;
    }

    public int getsecurityType() {
        return this.securityType;
    }

    public void setsecurityType(int securityType) {
        this.securityType = securityType;
    }

    public Drawable getimg() {
        return this.img;
    }

    public void setimg(Drawable img) {
        this.img = img;
    }

    public boolean getconnectedflag() {
        return this.connectedflag;
    }

    public void setconnectedflag(boolean connectedflag) {
        this.connectedflag = connectedflag;
    }

    public void readFromParcel(Parcel parcel){
        this.name = parcel.readString();
        this.macAdrress = parcel.readString();
        this.securityType = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.macAdrress);
        dest.writeInt(this.securityType);
    }


    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Access Point Info:\n");
        sb.append("\tName: "+name+"\n");
        sb.append("\tMac: "+macAdrress+"\n");
        sb.append("\tIp: "+IP+"\n");
        sb.append("\taliveFlag: "+aliveFlag+"\n");
        sb.append("\tsecurityType: "+securityType+"\n");
        sb.append("\tconnectedflag: "+connectedflag+"\n");
        return sb.toString();
    }
}
