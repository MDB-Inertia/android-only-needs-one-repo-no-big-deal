package com.inertia.phyzmo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anychart.core.resource.Grid;
import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GalleryActivity extends AppCompatActivity {

    private Button btnTakeVideo;
    private Button btnSelect;
    private ImageButton btnDeleteVideos;
    private TextView txtWelcome;

    private FirebaseAuth mAuth;

    private final int CAMERA_REQUEST_CODE_VIDEO = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.checkPermissions(this);

        RecyclerView recyclerView = findViewById(R.id.galleryView);
        GridLayoutManager gl = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gl);
        GalleryViewAdapter adapter = new GalleryViewAdapter(this, this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        PullRefreshLayout layout = findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               FirebaseUtils.loadThumbnails(GalleryActivity.this, adapter, layout);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        System.out.println("Already logged in.");
        FirebaseUtils.loadThumbnails(GalleryActivity.this, adapter, layout);

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

        Button logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(GalleryActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        txtWelcome = findViewById(R.id.welcomeText);
        TextPaint paint = txtWelcome.getPaint();
        float width = paint.measureText(txtWelcome.getText().toString());
        Shader textShader = new LinearGradient(0, 0, width, txtWelcome.getTextSize(),
                new int[]{
                        getResources().getColor(R.color.phyzmoBlue),
                        getResources().getColor(R.color.phyzmoPurple),
                }, null, Shader.TileMode.CLAMP);
        txtWelcome.setTextColor(getResources().getColor(R.color.phyzmoBlue));
        txtWelcome.getPaint().setShader(textShader);
        if (mAuth.getCurrentUser().getDisplayName() == null || mAuth.getCurrentUser().getDisplayName().isEmpty()) {
            txtWelcome.setText("Welcome");
        } else {
            txtWelcome.setText("Welcome, " + Utils.getFirstName(mAuth.getCurrentUser().getDisplayName()));
        }

        btnSelect = findViewById(R.id.select_button);
        btnDeleteVideos = findViewById(R.id.deleteVideo);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnSelect.getText().equals("Select")) {
                    btnDeleteVideos.setVisibility(View.VISIBLE);
                    btnSelect.setText("Cancel");
                    adapter.setSelectMode(true);
                } else {
                    btnDeleteVideos.setVisibility(View.INVISIBLE);
                    btnSelect.setText("Select");
                    adapter.setSelectMode(false);
                }
            }
        });

        btnDeleteVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getSelectedToDelete().isEmpty()) {
                    return;
                }
                FirebaseUtils.deleteVideosForUser(GalleryActivity.this, adapter.getSelectedToDelete());
                ArrayList<String> newData = adapter.getData();
                newData.removeAll(adapter.getSelectedToDelete());
                adapter.setData(newData);
                adapter.setSelectMode(false);
                btnSelect.setText("Select");
                btnDeleteVideos.setVisibility(View.INVISIBLE);
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
            PullRefreshLayout layout = findViewById(R.id.swipeRefreshLayout);
            FirebaseUtils.loadThumbnails(GalleryActivity.this, adapter, layout);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 89: {
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
