package com.inertia.phyzmo.gallery;

import android.content.Intent;
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

import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.authentication.LoginActivity;
import com.inertia.phyzmo.firebase.FirebaseAuthUtils;
import com.inertia.phyzmo.firebase.FirebaseDataUtils;
import com.inertia.phyzmo.firebase.FirebaseStorageUtils;
import com.inertia.phyzmo.utils.PermissionManager;
import com.inertia.phyzmo.utils.Utils;

import java.util.ArrayList;

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
        setContentView(R.layout.activity_gallery);

        PermissionManager.checkPermissions(this);

        RecyclerView recyclerView = findViewById(R.id.galleryView);
        GridLayoutManager gl = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gl);
        GalleryViewAdapter adapter = new GalleryViewAdapter(this, this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        PullRefreshLayout layout = findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(() -> FirebaseDataUtils.loadThumbnails(GalleryActivity.this, adapter, layout));

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        System.out.println("Already logged in.");
        FirebaseDataUtils.loadThumbnails(GalleryActivity.this, adapter, layout);

        btnTakeVideo = findViewById(R.id.take_video);
        btnTakeVideo.setOnClickListener(v -> {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent,
                        CAMERA_REQUEST_CODE_VIDEO);
            }
        });

        Button logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(GalleryActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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
        if (FirebaseAuthUtils.hasName()) {
            txtWelcome.setText(getString(R.string.welcome_no_name));
        } else {
            txtWelcome.setText(getString(R.string.welcome_name_prefix, FirebaseAuthUtils.getFirstName()));
        }

        btnSelect = findViewById(R.id.select_button);
        btnDeleteVideos = findViewById(R.id.deleteVideo);

        btnSelect.setOnClickListener(v -> {
            if (btnSelect.getText().equals("Select")) {
                btnDeleteVideos.setVisibility(View.VISIBLE);
                btnSelect.setText("Cancel");
                adapter.setSelectMode(true);
            } else {
                btnDeleteVideos.setVisibility(View.INVISIBLE);
                btnSelect.setText("Select");
                adapter.setSelectMode(false);
            }
        });

        btnDeleteVideos.setOnClickListener(v -> {
            if (adapter.getSelectedToDelete().isEmpty()) {
                return;
            }
            ArrayList<String> copyOfDeletedVideos = (ArrayList<String>) adapter.getSelectedToDelete().clone();
            FirebaseDataUtils.deleteVideosForUser(GalleryActivity.this, copyOfDeletedVideos);
            ArrayList<String> newData = adapter.getData();
            newData.removeAll(adapter.getSelectedToDelete());
            adapter.setData(newData);
            adapter.setSelectMode(false);
            btnSelect.setText("Select");
            btnDeleteVideos.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();
                String path = Utils.getPathFromURI(this, videoUri);
                FirebaseStorageUtils.uploadFile(this, path, true);
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
            FirebaseDataUtils.loadThumbnails(GalleryActivity.this, adapter, layout);
        }
    }
}
