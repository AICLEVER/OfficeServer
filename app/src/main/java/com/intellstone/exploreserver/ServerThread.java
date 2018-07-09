package com.intellstone.officeserver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.text.TextUtils;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

/***************************************************************************************************
 * Created by HUH MOONKI on 2018-04-08.
 * Socket : Low Level Network Programming API.
 * 안드로이드는 리눅스에 기반이며 자바언어로 앱을 개발하므로 운영체제와 언어수준에서 소켓 프로그래밍
 * API 완벽하게 지원함
 * 네트웤 프로그램은 서버 / 클라이언트 구조로 작성하는 경우가 많음
 * 서버는 먼저 실행하여 기다리는 프로그램, 클라이언트는 통신을 하고자 접속하는 프로그램.
 * 서버와 클라이언트는 서로 다른 IP 주소를 가지는 시스템에서 실행되며, 해당 시스템 내에서 고유한 포트번호를 가진다.
 * 클라이언트는 서버의 IP 주소와 포트 번호를 미리 아는 상태에서 접속하며, 서버는 클라이언트가 접속하였을 때 비로서
 * 클라이언트의 IP 주소와 포트번호를 인식하여 통신을 시작한다.
 **************************************************************************************************/
@SuppressWarnings("deprecation")

/**-----------------------------------------------------------------------------------------------
 * TCP 서버 프로그램 작성 시 주의할 사항
 * 소켓 API 의 문제는 상당수의 메서드가 블로킹 모드(호출 시 작업이 끝날 떄 까지 리턴하지 않음)로
 * 동작하기 떄문에 안드로이드 앱의 메인 스레드에서 소켓 API 호출 시 약 5초 내에 대답하지 않으면
 * ANR(Application Not Response) 문제가 생길 수 있다.
 * 따라서 소켓 통신 코드는 메인 스레드가 아닌 별도의 스레드에서 수행해야 함.
 * 서버를 시작하라는 사용자의 지시가 있으면 메인 스레드는 Server Thread 를 생성한다
 *----------------------------------------------------------------------------------------------*/
public class ServerThread extends Thread {

    /**--------------------------------------------------------------------------------
     * Context 는 크게 2가지 역활을 수행하는 Abstract 클래스임.
     * 1. 어플리케이션에 관하여 시스템이 관리하는 정보에 접근하기
     * 2. 안드로이드 시스템 서비스에서 제공하는 API 를 호출할 수 있는 기능
     * Context 인터페이스가 제공하는 API 중 getPackageName(), getResource() 등의 메서드들이
     * 첫번쨰 역활을 수행하는 대표적인 메스드임.
     * StartActivity(), bindService() 같은 메서드들이 두번쨰 역활을 수향하기 위한 메서드.
     * ------------------------------------------------------------------------------*/
    // 생성자 인자로 컨텍스트와 핸들러 객체를 받아서 내부에 저장해 둠.
    private Context mContext;
    private static Handler mMainHandler;

    public ServerThread(Context context, Handler mainHandler) {
        mContext     = context;
        mMainHandler = mainHandler;
    }

    @Override
    public void run() {

        ServerSocket serverSocket = null;
        //super.run();

        try {
            /**--------------------------------------------------------------------------------------
             * 1. 서버 소켓을 초기화한다
             *   서버로 동작하려면 ServerSocket 객체를 초기화해야 하며, 포트번호는 클라이언트에서
             *   서버에 접속할 때 IP 주소와 더불어 사용하는 고유한 식별자.
             *   포트번호는 6만개가 넘는 포트가 있으며 아래 정의된 포트번호로 클라이언트가 접속해야함
             *-------------------------------------------------------------------------------------*/
            serverSocket = new ServerSocket(9000);    // 서버로 작동하고자 포트번호 9000을 전달하여 ServerSocket 객체를 초기화

            Log.d("ServerThread", "서버가 실행됨"); // MKHUH 2018-04-14

            // 서버의 IP 주소와 포트 번호를 출력한다
            doPrintln(">> 서버시작!" + getDeviceIP() + "/" + serverSocket.getLocalPort());

            while (true) {
                /**---------------------------------------------------------------------------
                 * 2. 클라이언트 접속을 기다린다
                 * 서버소켓에 대해 accept()를 호출하면 클라이언트가 접속할 때까지 대기하다가
                 * 클라이언트가 접속하면 리턴함.
                 * accept() 가 리턴한 Socket 객체는 Client 와의 데이타 통신에 사용됨
                 * 접속한 Client 와 data를 주고받을려면 getInputStream(), getOutputStream()을
                 * 각각 호출하여 InputStream과 OutpurStream 객체를 얻는다.
                 *-------------------------------------------------------------------------*/
                Socket socket = serverSocket.accept();

                /**--------------------------------------------------------------------------
                 * 3. 접속한  클라이언트의 IP 주소와 Port 번호를 알아내어 출력한다
                 *-------------------------------------------------------------------------*/
                String ip = socket.getInetAddress().getHostAddress();
                int port  = socket.getPort();
                doPrintln(">> 클라이언트 접속: " + ip + "/" + port);

                /**-----------------------------------------------------------
                 *   별개의 sendThread 와 recvThread 로 클라이언트와 통신한다.
                 *-----------------------------------------------------------*/

                try {
                    // SendThread 라는 별도의 스레드를 생성하여 클라이언트와 통신하는 부분. (SendThread.java)
                    // OutputStream 객체는 곧바로 네트워크 통신에 사용 할 수 있음.
                    com.intellstone.officeserver.SendThread sendThread = new com.intellstone.officeserver.SendThread(socket.getOutputStream()); //MKHUH 2018-04-11 Echo 서버 기능 추가
                    com.intellstone.officeserver.RecvThread recvThread = new com.intellstone.officeserver.RecvThread(socket.getInputStream());  //MKHUH 2018-04-11 Echo 서버 기능 추가
                    sendThread.start();
                    recvThread.start(); //MKHUH 2018-04-11 Echo 서버 기능 추가
                    sendThread.join();
                    recvThread.join();  //MKHUH 2018-04-11 Echo 서버 기능 추가

                } catch (Exception e) {
                    //
                    doPrintln(e.getMessage());
                }

                // Client 와 통신이 끝나면 Socket 객체를 닫는다.
                socket.close();  // 한정적인 소켓 리소스 낭비를 방지하기 위해서 socket 을 close 해준다.
                doPrintln(">> 클라이언트 종료: " + ip + "/" + port);

            } // end of While-loop

        } catch (IOException e) {

            doPrintln(e.getMessage());

        } finally {
            // 서버 작동을 끝내려면 ServerSocket 객체를 닫는다.
            try {
                if(serverSocket != null) {
                    serverSocket.close();
                }

                doPrintln(">> 서버 종료!");

            } catch (IOException e) {
                doPrintln(e.getMessage());
            }
        }
    } // end of run()

    /**---------------------------------------------------------------------------------
     * static void doPrintln(String srt)
     *
     * -. 문자열을 액티비티 화면의 텍스트뷰에 출력하고자 메세지 객체에 실어서 보내는 메서드
     * -. 실제 출력은 MainActivity 의 mMainHandler() 에서 출력됨.
     **--------------------------------------------------------------------------------*/
    public static void doPrintln(String str) {

        Message msg = Message.obtain();
        msg.what = com.intellstone.officeserver.MainActivity.CMD_APPEND_TEXT;
        msg.obj  = str + "\n";
        mMainHandler.sendMessage(msg);

    }

    /**---------------------------------------------------------------------------------
     * String getDeviceIP()
     * -. 서버의 IP 주소를 리턴하는 메서드
     * -. IP 주소는 WiFi를 우선으로 하되 WiFi 가 꺼져 있으면 이동 통신망이 제공하는 IP를 리턴하고
     *    둘다 사용이 불가한 상태이면 로턴 통신용 IP 주소인 127.0.0.1을 리턴함.
     * @return String : ip address
     *--------------------------------------------------------------------------------*/
    private  String getDeviceIP() {

        String ipaddr = getWifiIP();

        if(ipaddr == null) {
            ipaddr = getMobileIP();
        }

        if(ipaddr == null) {
            ipaddr = "127.0.0.1";
        }
        return ipaddr;
    }

    /**-------------------------------------------------------------------------------------
     * 함수명 : String getWifiIP()
     * -. WiFiManager 에 접근해서 getConnectionInfo() 로 WiFi 정보를 얻고,
     *    getIPAddress()를 호출하면 서버의 IP 주소 (32비트 정수)를 얻을 수 있음.
     * @return String : ip address
     *------------------------------------------------------------------------------------*/
    private String getWifiIP() {

        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // isWifiEnable() 를 호출하여 WiFi 가 비활성화 상태라고 판단되면 null 을 리턴함
        if(wifiManager != null && wifiManager.isWifiEnabled()) {
            int ip = wifiManager.getConnectionInfo().getIpAddress();

            // Formatter 클래스의 formatIpAddress() 를 사용하여 이식성 문제없이 IP 주소를 변환하는 부분
            return Formatter.formatIpAddress(ip);
        }
        return null;
    }

    /**---------------------------------------------------------------------------------
     * getMobileIP()
     * -.
     * @return String : ip address
     *--------------------------------------------------------------------------------*/
    private String getMobileIP() {
        try {

            for (Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces(); e1.hasMoreElements(); ) {

                NetworkInterface networkInterface = e1.nextElement();

                for (Enumeration<InetAddress> e2 = networkInterface.getInetAddresses(); e2.hasMoreElements(); ) {

                    InetAddress inetAddress = e2.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {

                        String host = inetAddress.getHostAddress();

                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**-----------------------------------------------------------------------------------------
     * IP 주소 (32 비트 정수)를 인자로 받아 "124.0.0.1" 과 같이 문자열로 리턴하는 메서드
     * 엄밀히 말하면, 이 코드는 이식성이 좋지 않다.
     * 이는 현재 안드로이드 기기를 리틀 엔디언 (Little-endian) 시스템으로 가정하기 때문이다.
     * Formatter 클래스의 formatIpAddress() 를 사용하면 이식성 문제없이 IP 주소를 변환할 수 있음.
     * 현재 사용하지 않는 메서드임.
     *----------------------------------------------------------------------------------------
    private String ipv4ToString(int ip) {
        int a = (ip) & 0xFF, b = (ip >> 8) & 0xFF;
        int c = (ip >> 16) & 0xFF, d = (ip >> 24) & 0xFF;

        return Integer.toString(a) + "." + Integer.toString(b) + "." + Integer.toString(c) + "."
                + Integer.toString(d);
    }
     */

    /**
     *  TCP 서버
     *  에코 서버 : 클라이언트가 보낸 데이터를 변경없이 그대로 되돌려주는 서버로 네트웤 프로그램
     *              예제로 널리 사용함.
     *  소켓 API의 특성을 고려한 서버의 구조는 그림 13-2 와 같다.
     *  서버를 시작하라는 사용자의 지시가 있으면
     *  (1) 메인 드레든는 Server Thread를 생성함
     *  (2) Client 의 접속을 받아 들임
     *  (3) 접속한 Client 가 보낸 데이터를 수신하면
     *  (4) ServerThread 는 메인 스레드의 메세지 큐에 메세지(=수신데이터)를 넣음.
     *  (5) 메인 Thread 에서 수행되는 핸들러는 메세지 (=수신데이터)를 꺼내서 그대로 화면에 출력 (Echo Server인 경우)
     *  (6) 수신 데이터를 그대로 CLient에 보내므로 에코 서버라 부르는 것.
     * */
}
