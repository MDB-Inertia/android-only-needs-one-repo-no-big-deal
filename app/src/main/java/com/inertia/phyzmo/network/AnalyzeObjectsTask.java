package com.inertia.phyzmo.network;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.androidplot.xy.XYPlot;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.datadisplay.models.TableRowModel;
import com.inertia.phyzmo.datadisplay.views.DistanceSelectionView;
import com.inertia.phyzmo.datadisplay.views.TableMainLayout;
import com.inertia.phyzmo.firebase.FirebaseStorageUtils;
import com.inertia.phyzmo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeObjectsTask extends RequestTask {

    private DisplayDataActivity activity;
    private String videoId;
    private ArrayList<String> preEnabledObjects;
    private ArrayList<Double> point1;
    private ArrayList<Double> point2;
    private String unit;
    private JSONObject jsonObject;

    public AnalyzeObjectsTask(DisplayDataActivity a, String id, ArrayList<String> preEnabledObjects, ArrayList<Double> p1, ArrayList<Double> p2, String unit) {
        this.activity = a;
        this.videoId = id;
        this.preEnabledObjects = preEnabledObjects;
        this.point1 = p1;
        this.point2 = p2;
        this.unit = unit;
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("Data Computation Result: " + result);

        activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.loadingGif).setVisibility(View.INVISIBLE);

        try {
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        XYPlot plot = activity.findViewById(R.id.chartDisplay);
        Utils.setChart(plot, "Total Distance", jsonObject);
        plot.setVisibility(View.VISIBLE);

        try {
            List<TableRowModel> data = new ArrayList<>();
            for (int i = 0; i < jsonObject.getJSONArray("time").length(); i++) {
                data.add(new TableRowModel(
                        Utils.round(jsonObject.getJSONArray("time").getDouble(i), 3),
                        Utils.round(jsonObject.getJSONArray("total_distance").getDouble(i), 3),
                        Utils.round(jsonObject.getJSONArray("velocity").getDouble(i), 3),
                        Utils.round(jsonObject.getJSONArray("acceleration").getDouble(i), 3)
                ));
            }
            TableMainLayout table = activity.findViewById(R.id.tableLayout);
            table.setData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new RetrieveObjectsTask(activity, videoId, preEnabledObjects).execute(FirebaseStorageUtils.getJSONUrl(videoId));

        activity.initializePlayer(FirebaseStorageUtils.getVideoUrl(videoId));

        Glide.with(activity.getApplicationContext())
                .load(FirebaseStorageUtils.getThumbnailUrl(videoId))
                .placeholder(R.drawable.gray_square)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ((ImageView) activity.findViewById(R.id.distanceCanvas)).setImageDrawable(resource);
                        DistanceSelectionView distanceCanvas = activity.findViewById(R.id.distanceCanvas);
                        distanceCanvas.drawLine(point1.get(0).floatValue(), point1.get(1).floatValue(), point2.get(0).floatValue(), point2.get(1).floatValue());
                        return false;
                    }
                })
                .into((ImageView) activity.findViewById(R.id.distanceCanvas));

        final Spinner staticSpinner = activity.findViewById(R.id.chooseGraph);
        staticSpinner.setVisibility(View.VISIBLE);

        final ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(activity, R.array.graph_choices,
                        android.R.layout.simple_spinner_item);

        staticAdapter.setDropDownViewResource(R.layout.spinner_layout);
        staticSpinner.setAdapter(staticAdapter);

        String jsonCopy = jsonObject.toString();

        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                JSONObject temp = null;
                try {
                    temp = new JSONObject(jsonCopy);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("Updating chart with this JSONObject: " + temp);
                Utils.setChart(plot, staticAdapter.getItem(position).toString(), temp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ((EditText) activity.findViewById(R.id.distanceInput)).setText(unit);

        DistanceSelectionView distanceCanvas = activity.findViewById(R.id.distanceCanvas);
        distanceCanvas.drawLine(point1.get(0).floatValue(), point1.get(1).floatValue(), point2.get(0).floatValue(), point2.get(1).floatValue());

        activity.setVideoButtonEnabled(true);
        activity.setGraphButtonEnabled(false);
        activity.setChartButtonEnabled(true);
        activity.setObjectsButtonEnabled(true);
    }
}
