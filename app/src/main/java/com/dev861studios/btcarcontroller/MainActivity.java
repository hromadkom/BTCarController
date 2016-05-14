package com.dev861studios.btcarcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dev861studios.btcarcontroller.model.BluetoothComm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String CAR_UUID = "00001101-0000-1000-8000-00805f9b34fb";

   // private static final String CAR_MODULE_ADDRESS = "98:D3:31:90:6E:FB";
    private static final String CAR_MODULE_ADDRESS = "20:15:07:24:35:91";

    private static final int CAR_NOT_FOUND_MESSAGE = 404;

    Button buttonControlBtn, gyroControlBtn, retryBtn;

    private ProgressBar progressBar;

    private static int REQUEST_ENABLE_BT;

    private BluetoothComm bluetoothComm;

    private BluetoothAdapter bluetoothAdapter;

    BroadcastReceiver searchReceiver, pairReceiver, uuidReceiver;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_action_name);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        retryBtn = (Button) findViewById(R.id.retry_btn);
        buttonControlBtn = (Button) findViewById(R.id.button_control_btn);
        gyroControlBtn = (Button) findViewById(R.id.gyro_control_btn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        checkBluetoothConnection();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case CAR_NOT_FOUND_MESSAGE:
                        showRetryButton();
                        Toast toast = Toast.makeText(getApplicationContext(), "CarController not found, please power on device", Toast.LENGTH_LONG);
                        toast.show();
                        break;
                }
            }
        };

        registerReceivers();

        if(BluetoothComm.getInstance() == null){
            connectToCar();
        }else{
            showChooseControlButtons();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("Resumed");

        if(BluetoothComm.getInstance() == null){
            connectToCar();
        }else{
            showChooseControlButtons();
        }
    }

    public void retryButtonClick(View view){
        retryBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        connectToCar();
    }

    public void showButtonControl(View view) {
        Intent intent = new Intent(this, ButtonControlActivity.class);
        startActivity(intent);
    }

    public void showGyroControl(View view) {
        Intent intent = new Intent(this, GyroControlActivity.class);
        startActivity(intent);
    }

    private void checkBluetoothConnection() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void showChooseControlButtons(){
        progressBar.setVisibility(View.GONE);
        buttonControlBtn.setVisibility(View.VISIBLE);
        gyroControlBtn.setVisibility(View.VISIBLE);
    }

    private void showRetryButton(){
        buttonControlBtn.setVisibility(View.GONE);
        gyroControlBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        retryBtn.setVisibility(View.VISIBLE);
    }

    // BT Methods

    /**
     * Handles connection to car
     */
    public void connectToCar() {

        if(bluetoothComm != null) {
            showChooseControlButtons();
            return;
        }

        bluetoothAdapter.startDiscovery();
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    Message message = handler.obtainMessage(CAR_NOT_FOUND_MESSAGE);
                    message.sendToTarget();
                }
            }
        }, 10000);
    }

    private void registerReceivers(){
        searchReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getAddress().equals(CAR_MODULE_ADDRESS)) {
                        bluetoothAdapter.cancelDiscovery();
                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                        if (pairedDevices.contains(device)){
                            createBluetoothComForCar(device);
                            // bluetoothComm.testDrive();
                        }else{
                            device.fetchUuidsWithSdp();
                        }
                    }
                }
            }
        };

        IntentFilter actionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(searchReceiver, actionFoundFilter);

        uuidReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_UUID.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if(device != null && uuidExtra != null && bluetoothComm == null) {
                        System.out.println(device.getAddress() + ": " + uuidExtra[0].toString());
                        pairDevice(device);
                    }
                }
            }
        };


        IntentFilter actionUid = new IntentFilter(BluetoothDevice.ACTION_UUID);
        registerReceiver(uuidReceiver, actionUid);

        pairReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if ((state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) || (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDED)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Paired", Toast.LENGTH_LONG);
                        toast.show();

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        createBluetoothComForCar(device);

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Unpaired", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        };

        IntentFilter bondStateChangeFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(pairReceiver, bondStateChangeFilter);
    }

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

    private void createBluetoothComForCar(BluetoothDevice device){
        try {
            bluetoothComm = BluetoothComm.createInstance(device, UUID.fromString(CAR_UUID));
            showChooseControlButtons();
        } catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error while connection to car. Restart car and retry.", Toast.LENGTH_LONG);
            toast.show();
            showRetryButton();
        }
    }

    private void unregisterReceivers(){
        if(uuidReceiver != null){
            unregisterReceiver(uuidReceiver);
        }

        if(searchReceiver != null){
            unregisterReceiver(searchReceiver);
        }

        if(pairReceiver != null) {
            unregisterReceiver(pairReceiver);
        }
    }

    @Override
    protected void onStop() {
        //unregisterReceivers();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }
}
