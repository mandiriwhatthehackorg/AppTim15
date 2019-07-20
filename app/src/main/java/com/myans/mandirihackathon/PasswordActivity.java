package com.myans.mandirihackathon;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.keijumt.passwordview.ActionListener;
import com.keijumt.passwordview.PasswordView;
import com.myans.mandirihackathon.interfaces.Const;

import java.util.ArrayList;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener, ActionListener {

    private TextView tv_1;
    private TextView tv_2;
    private TextView tv_3;
    private TextView tv_4;
    private TextView tv_5;
    private TextView tv_6;
    private TextView tv_7;
    private TextView tv_8;
    private TextView tv_9;
    private TextView tv_0;
    private ImageView arrowBack;

    private ArrayList<Integer> pins = new ArrayList<>();

//    private PasswordView passwordView;
    boolean isCorrect = false;

    private ImageView pin1;
    private ImageView pin2;
    private ImageView pin3;
    private ImageView pin4;
    private ImageView pin5;
    private ImageView pin6;

    private ArrayList<ImageView> pinImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_password);
        tv_1 = findViewById(R.id.number1);
        tv_2 = findViewById(R.id.number2);
        tv_3 = findViewById(R.id.number3);
        tv_4 = findViewById(R.id.number4);
        tv_5 = findViewById(R.id.number5);
        tv_6 = findViewById(R.id.number6);
        tv_7 = findViewById(R.id.number7);
        tv_8 = findViewById(R.id.number8);
        tv_9 = findViewById(R.id.number9);
        tv_0 = findViewById(R.id.number0);
        arrowBack = findViewById(R.id.delete);

        pin1 = findViewById(R.id.pin1);
        pin6 = findViewById(R.id.pin6);
        pin5 = findViewById(R.id.pin5);
        pin4 = findViewById(R.id.pin4);
        pin3 = findViewById(R.id.pin3);
        pin2 = findViewById(R.id.pin2);

        pinImages.add(pin1);
        pinImages.add(pin2);
        pinImages.add(pin3);
        pinImages.add(pin4);
        pinImages.add(pin5);
        pinImages.add(pin6);

        tv_1.setOnClickListener(this);
        tv_2.setOnClickListener(this);
        tv_3.setOnClickListener(this);
        tv_4.setOnClickListener(this);
        tv_5.setOnClickListener(this);
        tv_6.setOnClickListener(this);
        tv_7.setOnClickListener(this);
        tv_8.setOnClickListener(this);
        tv_9.setOnClickListener(this);
        tv_0.setOnClickListener(this);
        arrowBack.setOnClickListener(this);

    }

    private void updatePin(boolean isAdd){
        if(isAdd){
            Glide.with(this).load(R.drawable.fill_dot).into(pinImages.get(pins.size()-1));
        }
        else{
            Glide.with(this).load(R.drawable.empty_dot).into(pinImages.get(pins.size()));
        }

        if(pins.size() == 6)
            startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public void onClick(View v) {
        boolean isAdd = true;
        if(v.getId() == R.id.number0)
            pins.add(0);
        else if(v.getId() == R.id.number1)
            pins.add(1);
        else if(v.getId() == R.id.number2)
            pins.add(2);
        else if(v.getId() == R.id.number3)
            pins.add(3);
        else if(v.getId() == R.id.number4)
            pins.add(4);
        else if(v.getId() == R.id.number5)
            pins.add(5);
        else if(v.getId() == R.id.number6)
            pins.add(6);
        else if(v.getId() == R.id.number7)
            pins.add(7);
        else if(v.getId() == R.id.number8)
            pins.add(8);
        else if(v.getId() == R.id.number9)
            pins.add(9);
        else if(v.getId() == R.id.delete){
            if(pins.size() != 0){
                pins.remove(pins.size()-1);
                isAdd = false;
            }
            else
                return;
        }
        updatePin(isAdd);

    }

    @Override
    public void onCompleteInput(String s) {

    }

    @Override
    public void onEndJudgeAnimation() {
        if(isCorrect)
            startActivity(new Intent(this, HomeActivity.class));
        Log.d(Const.TAG, "onEndJudgeAnimation: ");
    }
}
