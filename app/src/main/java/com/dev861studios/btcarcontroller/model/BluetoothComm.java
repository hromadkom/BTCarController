package com.dev861studios.btcarcontroller.model;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by martinhromadko on 01.05.16.
 */
public class BluetoothComm {

    private static BluetoothComm comm;

    private BluetoothDevice carDevice;

    private UUID uuid;

    private BluetoothComm(BluetoothDevice device, UUID uuid){
        carDevice = device;
        this.uuid = uuid;
        device.fetchUuidsWithSdp();
    }

    public static BluetoothComm createInstance(BluetoothDevice device, UUID uuid){
        comm = new BluetoothComm(device, uuid);
        return getInstance();
    }

    public static BluetoothComm getInstance(){
        return comm;
    }

    public void testDrive(){
        try {
            BluetoothSocket socket = carDevice.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
            stream.writeBytes("M1E1");
            stream.flush();
            stream.writeBytes("M1E0");
            stream.flush();
            //stream.close();
            //socket.close();
            System.out.println("Test drive finished");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("unable to connect");
        }
    }

}
