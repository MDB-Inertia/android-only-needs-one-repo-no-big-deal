package com.inertia.phyzmo;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class DisplayDataActivity extends AppCompatActivity {

    Button showVideo;
    Button showChart;
    Button showGraph;
    Button showObjectChooser;

    Button saveObjectSettings;

    PlayerView playerView;
    RecyclerView objectSelectionView;

    private boolean playWhenReady = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private SimpleExoPlayer player;
    private String videoUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        showVideo = findViewById(R.id.displayVideo);
        showChart = findViewById(R.id.displayChart);
        showGraph = findViewById(R.id.displayGraph);
        showObjectChooser = findViewById(R.id.displayObjectChooser);

        saveObjectSettings = findViewById(R.id.saveObjectsChosen);

        playerView = findViewById(R.id.video_view);

        showGraph.setEnabled(false);

        showVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Video option chosen");
                initializePlayer(videoUrl);
                showVideo.setEnabled(false);
                showChart.setEnabled(true);
                showGraph.setEnabled(true);
                showObjectChooser.setEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.VISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
            }
        });

        showChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Chart option chosen");
                releasePlayer();
                showVideo.setEnabled(true);
                showChart.setEnabled(false);
                showGraph.setEnabled(true);
                showObjectChooser.setEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
            }
        });

        showGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Graph option chosen");
                releasePlayer();
                showVideo.setEnabled(true);
                showChart.setEnabled(true);
                showGraph.setEnabled(false);
                showObjectChooser.setEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.VISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.VISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
            }
        });

        showObjectChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Graph option chosen");
                releasePlayer();
                showVideo.setEnabled(true);
                showChart.setEnabled(true);
                showGraph.setEnabled(true);
                showObjectChooser.setEnabled(false);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.VISIBLE);
                saveObjectSettings.setVisibility(View.VISIBLE);
            }
        });

        objectSelectionView = findViewById(R.id.objectSelectionView);
        ArrayList<ObjectChoiceModel> data = new ArrayList<>();
        objectSelectionView.setLayoutManager(new LinearLayoutManager(this));
        ObjectSelectionAdapter adapter = new ObjectSelectionAdapter(getApplicationContext(), data, this);
        adapter.notifyDataSetChanged();
        objectSelectionView.setAdapter(adapter);


        System.out.println(getIntent().getExtras().getString("video_url"));
        if (!getIntent().getExtras().getBoolean("existing_video")) {
            FirebaseUtils.trackObjects(this, getIntent().getExtras().getString("video_url"));
        } else {
            FirebaseUtils.openExistingVideo(getIntent().getExtras().getString("video_url").replace(".mp4", ""),this);
        }
        //FirebaseUtils.uploadFile(this, getIntent().getExtras().getString("video_url"));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            //initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT < 24 || player == null)) {
            //initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    public void initializePlayer(String videoUrl) {
        this.videoUrl = videoUrl;
        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);

        Uri uri = Uri.parse(videoUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
}
