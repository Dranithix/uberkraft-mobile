package com.dranithix.hackust;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

@EActivity(R.layout.activity_confirm_booking)
public class ConfirmBookingActivity extends AppCompatActivity implements SensorEventListener {
    @Extra
    String passenger;

    @Extra
    String airport;

    @Extra
    String airline;

    @Extra
    String flightNum;
    @Extra
    String operationDate;

    @ViewById(R.id.step_count)
    TextView stepCount;

    private boolean activityRunning;

    private SensorManager sensorManager;
    private int initialSteps = -1, numSteps = 0;
    private String ticketId;

    @AfterViews
    public void init() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @AfterExtras
    public void queryFlightInfo() {
        MeteorSingleton.getInstance().call("tickets.add", new Object[]{passenger, airport, airline, flightNum, operationDate}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                ticketId = result;
            }

            @Override
            public void onError(String error, String reason, String details) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
            if (initialSteps == -1) initialSteps = (int) event.values[0];
            else numSteps = (int) (event.values[0] - initialSteps);

            stepCount.setText("We have tracked " + String.valueOf(numSteps) + " step(s) from you.");
            if (ticketId != null) {
                MeteorSingleton.getInstance().call("step.update", new Object[]{ticketId, numSteps}, new ResultListener() {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println(result);
                    }

                    @Override
                    public void onError(String error, String reason, String details) {

                    }
                });
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
