package com.example.administrator.mymusic2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

import com.example.administrator.mymusic2.activity.LockScreenActivity;
import com.example.administrator.mymusic2.activity.MusicActivity;
import com.example.administrator.mymusic2.shake.ShakeListener;

import java.io.File;

import static com.example.administrator.mymusic2.activity.MusicActivity.SHAKE_STATE;

public class MusicService extends Service {
    //手机摇一摇
    private static final String TAG="ShakeService";
    private ShakeListener mShakeListener;
    private Vibrator vibrator;
    private long lastUpdateTime;
    private BroadcastReceiver receiver;

    /*private LockScreen receiver;
    private IntentFilter intentFilter;*/

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

        /*intentFilter=new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        receiver=new LockScreen();
        registerReceiver(receiver,intentFilter);*/
        serviceBroadcastManager=LocalBroadcastManager.getInstance(this);

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

        receiver = new BroadcastReceiver() {//接受息屏的本地广播

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                    System.out.println("收到锁屏广播");
                    Intent lockscreen = new Intent(MusicService.this, LockScreenActivity.class);
                    lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    /*startActivity(lockscreen);*/
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        mShakeListener.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void star(){
        if(mediaPlayer!=null){
            mediaPlayer.start();
            MusicActivity.STATE = "PLAY";
        }
    }
    public void pause(){
        if(mediaPlayer!=null){
            mediaPlayer.pause();
            MusicActivity.STATE = "PAUSE";
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

    /*public class LockScreen extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Intent lockscreen = new Intent(MusicService.this, LockScreenActivity.class);
                lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(lockscreen);
            }
        }
    }*/


    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mShakeListener.stop();
        /*unregisterReceiver(receiver);*/
    }

}
