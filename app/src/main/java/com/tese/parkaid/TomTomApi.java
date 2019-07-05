package com.tese.parkaid;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class TomTomApi {

    private final static String TOMTOM_BASE_URL = "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/xml?";
    private final static String pointSeperator = "%2C";
    private final static String TOMWEATHER_API_KEY = "Mc3hzO22aGsxW5o9j3tJYXsRbiuKOGbG";
    private final static String PARAM_API_KEY = "key";

    protected static String getResponseForAPI(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream input = urlConnection.getInputStream();
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }
        return null;
    }

    protected static URL buildUrlTomTom(){
        Uri buildUri = Uri.parse(TOMTOM_BASE_URL).buildUpon().appendQueryParameter("point", "").appendQueryParameter(PARAM_API_KEY, TOMWEATHER_API_KEY).build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

}
