package eu.smesec.core.utils;

import com.google.gson.Gson;

public class JsonUtils {
    public static String toJson(String company) {
        Gson gson = new Gson();
        return gson.toJson(company);
    }
}
