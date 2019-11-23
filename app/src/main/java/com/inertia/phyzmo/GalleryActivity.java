package com.inertia.phyzmo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private Button btnTakeVideo;

    private FirebaseAuth mAuth;

    private final int CAMERA_REQUEST_CODE_VIDEO = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.checkPermissions(this);

        final GalleryViewAdapter adapter = new GalleryViewAdapter(this, new ArrayList<>());

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInWithEmailAndPassword("b@b.com", "password123")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                System.out.println("signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                System.out.println("signInWithEmail:failure" + task.getException());
                                Toast.makeText(GalleryActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            System.out.println("Already logged in.");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("Users");
            Query specific_user = userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            specific_user.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> videoIds = new ArrayList<>();
                            for (DataSnapshot d: dataSnapshot.child("videoId").getChildren()) {
                                videoIds.add(d.getValue().toString());
                            }
                            adapter.setData(videoIds);
                            findViewById(R.id.loadingGifMainScreen).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }

        RecyclerView recyclerView = findViewById(R.id.galleryView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        btnTakeVideo = findViewById(R.id.take_video);

        btnTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent,
                            CAMERA_REQUEST_CODE_VIDEO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();
                String path = Utils.getPathFromURI(this, videoUri);

                FirebaseUtils.uploadFile(this, path);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            RecyclerView recyclerView = findViewById(R.id.galleryView);
            GalleryViewAdapter adapter = (GalleryViewAdapter) recyclerView.getAdapter();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("Users");
            Query specific_user = userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            specific_user.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> videoIds = new ArrayList<>();
                            for (DataSnapshot d : dataSnapshot.child("videoId").getChildren()) {
                                videoIds.add(d.getValue().toString());
                            }
                            adapter.setData(videoIds);
                            findViewById(R.id.loadingGifMainScreen).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 89: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("PERMISSION GRANTED!");
                } else {

                }
                return;
            }
        }
    }
}
