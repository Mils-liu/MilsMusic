package com.example.administrator.mymusic2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.administrator.mymusic2.activity.MusicActivity;
import com.example.administrator.mymusic2.shake.ShakeListener;

import java.io.File;

import static com.example.administrator.mymusic2.activity.MusicActivity.SHAKE_STATE;

public class MusicService extends Service {
    //手机摇一摇
    private static final String TAG="MusicServiceManager";
    private ShakeListener mShakeListener;
    private Vibrator vibrator;
    private long lastUpdateTime;
    private BroadcastReceiver receiver;
    private AudioManager mAm;

    private LocalBroadcastManager serviceBroadcastManager;
    public MediaPlayer mediaPlayer;
    public MusicService() {
        mediaPlayer=new MediaPlayer();
    }
    public void setUrl(String url){
        String[] newStr=url.split("/");
        String newUrl="";
        String musicName="";
        for(int i=0;i<newStr.length;i++){
            if(i==newStr.length-1){
                musicName=newStr[i];
                break;
            }
            if(i== newStr.length-2)
                newUrl=newUrl+newStr[i];
            else
                newUrl=newUrl+newStr[i]+"/";
        }
        try{
            if((!newUrl.equals(""))&&(!musicName.equals(""))){
                File file=new File(newUrl,musicName);
                mediaPlayer.setDataSource(file.getPath());
                mediaPlayer.prepare();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceBroadcastManager=LocalBroadcastManager.getInstance(this);
        mAm = (AudioManager) getSystemService(AUDIO_SERVICE);

        vibrator=(Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        mShakeListener=new ShakeListener(getBaseContext());
        mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
            @Override
            public void onShake() {
                // TODO Auto-generated method stub
                long currentUpdateTime=System.currentTimeMillis();
                long timeInterval=currentUpdateTime-lastUpdateTime;

                if((timeInterval>2000)&&SHAKE_STATE){
                    mShakeListener.stop();
                    Intent intent=new Intent();
                    intent.setAction("Shake");
                    serviceBroadcastManager.sendBroadcast(intent);
                    mShakeListener.start();
                    lastUpdateTime=currentUpdateTime;
                }
            }
        } );

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
// Pause playback
                pause();
                Log.d(TAG,"pause");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
// Resume playback
                resume();
                Log.d(TAG,"start");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
// mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                mAm.abandonAudioFocus(afChangeListener);
// Stop playback
                pause();
                Log.d(TAG,"stop");
            }
        }
    };

    /*获取音频焦点*/
    private boolean requestFocus() {
// Request audio focus for playback
        int result = mAm.requestAudioFocus(afChangeListener,
// Use the music stream.
                AudioManager.STREAM_MUSIC,
// Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void abandonAudioFocus(){
        mAm.abandonAudioFocus(afChangeListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        mShakeListener.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void start(){
        if(requestFocus()){
            if(mediaPlayer!=null){
                mediaPlayer.start();
                MusicActivity.STATE = "PLAY";
            }
        }
    }
    public void pause(){
        if(mediaPlayer!=null){
            mediaPlayer.pause();
            MusicActivity.STATE = "PAUSE";
        }
    }

    public void resume(){
        if(mediaPlayer!=null){
            mediaPlayer.start();
            MusicActivity.STATE = "PLAY";
        }
    }

    public MusicBinder binder=new MusicBinder();
    public class MusicBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mShakeListener.stop();
        /*unregisterReceiver(receiver);*/
    }

}
