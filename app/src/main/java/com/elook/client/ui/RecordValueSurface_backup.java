package com.elook.client.ui;

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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.elook.client.R;

import java.lang.reflect.Method;

/**
 * Created by haiming on 5/23/16.
 */
public class RecordValueSurface_backup extends SurfaceView implements
        SurfaceHolder.Callback2, Runnable{
    private static final String TAG = "RecordValueSurface";
    private static final int DEFAULT_MARGION_TOP = 10;
    private static final int DEFAULT_MARGION_BOTTOM = 5;
    private static final int DEFAULT_VALUE_TEXT_SIZE = 18;
    private static final int DEFAULT_DATE_TEXT_SIZE = 44;

    private static final int DEFAULT_TEXT_POINT_DISTANCE = 5;
    private static final int DEFAULT_DOT_RADIUS = 4;

    private static final int HISTOGRAM_WIDTH = 51;
    private static final int HISTOGRAM_HEIGHT = 360;

    private static final int DATE_HISTOGRAM_DISTANCE = 40;

    private static final int GRADIENT_TOP_LENGTH = 87;
    private static final int GRADIENT_TOP_START_COLOR = 0XFF22B86E;
    private static final int GRADIENT_END_END_COLOR = 0XFF1F92A7;
    private static final int GRADIENT_BOTTOM_START_COLOR = 0XFF1D58FE;


    Context mContext;
//    Paint mPaint;
    private int mMarginTop = DEFAULT_MARGION_TOP ;
    private int mMarginBottom = DEFAULT_MARGION_BOTTOM;
    private int mValueTextSize = DEFAULT_VALUE_TEXT_SIZE;
    private int mDateTextSize = DEFAULT_DATE_TEXT_SIZE;
    private int mTextDotDistance = DEFAULT_TEXT_POINT_DISTANCE;
    private int mDotRadius = DEFAULT_DOT_RADIUS;
    private Bitmap mHistogramTopBm;

//    private int mHistogramWidth;
    private Point mDotCoord;
    boolean isDotPattern = true;

    private int mRecordValue;
    private String mRecordDate;
    Typeface mSTXIHEITypeface;

    private Thread mThread; // SurfaceView通常需要自己单独的线程来播放动画
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;

    public RecordValueSurface_backup(Context context, boolean isDot) {
        super(context);
        init(context);
        this.isDotPattern = isDot;
    }

    public RecordValueSurface_backup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordValueSurface_backup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHistogramTopBm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.data_bg);
        mSTXIHEITypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/STXIHEI.TTF");

        this.mSurfaceHolder = getHolder();
        this.mSurfaceHolder.addCallback(this);
    }

    private boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return hasNavigationBar;
    }

    private int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        Log.d(TAG, "canvas Height = " + canvas.getHeight() + ", widgth = " + canvas.getWidth());
//
//        if(isDotPattern){
//            drawPointPic(canvas);
//        } else {
//            Paint gradientPaint = new Paint();
//            Shader topLinearShader = new LinearGradient(0, 0,canvas.getWidth(),canvas.getHeight(),
//                    new int[] {GRADIENT_TOP_START_COLOR, GRADIENT_END_END_COLOR},null,Shader.TileMode.REPEAT);
//            gradientPaint.setShader(topLinearShader);
//
//            canvas.drawRect(0, 0, canvas.getWidth(),canvas.getHeight(),  gradientPaint);
//
////            drawHistogram(canvas);
//        }
//    }

    private void drawPointPic(Canvas canvas){
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        int canvasCenterX = canvasWidth / 2;
        int canvasCenterY = canvasHeight / 2;

        /*draw record value*/
        Paint valueTextPaint = new Paint();
        valueTextPaint.setColor(Color.WHITE);
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setTextSize(mValueTextSize);
        valueTextPaint.setAntiAlias(true);
        Paint.FontMetrics valueFontMetrics = valueTextPaint.getFontMetrics();


        /*draw point*/
        float pointCenterX = canvasCenterX;
        float pointCenterY = mMarginTop + valueFontMetrics.bottom + mTextDotDistance + mDotRadius;

        /*draw date*/
        Paint datePaint = new Paint();
        datePaint.setColor(Color.WHITE);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTextSize(mDateTextSize);
        datePaint.setAntiAlias(true);
        Paint.FontMetrics dateFontMetrics = datePaint.getFontMetrics();
        float textCenterX = canvasCenterX;
        float dateTextBaseLineY = canvasHeight - mMarginBottom;
        float dateTextTop = canvasHeight - dateTextBaseLineY - dateFontMetrics.top;
        canvas.drawText(mRecordDate + "", textCenterX, dateTextBaseLineY, datePaint);

        /*recalculate the dot position*/
        if(pointCenterY > dateTextTop){
            pointCenterY = dateTextTop;
        }

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.YELLOW);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
        canvas.drawCircle(pointCenterX, pointCenterY, mDotRadius, dotPaint);

        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setAntiAlias(true);
        dotPaint.setStrokeWidth(2);
        canvas.drawCircle(pointCenterX, pointCenterY, mDotRadius + 1, dotPaint);

//        mDotCoord = new Point((int)pointCenterX, (int)pointCenterY - (mDotRadius + 2));

        float fontHeight = (valueFontMetrics.leading + valueFontMetrics.descent - valueFontMetrics.ascent);
        float valueTextCenterX = canvasCenterX;
        float valueTextBaseLineY =  pointCenterY - mTextDotDistance - mDotRadius;
        if(valueTextBaseLineY + fontHeight < 0)
            valueTextBaseLineY = mMarginTop + fontHeight;
        canvas.drawText(mRecordValue + "", valueTextCenterX, valueTextBaseLineY, valueTextPaint);
    }

    private void drawHistogram(Canvas canvas){
        if(canvas == null) return;
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        int canvasCenterX = canvasWidth / 2;
        int canvasCenterY = canvasHeight / 2;

        /*First draw the outline of Histogram*/
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setAntiAlias(true);
        int outLineLeft = canvasCenterX - (HISTOGRAM_WIDTH / 2  + 1);
        int outLineRight = canvasCenterX + (HISTOGRAM_WIDTH / 2 + 1);
        int outLineTop = 0;
        int outLineBottom = HISTOGRAM_HEIGHT;
        canvas.drawRect(outLineLeft, outLineTop, outLineRight, outLineBottom, outlinePaint);



        /*draw gradient*/
        if(mHistogramTopBm != null){
            Log.d(TAG, "draw data_bg");
            Log.d(TAG, "DotCoord(x, y) = ("+mDotCoord.x+", "+mDotCoord.y+")");
            int gradientTopStartX = canvasCenterX;
            int gradientTopStartY = mDotCoord.y + mHistogramTopBm.getHeight();

            int gradientCentEndX = canvasCenterX;
            int gradientCentEndY = gradientTopStartY + GRADIENT_TOP_LENGTH;

            int gradientBottomStartX = canvasCenterX;
            int gradientBottomStartY = HISTOGRAM_HEIGHT;


            Rect bitmapDstRect = new Rect(outLineLeft, mDotCoord.y,
                    outLineLeft + mHistogramTopBm.getWidth(), mDotCoord.y + mHistogramTopBm.getHeight());
            canvas.drawBitmap(mHistogramTopBm, null, bitmapDstRect, null);

            Paint gradientPaint = new Paint();
            Shader linearShader = new LinearGradient(gradientTopStartX,gradientTopStartY,
                    gradientTopStartX,gradientBottomStartY,
                    new int[] {GRADIENT_TOP_START_COLOR, GRADIENT_END_END_COLOR,GRADIENT_BOTTOM_START_COLOR },null,
                    Shader.TileMode.REPEAT);
            gradientPaint.setShader(linearShader);

            canvas.drawRect(outLineLeft,gradientTopStartY,outLineRight,outLineBottom, gradientPaint);
        }

        /*draw white line on bottom of Histogram */
        Paint whiteLinePaint = new Paint();
        whiteLinePaint.setColor(Color.WHITE);
        whiteLinePaint.setAntiAlias(true);
        whiteLinePaint.setStrokeWidth(1.5f);
        int whiteLineStartX = 0;
        int whiteLineStartY = HISTOGRAM_HEIGHT;
        int whiteLineEndX = canvasWidth;
        int whiteLineEndY = HISTOGRAM_HEIGHT;
        canvas.drawLine(whiteLineStartX, whiteLineStartY, whiteLineEndX, whiteLineEndY, whiteLinePaint);




        /*draw record value*/
//        float textCoordX = canvasCenterX;
//        float textCoordY = mMarginTop;
//
//        Paint valuePaint = new Paint();
//        valuePaint.setColor(Color.WHITE);
//        valuePaint.setTextAlign(Paint.Align.CENTER);
//        valuePaint.setTextSize(mValueTextSize);
//        valuePaint.setAntiAlias(true);
//        Paint.FontMetrics valueFontMetrics = valuePaint.getFontMetrics();
//        float valueFontHeight = (valueFontMetrics.leading + valueFontMetrics.descent - valueFontMetrics.ascent);
//        Log.d(TAG, "valueFontHeight = " + valueFontHeight);
//        float textCenterX = textCoordX;
//        float textBaseLineY = mMarginTop +  valueFontMetrics.descent - valueFontMetrics.ascent;
//        canvas.drawText(mRecordValue + "", textCenterX, textBaseLineY, valuePaint);


        /*draw date*/
        Paint datePaint = new Paint();
        datePaint.setColor(Color.WHITE);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTextSize(mDateTextSize);
        datePaint.setTypeface(mSTXIHEITypeface);
        datePaint.setAntiAlias(true);
        Paint.FontMetrics dateFontMetrics = datePaint.getFontMetrics();
        float dateFontHeight = (dateFontMetrics.leading + dateFontMetrics.descent - dateFontMetrics.ascent);
//        Log.d(TAG, "dateFontHeight  leading = "+dateFontMetrics.leading+", descent = "+dateFontMetrics.descent+", ascent = "+dateFontMetrics.ascent);

        float dateTextCenterX = canvasCenterX;
        float dateTextBaseLineY = HISTOGRAM_HEIGHT + DATE_HISTOGRAM_DISTANCE +dateFontHeight;
        canvas.drawText(mRecordDate + "", dateTextCenterX, dateTextBaseLineY, datePaint);


//        float textHeight = mMarginBottom + dateFontHeight + mMarginTop+valueFontHeight;
//        Log.d(TAG, "canvasHeight = "+canvasHeight+", textHeight = "+textHeight);
//
//        /*draw Rect*/
//        if(canvasHeight - textHeight < mTextDotDistance){
//            Paint linePaint = new Paint();
//            linePaint.setColor(Color.WHITE);
//            linePaint.setAntiAlias(true);
//            int coordY = (int)(canvasHeight - (mMarginBottom + dateFontHeight));
//            int start = canvasCenterX - (HISTOGRAM_WIDTH / 2);
//            int end = canvasCenterX + (HISTOGRAM_WIDTH / 2);
//            canvas.drawLine(start, coordY, end, coordY, linePaint);
//        } else {
//            Paint rectPaint = new Paint();
//            rectPaint.setColor(Color.WHITE);
//            rectPaint.setAntiAlias(true);
//            rectPaint.setStrokeWidth(2);
//            int left = canvasCenterX - (HISTOGRAM_WIDTH / 2);
//            int top =  (int)(mMarginTop + valueFontHeight + mTextDotDistance);
//            int right = canvasCenterX + (HISTOGRAM_WIDTH / 2);
//            int bottom = (int)(canvasHeight - (mMarginBottom + dateFontHeight+mTextDotDistance));
//            rectPaint.setColor(Color.WHITE);
//            rectPaint.setStyle(Paint.Style.FILL);
//            rectPaint.setAntiAlias(true);
//            canvas.drawRect(left, top, right, bottom, rectPaint);
//
//            rectPaint.setColor(Color.YELLOW);
//            rectPaint.setStyle(Paint.Style.STROKE);
//            rectPaint.setAntiAlias(true);
//            rectPaint.setStrokeWidth(2);
//            canvas.drawRect(left + 1, top + 1, right - 1, bottom - 1, rectPaint);
//        }

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

    public Point getDotCoord(){
        return mDotCoord;
    }

    public void setDotCoord(Point p){
        this.mDotCoord = p;
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

    @Override
    public void run() {
        Log.d(TAG, "Ready to Run");
        while (true){
            mCanvas = mSurfaceHolder.lockCanvas();
//            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawHistogram(mCanvas);
//            Paint gradientPaint = new Paint();
//            Shader topLinearShader = new LinearGradient(0, 0, 0, mCanvas.getHeight(),
//                    new int[]{
//                            GRADIENT_TOP_START_COLOR,GRADIENT_BOTTOM_START_COLOR
//                    }, null, Shader.TileMode.REPEAT);
//
//            gradientPaint.setShader(topLinearShader);
//
//            mCanvas.drawRect(0, 0, mCanvas.getWidth(), mCanvas.getHeight(), gradientPaint);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }

    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        this.setZOrderOnTop(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        this.mThread = new Thread(this);
        this.mThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
