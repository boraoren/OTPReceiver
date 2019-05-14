package nk.mobileapps.otpreceiver.utils;
/*Created by nagendra on 21/3/17.*/

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class RestServiceWithVolle {


    private final int ERROR_NETWORK = 1;
    private final int ERROR_TIME_OUT = 2;
    private final int ERROR_HTTP = 3;
    private ProgressDialog progressDialog;
    private String ws;
    private Context context;
    private ResponseListener iResponseCallBack;
    private int responseCode;
    private boolean showPD;
    private String message;
    private String methodType;
    private int resultCode = 0;
    private Map<String, String> postParams;
    private int timeOutInMillis = 0;
    private String httpErrorMsg = "";

    public RestServiceWithVolle(Context context, ResponseListener listener, int responseCode, String url, String methodType) {
        this(context, listener, responseCode, url, methodType, true);
    }

    public RestServiceWithVolle(Context context, ResponseListener listener, int responseCode, String url, String methodType, boolean showProgress) {
        this(context, listener, responseCode, url, methodType, showProgress, null);
    }

    public RestServiceWithVolle(Context context, ResponseListener listener, int responseCode, String url, String methodType, String message) {
        this(context, listener, responseCode, url, methodType, true, message);
    }

    public RestServiceWithVolle(Context context, ResponseListener listener, int responseCode, String url, String methodType, boolean showProgress, String message) {

        this.context = context;
        this.ws = url;
        this.iResponseCallBack = listener;
        this.responseCode = responseCode;
        this.showPD = showProgress;
        this.message = message;
        this.methodType = methodType;
    }

    private void showProgressDialog() {

        if (showPD) {
            try {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                if (message == null) {
                    progressDialog.setMessage("Loading data");
                } else {
                    progressDialog.setMessage(message);
                }
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadRequest(JSONObject jsonObject) {
        showProgressDialog();
        /* if (isNetworkAvailable(context)) {*/
        JsonObjectRequest objectRequest = new JsonObjectRequest(methodType.endsWith("POST") ? 1 : 0, ws, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                Log.d("loadResult", "onResponse: " + response.toString());
                if (iResponseCallBack == null) {
                    Log.e("Error", "Listener not implemented");
                } else {
                    iResponseCallBack.onSuccess(responseCode, response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();
                Log.d("ErrorResponse", "onErrorResponse: " + error.toString());
                if (iResponseCallBack == null) {
                    Log.e("Error", "Listener not implemented");
                } else {
                    iResponseCallBack.onError(responseCode, error.toString());
                }

            }
        });

        objectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                closeProgressDialog();
                Log.d("ErrorResponse", "onErrorResponse: " + error.toString());
                if (iResponseCallBack == null) {
                    Log.e("Error", "Listener not implemented");
                } else {
                    iResponseCallBack.onError(responseCode, error.toString());
                }
            }
        });

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(objectRequest);


        /*} else {
            resultCode = ERROR_NETWORK;
            if (iResponseCallBack == null) {
                Log.e("Error", "Listener not implemented");
            } else if (resultCode == ERROR_NETWORK) {
                iResponseCallBack.onError(responseCode, "Network is not available");
            }
        }*/


    }
}
