package com.elook.client.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elook.client.R;

import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.lang.reflect.Method;

/**
 * Created by haiming on 5/23/16.
 */
public class RecordValueView extends View {
    private static final String TAG = "RecordValueView";

    private static final int GRADIENT_TOP_START_COLOR = 0XFF22B86E;
    private static final int GRADIENT_END_END_COLOR = 0XFF1F92A7;
    private static final int GRADIENT_BOTTOM_START_COLOR = 0XFF1D58FE;


    Context mContext;
//    Paint mPaint;
    private int mMarginTop = 0 ;
    private int mMarginBottom = 0;
    private int mValueTextSize = 0;
    private int mDateTextSize = 0;
    private int mTextDotDistance = 0;
    private int mDotRadius = 0;

    private int mHistogramWidth = 0;

    private int mPatternHeight = 0;
    private int mPatternOffset = 0;
    private int mPatternFullHeight = 0;
    private int mRadiusSize = 0;

    private Bitmap mHistogramTopBm;
    private Bitmap mRecordValueMonthBgBitmap;
    private Bitmap mRecordValueDayBgBitmap;
    private Bitmap mSelectedRecordValueBgBitmap;
//    private int mHistogramWidth;
//    private Point mDotCoord;
    private int mDotCoordX, mDotCoordY;
    boolean isDotPattern = true;


    private int mRecordValue;
    private String mRecordDate;
    //Typeface mSTXIHEITypeface;

    public RecordValueView(Context context, boolean isDot) {
        super(context);
        init(context);
        this.isDotPattern = isDot;
    }

    public RecordValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordValueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHistogramTopBm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.data_bg);
        mRecordValueDayBgBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.data_tooltip_day);
        mRecordValueMonthBgBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.data_tooltip_month);
        if(isDotPattern){
            mSelectedRecordValueBgBitmap = mRecordValueDayBgBitmap;
        } else {
            mSelectedRecordValueBgBitmap = mRecordValueMonthBgBitmap;
        }

        mMarginTop = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_view_margin_top);
        mMarginBottom = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_view_margin_bottom);
        mValueTextSize = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_value_tesxt_size);
        mDateTextSize = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_data_text_size);
        mTextDotDistance = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_textpoint_distance);
        mDotRadius = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_dot_radius);

        mHistogramWidth = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_record_higram_width);

        mPatternHeight = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_pattern_height);
        mPatternOffset = (int)mContext.getResources().getDimension(R.dimen.measurement_detail_pattern_offset);
        mRadiusSize = (int)mContext.getResources().getDimension(R.dimen.measurement_day_record_radius);
        mPatternFullHeight = mPatternHeight+mPatternOffset;
        //mSTXIHEITypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/STXIHEI.TTF");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(canvas == null)return;

        if(isDotPattern){
            drawPointPic(canvas);
        } else {
            drawHistogram(canvas);
        }
    }

    private void drawPointPic(Canvas canvas){
        if(canvas == null)return;
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        int canvasCenterX = canvasWidth / 2;

        /*draw date*/
        Paint datePaint = new Paint();
        datePaint.setColor(Color.WHITE);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTextSize(mDateTextSize);
        //datePaint.setTypeface(mSTXIHEITypeface);
        datePaint.setAntiAlias(true);
        float dateTextCenterX = canvasCenterX;
        float dateTextBaseLineY = mPatternFullHeight  +mDateTextSize;
        canvas.drawText(mRecordDate + "", dateTextCenterX, dateTextBaseLineY, datePaint);

        if(mRecordValue < 0)
            return;

        /*First draw the outline of Histogram*/
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.RED);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);

//        int outLineLeft = 0;
//        int outLineRight = 0+canvas.getWidth();
//        int outLineTop = 0;
//        int outLineBottom = 0+canvas.getHeight();
//        canvas.drawRect(outLineLeft, outLineTop, outLineRight, outLineBottom, outlinePaint);

        /*draw point*/
        float pointCenterX = canvasCenterX;
        float pointCenterY = mDotCoordY;
        mDotCoordX = (int)pointCenterX;

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.YELLOW);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
        canvas.drawCircle(pointCenterX, pointCenterY, mDotRadius, dotPaint);

        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setAntiAlias(true);
        dotPaint.setStrokeWidth(2);
        canvas.drawCircle(pointCenterX, pointCenterY, mDotRadius + mRadiusSize, dotPaint);
        Log.d(TAG, "draw point : x=" + pointCenterX + "  y=" + pointCenterY);
    }

    private void drawHistogram(Canvas canvas){
        if(canvas == null || mRecordValue < 0)return;
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        int canvasCenterX = canvasWidth / 2;
        int canvasCenterY = canvasHeight / 2;
        mDotCoordX = canvasCenterX;
        if(isDotPattern){
            mSelectedRecordValueBgBitmap = mRecordValueDayBgBitmap;
        } else {
            mSelectedRecordValueBgBitmap = mRecordValueMonthBgBitmap;
        }


        /*First draw the outline of Histogram*/
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setAntiAlias(true);
        int outLineLeft = canvasCenterX - (mHistogramWidth / 2);
        int outLineRight = canvasCenterX + (mHistogramWidth / 2);
        int outLineTop = mPatternOffset;
        int outLineBottom = mPatternFullHeight;
        canvas.drawRect(outLineLeft, outLineTop, outLineRight, outLineBottom, outlinePaint);

        /*draw gradient*/
        if(mHistogramTopBm != null) {
            int gradientTopStartX = canvasCenterX;
            int gradientTopStartY = mDotCoordY + mHistogramTopBm.getHeight();
            int gradientBottomStartY = mPatternFullHeight;

            int topbm_bottom;
            topbm_bottom = mDotCoordY+mHistogramTopBm.getHeight();
            if(topbm_bottom>mPatternFullHeight){
                topbm_bottom = mPatternFullHeight;
            }

            Rect bitmapDstRect = new Rect(outLineLeft, mDotCoordY,
                    outLineRight, topbm_bottom);

            if (mRecordValue > 0){
                canvas.drawBitmap(mHistogramTopBm, null, bitmapDstRect, null);
            }
            Paint gradientPaint = new Paint();
            Shader linearShader = new LinearGradient(gradientTopStartX,gradientTopStartY,
                    gradientTopStartX,gradientBottomStartY,
                    new int[] {GRADIENT_TOP_START_COLOR, GRADIENT_END_END_COLOR,  GRADIENT_BOTTOM_START_COLOR},
                    null,
                    Shader.TileMode.REPEAT);
            gradientPaint.setShader(linearShader);

            canvas.drawRect(outLineLeft,gradientTopStartY,outLineRight,outLineBottom, gradientPaint);
        }

        /*draw record value*/
        Paint recordValuePaint = new Paint();
        recordValuePaint.setColor(Color.WHITE);
        recordValuePaint.setTextAlign(Paint.Align.CENTER);
        recordValuePaint.setTextSize(mDateTextSize);

        //recordValuePaint.setTypeface(mSTXIHEITypeface);
        recordValuePaint.setAntiAlias(true);
        Paint.FontMetricsInt valueFontMetrics = recordValuePaint.getFontMetricsInt();

        Rect bgRect = new Rect();
        bgRect.left = outLineRight ;
        bgRect.right = bgRect.left + mSelectedRecordValueBgBitmap.getWidth();
        if(mDotCoordY - mSelectedRecordValueBgBitmap.getHeight() > mSelectedRecordValueBgBitmap.getHeight()){
            bgRect.top = mDotCoordY - mSelectedRecordValueBgBitmap.getHeight();
        } else {
            bgRect.top = mDotCoordY;
        }
        bgRect.bottom = bgRect.top + mSelectedRecordValueBgBitmap.getHeight();

         /*draw the background of record val
         ue*/
        canvas.drawBitmap(mSelectedRecordValueBgBitmap, null, bgRect, null);

        int baseline = (bgRect.bottom + bgRect.top - valueFontMetrics.bottom - valueFontMetrics.top) / 2;
        canvas.drawText(mRecordValue + "", bgRect.centerX(), baseline, recordValuePaint);

        /*draw date*/
        Paint datePaint = new Paint();
        datePaint.setColor(Color.WHITE);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTextSize(mDateTextSize);
        //datePaint.setTypeface(mSTXIHEITypeface);
        datePaint.setAntiAlias(true);
        Paint.FontMetrics dateFontMetrics = datePaint.getFontMetrics();
        float dateFontHeight = (dateFontMetrics.leading + dateFontMetrics.descent - dateFontMetrics.ascent);
        float dateTextCenterX = canvasCenterX;
        float dateTextBaseLineY = mPatternFullHeight +dateFontHeight;
        canvas.drawText(mRecordDate + "", dateTextCenterX, dateTextBaseLineY, datePaint);

    }

    public int getRecordValue() {
        return mRecordValue;
    }

    public void setRecordValue(int recordValue) {
        this.mRecordValue = recordValue;
    }

    public String getRecordDate() {
        return mRecordDate;
    }

    public void setRecordDate(String recordDate) {
        this.mRecordDate = recordDate;
    }

    public int getDotCoordY(){
        return mDotCoordY;
    }

    public int getDotCoordX(){
        return mDotCoordX;
    }

    public void setDotCoordY(int y){
        this.mDotCoordY = y;
    }

    public void setDotCoordX(int x){
        this.mDotCoordX = x;
    }

    public int getMinHeight(){
        int minHeight = mMarginTop + mTextDotDistance + mMarginBottom;
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mValueTextSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float valueFontHeight = (fontMetrics.leading + fontMetrics.descent - fontMetrics.ascent);
        minHeight += valueFontHeight;

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mDateTextSize);
        fontMetrics = paint.getFontMetrics();
        valueFontHeight = (fontMetrics.leading + fontMetrics.descent - fontMetrics.ascent);
        minHeight += valueFontHeight;
        return minHeight;
    }

}
