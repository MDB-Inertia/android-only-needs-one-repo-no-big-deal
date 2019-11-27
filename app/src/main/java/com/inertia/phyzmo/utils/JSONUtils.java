package com.inertia.phyzmo.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONUtils {

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
}
