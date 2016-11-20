package com.geektime.reactnativeonesignal;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.onesignal.OneSignal;
import com.onesignal.OSNotification;

import org.json.JSONObject;

/**
 * Created by Avishay on 1/31/16.
 */
public class NotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {

    private ReactContext mReactContext;

    public NotificationReceivedHandler(ReactContext reactContext) {
        mReactContext = reactContext;
    }

    @Override
    public void notificationReceived(OSNotification result) {
        Bundle bundle = new Bundle();
        bundle.putString("result", result.stringify());

        final Intent intent = new Intent(RNOneSignal.NOTIFICATION_OPENED_INTENT_FILTER);
        intent.putExtras(bundle);

        if (mReactContext.hasActiveCatalystInstance()) {
            mReactContext.sendBroadcast(intent);
            return;
        }

        mReactContext.addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                mReactContext.sendBroadcast(intent);
                mReactContext.removeLifecycleEventListener(this);
            }

            @Override
            public void onHostPause() {

            }

            @Override
            public void onHostDestroy() {

            }
        });
    }
}
