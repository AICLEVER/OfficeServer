package com.intellstone.officeserver;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

import com.intellstone.officeserver.SendThread;

/************************************************************************************************
 *  기기의 위치를 얻어서 현재 서버에 접속한 클라이언트에게 보내는 기능을 담당하는 Class.
 *
 *  Created by HUH MOONKI on 2018-04-08.
 ***********************************************************************************************/
public class DeviceLocation {

    private LocationManager mLocationManager;
    public  double mLatitude;
    public  double mLongitude;

    // DeviceLocation 객체 생성 시 위치 관리자를 얻어두는 부분.
    public DeviceLocation(Context context) {

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // DeviceLocation 객체를 사용하는 쪽에서 start()를 호출하면 3초에 한 번씩 기기 위치를 얻는 부분
    public void start() {
        String provider = mLocationManager.getBestProvider(new Criteria(), true);
        mLocationManager.requestLocationUpdates(provider, 3000, 0, mLocListener);
    }

    // DeviceLocation 객체를 사용하는 쪽에서 stop()을 호출하면 기기 위치 얻기를 중지함.
    public void stop() {
        mLocationManager.removeUpdates(mLocListener);
    }

    LocationListener mLocListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            // SendThread 의 핸들러가 아직 준비됮 않았다면 리턴한다.
            if (com.intellstone.officeserver.SendThread.mHandler == null) return;

            // 기기 위치(위도, 경도)를 얻어서 DeviceLocation 객체의 필드에 저장해 두는 부분.
            mLatitude  = location.getLatitude();
            mLongitude = location.getLongitude();

            // 네트워크 전송 지연이 생기면 이전에 SendThread의 메세지 큐에 넣은 메세지가 아직 남아 있을 수 있다.
            // 앱의 특성상 모든 데이터를 반드시 보내야 하는 것이 아니기 때문에 기존 메세지를 삭제하고 새로운 메세지를 넣는다.
            if (com.intellstone.officeserver.SendThread.mHandler.hasMessages(com.intellstone.officeserver.SendThread.CMD_SEND_LOCATION)) {
                com.intellstone.officeserver.SendThread.mHandler.removeMessages(com.intellstone.officeserver.SendThread.CMD_SEND_LOCATION);
            }

            // 메세지 객체에 DeviceLocation 객체를 넣어서 SendThread 의 메세지 큐에 넣는 부분.
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
