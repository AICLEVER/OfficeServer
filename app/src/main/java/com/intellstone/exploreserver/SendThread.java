package com.intellstone.officeserver;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;


/**
 * Created by HUH MOONKI on 2018-04-08.
 *
 * Thread 를 상속해서 SendThread Class 를 정의하는 부분
 */
public class SendThread extends Thread {

    public static final int CMD_SEND_BITMAP   = 1;
    public static final int CMD_SEND_STREAM   = 2;  //MKHUH 2018-04-11
    public static final int CMD_SEND_LOCATION = 3;
    public static final int CMD_SEND_COMMAND  = 4;  //MKHUH 2018-04-11

    public static final int HEADER_BITMAP   = 0x11111111;
    public static final int HEADER_STREAM   = 0x22222222; //MKHUH 2018-04-11
    public static final int HEADER_LOCATION = 0x33333333;
    public static final int HEADER_COMMAND  = 0x44444444; //MKHUH 2018-04-11

    private DataOutputStream mDataOutputStream;  //이진(0,1) 데이타를 입출력할 때 사용하는 stream
    public  static Handler mHandler;

    public SendThread(OutputStream os) {
        mDataOutputStream = new DataOutputStream(os);
    }

    @Override
    /**----------------------------------------------------
     * public void run() 메소드가 Thread 임
     *---------------------------------------------------*/
    public void run() {
        //super.run();
        Looper.prepare();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                byte[] byteArray; // 클라이언트와 데이타 통신에 사용할 버퍼.

                try {
                    switch (msg.what) {

                        case CMD_SEND_BITMAP: // 비트맵 전송
                            Bitmap bitmap = (Bitmap) msg.obj;
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
                            byteArray = stream.toByteArray();

                            // 헤더 + 길이 + 데이타 순으로 보낸다
                            mDataOutputStream.writeInt(HEADER_BITMAP);
                            mDataOutputStream.writeInt(byteArray.length);
                            mDataOutputStream.write(byteArray);
                            mDataOutputStream.flush();
                            break;

                        case CMD_SEND_LOCATION: // 위치 전송
                            com.intellstone.officeserver.DeviceLocation loc = (com.intellstone.officeserver.DeviceLocation) msg.obj;
                            // 헤더 + 길이 + 데이타 순으로 보낸다
                            mDataOutputStream.writeInt(HEADER_LOCATION);
                            mDataOutputStream.writeInt(8*2); // 길이
                            mDataOutputStream.writeDouble(loc.mLatitude);
                            mDataOutputStream.writeDouble(loc.mLongitude);
                            mDataOutputStream.flush();
                            break;
                    }
                } catch (Exception e) {
                    getLooper().quit();
                }
            }
        };
        Looper.loop();
    }
}
