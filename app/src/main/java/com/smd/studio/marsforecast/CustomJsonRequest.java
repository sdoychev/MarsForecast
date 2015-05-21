package com.smd.studio.marsforecast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by Doychev on 22.5.2015
 */
public class CustomJsonRequest extends JsonObjectRequest {

    private Priority mPriority;

    public CustomJsonRequest(int method, String url, JSONObject jsonRequest,
                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    public Priority getPriority() {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

}
