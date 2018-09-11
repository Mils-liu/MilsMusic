package com.example.administrator.mymusic2.download;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.mymusic2.gson.musicGson.Lrc;

//下载服务
public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private LocalBroadcastManager serviceBroadcastManager;
    private String LrcName=null;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceBroadcastManager=LocalBroadcastManager.getInstance(this);
        Log.d("Service","DownloadService");
    }

    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onSuccess() {
            downloadTask=null;
            Intent intent=new Intent();
            intent.putExtra("lrcName",LrcName);
            intent.setAction("DownloadSuccess");
            serviceBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailed() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"获取歌词失败",Toast.LENGTH_SHORT).show();
        }
    };
    private DownloadBinder mBinder=new DownloadBinder();
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class DownloadBinder extends Binder {//让活动和DownloadService进行通信
        public void startDownload(String url,String lrcName){//开始下载
            if(downloadTask==null){
                LrcName=lrcName;
                downloadUrl=url;
                downloadTask=new DownloadTask(listener);
                downloadTask.execute(downloadUrl,lrcName);//启动任务downloadTask，
            }
        }
    }
}
