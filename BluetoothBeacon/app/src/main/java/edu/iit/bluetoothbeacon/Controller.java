package edu.iit.bluetoothbeacon;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import edu.iit.bluetoothbeacon.models.Masterpiece;

/**
 * Created by anderson on 6/25/16.
 */

public class Controller {

    // Server address *CHANGE THIS WHILE IT'S NOT ON THE CLOUD*
    private final String URL = "http://104.194.118.254:8080/getArtwork";

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

    public void requestMasterpieceInfo(String id, String language){
        final String dvcName = id;
        final String lang = language;

        String url = this.URL + "?dvcName=" + dvcName + "&language=" + lang;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Masterpiece mp = null;
                        try {
                            // JSON to Masterpiece
                            mp = new Masterpiece((String) response.get("title"), (String) response.get("content"));
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
