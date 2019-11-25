package com.inertia.phyzmo;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    TextView statusLine;

    EditText distanceInput;

    ConstraintLayout showVideo;
    ConstraintLayout showChart;
    ConstraintLayout showGraph;
    ConstraintLayout showObjectChooser;

    Button saveObjectSettings;

    Button goBack;

    PlayerView playerView;
    RecyclerView objectSelectionView;

    private boolean playWhenReady = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private SimpleExoPlayer player;
    private String videoUrl;

    ObjectSelectionAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        statusLine = findViewById(R.id.statusText);

        distanceInput = findViewById(R.id.distanceInput);

        ((CustomImageView) findViewById(R.id.distanceCanvas)).activity = this;

        showVideo = findViewById(R.id.displayVideo);
        showChart = findViewById(R.id.displayChart);
        showGraph = findViewById(R.id.displayGraph);
        showObjectChooser = findViewById(R.id.displayObjectChooser);

        saveObjectSettings = findViewById(R.id.saveObjectsChosen);

        goBack = findViewById(R.id.home_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playerView = findViewById(R.id.video_view);

        setGraphButtonEnabled(false);

        showVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Video option chosen");
                initializePlayer(videoUrl);
                setVideoButtonEnabled(false);
                setChartButtonEnabled(true);
                setGraphButtonEnabled(true);
                setObjectsButtonEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.VISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceObjectSwitch).setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
            }
        });

        showChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Chart option chosen");
                releasePlayer();
                setVideoButtonEnabled(true);
                setChartButtonEnabled(false);
                setGraphButtonEnabled(true);
                setObjectsButtonEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceObjectSwitch).setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
            }
        });

        showGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Graph option chosen");
                releasePlayer();
                setVideoButtonEnabled(true);
                setChartButtonEnabled(true);
                setGraphButtonEnabled(false);
                setObjectsButtonEnabled(true);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.VISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.VISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                saveObjectSettings.setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceObjectSwitch).setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
            }
        });

        showObjectChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Graph option chosen");
                releasePlayer();
                setVideoButtonEnabled(true);
                setGraphButtonEnabled(true);
                setChartButtonEnabled(true);
                setObjectsButtonEnabled(false);
                findViewById(R.id.video_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.chartDisplay).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseGraph).setVisibility(View.INVISIBLE);
                findViewById(R.id.tableLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.objectSelectionView).setVisibility(View.VISIBLE);
                saveObjectSettings.setVisibility(View.VISIBLE);
                findViewById(R.id.distanceObjectSwitch).setVisibility(View.VISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
                findViewById(R.id.switchToObjects).setEnabled(false);
                findViewById(R.id.switchToDistance).setEnabled(true);
                findViewById(R.id.distanceInput).setEnabled(false);
            }
        });

        findViewById(R.id.switchToObjects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.switchToObjects).setEnabled(false);
                findViewById(R.id.switchToDistance).setEnabled(true);
                findViewById(R.id.objectSelectionView).setVisibility(View.VISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceInput).setEnabled(false);
                //saveObjectSettings.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.switchToDistance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.switchToObjects).setEnabled(true);
                findViewById(R.id.switchToDistance).setEnabled(false);
                findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
                findViewById(R.id.distanceCanvas).setVisibility(View.VISIBLE);
                findViewById(R.id.distanceInput).setEnabled(true);
                //saveObjectSettings.setVisibility(View.INVISIBLE);
            }
        });

        objectSelectionView = findViewById(R.id.objectSelectionView);
        ArrayList<ObjectChoiceModel> data = new ArrayList<>();
        objectSelectionView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObjectSelectionAdapter(getApplicationContext(), data, this);
        adapter.notifyDataSetChanged();
        objectSelectionView.setAdapter(adapter);

        distanceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.updateSelectButton();
            }
        });


        System.out.println(getIntent().getExtras().getString("video_url"));
        if (!getIntent().getExtras().getBoolean("existing_video")) {
            FirebaseUtils.trackObjects(this, getIntent().getExtras().getString("video_url"));
        } else {
            FirebaseUtils.openExistingVideo(getIntent().getExtras().getString("video_url").replace(".mp4", ""),this);
            statusLine.setText("Gathering data...");
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

    private void setVideoButtonEnabled(boolean e) {
        ImageView img = showVideo.findViewById(R.id.imageView3);
        TextView label = showVideo.findViewById(R.id.textView2);
        showVideo.setEnabled(e);
        if (!e) {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.phyzmoBlue)));
            label.setTextColor(getResources().getColor(R.color.phyzmoBlue));
        } else {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.ap_black)));
            label.setTextColor(getResources().getColor(R.color.ap_black));
        }
    }

    private void setGraphButtonEnabled(boolean e) {
        ImageView img = showGraph.findViewById(R.id.imageView4);
        TextView label = showGraph.findViewById(R.id.textview3);
        showGraph.setEnabled(e);
        if (!e) {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.phyzmoBlue)));
            label.setTextColor(getResources().getColor(R.color.phyzmoBlue));
        } else {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.ap_black)));
            label.setTextColor(getResources().getColor(R.color.ap_black));
        }
    }

    private void setChartButtonEnabled(boolean e) {
        ImageView img = showChart.findViewById(R.id.imageView6);
        TextView label = showChart.findViewById(R.id.textView4);
        showChart.setEnabled(e);
        if (!e) {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.phyzmoBlue)));
            label.setTextColor(getResources().getColor(R.color.phyzmoBlue));
        } else {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.ap_black)));
            label.setTextColor(getResources().getColor(R.color.ap_black));
        }
    }

    private void setObjectsButtonEnabled(boolean e) {
        ImageView img = showObjectChooser.findViewById(R.id.imageView5);
        TextView label = showObjectChooser.findViewById(R.id.textView5);
        showObjectChooser.setEnabled(e);
        if (!e) {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.phyzmoBlue)));
            label.setTextColor(getResources().getColor(R.color.phyzmoBlue));
        } else {
            img.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.ap_black)));
            label.setTextColor(getResources().getColor(R.color.ap_black));
        }
    }
}
