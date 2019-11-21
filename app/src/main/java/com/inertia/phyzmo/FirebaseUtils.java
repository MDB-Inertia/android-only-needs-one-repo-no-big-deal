package com.inertia.phyzmo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FirebaseUtils {

    private static StorageReference storageRef;
    private static JSONObject jsonObject = null;

    public static void uploadFile(final Activity a, String videoPath) {

        storageRef = FirebaseStorage.getInstance().getReference();

        Uri file = Uri.fromFile(new File(videoPath));
        final StorageReference riversRef = storageRef.child(UUID.randomUUID().toString() + ".mp4");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        System.out.println("Video successfully uploaded!");
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Intent intent = new Intent(a, DisplayDataActivity.class);
                                Bundle mBundle = new Bundle();
                                mBundle.putString("video_url", riversRef.getName());
                                intent.putExtras(mBundle);
                                a.startActivity(intent);
                                //TextView t = a.findViewById(R.id.statusText);
                                //t.setText("Processing File");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        System.out.println("Video upload failed!");
                        exception.printStackTrace();
                    }
                });
    }

    public static void trackObjects(Activity a, String uri) {
        new TrackObjectsRequest(a, uri).execute("https://us-central1-phyzmo.cloudfunctions.net/position-cv-all-saver?uri=gs://phyzmo.appspot.com/" + uri);

    }

    static class RequestTask extends AsyncTask<String, String, String> {

        public Activity activity;

        public RequestTask() {
            System.err.println("No activity passed to the request task object.");
        }

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(((org.apache.http.StatusLine) statusLine).getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
                e.printStackTrace();
            } catch (IOException e) {
                //TODO Handle problems..
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    static class TrackObjectsRequest extends RequestTask {

        public Activity activity;
        private String name;

        public TrackObjectsRequest(Activity a, String name) {
            this.activity = a;
            this.name = name;
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            JSONObject jsonObject = null;
            List<String> keyList = new ArrayList<>();
            try {
                jsonObject = new JSONObject(result);
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    System.out.println(key);
                    keyList.add(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(result);
            MultipleSelectionSpinner mss = activity.findViewById(R.id.chooseItems);
            mss.setActivity(activity);
            mss.setFilename(name);
            mss.setItems(keyList);
            mss.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("Users");
            final DatabaseReference specific_user = userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            specific_user.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> videoIds = new ArrayList<>();
                            for (DataSnapshot d: dataSnapshot.child("videoId").getChildren()) {
                                videoIds.add(d.getValue().toString());
                            }
                            if (!videoIds.contains(name.replace(".mp4", ""))) {
                                videoIds.add(name.replace(".mp4", ""));
                                specific_user.child("videoId").setValue(videoIds);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            //new DataComputationRequest(this.activity).execute("https://us-central1-phyzmo.cloudfunctions.net/data-computation?objectsDataUri=https://storage.googleapis.com/phyzmo-videos/" + name.replace(".mp4", ".json") + "&obj_descriptions=[%27shoe%27]&ref_list=[[0.121,0.215],[0.9645,0.446],0.60]");
        }
    }

    static class DataComputationRequest extends RequestTask {

        public Activity activity;

        public DataComputationRequest(Activity a) {
            this.activity = a;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);

            final AnyChartView acv = activity.findViewById(R.id.chartDisplay);
            acv.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);

            try {
                jsonObject = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            setChart(acv, "Total Distance");

            try {
                List<SampleObject> data = new ArrayList<>();
                for (int i = 0; i < jsonObject.getJSONArray("time").length(); i++) {
                    data.add(new SampleObject(
                            jsonObject.getJSONArray("time").get(i).toString(),
                            jsonObject.getJSONArray("total_distance").get(i).toString(),
                            jsonObject.getJSONArray("velocity").get(i).toString(),
                            jsonObject.getJSONArray("acceleration").get(i).toString()
                    ));
                }
                TableMainLayout table = activity.findViewById(R.id.tableLayout);
                table.setData(data);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final Spinner staticSpinner = activity.findViewById(R.id.chooseGraph);
            staticSpinner.setVisibility(View.VISIBLE);

            final ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                    .createFromResource(activity, R.array.graph_choices,
                            android.R.layout.simple_spinner_item);

            staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            staticSpinner.setAdapter(staticAdapter);

            staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    setChart(acv, staticAdapter.getItem(position).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                }
            });
        }
    }

    static private void setChart(AnyChartView chart, String mode) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        List<DataEntry> seriesData = new ArrayList<>();
        Cartesian cartesian = AnyChart.line();
        String parsedMode = mode.toLowerCase().replace(" ", "_");
        System.out.println("Parse Mode: " + parsedMode);
        try {
            JSONArray timeArray = jsonObject.getJSONArray("time");
            JSONArray normalizedVelocity = jsonObject.getJSONArray(parsedMode);
            ArrayList<Double> roundedNormalizedVel = new ArrayList<Double>();
            for (int j = 0; j < normalizedVelocity.length(); j++) {
                Double d = Double.valueOf(df.format(normalizedVelocity.getDouble(j)));
                roundedNormalizedVel.add(d);
            }
            for (int i = 0; i < timeArray.length(); i++) {
                seriesData.add(new ValueDataEntry(timeArray.getDouble(i), roundedNormalizedVel.get(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name(mode);
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.title("Time vs. " + mode);
        cartesian.xAxis(0).title("Time (seconds)");
        cartesian.yAxis(0).title(mode + "(m / sec)");
        chart.setChart(cartesian);
    }

}
