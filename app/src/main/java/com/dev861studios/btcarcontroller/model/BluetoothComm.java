package com.dev861studios.btcarcontroller.model;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Communication singleton
 */
public class BluetoothComm {

    public static final int CONNECTION_OK = 200;

    private static BluetoothComm comm;

    private BluetoothDevice carDevice;

    private UUID uuid;

    private ConnectThread connectThread;

    private ConnectedThread connectedThread;

    private BluetoothSocket activeSocket;

    private Handler returnHandler;

    private BluetoothComm(BluetoothDevice device, UUID uuid) throws IOException {
        carDevice = device;
        this.uuid = uuid;
    }

    public static BluetoothComm createInstance(BluetoothDevice device, UUID uuid) throws IOException {
        comm = new BluetoothComm(device, uuid);
        return getInstance();
    }

    public static BluetoothComm getInstance() {
        return comm;
    }


    /**
     * Opens communication with device
     * @param handler
     */
    public void openCommunication(Handler handler){
        returnHandler = handler;
        connectThread = new ConnectThread(carDevice);
        connectThread.start();
    }

    /**
     * Ends communication with device
     */
    public void endCommunication() {
        connectedThread.cancel();
        //connectThread.cancel();
    }

    /**
     * Handle connected socket
     * @param socket
     */
    public void manageConnectedSocket(BluetoothSocket socket){
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        connectThread = null;
        CarController carController = new CarController(connectedThread);
        returnHandler.obtainMessage(BluetoothComm.CONNECTION_OK, carController).sendToTarget();
    }

    /**
     * ConnectThred - async open connection
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                // TODO: should be handled in Activity
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                    // TODO: should be handled in Activity
                }
                return;
            }

            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: should be handled in Activity
            }
        }
    }

    /**
     * ConnectedThread - async sending data to device
     */
    protected class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final DataOutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: should be handled in Activity
            }

            mmOutStream = new DataOutputStream(tmpOut);
        }

        public void write(String string) throws IOException {
            mmOutStream.writeBytes(string);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: should be handled in Activity
            }
        }
    }

}
