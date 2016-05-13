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

    private DataOutputStream outputStream;

    private BluetoothSocket socket;

    private BluetoothDevice carDevice;

    private UUID uuid;

    private BluetoothComm(BluetoothDevice device, UUID uuid){
        carDevice = device;
        this.uuid = uuid;
        try {
            socket = carDevice.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("unable to connect");
        }
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
            outputStream.writeBytes("M1E1");
            outputStream.flush();
            outputStream.writeBytes("M1E0");
            outputStream.flush();
            System.out.println("Test drive finished");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("unable to connect");
        }
    }

    public void endCommunication(){
        try {
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CarController getCarController(){
        return new CarController(outputStream);
    }

}
