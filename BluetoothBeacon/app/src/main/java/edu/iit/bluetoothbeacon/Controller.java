package edu.iit.bluetoothbeacon;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import edu.iit.bluetoothbeacon.models.Masterpiece;
import edu.iit.bluetoothbeacon.models.Translation;

public class Controller {

    // Server address *CHANGE THIS WHILE IT'S NOT ON THE CLOUD*
    private final String URL = "https://floating-journey-50760.herokuapp.com/getArtwork";

    private static OnResponseReceivedListener onResponseReceivedListener;

    private Context context;
    private static Controller Instance;

    private static RequestQueue requestQueue;

    public Controller(Context context, OnResponseReceivedListener listener){
        this.context = context;
        this.onResponseReceivedListener = listener;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public static Controller getInstance(Context context, OnResponseReceivedListener listener){
        if(Instance == null) {
            Instance = new Controller(context, listener);
        }

        if(onResponseReceivedListener == null) {
            onResponseReceivedListener = listener;
        }

        return Instance;
    }

    public void requestMasterpieceInfo(String id){
        final String dvcName = id;

        String url = this.URL + "?dvcName=" + dvcName;

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Masterpiece mp = null;
                        try {
                            // JSON to Masterpiece
                            JSONArray translations = response.getJSONArray("translations");
                            HashMap<String, Translation> translationsMap = new HashMap<>();
                            for (int i=0;i<translations.length();i++){
                                JSONObject translationJson = translations.getJSONObject(i);
                                translationsMap.put(translationJson.getString("language"), new Translation(translationJson.getString("title"), translationJson.getString("content")));
                            }
                            mp = new Masterpiece(response.getString("dvcName"), translationsMap);
                        } catch (Exception e){
                            // Parsing error
                            if(onResponseReceivedListener != null) {
                                onResponseReceivedListener.OnResponseReceived(null, true);
                            }
                        }
                        // Success
                        if(onResponseReceivedListener != null){
                            onResponseReceivedListener.OnResponseReceived(mp, false);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        if(onResponseReceivedListener != null) {
                            onResponseReceivedListener.OnResponseReceived(null, true);
                        }
                    }
                });
        requestQueue.add(request);
    }
}
