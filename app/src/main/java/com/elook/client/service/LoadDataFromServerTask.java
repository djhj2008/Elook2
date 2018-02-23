package com.elook.client.service;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by haiming on 5/16/16.
 */
public class LoadDataFromServerTask implements Callable<JSONObject> {
    String mConnectionUrl = "";
    Map<String, String> mParameters;
    String mData;
    LoadDataFromServer mTask;

    public LoadDataFromServerTask(String url, Map<String, String> parameters) {
        this.mConnectionUrl = url;
        this.mParameters = parameters;
        mTask = new LoadDataFromServer(mConnectionUrl, mParameters);
    }

    public LoadDataFromServerTask(String url, String data) {
        this.mConnectionUrl = url;
        this.mData = data;
        mTask = new LoadDataFromServer(mConnectionUrl, mData);
    }


    @Override
    public JSONObject call() {
        android.util.Log.d("LoadDataFromServerTask", "call");
        String result = this.mTask.POST();

        JSONObject ret = null;
        if (result.isEmpty()) {
            /*TODO*/
//            mMainHandler.sendEmptyMessage(MSG_PROCESS_ACITON_FAILED);
        } else {
                /*when we check the dev state or set dev state,
                * the result from servere is not json, jsut string,
                * so make the json.*/
            if (!result.startsWith("{")) {
                result = "{ result:" + result + "}";
            }

            try {
                ret = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
