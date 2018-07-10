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

    // 메세지 객체의 타입을 구분하는데 사용하는 부분
    public static final int CMD_SEND_BITMAP   = 1;
    public static final int CMD_SEND_STREAM   = 2;  //MKHUH 2018-04-11 Echo 서버 기능 추가
    public static final int CMD_SEND_LOCATION = 3;
    public static final int CMD_SEND_COMMAND  = 4;  //MKHUH 2018-04-11 Echo 서버 기능 추가

    // 서버에서 클라이언트로 보내는 데이타의 헤더를 정의하는 부분
    public static final int HEADER_BITMAP   = 0x11111111;
    public static final int HEADER_STREAM   = 0x22222222; //MKHUH 2018-04-11 Echo 서버 기능 추가
    public static final int HEADER_LOCATION = 0x33333333;
    public static final int HEADER_COMMAND  = 0x44444444; //MKHUH 2018-04-11 Echo 서버 기능 추가


    private DataOutputStream mDataOutputStream;  //이진(0,1) 데이타를 입출력할 때 사용하는 stream

    public  static Handler mHandler;

    // 생성자 인자로 전달받은 OutputStream 객체를 DataOutputStream 객체로 포장하여 사용하는 부분
    // DataOutputStream 클래스는 기본형(정수,실수,문자열,바이트 배열 등) 데이터를 보내는데 적함함.
    public SendThread(OutputStream os) {
        mDataOutputStream = new DataOutputStream(os);
    }

    @Override
    /**----------------------------------------------------
     * public void run() 메소드가 Thread 임
     *---------------------------------------------------*/
    public void run() {
        //super.run();
        //전형적인 looper와 핸들러 코드 처리 부분.
        Looper.prepare();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                byte[] byteArray; // 클라이언트와 데이타 통신에 사용할 버퍼.

                try {
                    switch (msg.what) {
                        /**----------------------------------------------------------------------
                         *  비트맵이 메세지 객체의 obj 필드로 전달되면, 40% 품질로 압축하고,
                         *  헤더 + 길이 + 데이터 순으로 보내는 부분.
                         *---------------------------------------------------------------------*/
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

                        /**----------------------------------------------------------------------
                         *  위치 정보를 가진  DeviceLocation 객체가 메세지 객체의 obj 필드로 전달되면,
                         *  헤더 + 길이 + 데이터 순으로 보내는 부분.
                         *  위도와 경도는 double 형이므로 길이는 8x2 로계산한다
                         *---------------------------------------------------------------------*/
                        case CMD_SEND_LOCATION: // 위치 전송
                            com.intellstone.officeserver.DeviceLocation loc = (com.intellstone.officeserver.DeviceLocation) msg.obj;
                            // 헤더 + 길이 + 데이타 순으로 보낸다
                            mDataOutputStream.writeInt(HEADER_LOCATION);
                            mDataOutputStream.writeInt(8*2); // 길이 계산 : 위도와 경도는 double 형이므로 길이는 8x2
                            mDataOutputStream.writeDouble(loc.mLatitude);
                            mDataOutputStream.writeDouble(loc.mLongitude);
                            mDataOutputStream.flush();
                            break;
                    }
                } catch (Exception e) {
                    // 전송과정에서 오류가 발생하면 루터를 탈출함으로써 현재 스레드를 종료함.
                    getLooper().quit();
                }
            }
        };
        Looper.loop();
    }
}
