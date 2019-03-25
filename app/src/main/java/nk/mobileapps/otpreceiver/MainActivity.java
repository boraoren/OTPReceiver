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

import nk.mobileapps.otpreceiver_lib.OTPReceiver;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OTPReceiver.OTPReceiveListener {

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(receiveOTP, intentFilter);
        requestHint();
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
        Toast.makeText(this, " SMS retriever API Timeout", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_HINT && resultCode == Activity.RESULT_OK) {

            /*You will receive user selected phone number here if selected and send it to the server for request the otp*/
            Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

            Toast.makeText(this, " Phone Number:" + credential.getId(), Toast.LENGTH_SHORT).show();

        }
    }
}

