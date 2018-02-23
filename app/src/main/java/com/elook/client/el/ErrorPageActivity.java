package com.elook.client.el;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.elook.client.R;

/**
 * Created by haiming on 5/28/16.
 */
public class ErrorPageActivity extends Activity {
    private static final String TAG = "ErrorPageActivity";
    public static final String ERROR_MSG_TAG = "error_msg";
    public static final String ERROR_CODE_TAG = "error_code";

    TextView mErrorCodeTV, mErorMsgTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews(){
        mErrorCodeTV = (TextView)findViewById(R.id.error_code);
        mErorMsgTV = (TextView)findViewById(R.id.error_msg);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        int errorCode = intent.getExtras().getInt(ERROR_CODE_TAG);
        String msg = intent.getExtras().getString(ERROR_MSG_TAG);
        mErrorCodeTV.setText(errorCode+"");
        mErorMsgTV.setText(msg);
    }

    public void back(View v){
        finish();
    }
}
