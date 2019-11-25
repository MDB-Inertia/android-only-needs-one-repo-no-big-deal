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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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
        v.setEnabled(false);
        String name = nameInput.getText().toString();
        String emailAddress = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (name.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Sign-up failed.",
                    Toast.LENGTH_SHORT).show();
            v.setEnabled(true);
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    user.updateProfile(profileUpdates);

                    FirebaseUtils.addUser(emailAddress, name);

                    Intent intent = new Intent(SignUpActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Sign-up failed.", Toast.LENGTH_SHORT).show();
                    System.out.println(task.getException());
                    v.setEnabled(true);
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        return false;
    }
}
