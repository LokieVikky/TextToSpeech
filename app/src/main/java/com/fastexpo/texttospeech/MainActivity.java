package com.fastexpo.texttospeech;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fastexpo.texttospeech.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mBinding;
    private static final String TAG = "MainActivity";
    String baseURL = "http://demo5951353.mockable.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Log.d(TAG, token);
                        saveToken(MainActivity.this, token);
                    }
                });

        mBinding.edtCustomerID.setText(getCustomerID(MainActivity.this));

        mBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String customerID = mBinding.edtCustomerID.getText().toString();
                    if (customerID.equals("")) {
                        Toast.makeText(MainActivity.this, "Enter Customer ID", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveCustomerID(MainActivity.this, mBinding.edtCustomerID.getText().toString());
                    String token = getToken(MainActivity.this);
                    if (token.equals("")) {
                        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name).setMessage("Unable to verify device" + "\n" +
                                "Turn on Internet" + "\n" +
                                "Clear app data and retry").setPositiveButton("OK", null).show();
                        return;
                    }

                    JSONObject tokenDetails = new JSONObject();
                    tokenDetails.put("CustomerID", customerID);
                    tokenDetails.put("FirebaseToken", token);

                    httpRequest(getApplicationContext(), tokenDetails, Request.Method.POST, new VolleyCallback() {
                        @Override
                        public void OnSuccess(String object) {
                            Toast.makeText(MainActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void OnFailure(VolleyError error) {
                            Toast.makeText(MainActivity.this, "Unable to save", Toast.LENGTH_SHORT).show();
                        }
                    }, 10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Log.d(TAG, "savedToken: "+getToken(MainActivity.this));
    }

    public void saveCustomerID(Context context, String customerID) {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(getString(R.string.customerID), customerID);
        editor.apply();
    }

    public String getCustomerID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE);
        return sp.getString(getString(R.string.customerID), "");
    }

    public void saveToken(Context context, String fcmToken) {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(getString(R.string.fcmToken), fcmToken);
        editor.apply();
    }

    public String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE);
        return sp.getString(getString(R.string.fcmToken), "");
    }

    public void httpRequest(Context mContext, @Nullable JSONObject message, final int method,
                            final VolleyCallback callBack, int timeOut) throws Exception {
        if (mContext == null) {
            throw new Exception("Null Context");
        }
        if (callBack == null) {
            throw new Exception("Null CallBack");
        }
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        //String URL = Base_url + apiType;
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.OnSuccess(response);
            }
        };

        Response.ErrorListener volleyErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.OnFailure(error);
            }
        };
        StringRequest stringRequest = new StringRequest(method, baseURL, responseListener, volleyErrorListener) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return message == null ? null : message.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", message.toString(), "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = new String(response.data);
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 1, 1.0f));
        requestQueue.add(stringRequest);
    }

    public interface VolleyCallback {
        public void OnSuccess(String object);

        public void OnFailure(VolleyError error);
    }

}