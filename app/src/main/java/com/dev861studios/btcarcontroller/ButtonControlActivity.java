package com.dev861studios.btcarcontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ButtonControlActivity extends AppCompatActivity {


    ImageButton leftBtn, rightBtn, forwardBtn, backwardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_button_control);

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        System.out.println("Pressed down");
                        switch (v.getId()){
                            case R.id.left_btn:
                                System.out.println("Left btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.right_btn:
                                System.out.println("Right btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.forward_btn:
                                System.out.println("Forward btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.backward_btn:
                                System.out.println("Backward btn");
                                // TODO: call bluetooth module
                                break;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println("Up");
                        switch (v.getId()){
                            case R.id.left_btn:
                                System.out.println("Left btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.right_btn:
                                System.out.println("Right btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.forward_btn:
                                System.out.println("Forward btn");
                                // TODO: call bluetooth module
                                break;
                            case R.id.backward_btn:
                                System.out.println("Backward btn");
                                // TODO: call bluetooth module
                                break;
                        }
                        break;
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
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


}
