/**------------------------------------------------------------------
 출처 : 단계별로 배우는 안드로이드 프로그래밍
 13장
 Modified by M.K. HUH
------------------------------------------------------------------*/
package com.intellstone.officeserver;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.intellstone.officeserver.CapturePreview;
import com.intellstone.officeserver.DeviceLocation;
import com.intellstone.officeserver.R;
import com.intellstone.officeserver.ServerThread;

public class MainActivity extends AppCompatActivity {

    public static final int CMD_APPEND_TEXT = 0;
    public static final int CMD_ENABLE_CONNECT_BUTTON = 1;
    public static final int CMD_SHOW_BITMAP = 2;
    public static final int CMD_SHOW_MAP = 3;
    public static final int CMD_SEND_COMMAND = 4;

    private TextView mTextStatus;
    private com.intellstone.officeserver.CapturePreview mCapturePreview;
    private com.intellstone.officeserver.DeviceLocation mDeviceLocation;

    private com.intellstone.officeserver.ServerThread mServerThread;  // 서버통신 담당 : 서버기능을 담당하는 스레드 클래스.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextStatus = (TextView) findViewById(R.id.textStatus);
        /*------------------------------------------------------------------
           이전에 정의한 CapturePreview 클래스 객체를 생성함.
           생성자 인자로는 액티비티 객체, 서피스뷰 객체, 이미지 뷰 객체를 차례로 전달함
        ------------------------------------------------------------------*/
        mCapturePreview = new com.intellstone.officeserver.CapturePreview(this,
                (SurfaceView) findViewById(R.id.surfacePreview),
                (ImageView)   findViewById(R.id.imageFrame));

        /** Start 버튼으로 옮김
        mDeviceLocation = new DeviceLocation(this);

        if(mServerThread == null) {  // 서버 시작
            mServerThread = new ServerThread(this, mMainHandler);
            mServerThread.start();
        }
        */

    }

    @Override
    protected void onStart() {
        super.onStart();
        //mDeviceLocation.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mDeviceLocation.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**------------------------------------------------------------------
         * 기기의 백 버튼을 누르거나 화면의 종료 버튼을 누르면 프로세스가 종료된다.
         * 참고로 android.os.process 클래스는 리눅스의 프로세스 제어 기능을 제공함.
         *-----------------------------------------------------------------*/
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) {

        switch(v.getId()) {

            case R.id.buttonStart:
                /**------------------------------------------------------------------
                  이전에 정의한 CapturePreview 클래스 객체를 생성함.
                  생성자 인자로는 액티비티 객체, 서피스뷰 객체, 이미지 뷰 객체를 차례로 전달함

                mCapturePreview = new CapturePreview(this,
                        (SurfaceView) findViewById(R.id.surfacePreview),
                        (ImageView)   findViewById(R.id.imageFrame));
                 ------------------------------------------------------------------*/
                mDeviceLocation = new com.intellstone.officeserver.DeviceLocation(this);  //MKHUH 2018-04-14

                /**------------------------------------------------------------------
                 * [시작] 버튼을 클릭하면 서버 스레드 객체를 만들어서 서버 스레드를 시작함.
                 * 생성자 인자로 액티비티 객체(this) 와 핸들러 객체를 전달한다.
                 *-----------------------------------------------------------------*/
                if(mServerThread == null) {
                    mServerThread = new com.intellstone.officeserver.ServerThread(this, mMainHandler);  //스레드 객체를 하나 만든다.
                    mServerThread.start();  //thread 내의 run() method 를 실행한다  // 서버 시작
                }
                break;

            case R.id.buttonQuit:
                //MKHUH 2018-04-11
                //Intent intent = new Intent(getApplicationContext(), AudioPlayActivity.class);//mkhuh_2018.02.23
                //startActivity(intent);
                finish();
                break;

        }
    }

    /**------------------------------------------------------------------------
     * 서버 IP 주소나 오류 메세지등 텍스트 정보를 표시하는 메서드.
     * 출력할 텍스트는 핸들러를 통해 메세지 객체로 받는다는 점에 주목하자.
     * Created by MKHUH 2018-004-08
     *-----------------------------------------------------------------------*/
    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CMD_APPEND_TEXT:// 화면에 데이타 출력
                case CMD_SEND_COMMAND:
                    mTextStatus.append((String)msg.obj);
                    break;
            }
        }
    };
}
