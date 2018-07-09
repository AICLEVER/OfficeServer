package com.intellstone.officeserver;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.intellstone.officeserver.R;

import java.io.File;
import java.io.IOException;

/******************************************************************************************************
 * [오디오 재생과 기록 2가지 방법]
 * 1. 인텐트를 이용하여 기존 앱의 기능을 가져다 사용하는 방법.
 * 2. 안드로이드 API 인 MediaPlayer 클래스를 사용하는 방법
 * MediaPlayer 는 오디오와 비디오 재생 기능을 제공하는 다재다능한 클래스
 * 앱에 내장된 raw 리소스나 로컬 파일은 물론이고 네트워크 스트리밍으로 원격 파일을 재생할 수도 있음.
 *
 * [MediaPlayer 클래스를 이용한 오디오 재생 절차]
 * (1) 매니페스트에 미디어 접근을 위한 퍼미션을 선언함. (READ_EXTERNAL_STORAGE, INTERNET)
 * (2) MediaPlayer 객체를 생성하고 정해진 절차대로 매서드를 호출하여 재생을 시작함.
 *     가장 일반적인 순서 : setDataSource() 로 위치를 지정 -> prepare()로 미디어 재생을 준비하여 start()를 호출
 *     ,또는 setDataSource() 와 prepare()를 합쳐놓은 create()를 대신 사용할 수 있다.
 **************************************************************************************************/
public class AudioPlayActivity extends AppCompatActivity {

    public static String url = "http://sites.google.com/site/ubiaccessmobile/sample_audio.amr"; //MKHUH 2018-04-13
    private File mFile;
    private MediaPlayer mediaPlayer;
    private int position; //MKHUH 2018-04-14

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);

        /**-----------------------------------------------------------------
         * 외부 저장소에 있는 sample.mp3 파일에 접근할 수 있도록 File 객체를 준비함.
         *----------------------------------------------------------------*/
        //File sdcard = Environment.getExternalStorageDirectory();
        //mFile = new File(sdcard.getAbsolutePath() + "/sample.mp3");
        //Toast.makeText(this, "mFile:" + mFile.toString(), Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13
    }

    @Override
    /**----------------------------------------------------
     *  액티비티 종료 시 MediaPlayer 객체를 정리함
     *---------------------------------------------------*/
    protected void onDestroy() {
        super.onDestroy();

        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void mOnClick(View v) throws IOException {

        Intent intent;

        switch (v.getId()) {

            case R.id.buttonIntent: //인텐트로 오디오 재생
                /**---------------------------------------------------------------------------------
                 * 오디오 파일을 재생할 수 있는 액티비티를 실행한다.
                 * setDataAndType()의 첫번째 인자에 필요한 Uri 객체는 미리 준비해둔 File 객체로 만들 수 있음.
                 *--------------------------------------------------------------------------------*/
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(mFile), "audio/*");
                startActivity(intent);
                break;

            case R.id.buttonPlay1: //MediaPlayer 로 재생
                /**---------------------------------------------------------------------------------
                 * MediaPlayer 객체를 생성하거나 초기화한다.
                 * MediaPlayer 객체는 생성 직후 또는 reset() 호출 시에 Idle 상태로 시작함.
                 *--------------------------------------------------------------------------------*/
                if(mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                } else {
                    mediaPlayer.reset();
                    mediaPlayer = null; //MKHUH 2018-04-14
                }

                /**---------------------------------------------------------------------------------
                 * Idle 상태의 MediaPlayer 객체로 미디어를 재생하려면 setDataSource(), prepare(), start()
                 * 순으로 호출해야 함.
                 * [참고] 별도의 서비스나 스레드를 사용하지 않고 미디어 재생을 시작하면, 액티비티 종료 시 미디어
                 *       재생도 같이 종료되는 문제가 발생함.
                 *--------------------------------------------------------------------------------*/
                try {
                    Toast.makeText(this, "재생 준비중!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13

                    //mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(mFile));
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(this, "재생 시작됨!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.buttonPlay2: //MediaPlayer 와 Service 로 재생
                /**---------------------------------------------------------------------------------
                 * 명시적 인텐트로 서비스를 시작하되 음악을 재생하라는 사용자 정의액션("andbook.example.PLAYMUSIC")
                 * 과 더불어 재생할 파일의 Uri 를 인텐트로 실어보낸다.
                 *--------------------------------------------------------------------------------
                intent = new Intent(this, MyService.class);
                intent.setAction("andbook.example.PLAYMUSIC");
                intent.setData(Uri.fromFile(mFile));
                startService(intent);  */
                playAudio();
                break;
        }

    }

    public void playAudio() {
        try {

            closePlayer();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "재생 시작됨!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pauseAudio() {
        if(mediaPlayer != null) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            Toast.makeText(this, "일시 정지됨!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13
        }
    }

    public void resumeAudio() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
            Toast.makeText(this, "재 시작됨!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13
        }
    }

    public void stopAudio() {

        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Toast.makeText(this, "정지됨!", Toast.LENGTH_LONG).show(); //MKHUH 2018-04-13
        }
    }

    public void closePlayer() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
