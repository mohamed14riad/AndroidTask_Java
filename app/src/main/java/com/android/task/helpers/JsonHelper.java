package com.android.task.helpers;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonHelper {

    private static final String TAG = "JsonHelper";

    public static String readJSONFromAsset(Context context) {
        String json = null;

        try {
            InputStream inputStream = context.getAssets().open("products.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Log.e(TAG, "readJSONFromAsset: ", ex);
        }

        return json;
    }
}
