package com.dev861studios.btcarcontroller;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dev861studios.btcarcontroller.model.BluetoothComm;
import com.dev861studios.btcarcontroller.model.CarController;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ButtonControlActivity extends AppCompatActivity {

    ImageButton leftBtn, rightBtn, forwardBtn, backwardBtn;

    CarController carController;

    ProgressDialog dialog;

    Handler connectionHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BluetoothComm.CONNECTION_OK:
                    carController = (CarController) message.obj;
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection ok", Toast.LENGTH_SHORT);
                    toast.show();
                    dialog.cancel();
                    try {
                        carController.setSpeed(CarController.SPEED_FULL);
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

        setContentView(R.layout.activity_button_control);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // be awake

        dialog = ProgressDialog.show(ButtonControlActivity.this, "",
                "Opening connection ...", true);

        BluetoothComm.getInstance().openCommunication(connectionHandler); // TODO: non-defensive

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            switch (v.getId()) {
                                case R.id.left_btn:
                                    carController.left();
                                    break;
                                case R.id.right_btn:
                                    carController.right();
                                    break;
                                case R.id.forward_btn:
                                    carController.forward();
                                    break;
                                case R.id.backward_btn:
                                    carController.reverse();
                                    break;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            switch (v.getId()) {
                                case R.id.left_btn:
                                    carController.left();
                                    break;
                                case R.id.right_btn:
                                    carController.right();
                                    break;
                                case R.id.forward_btn:
                                    carController.forward();
                                    break;
                                case R.id.backward_btn:
                                    carController.reverse();
                                    break;
                            }
                            break;
                    }
                } catch (IOException e) {

                }

                return true;
            }
        };

        leftBtn = (ImageButton) findViewById(R.id.left_btn);
        leftBtn.setOnTouchListener(touchListener);

        rightBtn = (ImageButton) findViewById(R.id.right_btn);
        rightBtn.setOnTouchListener(touchListener);

        forwardBtn = (ImageButton) findViewById(R.id.forward_btn);
        forwardBtn.setOnTouchListener(touchListener);

        backwardBtn = (ImageButton) findViewById(R.id.backward_btn);
        backwardBtn.setOnTouchListener(touchListener);

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
    protected void onDestroy() {
        BluetoothComm.getInstance().endCommunication();
        super.onDestroy();
    }
}
