package nk.mobileapps.otpreceiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import nk.mobileapps.otpreceiver.utils.ResponseListener;
import nk.mobileapps.otpreceiver.utils.RestServiceWithVolle;
import nk.mobileapps.otpreceiver_lib.OTPReceiver;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OTPReceiver.OTPReceiveListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    GoogleApiClient mCredentialsApiClient;
    TextView tv_otp;
    OTPReceiver receiveOTP;
    private int RC_HINT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

    }

    private void findViews() {
        tv_otp = findViewById(R.id.tv_otp);

        mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        startSMSListener();
        receiveOTP = new OTPReceiver();
        receiveOTP.initOTPListener(this);

        requestHint();



        startReceiver();

    }

    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder().setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                mCredentialsApiClient, hintRequest);

        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RC_HINT, null, 0, 0, 0);
        } catch (Exception e) {

        }

    }

    private void startSMSListener() {

        SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);
        client.startSmsRetriever().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                tv_otp.setText("Waiting for the OTP");
                Toast.makeText(MainActivity.this, "SMS Retriever starts", Toast.LENGTH_LONG).show();
            }
        });
        client.startSmsRetriever().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tv_otp.setText("Cannot Start SMS Retriever");
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onOTPReceived(String otp) {
        if (receiveOTP != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveOTP);
        }
        Toast.makeText(this, otp, Toast.LENGTH_SHORT).show();
        tv_otp.setText("Your OTP is: " + otp);
        Log.e("OTP Received", otp);
    }

    @Override
    public void onOTPTimeOut() {
        tv_otp.setText("Timeout");
        if (receiveOTP != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveOTP);
        }
        Toast.makeText(this, " SMS retriever API Timeout", Toast.LENGTH_SHORT).show();
    }

    private void stopReceiver(){
        if (receiveOTP != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveOTP);
        }
    }

    private void startReceiver(){

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(receiveOTP, intentFilter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_HINT && resultCode == Activity.RESULT_OK) {

            /*You will receive user selected phone number here if selected and send it to the server for request the otp*/
            Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

            Toast.makeText(this, " Phone Number:" + credential.getId(), Toast.LENGTH_SHORT).show();
            startVerify(credential.getId().substring(3));
        }
    }

    private void startVerify(String phoneNo) {


        // Communicate to background servers to send SMS and get the expect OTP
        // notifyStatus(STATUS_REQUESTING, phoneNo);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MobileNo", phoneNo);
            jsonObject.put("DealerType", "1");
            jsonObject.put("DeviceID", "");
            jsonObject.put("IMEI", "0");
            jsonObject.put("Version", "1.0");

              /*  String key[] = {"Data"};
                String value[] = {jsonObject.toString()};*/

            RestServiceWithVolle loginobj = new RestServiceWithVolle(this, new ResponseListener() {
                @Override
                public void onSuccess(int responseCode, String response) {
                    try {
                        JSONObject loginobject = new JSONObject(response);
                        if (loginobject.getString("Status").trim().equalsIgnoreCase("200")) {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.verifier_server_response),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Unsuccessful request call.");
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.toast_unverified), Toast.LENGTH_LONG).show();
                           // stopSelf();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Unsuccessful request call.");
                        Toast.makeText(MainActivity.this,
                                getString(R.string.toast_unverified), Toast.LENGTH_LONG).show();
                        //stopSelf();
                    }
                }

                @Override
                public void onError(int responseCode, String error) {
                    Log.d(TAG, "Error getting response");
                    Toast.makeText(MainActivity.this,
                            getString(R.string.toast_request_error), Toast.LENGTH_LONG).show();
                   // stopSelf();
                }
            }, 1245,
                    "", "POST");


            loginobj.loadRequest(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
                    getString(R.string.toast_request_error), Toast.LENGTH_LONG).show();
            //stopSelf();
        }


    }
}

