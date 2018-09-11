package com.example.administrator.mymusic2.download;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/24.
 */
//下载功能
public class DownloadTask extends AsyncTask<String,Integer,Integer>{
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;

    private DownloadListener listener;
    private boolean isCanceled=false;
    private boolean isPaused=false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }

    protected Integer doInBackground(String... params){
        InputStream is=null;
        RandomAccessFile saveFile=null;
        File file=null;
        try{
            long downloadedLength=0;
            String downloadUrl=params[0];
            String fileName=params[1];//根据Url地址截取出文件名
            Log.d("TAG","fileName:"+fileName);
            String directory= Environment.getExternalStorageDirectory().getAbsolutePath()+
                    File.separator+"myMusic"+File.separator+"lrc";
            File dirfile=new File(directory);
            Log.d("TAG",dirfile+"");
            if(!dirfile.exists()){
                dirfile.mkdirs();
                Log.d("TAG","fileCreate");
            }
            file=new File(directory,fileName);
            Log.d("TAG",file+"");
            if(file.exists()){//判断文件是否存在，若存在则启动断点续传的功能
                downloadedLength=file.length();
            }
            long contentLength=getContentLength(downloadUrl);//获取下载文件的总长度
            if(contentLength==0){//说明文件有问题
                return TYPE_FAILED;
            }else if(contentLength==downloadedLength){//文件下载成功
                return TYPE_SUCCESS;
            }
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .addHeader("RANGE","bytes="+downloadedLength+"-")//告诉服务器从哪个字节开始下载，
                                                                       //下载过的部分无需重新下载
                    .url(downloadUrl)
                    .build();
            Response response=client.newCall(request).execute();//获取服务器返回的数据
            if(response!=null){//使用Java的文件流方式，从网络上读取数据，写入本地
                is=response.body().byteStream();
                saveFile=new RandomAccessFile(file,"rw");
                saveFile.seek(downloadedLength);
                byte[] b=new byte[1024];
                int total=0;
                int len;
                while((len=is.read(b))!=-1){
                    if(isCanceled){//判定用户是否触发取消操作
                        return TYPE_CANCELED;
                    }
                    else if(isPaused){//判定用户是否触发暂停操作
                        return TYPE_PAUSED;
                    }
                    else{
                        total += len;
                        saveFile.write(b,0,len);
                        int progress=(int)((total+downloadedLength)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(is!=null){
                    is.close();
                }
                if(saveFile!=null){
                    saveFile.close();
                }
                if(isCanceled&&file!=null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress=values[0];//获取当前下载进度
        Log.d("TAG",lastProgress+"");
        if(progress>lastProgress){//与上一次下载进度进行对比，有变化就对进度进行更新
            lastProgress=progress;
        }
    }
    @Override
    protected void onPostExecute(Integer status) {//根据doInBackground（）返回的下载状态进行回调
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            default:
                break;
        }
    }
    private long getContentLength(String downloadUrl)throws IOException{
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response=client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()){
            long contentLength=response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}
