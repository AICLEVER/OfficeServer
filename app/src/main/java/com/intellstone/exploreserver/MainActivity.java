/**-----------------------------------------------------------------------------------------------
 출처 : 단계별로 배우는 안드로이드 프로그래밍
 13장
 Modified by M.K. HUH
 서버 기능 설명
 - 앱 시작 시 서버로 작동을 시작하며 자신의 IP 주소를 표시한다
   IP 주소는 WiFi를 우선으로 하되 WiFi가 꺼져 있으면 이동통신망이 제공하는 IP를 표시한다.
   둘 다 사용이 불가한 상태이면 로컬 통신용 IP 주소인 127.0.0.1을 표시한다.
 - 화면 왼쪽 상단에 카메라의 미리 보기 영상을 표시한다
 - 화면 오른쪽 상단에 카메라의 영상을 일정 간격으로 캡쳐하여 표시하고, 현재 접속한 클라이언트에도
   전송한다.
 - 3초에 1번씩 기기의 위치(위도, 경도)를 현재 접속한 Client에 전송한다
 - Client 가 접속을 끊으면 감지하여 화면에 표시한다
 - 기긱의 백 버튼을 누르거나 화면의 [종료] 버튼을 클릭하면 종료한다.

 서버에서 클라이언트로 전송하는 데이터는 2가지 종류가 있는데 비트맵 데이터와 위치 데이터이다.
 각각의 데이터 형식을 다음과 같이 약속한다
 클라이언트는 자신이 받은 데이터의 앞쪽 4바이트를 읽어서 비트맵인지 위치인지 파악한 후 데이터 길이와
 실제 데이터를 읽는다.
 비트맵 데이터 : 0x11111111(4바이트) + 비트맵 데이터 길이(4바이트)+비트맵 데이터(가변길이)
 위치 데이터   : 0x22222222(4바이트) + 위치데이터 길이(4바이트) + 위치 데이터 (위도,경도)
------------------------------------------------------------------------------------------------*/
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
    public static final int CMD_ECHO_Mode = 5; //Modified by M.K. HUH - Echo 서버 기능 추가

    /**----------------------------------------------------------------------
     *  Server Mode를 정의하는 부분
     *  Created by MKHUH 2018-07.10
     *---------------------------------------------------------------------*/
    public static final int ExploreServer = 0;
    public static final int ECHO_SERVER   = 1;
    public static int serverMode = ExploreServer;

    private TextView mTextStatus;
    private com.intellstone.officeserver.CapturePreview mCapturePreview;
    private com.intellstone.officeserver.DeviceLocation mDeviceLocation;

    // 서버통신 담당 : 서버기능을 담당하는 스레드 클래스.
    private com.intellstone.officeserver.ServerThread mServerThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextStatus = (TextView) findViewById(R.id.textStatus);
        /**-----------------------------------------------------------------------------
           이전에 정의한 CapturePreview 클래스 객체를 생성함.
           생성자 인자로는 액티비티 객체, 서피스뷰 객체, 이미지 뷰 객체를 차례로 전달함
        ------------------------------------------------------------------------------*/
        mCapturePreview = new com.intellstone.officeserver.CapturePreview(this,
                (SurfaceView) findViewById(R.id.surfacePreview),
                (ImageView)   findViewById(R.id.imageFrame));

        /** Start 버튼으로 옮김*/
        mDeviceLocation = new DeviceLocation(this);

        if(mServerThread == null) {  // 서버 시작
            mServerThread = new ServerThread(this, mMainHandler);
            mServerThread.start();
        }


    }

    // 액티비티가 화면에 보이면 위치 정보를 얻기 시작한다.
    @Override
    protected void onStart() {
        super.onStart();
        mDeviceLocation.start();
    }

    // 액티비티가 화면에 보이지 않으면 위치 정보 얻기를 중지한다.
    @Override
    protected void onStop() {
        super.onStop();
        mDeviceLocation.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**-------------------------------------------------------------------------
         * 기기의 백 버튼을 누르거나 화면의 종료 버튼을 누르면 프로세스가 종료된다.
         * 참고로 android.os.process 클래스는 리눅스의 프로세스 제어 기능을 제공함.
         *------------------------------------------------------------------------*/
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) {

        switch(v.getId()) {

            case R.id.buttonStart:
                /**------------------------------------------------------------------
                  이전에 정의한 CapturePreview 클래스 객체를 생성함.
                  생성자 인자로는 액티비티 객체, 서피스뷰 객체, 이미지 뷰 객체를 차례로 전달함.

                mCapturePreview = new CapturePreview(this,
                        (SurfaceView) findViewById(R.id.surfacePreview),
                        (ImageView)   findViewById(R.id.imageFrame));
                 ------------------------------------------------------------------*/
                // DeviceLocation 객체를 초기화하는 부분.
                mDeviceLocation = new com.intellstone.officeserver.DeviceLocation(this);  //MKHUH 2018-04-14

                /**-------------------------------------------------------------------------
                 * [시작] 버튼을 클릭하면 서버 스레드 객체를 만들어서 서버 스레드를 시작함.
                 * 생성자 인자로 액티비티 객체(this) 와 핸들러 객체를 전달한다.
                 *-----------------------------------------------------------------------*/
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

            case R.id.buttonEcho:
                //MKHUH 2018-04-11
                serverMode = ECHO_SERVER;
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
                // 클라이언트로 부터 받은 데이터를 텍스트뷰에 출력하는 핸들러.
                case CMD_ECHO_Mode:
                    mTextStatus.append((String)msg.obj);
                    break;
            }
        }
    };
}
