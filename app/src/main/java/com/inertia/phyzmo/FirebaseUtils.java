package com.inertia.phyzmo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
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
        String fileName;

        public DataComputationRequest(Activity a, String filename) {
            this.activity = a;
            this.fileName = filename;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);

            activity.findViewById(R.id.statusText).setVisibility(View.INVISIBLE);

            try {
                jsonObject = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            XYPlot plot = activity.findViewById(R.id.chartDisplay);
            setChart(plot, "Total Distance");
            plot.setVisibility(View.VISIBLE);

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

            ((DisplayDataActivity)(activity)).initializePlayer("https://storage.googleapis.com/phyzmo.appspot.com/" + fileName);

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
                    setChart(plot, staticAdapter.getItem(position).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            activity.findViewById(R.id.displayVideo).setEnabled(true);
            activity.findViewById(R.id.displayChart).setEnabled(true);
            activity.findViewById(R.id.displayObjectChooser).setEnabled(true);
        }
    }

    static private void setChart(XYPlot chart, String mode) {
        String parsedMode = mode.toLowerCase().replace(" ", "_");
        System.out.println("Parse Mode: " + parsedMode);
        chart.clear();
        chart.setTitle(mode + " vs. Time");
        chart.setRangeLabel(mode);
        try {
            JSONArray timeArray = jsonObject.getJSONArray("time");
            JSONArray dataSet = jsonObject.getJSONArray(parsedMode);
            XYSeries series1 = new SimpleXYSeries(Utils.jsonArrayToArrayList(timeArray), Utils.jsonArrayToArrayList(dataSet), mode);
            LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.LTGRAY, Color.BLUE, null, null);
            series1Format.setLegendIconEnabled(false);
            chart.addSeries(series1, series1Format);
            chart.redraw();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
