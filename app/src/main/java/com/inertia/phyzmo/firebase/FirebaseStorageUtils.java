package com.inertia.phyzmo.firebase;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;

import java.io.File;
import java.util.UUID;

public class FirebaseStorageUtils {

    public static String getVideoUrl(String videoId) {
        return "https://storage.googleapis.com/phyzmo.appspot.com/" + videoId + ".mp4";
    }

    public static String getJSONUrl(String videoId) {
        return "https://storage.googleapis.com/phyzmo-videos/" + videoId + ".json";
    }

    public static String getThumbnailUrl(String videoId) {
        return "https://storage.googleapis.com/phyzmo.appspot.com/" + videoId + ".jpg";
    }

    public static void uploadFile(final Activity a, String videoPath, boolean deleteAfter) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(videoPath));
        String videoId = UUID.randomUUID().toString();
        final StorageReference videoRef = storageRef.child(videoId + ".mp4");
        videoRef.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    System.out.println("Video successfully uploaded!");
                    videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Intent intent = new Intent(a, DisplayDataActivity.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putString("video_url", videoId);
                        mBundle.putBoolean("existing_video", false);
                        intent.putExtras(mBundle);
                        a.startActivity(intent);
                    });
                })
                .addOnFailureListener(exception -> {
                    System.out.println("Video upload failed!");
                    exception.printStackTrace();
                });
    }
}
