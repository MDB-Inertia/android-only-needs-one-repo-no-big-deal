package com.inertia.phyzmo.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.inertia.phyzmo.R;
import com.inertia.phyzmo.firebase.FirebaseAuthUtils;
import com.inertia.phyzmo.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener, View.OnTouchListener {

    EditText emailInput;
    EditText passwordInput;
    Button loginButton;
    TextView toggleSignUp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        toggleSignUp = findViewById(R.id.toggleSignUp);

        emailInput.setOnFocusChangeListener(this);
        passwordInput.setOnFocusChangeListener(this);

        loginButton.setOnClickListener(this);
        toggleSignUp.setOnTouchListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            Utils.hideKeyboard(this, v);
        }
    }

    @Override
    public void onClick(View v) {
        String emailAddress = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        FirebaseAuthUtils.login(this, emailAddress, password);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
        return false;
    }
}
