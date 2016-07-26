package com.sparta.onyxbeacon;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import android.content.Intent;
import android.os.Bundle;

import com.onyxbeacon.*;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.service.OnyxBeaconService;
import com.onyxbeacon.rest.auth.util.AuthenticationMode;

import com.sparta.onyxbeacon.ContentReceiver;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.content.IntentFilter;
import com.onyxbeacon.rest.model.Coupon;

import com.onyxbeaconservice.IBeacon;
import java.util.ArrayList;
import java.util.List;

public class OnyxbeaconPhonegap extends CordovaPlugin {
    private OnyxBeaconManager onyxManager;
    private OnyxBeaconsListener mOnyxBeaconListener;
    private String CONTENT_INTENT_FILTER;
    private CordovaInterface mCordovaInterface;
    private ContentReceiver mContentReceiver;
    
    private CallbackContext mBluetoothStateCallbackContext;
    private CallbackContext mRangingCallback;

    private boolean receiverRegistered = false;
    
    public static final String EXTRA_COUPONS = "coupons";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String LOGTAG = "OnyxbeaconsPhonegap";
    
    private static CallbackContext pushContext;
    private static CordovaWebView gWebView;
    private static Bundle gCachedExtras = null;
    
    private List<IBeacon> mRangedBeacons;
    
    public static final String LOG_TAG = "Onyxneacon Phonegap";
    
    private static boolean gForeground = false;
    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        gForeground = true;
        CONTENT_INTENT_FILTER = cordova.getActivity().getPackageName() + ".content";
        // Register for Onyx content
        mContentReceiver = ContentReceiver.getInstance();

        //Register ContentReceiver
        cordova.getActivity().getApplicationContext().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;

        if(onyxManager == null){
            onyxManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity().getApplicationContext());
        }
        onyxManager.setAPIEndpoint("https://connect.onyxbeacon.com");

        //INITIALISING THE SDK
        onyxManager.initSDK(AuthenticationMode.CLIENT_SECRET_BASED);

        // Enable beacons and coupons retrieval
        onyxManager.setCouponEnabled(true);
        onyxManager.setAPIContentEnabled(true);
        
		mRangedBeacons = new ArrayList<IBeacon>();

    }
    
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        gWebView = this.webView;
        if("initialiseSDK".equals(action)){
            Log.e("Initialising SDK" , "Working");
            //callbackContext.success("Initiate Successfully");
            pushContext = callbackContext;
        }else if("checkbluetoothState".equals(action)){
            checkBluetoothState(args , callbackContext);
        }else if("rangeBeacon".equals(action)){
            startRangingBeacons(args , callbackContext);
        }else{
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }
        return true;
    }
    
    private void checkBluetoothState(CordovaArgs cordovaArgs,final CallbackContext callbackContext) throws JSONException {
        
        // Check that no Bluetooth state request is in progress.
		if (null != mBluetoothStateCallbackContext) {
			callbackContext.error("Bluetooth state request already in progress");
			return;
		}
        
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            final CordovaPlugin self = this;

			Runnable openBluetoothDialog = new Runnable() {
				public void run() {
					Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					mCordovaInterface.startActivityForResult(
						self,
						enableIntent,
						REQUEST_ENABLE_BLUETOOTH);
				}
			};
			mCordovaInterface.getActivity().runOnUiThread(openBluetoothDialog);
        }else {
            sendResultForBluetoothEnabled(callbackContext);
        }
    }

    private void startRangingBeacons(CordovaArgs cordovaArgs,final CallbackContext callbackContext){
        mRangingCallback = callbackContext;
        new BeaconListener();
    }
    
    /**
	 * Check if Bluetooth is enabled and return result to JavaScript.
	 */
	public void sendResultForBluetoothEnabled(CallbackContext callbackContext)
	{
		if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			callbackContext.success("Enabled");
		}
		else {
			callbackContext.error("Disabled");
		}
	}
    
    /**
	 * Called when the Bluetooth dialog is closed.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Log.i(LOGTAG, "onActivityResult");
		if (REQUEST_ENABLE_BLUETOOTH == requestCode) {
			sendResultForBluetoothEnabled(mBluetoothStateCallbackContext);
			mBluetoothStateCallbackContext = null;
		}
	}
    
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;
        //SET MANAGER TO BACKGROUND MODE
        onyxManager.setForegroundMode(true);
        // if (receiverRegistered) {
            
        //     //UNREGISTER THE RECEIVER
        //     cordova.getActivity().getApplicationContext().unregisterReceiver(mContentReceiver);
        //     receiverRegistered = false;
        // }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
        if (mContentReceiver == null){
            mContentReceiver = ContentReceiver.getInstance();
        }
        
        //REGISTER THE RECEIVER
        cordova.getActivity().getApplicationContext().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
        onyxManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity().getApplicationContext());
        
        //SET MANAGER TO FOREGROUND MODE
        onyxManager.setForegroundMode(true);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        gForeground = false;
        gWebView = null;
    }
    
    /*
     * Sends the pushbundle extras to the client application.
     * If the client application isn't currently active, it is cached for later processing.
     */
    public static void sendExtras(Bundle extras) {
        if (extras != null) {
            if (gWebView != null) {
                sendEvent(convertBundleToJson(extras));
            } else {
                Log.v("Onyxbeacon Phonegap", "sendExtras: caching extras to send at a later time.");
                gCachedExtras = extras;
            }
        }
    }
    
    public static void sendEvent(JSONObject _json) {
        Log.d(LOG_TAG, "sending callback");
        Log.d(LOG_TAG, "sending json "+_json);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
        pluginResult.setKeepCallback(true);
        if (pushContext != null) {
            pushContext.sendPluginResult(pluginResult);
        }
    }
    
    private static JSONObject convertBundleToJson(Bundle extras) {
        Log.d(LOG_TAG, "convert extras to json");
        try {
            JSONObject json = new JSONObject();
            JSONObject additionalData = new JSONObject();

            Iterator<String> it = extras.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object value = extras.get(key);

                Log.d(LOG_TAG, "key = " + key);
                
                json.put(key, value);

                // if (jsonKeySet.contains(key)) {
                //     json.put(key, value);
                // }
            } // while

            Log.v(LOG_TAG, "extrasToJSON: " + json.toString());

            return json;
        }
        catch( JSONException e) {
            Log.e(LOG_TAG, "extrasToJSON: JSON exception");
        }
        return null;
    }
    
    public static boolean isInForeground() {
      return gForeground;
    }
    
    public static boolean isActive() {
        return gWebView != null;
    }

    class BeaconListener implements OnyxBeaconsListener {

        @Override
        public void didRangeBeaconsInRegion(final List<IBeacon> beacons) {

            Log.i("BeaconListener" , " "+beacons);

            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            mRangedBeacons.clear();
            mRangedBeacons.addAll(beacons);

            try{

                for(IBeacon b:beacons){
                    String address = b.getBluetoothAddress();
                    String proximityUUID = b.getProximityUuid();

                    JSONObject beacon = new JSONObject();

                    beacon.put("macAddress", address);
                    beacon.put("proximityUUID", proximityUUID);
                    beacon.put("major", b.getMajor());
                    beacon.put("minor", b.getMinor());
                    beacon.put("rssi", b.getRssi());

                    array.put(beacon);

                }

                json.put("beacons",array);

                if(mRangingCallback != null){
                    PluginResult result = new PluginResult(PluginResult.Status.OK , json);
                    result.setKeepCallback(true);
                    mRangingCallback.sendPluginResult(result);
                }else{
                    Log.i("Beacon Listener", "CallbackContext for discovery does not exist");
                }

            }catch(JSONException e){
                Log.i(LOGTAG , "JSON Exception "+e);
            }
        }
    }
}