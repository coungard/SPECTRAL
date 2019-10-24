package ru.app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class JsonHelper {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final Type TT_mapStringString = new TypeToken<Map<String,String>>(){}.getType();

    static Map<String, String> jsonToMapStringString(String json) {
        Map<String, String> ret = new HashMap<String, String>();
        if (json == null || json.isEmpty())
            return ret;
         return gson.fromJson(json, TT_mapStringString);
    }
    static String mapStringStringToJson(Map<String, String> map) {
        if (map == null)
            map = new HashMap<String, String>();
         return gson.toJson(map);
    }
}
