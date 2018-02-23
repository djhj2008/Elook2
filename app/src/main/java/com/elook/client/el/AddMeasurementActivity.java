package com.elook.client.el;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.elook.client.R;

/**
 * Created by haiming on 5/25/16.
 */
public class AddMeasurementActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);
    }


    public void addMeasurement(View v){
        int id = v.getId();
        switch (id){
            case R.id.activity_measurement_add_device:
            case R.id.add_water_measurement:
            case R.id.add_gas_measurement:
                Intent intent = new Intent(AddMeasurementActivity.this, ScanQRActivity.class);
                startActivity(intent);
                break;
        }

    }

    public void back(View v){
        finish();
    }
}
