package com.inertia.phyzmo.firebase;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.datadisplay.ObjectSelectionAdapter;
import com.inertia.phyzmo.datadisplay.views.DistanceSelectionView;
import com.inertia.phyzmo.gallery.GalleryActivity;
import com.inertia.phyzmo.gallery.GalleryViewAdapter;
import com.inertia.phyzmo.network.AnalyzeObjectsTask;
import com.inertia.phyzmo.network.TrackObjectsTask;
import com.inertia.phyzmo.utils.StringUtils;

import java.util.ArrayList;

public class FirebaseDataUtils {

    public static void saveLabConfigToDatabase(DisplayDataActivity activity, String videoId) {
        RecyclerView objectSelectionView = activity.findViewById(R.id.objectSelectionView);
        DistanceSelectionView distanceCanvas = activity.findViewById(R.id.distanceCanvas);
        EditText distanceInput = activity.findViewById(R.id.distanceInput);

        ObjectSelectionAdapter objectSelectionAdapter = (ObjectSelectionAdapter)objectSelectionView.getAdapter();

        System.out.println("Analyzing just the following: " + objectSelectionAdapter.getSelectedItemsAsString());
        activity.findViewById(R.id.statusText).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.objectSelectionView).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.saveObjectsChosen).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.distanceObjectSwitch).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.distanceCanvas).setVisibility(View.INVISIBLE);
        TextView t = activity.findViewById(R.id.statusText);
        t.setText(activity.getString(R.string.status_analyzing_objects));

        ArrayList<ArrayList<Double>> positions = new ArrayList<>();
        ArrayList<Double> firstPoint = new ArrayList<>();
        ArrayList<Double> secondPoint = new ArrayList<>();
        firstPoint.add((double) (distanceCanvas.firstPoint.x / distanceCanvas.getWidth()));
        firstPoint.add((double) (distanceCanvas.firstPoint.y / distanceCanvas.getHeight()));
        secondPoint.add((double) (distanceCanvas.secondPoint.x / distanceCanvas.getWidth()));
        secondPoint.add((double) (distanceCanvas.secondPoint.y / distanceCanvas.getHeight()));
        positions.add(firstPoint);
        positions.add(secondPoint);
        double units = Double.valueOf(distanceInput.getText().toString());

        ImageView loadingImage = activity.findViewById(R.id.loadingGif);
        loadingImage.setVisibility(View.VISIBLE);
        String selectedList = StringUtils.escapeStringUrl(objectSelectionAdapter.buildSelectedItemString());
        String executedURL = activity.getString(R.string.analyze_objects_endpoint, videoId, selectedList, firstPoint.get(0), firstPoint.get(1), secondPoint.get(0), secondPoint.get(1), units);
        System.out.println("Populating URL: " + executedURL);
        new AnalyzeObjectsTask(activity, videoId, objectSelectionAdapter.getSelectedItems(), firstPoint, secondPoint, String.valueOf(units)).execute(executedURL);

        updateVideoDetails(videoId, objectSelectionAdapter.getSelectedItems(), positions, units);
    }

    public static void updateVideoDetails(String videoId, ArrayList<String> objects, ArrayList<ArrayList<Double>> positions, double units) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference videosRef = database.getReference("Videos");
        DatabaseReference videoRef = videosRef.child(videoId);
        videoRef.child("objects_selected").setValue(objects);
        videoRef.child("unit").setValue(units);
        videoRef.child("line").setValue(positions);
    }

    public static void addVideoIdToUser(String videoId, FirebaseUser user) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users");
        final DatabaseReference specific_user = userRef.child(user.getUid());
        specific_user.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> videoIds = new ArrayList<>();
                        for (DataSnapshot d: dataSnapshot.child("videoId").getChildren()) {
                            videoIds.add(d.getValue().toString());
                        }
                        if (!videoIds.contains(videoId)) {
                            videoIds.add(videoId);
                            specific_user.child("videoId").setValue(videoIds);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public static void deleteVideosForUser(GalleryActivity a, ArrayList<String> selectedToDelete) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users");
        final DatabaseReference specific_user = userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        specific_user.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> videoIds = new ArrayList<>();
                        for (DataSnapshot d: dataSnapshot.child("videoId").getChildren()) {
                            if (!selectedToDelete.contains(d.getValue().toString())) {
                                videoIds.add(d.getValue().toString());
                            } else {
                                System.out.println("Deleting video with ID#: " + d.getValue().toString());
                            }
                        }
                        specific_user.child("videoId").setValue(videoIds);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public static void loadThumbnails(GalleryActivity a, GalleryViewAdapter adapter, PullRefreshLayout layout) {
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
                        a.findViewById(R.id.loadingGifMainScreen).setVisibility(View.INVISIBLE);
                        layout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        layout.setRefreshing(false);
                    }
                });
    }

    public static void openExistingVideo(String id, DisplayDataActivity a) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference videosRef = database.getReference("Videos");
        DatabaseReference videoRef = videosRef.child(id);
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    ArrayList<String> selectedObjects = new ArrayList<>();
                    for (DataSnapshot d: dataSnapshot.child("objects_selected").getChildren()) {
                        selectedObjects.add(d.getValue().toString());
                    }
                    DataSnapshot line = dataSnapshot.child("line");
                    String point1X = line.child("0").child("0").getValue().toString();
                    String point1Y = line.child("0").child("1").getValue().toString();
                    String point2X = line.child("1").child("0").getValue().toString();
                    String point2Y = line.child("1").child("1").getValue().toString();
                    ArrayList<Double> point1 = new ArrayList<>();
                    ArrayList<Double> point2 = new ArrayList<>();
                    point1.add(Double.valueOf(point1X));
                    point1.add(Double.valueOf(point1Y));
                    point2.add(Double.valueOf(point2X));
                    point2.add(Double.valueOf(point2Y));
                    String unit = dataSnapshot.child("unit").getValue().toString();
                    String selectedList = StringUtils.escapeStringUrl(StringUtils.buildStringFromArray(selectedObjects));
                    String executedURL = a.getString(R.string.analyze_objects_endpoint, id, selectedList, point1.get(0), point1.get(1), point2.get(0), point2.get(1), Double.valueOf(unit));
                    System.out.println(executedURL);
                    new AnalyzeObjectsTask(a, id, selectedObjects, point1, point2, unit).execute(executedURL);
                } else {
                    System.err.println("Video does not exit in Videos list in Firebase.");
                    TrackObjectsTask.trackObjects(a, id);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
