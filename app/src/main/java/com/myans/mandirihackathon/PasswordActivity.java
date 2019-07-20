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

import com.keijumt.passwordview.ActionListener;
import com.keijumt.passwordview.PasswordView;
import com.myans.mandirihackathon.interfaces.Const;

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

    private PasswordView passwordView;
    boolean isCorrect = false;

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
        passwordView = findViewById(R.id.passwordView);

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

        passwordView.setListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.number0)
            passwordView.appendInputText("0");
        else if(v.getId() == R.id.number1)
            passwordView.appendInputText("1");
        else if(v.getId() == R.id.number2)
            passwordView.appendInputText("2");
        else if(v.getId() == R.id.number3)
            passwordView.appendInputText("3");
        else if(v.getId() == R.id.number4)
            passwordView.appendInputText("4");
        else if(v.getId() == R.id.number5)
            passwordView.appendInputText("5");
        else if(v.getId() == R.id.number6)
            passwordView.appendInputText("6");
        else if(v.getId() == R.id.number7)
            passwordView.appendInputText("7");
        else if(v.getId() == R.id.number8)
            passwordView.appendInputText("8");
        else if(v.getId() == R.id.number9)
            passwordView.appendInputText("9");
        else if(v.getId() == R.id.delete)
            passwordView.removeInputText();
    }

    @Override
    public void onCompleteInput(String s) {
        if(s.equals("123456")){
            passwordView.correctAnimation();
            isCorrect = true;

        }
        else{
            passwordView.incorrectAnimation();
            isCorrect = false;
        }
    }

    @Override
    public void onEndJudgeAnimation() {
        if(isCorrect)
            startActivity(new Intent(this, HomeActivity.class));
        Log.d(Const.TAG, "onEndJudgeAnimation: ");
    }
}
