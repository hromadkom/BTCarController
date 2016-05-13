package com.dev861studios.btcarcontroller.model;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by martinhromadko on 13.05.16.
 */
public class CarController {

    public static final byte SPEED_FULL = 'F';

    public static final byte SPEED_HALF = 'H';

    private static final byte LEFT_MOTOR = 1;
    private static final byte RIGHT_MOTOR = 2;

    private DataOutputStream outputStream;

    private int actualSpeed;

    private int actualState = 0;

    private int beforeState = 0;

    public CarController(DataOutputStream dataOutputStream){
        outputStream = dataOutputStream;
    }

    public void forward() throws IOException {
        switch (actualState){
            case 0:
                sendCommand("M0DD");
                sendCommand("M0E1");
                setState(1);
                break;
            case 1:
                sendCommand("M0E0");
                setState(beforeState);
                break;
        }
    }

    public void reverse() throws IOException {
        switch (actualState){
            case 0:
                sendCommand("M0DR");
            case 4:
                sendCommand("M0E1");
                setState(5);
                break;
            case 5:
                sendCommand("M0E0");
                setState(0);
                break;
        }
    }

    public void left() throws IOException {
        switch (actualState){
            case 1:
            case 5:
                sendCommand("M".concat(String.valueOf(LEFT_MOTOR)).concat("E0"));
                setState(3);
                break;
            case 3:
                sendCommand("M0E1");
                setState(beforeState);
                beforeState = 0;
                break;
        }
    }


    public void right() throws IOException {
        switch (actualState){
            case 1:
            case 5:
                sendCommand("M".concat(String.valueOf(RIGHT_MOTOR)).concat("E0"));
                setState(4);
                break;
            case 4:
                sendCommand("M0E1");
                setState(beforeState);
                beforeState = 0;
                break;
        }
    }

    private void setState(int state){
        beforeState = actualState;
        actualState = state;
        System.out.println("Actual state: " + actualState);
        System.out.println("Before state: " + beforeState);
    }

    private void sendCommand(String command) throws IOException {
        outputStream.writeBytes(command);
        outputStream.flush();
    }
}
