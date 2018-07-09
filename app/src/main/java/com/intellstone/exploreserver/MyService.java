package com.intellstone.officeserver;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**********************************************************************************************
 *  Created by HUH MOONKI on 2018-04-12.
 *  [MediaPlayer 클래스를 이용한 오디오 재생 절차]
 *  1. 매니패스트에 미디어 접근을 위한 퍼미션을 선언함.
 *     (로컬파일이면 READ_EXTERNAL_STORAGE, 원격파일이면 INTERNET 퍼미션이 필요함)
 *  2. MediaPlayer 객체를 생성하고 정해진 절차대로 매서드를 호출하여 재생을 시작함.
 *     setDataSource()로 위치를 지정 -> prepare()로 미디어 재생을 준비 -> start() 호출
 *     또는 setDataSource()와 prepare()를 합쳐 놓은 create()를 대신 사용 가능
 ********************************************************************************************/

public class MyService extends Service {

    /** 로컬 파일 (예: /storage/emulated/sample mp3) 재생 */
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //super.onCreate();
        /**-------------------------------------------------------------------------------
         * 1. 서비스 생성 시 MediaPlayer 객체를 만들어 둠.
         * 2. MediaPlayer 객체에 대해 setOnCompletionListener() 를 호출하여
         *    MediaPlayer.OnCompletionListener 객체를 전달하면, 미디어 재생이 끝났을 때
         *    onCompletion() 이 자동 호출됨.
         * 3. reset() 호출하면 MediaPlayer 객체가 미디어 재생 완료 시 초기상탱인 Idle 상태로 돌아감.
         *------------------------------------------------------------------------------*/
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset(); // 재생 끝나면 리셋!!
                }
            });
        }
    }

    @Override

    public void onStart(Intent intent, int startId) {
        //super.onStart(intent, startId);
        /**-----------------------------------------------------------------------------
         * 서비스가 "andbook.example.PLAYMUSIC" 액션을 담은 인텐트를 수신했고 MediaPlayer 가
         * 재생 중이 아니라면 MusicThread 를 시작함.
         * 이때 스레드의 생성자 인자로 재생할 미디어의 위치를 나타내는 Uri 객체를 넘겨줌.
         *----------------------------------------------------------------------------*/
        if(intent.getAction().equals("andbook.example.PLAYMUSIC")) {

            if(!mMediaPlayer.isPlaying()) {
                new MusicThread(intent.getData()).start();
            }
        }
    }

    @Override
    public void onDestroy() {
        /**-------------------------------------------
         *  서비스 종료 시 MediaPlayer 객체를 정리함
         *------------------------------------------*/
        //super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**-------------------------------------------------------------------------------
     * 1. 스레드의 생성자로 전달되는 Uri 객체를 저장해 두었다가 mMeidaPlayer.setDataSource()
     *    에서 사용함.
     * 2. mMediaPlayer.start() 실행하면 음악재생이 별도의 스레드에서 진행되므로 액티비티를 종료
     *    하더라도 중단되지 않는다.
     *------------------------------------------------------------------------------*/
    private class MusicThread extends Thread {

        private Uri mUri;

        MusicThread(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            //super.run();
            try {

                mMediaPlayer.setDataSource(getApplicationContext(), mUri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
