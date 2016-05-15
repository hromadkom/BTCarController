package com.dev861studios.btcarcontroller;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dev861studios.btcarcontroller.model.BluetoothComm;
import com.dev861studios.btcarcontroller.model.CarController;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GyroControlActivity extends AppCompatActivity {


    private static final float EDGE = 2.5f;

    private static final float FAST_EDGE = 4.0f;

    private SensorManager sensorManager;

    private Sensor sensor;

    CarController carController;

    ProgressDialog dialog;

    private float lastAxisX = 0;

    private float lastAxisY = 0;

    Handler connectionHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BluetoothComm.CONNECTION_OK:
                    carController = (CarController) message.obj;
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection ok", Toast.LENGTH_SHORT);
                    toast.show();
                    prepareSensors();
                    dialog.cancel();
                    try {
                        carController.setSpeed(CarController.SPEED_HALF);
                    } catch (IOException e) {
                        toast = Toast.makeText(getApplicationContext(), "Speed setting error", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gyro_control);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // be awake

        dialog = ProgressDialog.show(GyroControlActivity.this, "",
                "Opening connection ...", true);

        BluetoothComm.getInstance().openCommunication(connectionHandler); // TODO: non-defensive
    }

    private void prepareSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float axisX = event.values[0];
                float axisY = event.values[1];

                try {
                    if (((axisX > -EDGE && axisX < 0) && lastAxisX < -EDGE) || (axisX < -EDGE && (lastAxisX > -EDGE && lastAxisX < 0))){
                        carController.forward();
                    }
                    if (((axisX < EDGE && axisX > 0) && lastAxisX > EDGE) || (axisX > EDGE && (lastAxisX < EDGE && lastAxisX > 0))){
                        carController.reverse();
                    }
                    lastAxisX = axisX;

                    if (((axisY > -EDGE && axisY < 0) && lastAxisY < -EDGE) || (axisY < -EDGE && (lastAxisY > -EDGE && lastAxisY < 0))){
                        carController.left();
                    }
                    if (((axisY < EDGE && axisY > 0) && lastAxisY > EDGE) || (axisY > EDGE && (lastAxisY < EDGE && lastAxisY > 0))){
                        carController.right();
                    }
                    lastAxisY = axisY;

                    if(Math.abs(axisX) > FAST_EDGE){
                        carController.setSpeed(CarController.SPEED_FULL);
                    }else{
                        carController.setSpeed(CarController.SPEED_HALF);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void halt(View view){
        try {
            carController.stop();
        } catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Communication error occurred", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        BluetoothComm.getInstance().endCommunication();
        super.onDestroy();
    }

}
