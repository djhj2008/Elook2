package com.elook.client.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elook.client.R;

/**
 * Created by haiming on 5/17/16.
 */
public class RecordPanel extends LinearLayout {
    private static final String TAG = "RecordPanel";
    private Context mContext;
    private int mRecordValue;
    public RecordPanel(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public RecordPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recordPanel);
        int value = a.getInt(R.styleable.recordPanel_recordValue, 0);
        this.mRecordValue = value;
        Log.d(TAG, "recordValue = "+value);
        initView();
    }

    public RecordPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recordPanel);
        int value = a.getInt(R.styleable.recordPanel_recordValue, 0);
        this.mRecordValue = value;
        Log.d(TAG, "recordValue = "+value);
        initView();
    }

    public void setRecordValue(int value){
        this.mRecordValue = value;
        initView();
    }


    private void initView(){
        removeAllViews();
        setOrientation(LinearLayout.HORIZONTAL);
        //Typeface msyhTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/msyh.ttf");
        if(mRecordValue >= 0){
            Log.d(TAG, "recordValue = "+mRecordValue);
            String recordValueStr = new String(mRecordValue+"");
            int recordValueLength = recordValueStr.length();
            for (int i = 0; i < recordValueLength; i++){
                TextView tv = new TextView(mContext);
                TextPaint tp = tv.getPaint();
                tp.setFakeBoldText(true);
                tv.setBackgroundResource(R.drawable.home_bg_numbe);
                tv.setGravity(Gravity.CENTER);
                //tv.setTypeface(msyhTypeface);
                tv.setTextColor(mContext.getResources().getColor(R.color.home_measurement_record_value_font_color));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.home_measurement_record_value_font_size));
                tv.setText(recordValueStr.charAt(i) + "");
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        (int)mContext.getResources().getDimension(R.dimen.home_measurement_record_value_height));
                params.gravity = Gravity.CENTER;
                //params.setMargins(1, 0, 1, 0);
                addView(tv, params);
            }
        }
        postInvalidate();
    }
}
