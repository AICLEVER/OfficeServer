package com.intellstone.officeserver;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

import com.intellstone.officeserver.SendThread;

/***********************************************
 * Created by HUH MOONKI on 2018-04-08.
 ********************************************/

public class DeviceLocation {

    private LocationManager mLocationManager;
    public  double mLatitude;
    public  double mLongitude;

    public DeviceLocation(Context context) {

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start() {
        String provider = mLocationManager.getBestProvider(new Criteria(), true);
        mLocationManager.requestLocationUpdates(provider, 3000, 0, mLocListener);
    }

    public void stop() {
        mLocationManager.removeUpdates(mLocListener);
    }

    LocationListener mLocListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (com.intellstone.officeserver.SendThread.mHandler == null) return;

            mLatitude  = location.getLatitude();
            mLongitude = location.getLongitude();

            if (com.intellstone.officeserver.SendThread.mHandler.hasMessages(com.intellstone.officeserver.SendThread.CMD_SEND_LOCATION)) {
                com.intellstone.officeserver.SendThread.mHandler.removeMessages(com.intellstone.officeserver.SendThread.CMD_SEND_LOCATION);
            }
            Message msg = Message.obtain();
            msg.what    = com.intellstone.officeserver.SendThread.CMD_SEND_LOCATION;
            msg.obj     = DeviceLocation.this;
            com.intellstone.officeserver.SendThread.mHandler.sendMessage(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
