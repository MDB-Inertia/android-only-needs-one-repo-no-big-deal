package com.inertia.phyzmo.firebase;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.authentication.LoginActivity;
import com.inertia.phyzmo.authentication.SignUpActivity;
import com.inertia.phyzmo.gallery.GalleryActivity;
import com.inertia.phyzmo.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthUtils {

    public static void login(LoginActivity loginActivity, String emailAddress, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Button loginButton = loginActivity.findViewById(R.id.loginButton);
        Toast failedLogin =Toast.makeText(loginActivity, loginActivity.getString(R.string.login_failed_notification), Toast.LENGTH_SHORT);

        loginButton.setEnabled(false);
        if (emailAddress.isEmpty() || password.isEmpty()) {
            failedLogin.show();
            loginButton.setEnabled(true);
            return;
        }
        auth.signInWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener(loginActivity, task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(loginActivity, GalleryActivity.class);
                    loginActivity.startActivity(intent);
                    loginActivity.finish();
                } else {
                    failedLogin.show();
                    loginButton.setEnabled(true);
                }
            });
    }

    public static void signUp(SignUpActivity signUpActivity, String emailAddress, String password, String name) {
        Button signUpButton = signUpActivity.findViewById(R.id.loginButton);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Toast failedSignUp =  Toast.makeText(signUpActivity, signUpActivity.getString(R.string.signup_failed_notification), Toast.LENGTH_SHORT);

        signUpButton.setEnabled(false);

        if (name.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
            failedSignUp.show();
            signUpButton.setEnabled(true);
            return;
        }

        auth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(signUpActivity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();
                        user.updateProfile(profileUpdates);

                        addUser(emailAddress, name);

                        Intent intent = new Intent(signUpActivity, GalleryActivity.class);
                        signUpActivity.startActivity(intent);
                        signUpActivity.finish();
                    } else {
                        failedSignUp.show();
                        System.out.println(task.getException());
                        signUpButton.setEnabled(true);
                    }
                });
    }

    public static void addUser(String emailAddress, String name) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference users = db.getReference("Users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> update = new HashMap<>();
        update.put("email", emailAddress);
        update.put("fullname", name);
        update.put("videoId", new ArrayList<>());
        users.child(userId).updateChildren(update);
    }

    public static String getFirstName() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return StringUtils.getFirstWord(auth.getCurrentUser().getDisplayName());
    }

    public static boolean hasName() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser().getDisplayName() == null || auth.getCurrentUser().getDisplayName().isEmpty();
    }
}
