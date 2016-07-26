package com.sparta.onyxbeacon;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;
import java.lang.System;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.listeners.OnyxCouponsListener;
import com.onyxbeacon.listeners.OnyxPushListener;
import com.onyxbeacon.listeners.OnyxTagsListener;
import com.onyxbeacon.service.model.Tag;
import com.onyxbeacon.rest.model.Coupon;
import com.onyxbeaconservice.IBeacon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;
import android.os.Parcelable;
import java.lang.CharSequence;
import com.sparta.onyxbeacon.OnyxbeaconPhonegap;

/**
 * Created by Work 2 on 4/2/2015.
 */
public class ContentReceiver extends BroadcastReceiver {
    private OnyxBeaconManager onyxManager;    
    private OnyxBeaconsListener mOnyxBeaconListener;
    private OnyxCouponsListener mOnyxCouponsListener;
    private OnyxTagsListener mOnyxTagsListener;
    private OnyxPushListener mOnyxPushListener;
    private static ContentReceiver sInstance;

    /* Coupons */
    private static String COUPONS_TAG = "coupons_tag";
    private SharedPreferences mSharedPref;
    private Gson gson = new Gson();
    private static final String COUPONS_LIST_ENTRY = "couponsList";
    private static final String COUPONS_NEW_COUNTER = "couponsNewCounter";
    private static final String SHARED_PREF_NO_ENTRY = "noEntry";

    public ContentReceiver() {}

    public static ContentReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new ContentReceiver();
            return sInstance;
        } else {
            return sInstance;
        }
    }

    public void setOnyxBeaconsListener(OnyxBeaconsListener onyxBeaconListener) {
        mOnyxBeaconListener = onyxBeaconListener;
    }

    public void setOnyxCouponsListener(OnyxCouponsListener onyxCouponsListener) {
        mOnyxCouponsListener = onyxCouponsListener;
    }

    public void setOnyxTagsListener(OnyxTagsListener onyxTagsListener){
        mOnyxTagsListener = onyxTagsListener;
    }

    public void setOnyxPushListener(OnyxPushListener onyxPushListener) {
        mOnyxPushListener = onyxPushListener;
    }

    public void onReceive(Context context, Intent intent) {
        String payloadType = intent.getStringExtra(OnyxBeaconApplication.PAYLOAD_TYPE);
        onyxManager = OnyxBeaconApplication.getOnyxBeaconManager(context);
        if(payloadType.equals(OnyxBeaconApplication.TAG_TYPE)){
            ArrayList<Tag> tagsList = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_TAGS);
                if (mOnyxTagsListener != null) {
                    mOnyxTagsListener.onTagsReceived(tagsList);
                } else {
                    // In background display notification
                }
        }else if(payloadType.equals(OnyxBeaconApplication.BEACON_TYPE)){
            ArrayList<IBeacon> beacons = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_BEACONS);
                if (mOnyxBeaconListener != null) {
                    mOnyxBeaconListener.didRangeBeaconsInRegion(beacons);
                } else {
                    // In background display notification
                }
        }else if(payloadType.equals(OnyxBeaconApplication.COUPON_TYPE)){
            mSharedPref = context.getSharedPreferences("COUPONS_PREF",
                        Context.MODE_PRIVATE);
            	Resources resources = context.getResources();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            	Coupon coupon = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_COUPON);
                IBeacon beacon = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_BEACON);
                
            	System.out.println("Coupon receiver - Received coupon " + coupon);
            
            	if (coupon != null) {
                    String couponsListAsString = mSharedPref.getString(COUPONS_LIST_ENTRY, SHARED_PREF_NO_ENTRY);
                    ArrayList<Coupon> couponsFromStorage = new ArrayList<Coupon>();
                    ArrayList<Coupon> newCoupons = new ArrayList<Coupon>();
                    
                    if (!couponsListAsString.equals(SHARED_PREF_NO_ENTRY)) {
                        couponsFromStorage = (ArrayList<Coupon>)gson.fromJson(couponsListAsString, new TypeToken<List<Coupon>>() {}.getType());
                    }

                    if (!couponsFromStorage.contains(coupon)) {
                        couponsFromStorage.add(coupon);
                    }

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    
                    try {
                        String packageName = context.getPackageName();
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        String className = launchIntent.getComponent().getClassName();
                        Log.d("OnyxPhonegap", "launching activity for class " + className);

                        @SuppressWarnings("rawtypes")
                        Class cl = Class.forName(className); 
                        
                        int notificationID = (int) coupon.couponId;

                        Intent i = new Intent(context, NotificationHandler.class);
                        //Intent i = new Intent(context, cl);
                        i.putExtra("coupon_object", (Parcelable) coupon);
                        //stackBuilder.addParentStack(cl);
                        stackBuilder.addNextIntent(i);
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(context , (int) System.currentTimeMillis() , i , PendingIntent.FLAG_UPDATE_CURRENT);
                        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent((int) System.currentTimeMillis(), PendingIntent.FLAG_UPDATE_CURRENT);

                        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        long[] vibratePattern = {500, 500, 500, 500};
                        int smallIconId = resources.getIdentifier("ic_notification", "drawable", context.getPackageName());
                        if (smallIconId == 0) {
                            smallIconId = resources.getIdentifier("ic_notification", "drawable", context.getPackageName());
                        }
                        
                        CharSequence appName = resources.getText(resources.getIdentifier("app_name","string", context.getPackageName()));
                        Notification.Builder builder =
                                new Notification.Builder(context)
                            				.setSmallIcon(smallIconId)
                                            .setContentTitle(appName)
                            				.setContentText(coupon.message)
                                            .setAutoCancel(true)
                                            .setVibrate(vibratePattern)
                                            .setLights(Color.BLACK, 500, 500)
                                            .setSound(notificationSound);

                        builder.setContentIntent(resultPendingIntent);
                        
                        notificationManager.notify(COUPONS_TAG, notificationID, builder.build());


                        if (mOnyxCouponsListener != null) {
                            mOnyxCouponsListener.onCouponReceived(coupon, beacon);
                        } else {
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString(COUPONS_LIST_ENTRY, gson.toJson(couponsFromStorage));
                            editor.apply();
                        }
                        
                        onyxManager.deleteCoupon(coupon.couponId, coupon.beaconId);
                        
                    }catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    
                }
        }

    }
}
