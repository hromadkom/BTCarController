package com.dev861studios.btcarcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dev861studios.btcarcontroller.model.BluetoothComm;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final String CAR_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    private static final int LONG_TOAST_MESSAGE = 100;

    Button connectBtn, buttonControlBtn, gyroControlBtn;

    private static int REQUEST_ENABLE_BT;

    private ProgressBar progressBar;

    private BluetoothComm bluetoothComm;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothDevice carDevice;

    BroadcastReceiver receiver;

    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_action_name);
        }catch (NullPointerException e){
            // TODO: error
        }

        buttonControlBtn = (Button) findViewById(R.id.button_control_btn);
        gyroControlBtn = (Button) findViewById(R.id.gyro_control_btn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what){
                    case LONG_TOAST_MESSAGE:
                        Toast toast = Toast.makeText(getApplicationContext(), (String) message.obj, Toast.LENGTH_LONG);
                        toast.show();
                        break;
                }
            }
        };

        checkBluetoothConnection();
        connectToCar();
    }

    public void connectToCar(){

        bluetoothAdapter.startDiscovery();
        System.out.println(bluetoothAdapter.isDiscovering());
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                    Message message = handler.obtainMessage(LONG_TOAST_MESSAGE, "Car not found");
                    message.sendToTarget();
                }
            }
        }, 10000);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    System.out.println(device.getAddress());
                    if(device.getAddress().equals("98:D3:31:90:6E:FB")){
                        carDevice = device;
                        bluetoothAdapter.cancelDiscovery();
                        pairDevice(carDevice);
                    }
                }

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if ((state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) || (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDED)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Paired", Toast.LENGTH_LONG);
                        toast.show();

                        bluetoothComm = BluetoothComm.createInstance(carDevice, UUID.fromString(CAR_UUID));
                        bluetoothComm.testDrive();

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                        Toast toast = Toast.makeText(getApplicationContext(), "Unpaired", Toast.LENGTH_LONG);
                        toast.show();
                    }

                }

            }
        };

        IntentFilter actionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter bondStateChangeFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, actionFoundFilter);
        registerReceiver(receiver, bondStateChangeFilter);
    }

    public void showButtonControl(View view){
        Intent intent = new Intent(this, ButtonControlActivity.class);
        startActivity(intent);
    }

    public void showGyroControl(View view){
        Intent intent = new Intent(this, GyroControlActivity.class);
        startActivity(intent);
    }

    private void checkBluetoothConnection(){
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // TODO: check already paired devices
    private void pairDevice(BluetoothDevice device) {
        try {
            System.out.println("Pairing");
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
