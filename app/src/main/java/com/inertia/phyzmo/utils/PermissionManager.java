package com.inertia.phyzmo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionManager {
    public static void checkPermissions(Activity activity) {

        ArrayList<String> permsToRequest = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permsToRequest.add(Manifest.permission.CAMERA);
        }

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] array = {};
        if (!permsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permsToRequest.toArray(array), 89);
        }
    }
}
