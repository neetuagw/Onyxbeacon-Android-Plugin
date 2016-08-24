package com.sparta.onyxbeacon;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.onyxbeacon.rest.model.Coupon;
import com.onyxbeacon.*;
import com.onyxbeacon.OnyxBeaconApplication;
import com.sparta.onyxbeacon.TransparentActivity;
import com.onyxbeacon.OnyxBeaconManager;
//import com.onyxbeacon.service.api.OnyxBeaconWebService;

import java.util.ArrayList;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import android.content.Context;

import java.lang.Object;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.app.ActionBar;
import android.view.MenuItem;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.ProgressDialog;

import android.view.*; 
import android.webkit.*;
import com.android.volley.toolbox.NetworkImageView;

import android.os.Parcelable;

public class NotificationHandler extends TransparentActivity{
    private OnyxBeaconManager onyxManager;
    TextView type;
    ImageView couponImage;
    Button openApp;
    private WebView webView;
    
    private NetworkImageView mCouponImageView; 
    private WebView mCouponWebView; 
    private Coupon coupon;
    
    ProgressDialog progressDialog;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        
		Log.v("Notification Handler ", "onCreate");
        
        progressDialog = new ProgressDialog(NotificationHandler.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        
        String package_name = getApplicationContext().getPackageName();
 		Resources resources = getApplicationContext().getResources();
        
        setContentView(resources.getIdentifier("notification" , "layout" , package_name));
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        boolean foreground = OnyxbeaconPhonegap.isInForeground();
        boolean isOnyxbeaconActive = OnyxbeaconPhonegap.isActive();
        
        //initialising OnyxManager 
        onyxManager = OnyxBeaconApplication.getOnyxBeaconManager(getApplicationContext());     
        
        //Check If this Apptivity open from Intent
        if(getIntent().getParcelableExtra("coupon_object") != null){
            
            //Get Coupons from the Intent Bundle
            coupon = (Coupon)getIntent().getParcelableExtra("coupon_object");
            Log.e("Coupon Clicked" , " " +coupon.couponId);
            
            actionBar.setTitle((CharSequence)coupon.name);
            
            if( coupon.type == 0 || coupon.type == 3) {
                Bundle extras = new Bundle();
                extras.putString("coupon_value", coupon.name);
                OnyxbeaconPhonegap.sendExtras(extras);
           
                //Deleting received coupon
                onyxManager.deleteCoupon(coupon.couponId, coupon.beaconId);
                
                forceMainActivityReload();

            }else if(coupon.type == 1){
                setContentView(resources.getIdentifier("notification" , "layout" , package_name));
                couponImage = (ImageView)findViewById(resources.getIdentifier("couponImage" , "id" , package_name));
                couponImage.setVisibility(View.VISIBLE);
                try{
                    URL url = new URL(coupon.image);
                	Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                	couponImage.setImageBitmap(bitmap);
                }catch(IOException e){
                    e.printStackTrace();
                }
                
                progressDialog.dismiss();
                
            }else if(coupon.type == 2){
            
                Bundle extras2 = new Bundle();
                extras2.putString("coupon_value", coupon.name);
                OnyxbeaconPhonegap.sendExtras(extras2);
           
                //Deleting received coupon
                onyxManager.deleteCoupon(coupon.couponId, coupon.beaconId);
                
                forceMainActivityReload();
                
                /*mCouponWebView = new WebView(this); 
                mCouponWebView.setWebViewClient(new WebViewClient()); 
                mCouponWebView.setWebChromeClient(new WebChromeClient()); 
                mCouponWebView.getSettings().setJavaScriptEnabled(true); 
                setContentView(mCouponWebView, new android.view.ViewGroup.LayoutParams(-1, -1)); 
                mCouponWebView.loadUrl(coupon.couponURL);
                
                progressDialog.dismiss();*/
            }
            
            //Deleting received coupon
            onyxManager.deleteCoupon(coupon.couponId, coupon.beaconId);
        }
        
}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	Bundle extras = new Bundle();
            	extras.putParcelable("coupon_value", (Parcelable)coupon);
            	//Bundle extras = getIntent().getBundleExtra("coupon_object");
            	OnyxbeaconPhonegap.sendExtras(extras);
            
            	NotificationHandler.this.finish();
            
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Forces the main activity to re-launch if it's unloaded.
     */
    private void forceMainActivityReload() {
        Log.e("forceMainActivityReload" , "Thats been called ");
        NotificationHandler.this.finish();
        try{
            PackageManager pm = getApplicationContext().getPackageManager();
        	Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName()); 
            String className = launchIntent.getComponent().getClassName();
            Class cl = Class.forName(className); 
            Intent newIntent = new Intent(NotificationHandler.this, cl);
        	startActivity(launchIntent);
        } catch ( ClassNotFoundException e){
            e.printStackTrace(); 
        }
    }
    
    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        super.onBackPressed();
    }
    
}
