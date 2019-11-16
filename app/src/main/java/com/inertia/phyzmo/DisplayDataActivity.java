package com.inertia.phyzmo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayDataActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        System.out.println(getIntent().getExtras().getString("video_url"));
        FirebaseUtils.uploadFile(this, getIntent().getExtras().getString("video_url"));
    }

}
