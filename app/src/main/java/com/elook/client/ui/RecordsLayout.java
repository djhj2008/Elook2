package com.elook.client.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.service.MeasurementListService;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.utils.ELUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 5/23/16.
 */
public class RecordsLayout extends ViewGroup {
    private static final String TAG = "RecordsLayout";
    int mLeft = 0, mTop = 0;
    List<RecordWrapper> mRecords = new ArrayList<>();
    boolean mIsDotPattern = true;
    private Context mContext;
    private int mRule=0;
    private int mRecordsLayoutOffset = 0;
    private int mDashLineDistance = 0;
    private int mPatternHeight = 0;
    private int mPatternOffset = 0;
    private int mPatternFullHeight = 0;
    private int mDayRecordShowWidth = 0;
    private int mDayRecordShowHeight = 0;
    private int mDayRecordShowOffset = 0;
    private int mLineTextSize = 0;


    public RecordsLayout(Context context) {
        super(context);
        init(context);
    }

    public RecordsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setRecords(List<MeasurementRecord> records){

    }

    public void setRecordWrapper(List<RecordWrapper> records){
        this.mRecords = records;
        updateRecordsLayout();
    }

    public void setIsDotPattern(boolean isDot){
        this.mIsDotPattern = isDot;
    }

    private void init(Context context){
        this.mContext = context;
        mRecordsLayoutOffset= (int)mContext.getResources().getDimension(R.dimen.measurement_detail_pattern_wrapper_paddingLeft);
        mDashLineDistance = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_dashline_distance);
        mPatternHeight = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_pattern_height);
        mPatternOffset = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_pattern_offset);
        mDayRecordShowWidth = (int)mContext.getResources().getDimension(R.dimen.measurement_day_record_show_width);
        mDayRecordShowHeight = (int)mContext.getResources().getDimension(R.dimen.measurement_day_record_show_height);
        mDayRecordShowOffset = (int)mContext.getResources().getDimension(R.dimen.measurement_day_record_show_offset);
        mLineTextSize = (int)mContext.getResources().getDimension(R.dimen.measurement_day_record_line_text_size);


        mPatternFullHeight = mPatternHeight+mPatternOffset;
//        updateRecordsLayout();
    }

    private int getRecordsHeight(int maxValue){
        int retValue=-1;
        int tmp = 0;
        if(maxValue<=10) {
            retValue = 10;
        }else if(maxValue<=100){
            tmp = maxValue/10;
            retValue = (tmp+1)*10;
        }else if(maxValue<=1000){
            tmp = maxValue/100;
            retValue = (tmp+1)*100;
        }else if(maxValue<=10000){
            tmp = maxValue/1000;
            retValue = (tmp+1)*1000;
        }

        return retValue;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private void updateRecordsLayout(){
        int currentHeight = mPatternHeight;
        int minValue = getMinValue();
        int maxValue = getMaxValue();
        if( maxValue - minValue < 0) return;
        int minViewHeight =0; //(int)(currentHeight * 0.1);
        int childCount = mRecords.size();
        if(childCount == 0){
            return;
        }
        int maxViewHeight = currentHeight;//(int)(currentHeight * 0.9);
        double perValueHeight = ( (double)(maxViewHeight - minViewHeight) / getRecordsHeight(maxValue));
        mRule = getRecordsHeight(maxValue)/5;
        removeAllViews();
        for (int i = 0; i< childCount; i++){
            RecordWrapper r = mRecords.get(i);
            RecordValueView recordValueView = new RecordValueView(mContext, mIsDotPattern);
            int coordY = (int)(currentHeight - (r.getValue()* perValueHeight))+mPatternOffset;
            recordValueView.setRecordValue(r.getValue());
            recordValueView.setRecordDate(r.getDate());
            recordValueView.setDotCoordY(coordY);
            r.setRecordValueSurface(recordValueView);
            //Log.d(TAG, "updateRecordsLayout coordY=" + coordY);
            addView(recordValueView);
        }
        int l=0;
        int t=0;
        int r=getWidth();
        int b=getHeight();
        //Log.d(TAG, "l = "+l+", t = "+t+", r = "+r + ", b = "+b);
        mLeft = l;
        mTop = t;
        int currentWidth = getWidth();
        int recordViewWidth = currentWidth / childCount;
        for (int i = 0; i < childCount; i++){
            RecordValueView recordValueView = (RecordValueView)getChildAt(i);
            View view = getChildAt(i);
            RecordWrapper record = mRecords.get(i);
            View childView = getChildAt(i);
            int left = l + recordViewWidth * i;
            int top = t;
            int right = left + recordViewWidth;
            int bottom = b;
            if(bottom - top < recordValueView.getMinHeight()){
                top = bottom - recordValueView.getMinHeight();
                if(top < 0)top = 0;
            }
            recordValueView.setDotCoordX((left + right) / 2);
            //Log.d(TAG, "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
            childView.layout(left, top, right, bottom);
            record.setRecordViewRect(new Rect(left, top, right, bottom));
        }
        requestLayout();
    }


    private int getMinValue(){
        if(mRecords.size()==0)
            return -1;
        int min = mRecords.get(0).getValue();
        for (RecordWrapper r : mRecords){
            if(r.getValue() == 0) {
                continue;
            } else if(r.getValue() < min) {
                min = r.getValue();
            }
        }
        return min;
    }

    private int getMaxValue(){
        if(mRecords.size()==0)
            return -1;
        int max = mRecords.get(0).getValue();;
        for (RecordWrapper r : mRecords){
            if(r.getValue() == 0){
                continue;
            } else if(r.getValue() > max) {
                max = r.getValue();
            }
        }
        return max;
    }

    public void showTodayRecordsView(){
        boolean hasValuableRecord = false;
        removePopDetailRecordView();
        int size = mRecords.size();
        if(size <= 0 || mIsDotPattern ==false)
            return;
        RecordWrapper touchRecord = null;
        for (int i = (size-1); i >= 0; i--) {
            touchRecord = mRecords.get(i);
            if (isValuableRecordView(touchRecord)) {
                hasValuableRecord=true;
                break;
            }
        }
        if(!hasValuableRecord||touchRecord.getType()!= MeasurementListService.TABLE_TYPE_DAY)
            return;

        mCurrentTouchRecord = touchRecord;
        int[] location = new int[2];
        //getLocationInWindow(location);
        getLocationOnScreen(location);
        int viewLeft =  mRecordsLayoutOffset;
        int viewTop = location[1];
        Log.d(TAG,"RecordWrapper X= "+viewLeft +"  Y="+viewTop);
        int pointX = mCurrentTouchRecord.getRecordValueView().getDotCoordX();
        int pointY = mCurrentTouchRecord.getRecordValueView().getDotCoordY();
        Rect rect = mCurrentTouchRecord.getRecordViewRect();
        if(rect == null)return;
        int x = rect.centerX();
        if(x > rect.right) x = rect.right;
        int y = rect.top + pointY;
        if(y > rect.bottom) y = rect.bottom;
        showDayRecordThubmail(mCurrentTouchRecord, x + viewLeft, y + viewTop - getStatusBarHeight());

    }

    private RecordWrapper mCurrentTouchRecord = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mIsDotPattern)return false;

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        int touchedIndex = -1;
        RecordWrapper touchRecord = null;
        for (int i = 0; i < mRecords.size(); i++) {
            touchRecord = mRecords.get(i);
            if (isInnerRecordView(touchRecord, touchX, touchY)) {
                touchedIndex = i;
                break;
            }
        }
        if (touchedIndex < 0) {
            //removePopDetailRecordView();
            return false;
        }


        if(mCurrentTouchRecord != touchRecord){
            removePopDetailRecordView();
            mCurrentTouchRecord = touchRecord;
            int[] location = new int[2];
            //getLocationInWindow(location);
            getLocationOnScreen(location);
            int viewLeft = location[0];
            int viewTop = location[1];
            Log.d(TAG,"RecordWrapper X= "+viewLeft +"  Y="+viewTop);
            int pointX = mCurrentTouchRecord.getRecordValueView().getDotCoordX();
            int pointY = mCurrentTouchRecord.getRecordValueView().getDotCoordY();
            Rect rect = mCurrentTouchRecord.getRecordViewRect();
            if(rect == null)return false;
            int x = rect.centerX();
            if(x > rect.right) x = rect.right;
            int y = rect.top + pointY;
            if(y > rect.bottom) y = rect.bottom;
            Rect frame = new Rect();

            int statusBarHeight = frame.top;
            showDayRecordThubmail(mCurrentTouchRecord, x + viewLeft, y + viewTop - getStatusBarHeight());
        }
        return false;
    }

    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            Log.d(TAG, "get status bar height fail");
            e1.printStackTrace();
            return 75;
        }
    }

    View mPopDetailRecordRootView = null;

    private boolean isInnerRecordView(RecordWrapper record, int x, int y) {
        boolean isInner = false;
        if( record.getValue() < 0){
            return isInner;
        }
        RecordValueView view = record.getRecordValueView();
        if (y >= view.getTop() && y <= view.getBottom() &&
                x >= view.getLeft() && x <= view.getRight()) {
            isInner = true;
        }
        return isInner;
    }

    private boolean isValuableRecordView(RecordWrapper record) {
        boolean isInner = false;
        if (record.getValue() >=0 ) {
            isInner = true;
        }
        return isInner;
    }

    public void removePopDetailRecordView(){
        final WindowManager mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        if (mPopDetailRecordRootView != null && mPopDetailRecordRootView.isShown()) {
            Log.d(TAG, "mPopDetailRecordRootView != null");
            mWindowManager.removeView(mPopDetailRecordRootView);
//            removeView(mPopDetailRecordRootView);
        }

    }


    public void showDayRecordThubmail(RecordWrapper r, int locationX, int locationY) {

        Log.d(TAG, "locationX = " + locationX + ", locationY = " + locationY);
        mPopDetailRecordRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_detail_record, null);
        TextView recordValueTV = (TextView)mPopDetailRecordRootView.findViewById(R.id.dialog_record_value);
        ImageView imageTV = (ImageView)mPopDetailRecordRootView.findViewById(R.id.dialog_record_image);
        recordValueTV.setText(r.getValue() + "");
        if(r.getValue()<30)//temp for auto show image view
            imageTV.setVisibility(View.GONE);
        Log.d(TAG, "mPopDetailRecordRootView getWidth = " + mPopDetailRecordRootView.getWidth() + ", getHeight = " +  mPopDetailRecordRootView.getHeight());
        WindowManager.LayoutParams wmParams;
        wmParams = new WindowManager.LayoutParams();
        final WindowManager mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP |Gravity.CENTER;
        wmParams.x = locationX-mDayRecordShowWidth/2;
        if(wmParams.x < 0){
            wmParams.x = locationX;
        }
        wmParams.y = locationY - mDayRecordShowHeight - mDayRecordShowOffset;
        //mContext.getResources().getDimension()
        wmParams.width = mDayRecordShowWidth;
        wmParams.height = mDayRecordShowHeight;
        mWindowManager.addView(mPopDetailRecordRootView, wmParams);
        //addView(mPopDetailRecordRootView, wmParams);
    }

    private static final int DASH_COLOR = 0x64FFFFFF;// alpha 0.39, white
    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        if(mIsDotPattern){
            drawConnectedLine(canvas);
        }
    }

    private void drawBackground(Canvas canvas){
        int canvasWidth = canvas.getWidth();
        int bgheight = mPatternFullHeight;
        Log.d(TAG, "canvasWidth = " + canvasWidth);
        /*draw white line  */
        Paint whiteLinePaint = new Paint();
        whiteLinePaint.setColor(Color.WHITE);
        whiteLinePaint.setAntiAlias(true);
        whiteLinePaint.setStrokeWidth(1.5f);
        whiteLinePaint.setTextSize(mLineTextSize);
        int whiteLineStartX = 0;
        int whiteLineStartY = bgheight ;
        int whiteLineEndX = canvasWidth;
        int whiteLineEndY = bgheight ;
        canvas.drawLine(whiteLineStartX + mRecordsLayoutOffset, whiteLineStartY, whiteLineEndX - mRecordsLayoutOffset, whiteLineEndY, whiteLinePaint);

        Paint titleValuePaint = new Paint();
        titleValuePaint.setTextSize(mLineTextSize);
        titleValuePaint.setColor(Color.WHITE);
        whiteLinePaint.setAntiAlias(true);
        titleValuePaint.setTextAlign(Paint.Align.LEFT);

        /*draw dash line on background*/
        PathEffect effects = new DashPathEffect(new float[]{5,2,5,2},1);
        Paint dashLinePaint = new Paint();
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(1.5f);
        dashLinePaint.setColor(DASH_COLOR);
        dashLinePaint.setPathEffect(effects);
        Path path = new Path();

        canvas.drawText(0 + "", whiteLineStartX, whiteLineStartY, titleValuePaint);
        if(mRule > 0) {
            for(int i=1;i<=5;i++) {
                canvas.drawText(mRule * i + "", 0, bgheight  - mDashLineDistance * i, titleValuePaint);
                /*draw dash line*/
                path.moveTo(0 + mRecordsLayoutOffset, bgheight - mDashLineDistance * i);
                path.lineTo(canvasWidth - mRecordsLayoutOffset, bgheight  - mDashLineDistance * i);
                canvas.drawPath(path, dashLinePaint);
            }
        }


    }

    private static final int CONNECTED_LINE_COLOR = 0x6BFFFFFF;// alpha 0.42, white
    private static final int COVER_MASK_COLOR = 0x6B000000;
    private void  drawConnectedLine(Canvas canvas){
        Log.d(TAG, "drawConnectedLine");
        int childCount = getChildCount();
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1.5f);
        linePaint.setColor(CONNECTED_LINE_COLOR);
        linePaint.setAntiAlias(true);
        int index = 0;

        if(childCount==0)
            return;

        while (true) {
                RecordValueView recordValueView = (RecordValueView) getChildAt(index);
                for(int i=index;i<childCount-1;i++) {
                    if (recordValueView.getRecordValue() < 0) {
                        index++;
                        recordValueView = (RecordValueView) getChildAt(index);
                    }else{
                        break;
                    }
                }

                RecordValueView nextRecordValueView = (RecordValueView) getChildAt(index + 1);
                for(int i=index + 1;i<childCount;i++) {
                    if (nextRecordValueView.getRecordValue() < 0) {
                        index++;
                        nextRecordValueView = (RecordValueView) getChildAt(index);
                    }else{
                        break;
                    }
                }

                index++;

                if(index>childCount - 1)
                    break;

                int startX = recordValueView.getDotCoordX();
                int startY = recordValueView.getDotCoordY();
                int endX = nextRecordValueView.getDotCoordX();
                int endY = nextRecordValueView.getDotCoordY();
                canvas.drawLine(startX, startY, endX, endY, linePaint);

                Paint coverPaint = new Paint();
                Path coverPath = new Path();
                coverPaint.setColor(Color.BLACK);
                coverPaint.setStyle(Paint.Style.FILL);
                coverPaint.setAlpha(107);
                coverPath.moveTo(startX, startY);
                coverPath.lineTo(endX, endY);
                coverPath.lineTo(endX, mPatternFullHeight);
                coverPath.lineTo(startX, mPatternFullHeight);
                coverPath.close();
                canvas.drawPath(coverPath, coverPaint);

        }
    }
}
