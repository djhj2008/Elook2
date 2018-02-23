package com.elook.client.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.elook.client.R;

/**
 * Created by haiming on 5/24/16.
 */
public class PopDetailRecordView extends View {

    private Context mContext;
    private int mRecordValue;
    private String mRecordImagePath;
    public PopDetailRecordView(Context context, int recordValue) {
        super(context);
        init(context, recordValue);
    }


    private void init(Context context, int recordValue){
        this.mContext = context;
        this.mRecordValue = recordValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int transparentColor = 0x00FFFFFF;
        canvas.drawColor(transparentColor);

        int width = canvas.getWidth();
        int height =  canvas.getHeight();

        int rectangleHeight = (int)(height * 0.83);
        int rectangleWidth = width;

        int transtangleP1X = (int)(width * 0.2);
        int transtangleP1Y = rectangleHeight;

        int transtangleP2X = (int)(width * 0.4);
        int transtangleP2Y = rectangleHeight;

        int transtangleP3X = (int)(width * 0.3);
        int transtangleP3Y = height;

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        canvas.drawRect(0, 0, width, rectangleHeight, paint);

        Path path = new Path();
        path.moveTo(transtangleP1X, transtangleP1Y);
        path.lineTo(transtangleP2X, transtangleP2Y);
        path.lineTo(transtangleP3X, transtangleP3Y);
        path.close();
        canvas.drawPath(path, paint);

        /*draw value*/
        float textCoordX = 10;
        float textCoordY = rectangleHeight / 2;

        Paint mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(30);
        mPaint.setAntiAlias(true);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        float textCenterX = textCoordX;
        float textBaseLineY = textCoordY + fontMetrics.descent;

        canvas.drawText(mRecordValue + "", textCenterX, textBaseLineY, mPaint);

        Drawable drawable = getResources().getDrawable(R.drawable.u438);


    }
}
