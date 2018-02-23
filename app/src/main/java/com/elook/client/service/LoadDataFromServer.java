package com.elook.client.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.elook.client.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by xy on 16-2-2.
 */
public class LoadDataFromServer {
    private static final String TAG = "LoadDataFromServer";
    private static final String CLIENT_AIP = new String("aip="+ Constant.AIP);
    private static final int MSG_ORIGIN = 0;
    private static final String CANNOT_CONNECT_SERVER_JSON = "{\n" +
            "    \"UserInfo\": {\n" +
            "    \"ret\": {\n" +
            "        \"ret_message\": \"Cannot connect to server.\",\n" +
            "        \"status_code\": \"-1\"\n" +
            "             }\n" +
            "                   }\n" +
            "}";

    private String mUrl;
    private String mData;

    public LoadDataFromServer(String url){
        this.mUrl = url;
    }

    public LoadDataFromServer(String url, Map<String, String> parametersMap) {
        mUrl = url;
        mData = createUrlParameters(parametersMap);
    }

    public LoadDataFromServer(String url, String data){
        mUrl = url;
        this.mData = data;
        this.mData += CLIENT_AIP;
    }

    private HttpURLConnection getCommonConnection(){
        URL connectionURL = null;
        HttpURLConnection connection = null;
        try {
            connectionURL = new URL(mUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (connectionURL != null) {
            try {
                connection = (HttpURLConnection) connectionURL.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return connection;
    }

    public String POST(){
        Log.d(TAG, "POST");
        String resultData = "";
        HttpURLConnection connection = getCommonConnection();
        if(connection != null){
            Log.d(TAG, "Connection not null");
            try {
                connection.connect();
            } catch (IOException e){
                e.printStackTrace();
                return CANNOT_CONNECT_SERVER_JSON;
            }

            try {
                Log.d(TAG, "URL: "+mUrl+"\n\tparameters: "+ mData);
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                byte[] bytes = mData.getBytes();
                for (int i =0; i< bytes.length; i++){
                    bytes[i] = (byte)(bytes[i] & 0xFF);
                }
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = null;
                while ((inputLine = reader.readLine()) != null) {
                    resultData += inputLine + "\n";
                }
                reader.close();
                connection.disconnect();
            } catch (IOException e){
                e.printStackTrace();
                resultData = CANNOT_CONNECT_SERVER_JSON;
            }
        } else {
            resultData = CANNOT_CONNECT_SERVER_JSON;
        }
        Log.d(TAG, "resultData: " + resultData);
        return resultData;
    }

    public void getData(final DataCallback dataCallback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_ORIGIN && dataCallback != null && msg.what == MSG_ORIGIN) {
                    dataCallback.onDataCompletely((JSONObject) msg.obj);
                } else {
//                    Toast.makeText(mContext, "访问服务器出错...", Toast.LENGTH_LONG).show();
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                URL registeUrl = null;
                String resultData = POST();

                if (!resultData.isEmpty()) {
                    try {
                        Message msg = handler.obtainMessage();
                        msg.what = MSG_ORIGIN;
                        msg.obj = new JSONObject(resultData);
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void sendFileInBinary(final String filePath,
                        final DataCallback dataCallback){
        final File file = new File(filePath);
        if(!file.exists()){
            Log.e(TAG, "The File "+filePath+" not exists");
            return;
        }


        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_ORIGIN && dataCallback != null && msg.what == MSG_ORIGIN){
                    dataCallback.onDataCompletely((JSONObject)msg.obj);
                } else {
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultData = "";
                HttpURLConnection connection = getCommonConnection();
                if(connection != null){
                    try {
                        connection.connect();
                        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                        byte[] bytes = mData.getBytes();
                        for (int i =0; i< bytes.length; i++){
                            bytes[i] = (byte)(bytes[i] & 0xFF);
                        }
                        outputStream.write(bytes);

                        FileInputStream bmpInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        while (true){
                            int ins = bmpInputStream.read(buffer);
                            if(ins == -1){
                                bmpInputStream.close();
                                break;
                            }
                            outputStream.write(buffer);
                            outputStream.flush();
                        }

                        outputStream.flush();
                        outputStream.close();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine = null;
                        while ((inputLine = reader.readLine()) != null) {
                            resultData += inputLine;
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e){

                            }

                        }
                        reader.close();
                        connection.disconnect();
                        if (!resultData.isEmpty()) {
                            try {
                                Message msg = handler.obtainMessage();
                                msg.what = MSG_ORIGIN;
                                msg.obj = new JSONObject(resultData);
                                handler.sendMessage(msg);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String createUrlParameters(Map<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        for(String key : parameters.keySet()){
            String value = parameters.get(key);
            sb.append(key.trim()+"="+value.trim()+"&");
        }
        sb.append(CLIENT_AIP);
        return sb.toString();
    }

    public interface  DataCallback {
        void onDataCompletely(JSONObject data);
    }

    public static void loadImageFromServer(String imgUrl, String localTargetPath){
        Log.d(TAG, "imgUrlInServer = "+imgUrl+", localTargetPath = "+localTargetPath);
        URL connectionURL = null;
        HttpURLConnection connection = null;
        try {
            connectionURL = new URL(imgUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (connectionURL != null) {
            try {
                connection = (HttpURLConnection) connectionURL.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                connection.connect();

                InputStream reader = connection.getInputStream();

                File localImageFile = new File(localTargetPath);
                if(localImageFile.exists()){

                } else {
                    localImageFile.createNewFile();
                    byte[] data = new byte[1024];
                    int len = 0;
                    FileOutputStream outputStream = new FileOutputStream(localImageFile);
                    while ((len = reader.read(data)) != -1) {
                        outputStream.write(data, 0, len);
                        outputStream.flush();
                    }
                    outputStream.flush();
                    outputStream.close();
                }
                reader.close();
                connection.disconnect();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
