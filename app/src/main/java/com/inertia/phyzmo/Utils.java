package com.inertia.phyzmo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class Utils {
    public static String getPathFromURI(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static ArrayList<Double> jsonArrayToArrayList(JSONArray a) {
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            try {
                result.add(a.getDouble(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public static String capitalizeTitle(String input) {
        Pattern pattern = Pattern.compile("\\b([a-z])([\\w]*)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase() + matcher.group(2));
        }
        String capitalized = matcher.appendTail(buffer).toString();
        return capitalized;
    }

    public static ArrayList<String> getKeysOfJSONObject(String json) {
        JSONObject jsonObject = null;
        ArrayList<String> keyList = new ArrayList<>();
        try {
            jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                keyList.add(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return keyList;
    }

    public static void setChart(XYPlot chart, String mode, JSONObject jsonObject) {
        String parsedMode = mode.toLowerCase().replace(" ", "_");
        if (parsedMode.equals("normalized_acceleration")) {
            parsedMode = "normalized_acce";
        }
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

    public static String buildStringFromArray(ArrayList<String> data) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < data.size(); ++i) {
            if (foundOne) {
                sb.append(",");
            }
            foundOne = true;
            sb.append("'" + escapeStringUrl(data.get(i).toLowerCase()) + "'");
        }
        return sb.toString();
    }

    public static String escapeStringUrl(String str) {
        return str.replace(" ", "%20").replace("&", "%26")
                    .replace("\"", "\'").replace("[", "%5B")
                    .replace("]", "%5D");
    }

    public static String round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toString();
    }

    public static void checkPermissions(Activity activity) {

        ArrayList<String> permsToRequest = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //String[] permissionRequested={Manifest.permission.CAMERA};
            permsToRequest.add(Manifest.permission.CAMERA);
            //ActivityCompat.requestPermissions(activity, permissionRequested, 101);
        }

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //String[] permissionRequested={Manifest.permission.READ_EXTERNAL_STORAGE};
            //ActivityCompat.requestPermissions(activity, permissionRequested, 101);
            permsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //String[] permissionRequested={Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //ActivityCompat.requestPermissions(activity, permissionRequested, 101);
            permsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] array = {};
        if (!permsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permsToRequest.toArray(array), 89);
        }
    }
}
