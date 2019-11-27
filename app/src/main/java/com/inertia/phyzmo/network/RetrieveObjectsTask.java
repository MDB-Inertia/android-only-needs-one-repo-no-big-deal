package com.inertia.phyzmo.network;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.datadisplay.ObjectSelectionAdapter;
import com.inertia.phyzmo.datadisplay.models.ObjectChoiceModel;
import com.inertia.phyzmo.firebase.FirebaseDataUtils;
import com.inertia.phyzmo.utils.JSONUtils;
import com.inertia.phyzmo.utils.StringUtils;

import org.json.JSONObject;

import java.util.ArrayList;

public class RetrieveObjectsTask extends RequestTask {
    private DisplayDataActivity activity;
    private String videoId;
    private ArrayList<String> preEnabledObjects;
    private JSONObject jsonObject;

    public RetrieveObjectsTask(DisplayDataActivity a, String videoId, ArrayList<String> preEnabledObjects) {
        this.activity = a;
        this.videoId = videoId;
        this.preEnabledObjects = preEnabledObjects;
    }

    @Override
    protected void onPostExecute(String result) {
        activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);
        try {
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> keyList = JSONUtils.getKeysOfJSONObject(result);

        RecyclerView objectSelectionView = activity.findViewById(R.id.objectSelectionView);
        ArrayList<ObjectChoiceModel> objectChoiceModels = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            objectChoiceModels.add(new ObjectChoiceModel(StringUtils.capitalizeTitle(keyList.get(i)), preEnabledObjects.contains(keyList.get(i))));
        }

        ObjectSelectionAdapter objectSelectionAdapter = (ObjectSelectionAdapter)objectSelectionView.getAdapter();
        objectSelectionAdapter.setData(objectChoiceModels);
        Button saveObjects = activity.findViewById(R.id.saveObjectsChosen);
        saveObjects.setOnClickListener(v -> {
            FirebaseDataUtils.saveLabConfigToDatabase(activity, videoId);
        });
    }
}