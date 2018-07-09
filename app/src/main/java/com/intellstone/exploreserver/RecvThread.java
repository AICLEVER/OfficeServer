package com.intellstone.officeserver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.intellstone.officeserver.ServerThread;

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Created by HUH MOONKI on 2018-04-11.
 */

public class RecvThread extends Thread {

    public static final int CMD_SEND_KEY = 3;

    public static final int HEADER_COMMAND  = 0x44444444; //MKHUH 2018-04-11

    private DataInputStream mDataInputStream;  //이진(0,1) 데이타를 입출력할 때 사용하는 stream
    public  static Handler  mHandler;

    public RecvThread(InputStream is) {
        mDataInputStream = new DataInputStream(is);
    }

    @Override
    public void run() {
        //super.run();
        int header, length;
        byte[] byteArray;

        try {
            while(true) {
                //(1) 헤더를 읽는다
                header = mDataInputStream.readInt();

                //(2) 데이터의 길이를 읽는다.
                length = mDataInputStream.readInt();

                //(3) 헤더의 타입에 따라 다르게 처리함
                switch (header) {
                    case HEADER_COMMAND:
                        int clientCommand = mDataInputStream.readInt();
                        com.intellstone.officeserver.ServerThread.doPrintln(">> 클라이언트 명령 수신됨:" + String.valueOf(clientCommand)); //MKHUH 2018-04-11 For DEBUG
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
