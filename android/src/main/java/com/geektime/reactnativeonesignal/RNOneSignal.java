package com.geektime.reactnativeonesignal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.onesignal.OneSignal;

import org.json.JSONObject;
import org.json.JSONException;


/**
 * Created by Avishay on 1/31/16.
 */
public class RNOneSignal extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NOTIFICATION_OPENED_INTENT_FILTER = "GTNotificatinOpened";

    private ReactContext mReactContext;

    public void forceAppFocusStatus() {
        try {
            java.lang.reflect.Method onAppFocus = OneSignal.class.getDeclaredMethod("onAppFocus");
            onAppFocus.setAccessible(true);
            onAppFocus.invoke(null);
            java.lang.reflect.Method isAppActive = OneSignal.class.getDeclaredMethod("isAppActive");
            isAppActive.setAccessible(true);
            boolean isActive = (boolean)isAppActive.invoke(null);
            Log.i("ReactNativeJS", "OneSignal isAppActive: " + isActive);
        } catch (java.lang.NoSuchMethodException x) {
            x.printStackTrace();
        } catch (java.lang.IllegalAccessException x) {
            x.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException x) {
            x.printStackTrace();
        }
    }

    public RNOneSignal(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        mReactContext.addLifecycleEventListener(this);
        OneSignal.startInit(mReactContext)
                .setNotificationOpenedHandler(new NotificationOpenedHandler(mReactContext))
                .setNotificationReceivedHandler(new NotificationReceivedHandler(mReactContext))
                .init();
        forceAppFocusStatus();
        //OneSignal.setInFocusDisplaying(0);
        registerNotificationsReceiveNotification();
    }

    private void sendEvent(String eventName, Object params) {
        try {
        mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
        catch (Exception e) {
        }
    }

    @ReactMethod
    public void sendTag(String key, String value) {
        OneSignal.sendTag(key, value);
    }

    @ReactMethod
    public void sendTags(ReadableMap tags) {
        OneSignal.sendTags(RNUtils.readableMapToJson(tags));
    }

    @ReactMethod
    public void getTags(final Callback callback) {
        OneSignal.getTags(new OneSignal.GetTagsHandler() {
            @Override
            public void tagsAvailable(JSONObject tags) {
                callback.invoke(RNUtils.jsonToWritableMap(tags));
            }
        });
    }

    @ReactMethod
    public void configure() {
        forceAppFocusStatus();
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            public void idsAvailable(String userId, String registrationId) {
                final WritableMap params = Arguments.createMap();

                params.putString("userId", userId);
                params.putString("pushToken", registrationId);

                sendEvent("idsAvailable", params);
            }
        });
    }

    @ReactMethod
    public void deleteTag(String key) {
        OneSignal.deleteTag(key);
    }

    @ReactMethod
    public void enableVibrate(Boolean enable) {
        OneSignal.enableVibrate(enable);
    }

    @ReactMethod
    public void enableSound(Boolean enable) {
        OneSignal.enableSound(enable);
    }

    /*@ReactMethod
    public void enableNotificationsWhenActive(Boolean enable) {
        OneSignal.enableNotificationsWhenActive(enable);
    }

    @ReactMethod
    public void enableInAppAlertNotification(Boolean enable) {
        OneSignal.enableInAppAlertNotification(enable);
    }*/

    @ReactMethod
    public void setSubscription(Boolean enable) {
        OneSignal.setSubscription(enable);
    }

    @ReactMethod
    public void promptLocation() {
        OneSignal.promptLocation();
    }

    @ReactMethod
    public void postNotification(String contents, String data, String player_id) {
        try {
          OneSignal.postNotification(new JSONObject("{'contents': " + contents + ", 'data': {'p2p_notification': " + data +"}, 'include_player_ids': ['" + player_id + "']}"),
             new OneSignal.PostNotificationResponseHandler() {
               @Override
               public void onSuccess(JSONObject response) {
                 Log.i("OneSignal", "postNotification Success: " + response.toString());
               }

               @Override
               public void onFailure(JSONObject response) {
                 Log.e("OneSignal", "postNotification Failure: " + response.toString());
               }
             });
        } catch (JSONException e) {
          e.printStackTrace();
        }
    }

    @ReactMethod
    public void clearOneSignalNotifications() {
        OneSignal.clearOneSignalNotifications();
    }

    @ReactMethod
    public void cancelNotification(int id) {
        OneSignal.cancelNotification(id);
    }

    private void registerNotificationsReceiveNotification() {
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_OPENED_INTENT_FILTER);
        mReactContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotification(intent.getExtras());
            }
        }, intentFilter);
    }

    private void notifyNotification(Bundle bundle) {
        final WritableMap params = Arguments.createMap();
        params.putString("result", bundle.getString("result"));
        //params.putString("message", bundle.getString("message"));
        //params.putString("additionalData", bundle.getString("additionalData"));
        //params.putBoolean("isActive", bundle.getBoolean("isActive"));

        sendEvent("remoteNotificationOpened", params);
    }

    @Override
    public String getName() {
        return "RNOneSignal";
    }

    @Override
    public void onHostDestroy() {
        OneSignal.removeNotificationOpenedHandler();
        OneSignal.removeNotificationReceivedHandler();
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostResume() {

    }

}
