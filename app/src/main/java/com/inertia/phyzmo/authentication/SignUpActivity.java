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

public class SignUpActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener, View.OnTouchListener {

    EditText nameInput;
    EditText emailInput;
    EditText passwordInput;
    Button signUpButton;
    TextView toggleSignIn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_layout);

        nameInput = findViewById(R.id.signUpNameInput);
        emailInput = findViewById(R.id.signUpEmailInput);
        passwordInput = findViewById(R.id.signUpPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        toggleSignIn = findViewById(R.id.toggleSignIn);

        nameInput.setOnFocusChangeListener(this);
        emailInput.setOnFocusChangeListener(this);
        passwordInput.setOnFocusChangeListener(this);

        signUpButton.setOnClickListener(this);

        toggleSignIn.setOnTouchListener(this);
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
        String name = nameInput.getText().toString();
        FirebaseAuthUtils.signUp(this, emailAddress, password, name);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        return false;
    }
}
