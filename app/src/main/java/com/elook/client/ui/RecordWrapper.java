package com.elook.client.ui;

import android.graphics.Rect;

/**
 * Created by haiming on 6/2/16.
 */
public class RecordWrapper {
    RecordValueView mRecordValueView;
    Rect mRecordViewRect;
    int mValue;
    String mDate;
    int mType;
    boolean mImageFlag=false;

    public RecordWrapper(){};

    public RecordWrapper(int value, String date){
        this.mValue = value;
        this.mDate = date;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setImageFlag(boolean flag) {
        this.mImageFlag = flag;
    }

    public boolean getImageFlag() {
        return mImageFlag;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public Rect getRecordViewRect() {
        return mRecordViewRect;
    }

    public void setRecordViewRect(Rect recordViewRect) {
        this.mRecordViewRect = recordViewRect;
    }

    public RecordValueView getRecordValueView() {
        return mRecordValueView;
    }

    public void setRecordValueSurface(RecordValueView recordValueView) {
        this.mRecordValueView = recordValueView;
    }
}
