package com.elook.client.wifi;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.adapter.AcessPointInfoAdapter;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.AccessPointInfo;

import org.w3c.dom.ProcessingInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 4/2/16.
 */
public class WiFiListPreview extends AlertDialog implements WiFiAdmin.OnCollectAPInfoFinished {
    private static final String TAG = "WiFiListPreview";
    List<AccessPointInfo> mAllApInfos = new ArrayList<>();
    AcessPointInfoAdapter mAllApInfosAdapter;
    Context mContext;
    WiFiAdmin mWiFiAdmin;
    ELookDatabaseHelper mDatabaseHelper;

    View mRootView;
    LinearLayout mWiFiListContainer;
    ListView mWiFiScanResultsListView;
    LinearLayout mWaitingWiFiContainer;
    OnWiFiSelected mDataCallback;
    static WiFiListPreview SWiFiListPreview;

    public WiFiListPreview(Context context, WiFiAdmin wiFiAdmin, OnWiFiSelected dataCallback){
        super(context);
        if(SWiFiListPreview != null)return;
        mContext = context;
        mWiFiAdmin = wiFiAdmin;
        mDataCallback = dataCallback;
        mWiFiAdmin.registeScanFinishedListener(this);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(mContext);
        mAllApInfosAdapter = new AcessPointInfoAdapter(context);
        initDialogUI();
        initScanResultListView();
    }

    private void initDialogUI(){
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_wifi_scanresults, null);
        mWaitingWiFiContainer = (LinearLayout)mRootView.findViewById(R.id.waiting_wifi_container);
        mWiFiListContainer = (LinearLayout)mRootView.findViewById(R.id.wifi_list_container);
        mWiFiScanResultsListView = (ListView)mRootView.findViewById(R.id.wifi_listview);// new ListView(context);
    }

    public void setApInfos(List<AccessPointInfo> infos){
        mAllApInfos = infos;
        mAllApInfosAdapter.setAdapterList(infos);
        handler.sendEmptyMessage(MSG_UPDATE_WIFI_LIST);
    }

    private void initScanResultListView(){
        mWiFiScanResultsListView.setAdapter(mAllApInfosAdapter);
        mWiFiScanResultsListView.setOnItemClickListener(mApClickedListener);
    }


    ListView.OnItemClickListener mApClickedListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AccessPointInfo apInfo = mAllApInfos.get(position);
            showConnectApDialog(apInfo);
        }
    };

    public void onCollectAPInfoFinished(){
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){}
        setApInfos(mWiFiAdmin.getCollectedApInfos());
        handler.sendEmptyMessage(MSG_UPDATE_WIFI_LIST);
    }

    private ProgressDialog getProgressDialog(String title, String content){
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "退出", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return  dialog;
    }

    Builder mConnectApDialogBuilder;
    AlertDialog mConnectApDialog;
    private boolean isConnectedSuccessful = false;
    private void showConnectApDialog(final AccessPointInfo apInfo) {
        mConnectApDialogBuilder = new Builder(mContext);
        String dialogTile = new String("请输入" + apInfo.getName()+"密码:");
        mConnectApDialogBuilder.setTitle(dialogTile);

        View dialogView = getLayoutInflater().inflate(R.layout.wifi_connect_dailog, null);
        mConnectApDialogBuilder.setView(dialogView);

        List<AccessPointInfo> hasConnectedAps = mDatabaseHelper.getConnectedAps();
        String savePassword = "";
        savePassword = mDatabaseHelper.getPasswordOfAp(apInfo);
        final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.wifi_connected_passwd);
        if (!savePassword.isEmpty()){
            passwordEditText.setText(savePassword);
        }
        CheckBox isShowPasswd = (CheckBox) dialogView.findViewById(R.id.wifi_connected_show_password);
        isShowPasswd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mConnectApDialogBuilder.setPositiveButton("连接", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String password = passwordEditText.getText().toString();
                dialog.dismiss();
                Log.d(TAG, "password = "+password);
                mDataCallback.onWiFiSelected(apInfo, password);
            }
        });
        mConnectApDialogBuilder.setNegativeButton("退出", null);
        mConnectApDialog = mConnectApDialogBuilder.create();
        mConnectApDialog.show();
        final Button positiveButton =  mConnectApDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if(passwordEditText!=null && passwordEditText.getText().toString().equals(""))
            positiveButton.setEnabled(false);

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){}
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < 8){
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        setContentView(mRootView);
    }
    public interface  OnWiFiSelected{
        void onWiFiSelected(final AccessPointInfo info, final String passwd);
    }

    private static final int MSG_UPDATE_WIFI_LIST = 1;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_UPDATE_WIFI_LIST:
                    mWaitingWiFiContainer.setVisibility(View.GONE);
                    mWiFiListContainer.setVisibility(View.VISIBLE);
                    mAllApInfosAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
