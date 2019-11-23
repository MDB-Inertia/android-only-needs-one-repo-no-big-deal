package com.inertia.phyzmo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

        if (emailAddress.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    System.out.println("signInWithEmail:failure" + task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
        return false;
    }
}
