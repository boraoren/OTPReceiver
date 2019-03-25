package nk.mobileapps.otpreceiver_lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/*
 * created by nk
 * https://github.com/nisrulz/UploadToBintray
 * */
public class OTPReceiver extends BroadcastReceiver {

    private OTPReceiveListener otpReceiveListener = null;

    public void initOTPListener(OTPReceiveListener receiver) {
        this.otpReceiveListener = receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String otp = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server for SMS authenticity.
                    if (otpReceiveListener != null) {
                        otpReceiveListener.onOTPReceived(otp);
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    if (otpReceiveListener != null) {
                        otpReceiveListener.onOTPTimeOut();
                    }
                    break;

            }
        }
    }

    public interface OTPReceiveListener {

        public void onOTPReceived(String otp);

        public void onOTPTimeOut();
    }
}
