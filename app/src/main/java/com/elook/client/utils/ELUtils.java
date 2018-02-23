package com.elook.client.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elook.client.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by haiming on 5/20/16.
 */
public class ELUtils {
    /*
* @param timestamp milliseconds
* */
    public static String convertTimestampToLocalString(String dateFormat, int timestamp){
        return new SimpleDateFormat(dateFormat).format(new Date(timestamp * 1000L));
    }

    //return milliseconds
    public static long convertLocaleStringTOTimeStamp(String dateFormat, String localeString){
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(dateFormat).parse(localeString).getTime();
        } catch (ParseException e){
            e.printStackTrace();
        }
        return timestamp;
    }


    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        //TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.animation_loading);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        //tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        return loadingDialog;
    }

    public static boolean isNetworkAvaliable() {
        boolean isAvaliable = false;
        Process p;
        try {
            p = Runtime.getRuntime().exec("ping -c 3 -w 100 www.baidu.com");
            int status = p.waitFor();
            if (status == 0) {
                isAvaliable = true;
            } else {
                isAvaliable = false;
            }
        } catch (IOException e) {
            isAvaliable = false;
            e.printStackTrace();
        } catch (InterruptedException e) {
            isAvaliable = false;
            e.printStackTrace();
        }
        return isAvaliable;
    }

    public static boolean isPhoneNumberAvaliable(String phoneNumber) {
        boolean isAvaliable = false;
        if (phoneNumber.matches("1[34578][0-9]{9,9}")) {
            isAvaliable = true;
        } else {
            isAvaliable = false;
        }
        return isAvaliable;
    }

    public static boolean isEmailAvaliable(String email) {
        boolean isAvaliable = false;
        if (email.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")) {
            isAvaliable = true;
        } else {
            isAvaliable = false;
        }
        return isAvaliable;
    }
}
