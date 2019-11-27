package com.inertia.phyzmo.network;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.datadisplay.ObjectSelectionAdapter;
import com.inertia.phyzmo.datadisplay.models.ObjectChoiceModel;
import com.inertia.phyzmo.firebase.FirebaseDataUtils;
import com.inertia.phyzmo.firebase.FirebaseStorageUtils;
import com.inertia.phyzmo.utils.JSONUtils;
import com.inertia.phyzmo.utils.StringUtils;

import java.util.ArrayList;

public class TrackObjectsTask extends RequestTask {

    public DisplayDataActivity activity;
    private String videoId;

    public TrackObjectsTask(DisplayDataActivity a, String id) {
        this.activity = a;
        this.videoId = id;
    }

    @Override
    protected void onPostExecute(String result) {
        ArrayList<String> keyList = JSONUtils.getKeysOfJSONObject(result);

        RecyclerView objectSelectionView = activity.findViewById(R.id.objectSelectionView);
        ArrayList<ObjectChoiceModel> objectChoiceModels = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            objectChoiceModels.add(new ObjectChoiceModel(StringUtils.capitalizeTitle(keyList.get(i)), false));
        }

        ObjectSelectionAdapter objectSelectionAdapter = (ObjectSelectionAdapter)objectSelectionView.getAdapter();
        objectSelectionAdapter.setData(objectChoiceModels);
        Button saveObjects = activity.findViewById(R.id.saveObjectsChosen);
        saveObjects.setOnClickListener(v -> {
            FirebaseDataUtils.saveLabConfigToDatabase(activity, videoId);
        });

        Glide.with(activity.getApplicationContext())
                .load(FirebaseStorageUtils.getThumbnailUrl(videoId))
                .placeholder(R.drawable.gray_square)
                .dontAnimate()
                .into((ImageView) activity.findViewById(R.id.distanceCanvas));
        activity.findViewById(R.id.distanceCanvas).setVisibility(View.VISIBLE);

        System.out.println(result);
        activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);
        ImageView loadingImage = activity.findViewById(R.id.loadingGif);
        loadingImage.setVisibility(View.INVISIBLE);

        ConstraintLayout showObjectChooser = activity.findViewById(R.id.displayObjectChooser);
        showObjectChooser.callOnClick();

        activity.findViewById(R.id.displayVideo).setEnabled(false);
        activity.findViewById(R.id.displayChart).setEnabled(false);
        activity.findViewById(R.id.displayGraph).setEnabled(false);

        FirebaseDataUtils.addVideoIdToUser(videoId, FirebaseAuth.getInstance().getCurrentUser());
    }

    public static void trackObjects(DisplayDataActivity a, String id) {
        new TrackObjectsTask(a, id).execute(a.getString(R.string.track_objects_endpoint, id));

    }
}