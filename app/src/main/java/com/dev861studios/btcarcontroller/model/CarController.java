package com.dev861studios.btcarcontroller.model;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Car controller
 * Implemented as finite state machine
 *
 * Created by martinhromadko on 13.05.16.
 */
public class CarController {

    public static final byte SPEED_FULL = 'F';

    public static final byte SPEED_HALF = 'H';

    private static final byte LEFT_MOTOR = 1;
    private static final byte RIGHT_MOTOR = 2;

    private DataOutputStream outputStream;

    private BluetoothComm.ConnectedThread connectedThread;

    private int actualSpeed;

    private int actualState = 0;

    private int beforeState = 0;

    public CarController(BluetoothComm.ConnectedThread connectedThread){
        this.connectedThread = connectedThread;
    }

    /**
     * Method called when forward event (button press/release, gravity sensor axis) occurred
     * @throws IOException
     */
    public void forward() throws IOException {
        switch (actualState){
            case 0:
                setState(1);
                break;
            case 1:
                setState(0);
                break;
            case 2:
                setState(4);
                break;
            case 3:
                setState(5);
                break;
            case 4:
                setState(2);
                break;
            case 5:
                setState(3);
                break;
        }
    }

    /**
     * Method called when reverse event (button press/release, gravity sensor axis) occurred
     * @throws IOException
     */
    public void reverse() throws IOException {
        switch (actualState){
            case 0:
                setState(6);
                break;
            case 6:
                setState(0);
                break;
            case 7:
                setState(10);
                break;
            case 8:
                setState(9);
                break;
        }
    }

    /**
     * Method called when left event (button press/release, gravity sensor axis) occurred
     * @throws IOException
     */
    public void left() throws IOException {
        switch (actualState){
            case 0:
                setState(4);
                break;
            case 1:
                setState(2);
                break;
            case 2:
                setState(1);
                break;
            case 4:
                setState(0);
                break;
            case 6:
                setState(8);
                break;
            case 8:
                setState(6);
                break;
            case 9:
                setState(0);
                break;
        }
    }

    /**
     * Method called when right event (button press/release, gravity sensor axis) occurred
     * @throws IOException
     */
    public void right() throws IOException {
        switch (actualState){
            case 0:
                setState(5);
                break;
            case 1:
                setState(3);
                break;
            case 3:
                setState(1);
                break;
            case 5:
                setState(0);
                break;
            case 6:
                setState(7);
                break;
            case 7:
                setState(6);
                break;
            case 10:
                setState(0);
                break;
        }
    }


    /**
     * Sets state to car
     * @param state
     * @throws IOException
     */
    private void setState(int state) throws IOException {
        // Def states of car
        switch (state){
            case 0:
                sendCommand("M0E0");
                break;
            case 1:
                sendCommand("M0DD");
                sendCommand("M0E1");
                break;
            case 2:
                sendCommand("M".concat(String.valueOf(LEFT_MOTOR)).concat("E0"));
                break;
            case 3:
                sendCommand("M".concat(String.valueOf(RIGHT_MOTOR)).concat("E0"));
                break;
            case 4:
                sendCommand("M0DD");
                sendCommand("M".concat(String.valueOf(RIGHT_MOTOR)).concat("E1"));
                break;
            case 5:
                sendCommand("M0DD");
                sendCommand("M".concat(String.valueOf(LEFT_MOTOR)).concat("E1"));
                break;
            case 6:
                sendCommand("M0DR");
                sendCommand("M0E1");
                break;
            case 7:
                sendCommand("M".concat(String.valueOf(RIGHT_MOTOR)).concat("E0"));
                break;
            case 8:
                sendCommand("M".concat(String.valueOf(LEFT_MOTOR)).concat("E0"));
                break;
        }

        beforeState = actualState;
        actualState = state;
        System.out.println("Actual state: " + actualState);
        System.out.println("Before state: " + beforeState);
    }

    /**
     * Sending commands
     * @param command
     * @throws IOException
     */
    private void sendCommand(String command) throws IOException {
        connectedThread.write(command);
    }
}
