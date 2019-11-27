package com.inertia.phyzmo.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String getFirstWord(String displayName) {
        if (displayName == null) {
            return "";
        }
        if (displayName.contains(" ")) {
            return capitalizeTitle(displayName.substring(0, displayName.indexOf(' ')));
        }
        return capitalizeTitle(displayName);
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
}
