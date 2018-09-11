package com.example.administrator.mymusic2.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.mymusic2.lrcView.LrcDataBuilder;
import com.example.administrator.mymusic2.lrcView.LrcRow;
import com.example.administrator.mymusic2.lrcView.LrcView;
import com.example.administrator.mymusic2.service.MusicService;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.download.DownloadService;
import com.example.administrator.mymusic2.fragment.DialogFragment_playMusic;
import com.example.administrator.mymusic2.gson.musicGson.Lrc;
import com.example.administrator.mymusic2.util.DisplayUtils;
import com.example.administrator.mymusic2.util.HttpUtil;
import com.example.administrator.mymusic2.util.TimeFormat;
import com.example.administrator.mymusic2.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.Total;

/**
 * Created by Administrator on 2018/3/7.
 */

public class PlayActivity extends AppCompatActivity implements View.OnClickListener{
    private MusicService musicService;
    private Button mode_play,list_music,play_right,play_left;
    private SeekBar seekBar;
    private TextView currentTime,endTime,title_song,title_singer;
    private ImageView imageView,cover,playBtn;
    private FrameLayout coverLayout;
    private AudioManager audioManager;
    private SeekBar seekBar_volume;
    private int MaxSound=0;

    private LocalMusicUrlReceiver_Play receiver;
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private Boolean BroadcastState=false;

    private Vibrator vibrator;

    private DownloadService.DownloadBinder downloadBinder;
    private String coverUrl="http://gecimi.com/api/cover";
    private String lrcUrl="http://gecimi.com/api/lyric";
    private String lrcPath=Environment.getExternalStorageDirectory().getAbsolutePath()+
             File.separator+"myMusic"+File.separator+"lrc";
    private File dirfile;

    private LrcView mLrcView;
    private LinearLayout lrcLayout;
    private FrameLayout centerLayout;
    private Boolean messageFlag=false;

    public static Boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityTAG","PlayActivity");
        if(Build.VERSION.SDK_INT>=21){//如果版本号大于或等于21
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//让状态栏也可以显示活动的布局
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明色

        }
        setContentView(R.layout.activity_play);
        ConnectService();//连接音乐Service
        ConnectDownload();//连接下载Service
        init();//初始化

        //设置自定义的LrcView上下拖动歌词时监听
        mLrcView.setLrcViewListener(new com.example.administrator.mymusic2.lrcView.ILrcViewListener() {
            @Override
            public void onSeek(com.example.administrator.mymusic2.lrcView.LrcRow currentLrcRow, long CurrentSelectedRowTime) {
                if ((musicService!=null)&&(musicService.mediaPlayer != null)) {
                    musicService.mediaPlayer.seekTo((int) CurrentSelectedRowTime);
                }
            }

            @Override
            public void onClick() {//LrcView的点击事件
                if(flag==false){
                    coverLayout.setVisibility(View.INVISIBLE);
                    lrcLayout.setVisibility(View.VISIBLE);
                    flag=true;
                    Log.d("TAG","flag:"+flag+"");
                    Log.d("TAG","visibility:"+centerLayout.getVisibility()+"");
                }else if(flag==true){
                    coverLayout.setVisibility(View.VISIBLE);
                    lrcLayout.setVisibility(View.INVISIBLE);
                    flag=false;
                    Log.d("TAG","flag:"+flag+"");
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(DialogFragment_playMusic.Total!=0){
            title_song.setText(DialogFragment_playMusic.playlist.
                    get(DialogFragment_playMusic.Position_Play).getMusicName());
            title_singer.setText(DialogFragment_playMusic.playlist.
                    get(DialogFragment_playMusic.Position_Play).getMusicSinger());
        }
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    if ((musicService!=null)&&(musicService.mediaPlayer != null)) {
                        // 更新当前播放时间
                        currentTime.setText(TimeFormat.time(musicService.mediaPlayer.getCurrentPosition()));
                        // 更新结束时间
                        endTime.setText(TimeFormat.time(musicService.mediaPlayer.getDuration()));
                        // 更新拖动条的当前进度
                        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
                        // 更新拖动条最大值
                        seekBar.setMax(musicService.mediaPlayer.getDuration());
                        // 若当前音乐在播放，更新图片的rotation，令其随着音乐播放旋转
                        if (musicService.mediaPlayer.isPlaying()) {
                            imageView.setRotation((imageView.getRotation() + 0.5f) % 360);
                            cover.setRotation((imageView.getRotation() + 0.5f) % 360);
                        }
                        if((musicService.mediaPlayer.getDuration()-musicService.mediaPlayer.getCurrentPosition()<150)
                                &&musicService.mediaPlayer.isPlaying()){
                            if(DialogFragment_playMusic.Mode_Play==1000){
                                cycle_all("Right");
                            }else if(DialogFragment_playMusic.Mode_Play==1001){
                                cycle_one();
                            }else if(DialogFragment_playMusic.Mode_Play==1002){
                                cycle_random();
                            }
                            seekBar.setProgress(0);
                            break;
                        }
                    }
                    mhandler.sendEmptyMessageDelayed(0, 10);
                    break;
                case 1:
                    mLrcView.smoothScrollToTime(musicService.mediaPlayer.getCurrentPosition());
                    mhandler.sendEmptyMessageDelayed(1,100);
                    break;
            }
        }
    };
    private void init(){
        //控件
        list_music=(Button)findViewById(R.id.list_music);
        playBtn = (ImageView)findViewById(R.id.play);
        mode_play=(Button) findViewById(R.id.mode_play);
        play_left=(Button)findViewById(R.id.left);
        play_right=(Button)findViewById(R.id.right);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        currentTime = (TextView)findViewById(R.id.currentTime);
        endTime = (TextView)findViewById(R.id.endTime);
        imageView = (ImageView)findViewById(R.id.imageView);
        cover=(ImageView)findViewById(R.id.cover);
        coverLayout=(FrameLayout)findViewById(R.id.coverLayout);
        /*imageView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        cover.setLayerType(View.LAYER_TYPE_HARDWARE,null);*/
        title_singer=(TextView)findViewById(R.id.title_singer);
        title_song=(TextView)findViewById(R.id.title_song);
        mLrcView=(LrcView) findViewById(R.id.lrcView);
        lrcLayout=(LinearLayout)findViewById(R.id.lrcLayout);
        centerLayout=(FrameLayout)findViewById(R.id.centerLayout);
        seekBar_volume=(SeekBar)findViewById(R.id.seekBar_volume);

        Log.d("TAG","layoutheight:"+getViewHeight(centerLayout));

        //音量
        initVolume();

        if(DialogFragment_playMusic.Mode_Play==1000)
            mode_play.setBackgroundResource(R.drawable.music_cycle_all);
        else if(DialogFragment_playMusic.Mode_Play==1001)
            mode_play.setBackgroundResource(R.drawable.music_cycle_one);
        else if(DialogFragment_playMusic.Mode_Play==1002)
            mode_play.setBackgroundResource(R.drawable.music_cycle_random);

        //文字加粗
        /*TextPaint tp=title_song.getPaint();
        tp.setFakeBoldText(true);*/

        playBtn.setOnClickListener(this);
        play_left.setOnClickListener(this);
        play_right.setOnClickListener(this);
        mode_play.setOnClickListener(this);
        list_music.setOnClickListener(this);
        seekBar.setEnabled(true);
        centerLayout.setOnClickListener(this);

        //设置接受的本地广播
        intentFilter=new IntentFilter();
        intentFilter.addAction("MusicUrl");
        intentFilter.addAction("Delete");
        intentFilter.addAction("Delete_All");
        intentFilter.addAction("DownloadSuccess");
        intentFilter.addAction("Shake");
        receiver=new LocalMusicUrlReceiver_Play();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,intentFilter);
        localBroadcastManager=LocalBroadcastManager.getInstance(this);

        initState();//设置进入该Activity后界面的状态

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//音乐进度条的监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {   // 判断是否来自用户
                    currentTime.setText(TimeFormat.time(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                musicService.mediaPlayer.seekTo(progress);
            }
        });

        seekBar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//音量条的监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){// 判断是否来自用户
                    int seekPosition=seekBar_volume.getProgress();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekPosition, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initVolume(){//初始化音量控件
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//获取音量服务
        MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
        seekBar_volume.setMax(MaxSound);
        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
        seekBar_volume.setProgress(currentSount);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play://播放音乐
                if((musicService.mediaPlayer!=null)&&(DialogFragment_playMusic.Total!=0)){
                    if(MusicActivity.STATE.equals(getResources().getString(R.string.play))){
                        musicService.mediaPlayer.pause();
                        playBtn.setImageResource(R.drawable.play);
                        mhandler.removeCallbacksAndMessages(null);
                        /*Message message=new Message();
                        message.what=1;
                        mhandler.sendMessage(message);*/
                        MusicActivity.STATE=getResources().getString(R.string.pause);
                    }else if(MusicActivity.STATE.equals(getResources().getString(R.string.pause))){//暂停
                        musicService.mediaPlayer.start();
                        playBtn.setImageResource(R.drawable.pause);
                        MusicActivity.STATE=getResources().getString(R.string.play);
                        Message message=new Message();
                        message.what=0;
                        mhandler.sendMessage(message);
                        Message message1=new Message();
                        message1.what=1;
                        mhandler.sendMessage(message1);
                    }if((musicService.mediaPlayer.getDuration()-musicService.mediaPlayer.getCurrentPosition()<150)
                            &&musicService.mediaPlayer.isPlaying()){//音乐切换
                        if(DialogFragment_playMusic.Mode_Play==1000){
                            cycle_all("Right");
                        }else if(DialogFragment_playMusic.Mode_Play==1001){
                            cycle_one();
                        }else if(DialogFragment_playMusic.Mode_Play==1002){
                            cycle_random();
                        }
                        seekBar.setProgress(0);
                        break;
                    }
                }else
                    Toast.makeText(PlayActivity.this,"请先添加歌曲",Toast.LENGTH_SHORT).show();
                break;
            case R.id.list_music://打开播放列表
                new DialogFragment_playMusic().show(getFragmentManager(),"dialog_fragment");
                break;
            case R.id.mode_play://甚至音乐播放方式
                if(DialogFragment_playMusic.Mode_Play==1000){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_one);
                    DialogFragment_playMusic.Mode_Play=1001;
                    Toast.makeText(PlayActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                }else if(DialogFragment_playMusic.Mode_Play==1001){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_random);
                    DialogFragment_playMusic.Mode_Play=1002;
                    Toast.makeText(PlayActivity.this,"随机播放",Toast.LENGTH_SHORT).show();
                }else if(DialogFragment_playMusic.Mode_Play==1002){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_all);
                    DialogFragment_playMusic.Mode_Play=1000;
                    Toast.makeText(PlayActivity.this,"列表循环",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.left://上一首
                if((musicService.mediaPlayer!=null)&&(DialogFragment_playMusic.Total!=0)){
                    if(DialogFragment_playMusic.Mode_Play==1002)
                        cycle_random();
                    else
                        cycle_all("Left");
                }
                break;
            case R.id.right://下一首
                if((musicService.mediaPlayer!=null)&&(DialogFragment_playMusic.Total!=0)){
                    if(DialogFragment_playMusic.Mode_Play==1002)
                        cycle_random();
                    else
                        cycle_all("Right");
                }
                break;
            case R.id.centerLayout://隐藏音乐转盘显示歌词界面
                Log.d("TAG","visibility:"+centerLayout.getVisibility()+"");
                if(flag==false){
                    coverLayout.setVisibility(View.INVISIBLE);
                    lrcLayout.setVisibility(View.VISIBLE);
                    flag=true;
                    Log.d("TAG","flag:"+flag+"");
                    Log.d("TAG","visibility:"+centerLayout.getVisibility()+"");
                }else if(flag==true){
                    coverLayout.setVisibility(View.VISIBLE);
                    lrcLayout.setVisibility(View.INVISIBLE);
                    flag=false;
                    Log.d("TAG","flag:"+flag+"");
                }
                break;
            default:
                break;
        }
    }

    private void getCover(final int albumId){//获得音乐封面
        String coverurl=coverUrl+"/"+albumId;
        Log.d("TAG","coverurl:"+coverurl);
            HttpUtil.sendOkHttpRequest(coverurl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayActivity.this,"获取音乐封面失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText=response.body().string();
                    Log.d("TAG","response:"+responseText);
                    final String cover= Utility.handleCoverResponse(responseText);
                    final String[] newstr=cover.split("/");
                    Log.d("TAG","newstr:"+newstr[newstr.length-2]);
                    Log.d("TAG","cover:"+cover);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(Integer.valueOf(newstr[newstr.length-2])!=0){
                                Glide.with(PlayActivity.this).load(cover).into(imageView);
                            }else {
                                Toast.makeText(PlayActivity.this,"获取音乐封面失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
    }

    private void getLrc(final String music_name){//获取歌词
        String lrcurl=lrcUrl+"/"+music_name;
        if(checkLrcFile(music_name)){//判断本地是否已存在歌词文件
            showLrc(dirfile);
        }else {
            showLrc(null);
            HttpUtil.sendOkHttpRequest(lrcurl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayActivity.this,"获取歌词失败",Toast.LENGTH_SHORT).show();
                            showLrc(null);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText=response.body().string();
                    Log.d("TAG","response:"+responseText);
                    final Lrc lrc=Utility.handleLrcResponse(responseText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(lrc.getResult()!=null){
                                if(lrc.getResult().size()>0){
                                    final String url=lrc.getResult().get(0).getLrc();
                                    downloadBinder.startDownload(url,music_name);//下载获取的歌词文件
                                }else{
                                    Toast.makeText(PlayActivity.this,"获取歌词失败",Toast.LENGTH_SHORT).show();
                                    showLrc(null);
                                }
                            }else{
                                Toast.makeText(PlayActivity.this,"获取歌词失败",Toast.LENGTH_SHORT).show();
                                showLrc(null);
                            }

                        }
                    });
                }
            });
        }
    }

    private void cycle_all(String state){
        musicService.mediaPlayer.setLooping(false);
        musicService.mediaPlayer.reset();
        if(state.equals("Right")){
            if (DialogFragment_playMusic.Position_Play==(DialogFragment_playMusic.Total-1)){
                DialogFragment_playMusic.Position_Play=-1;
            }
            DialogFragment_playMusic.Position_Play++;
        }else if(state.equals("Left")){
            if (DialogFragment_playMusic.Position_Play==0){
                DialogFragment_playMusic.Position_Play=DialogFragment_playMusic.Total;
            }
            DialogFragment_playMusic.Position_Play--;
        }
        LocalMusic music = DialogFragment_playMusic.playlist.get(DialogFragment_playMusic.Position_Play);
        musicService.setUrl(music.getUrl());
        musicService.mediaPlayer.start();
        title_song.setText(music.getMusicName());
        title_singer.setText(music.getMusicSinger());
        playBtn.setImageResource(R.drawable.pause);
        MusicActivity.STATE=getResources().getString(R.string.play);
        getLrc(music.getMusicName());
        getCover(music.getAlbum_id());

        SongChange();

        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
        if (messageFlag){
            Message message1=new Message();
            message.what=1;
            mhandler.sendMessage(message1);
        }
    }
    private void cycle_one(){
        musicService.mediaPlayer.setLooping(true);
        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
    }
    private void cycle_random(){
        musicService.mediaPlayer.setLooping(false);//取消单曲循环
        int p=new Random().nextInt(DialogFragment_playMusic.Total);
        LocalMusic music=DialogFragment_playMusic.playlist.get(p);
        DialogFragment_playMusic.Position_Play=p;
        musicService.mediaPlayer.reset();
        musicService.setUrl(music.getUrl());
        musicService.mediaPlayer.start();
        title_song.setText(music.getMusicName());
        title_singer.setText(music.getMusicSinger());
        getLrc(music.getMusicName());
        getCover(music.getAlbum_id());
        playBtn.setImageResource(R.drawable.pause);
        MusicActivity.STATE=getResources().getString(R.string.play);

        SongChange();

        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
        if (messageFlag){
            Message message1=new Message();
            message.what=1;
            mhandler.sendMessage(message1);
        }
    }

    private void initState(){//初始化音乐播放状态
        if(MusicActivity.STATE.equals(getResources().getString(R.string.play))){
            playBtn.setImageResource(R.drawable.pause);
            Message message=new Message();
            message.what=0;
            mhandler.sendMessage(message);
        } else if(MusicActivity.STATE.equals(getResources().getString(R.string.pause)))
            playBtn.setImageResource(R.drawable.play);
        if(DialogFragment_playMusic.Total!=0){
            LocalMusic music=DialogFragment_playMusic.playlist.get(DialogFragment_playMusic.Position_Play);
            title_song.setText(music.getMusicName());
            title_singer.setText(music.getMusicSinger());
            seekBar.setMax(getIntent().getIntExtra("Duration",0));
            seekBar.setProgress(getIntent().getIntExtra("CurrentPosition",0));
            currentTime.setText(TimeFormat.time(getIntent().getIntExtra("CurrentPosition",0)));
            endTime.setText(TimeFormat.time(getIntent().getIntExtra("Duration",0)));
            getLrc(music.getMusicName());
            getCover(music.getAlbum_id());
            Message message=new Message();
            message.what=1;
            mhandler.sendMessage(message);
        }else{
            title_song.setText("song");
            title_singer.setText("singer");
        }
    }

    public class LocalMusicUrlReceiver_Play extends BroadcastReceiver {//本地广播监听
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("MusicUrl")){//播放音乐
                String url = intent.getStringExtra("url");
                String songStr=intent.getStringExtra("song");
                String singerStr=intent.getStringExtra("singer");
                if(musicService.mediaPlayer!=null){
                    getLrc(songStr);
                    musicService.mediaPlayer.reset();
                    musicService.setUrl(url);
                    musicService.star();
                    mhandler.removeCallbacksAndMessages(null);
                    Message message=new Message();
                    message.what=0;
                    mhandler.sendMessage(message);
                    playBtn.setImageResource(R.drawable.pause);
                    title_song.setText(songStr);
                    title_singer.setText(singerStr);
                    MusicActivity.STATE=getResources().getString(R.string.play);
                    seekBar.setEnabled(true);
                }
            }
            if(action.equals("Delete")){//删除音乐
                cycle_all("Right");
            }
            if(action.equals("Delete_All")){//删除播放列表所有音乐
                mhandler.removeCallbacksAndMessages(null);
                seekBar.setProgress(0);
                seekBar.setEnabled(false);
                currentTime.setText("00:00");
                endTime.setText("00:00");
                musicService.mediaPlayer.reset();
                MusicActivity.STATE=getResources().getString(R.string.pause);
                title_song.setText("");
                title_singer.setText("");
                playBtn.setImageResource(R.drawable.play);
                showLrc(null);
            }
            if(action.equals("ModeChange")){//播放模式改变
                if(DialogFragment_playMusic.Mode_Play==1000){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_one);
                }else if(DialogFragment_playMusic.Mode_Play==1001){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_random);
                }else if(DialogFragment_playMusic.Mode_Play==1002){
                    mode_play.setBackgroundResource(R.drawable.music_cycle_all);
                }
            }
            if (action.equals("DownloadSuccess")){//下载成功
                String lrcName=intent.getStringExtra("lrcName");
                if(checkLrcFile(lrcName))
                    showLrc(dirfile);
            }
            if (action.equals("Shake")){//摇摇切歌
                if(Total>0){
                    vibrator=(Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500);

                    if(DialogFragment_playMusic.Mode_Play==1002){
                        cycle_random();
                    }else{
                        cycle_all("Right");
                    }
                    Log.d("Shake","PlayShake");
                }else{
                    Toast.makeText(PlayActivity.this,"请先添加歌曲",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void SongChange(){//切歌
        Intent intent=new Intent();
        intent.setAction("SongChange");
        localBroadcastManager.sendBroadcast(intent);
    }

    private boolean checkLrcFile(String filename){//检查本地是否已存在歌词文件
        String directory= lrcPath+File.separator+filename;
        dirfile=new File(directory);
        if (dirfile.exists())
            return true;
        else
            return false;
    }

    private void showLrc(File lrcFile){//显示歌词
        List<LrcRow> lrcRows=new LrcDataBuilder().Build(lrcFile);
        mLrcView.getLrcSetting()
                .setTimeTextSize(40)//时间字体大小
                .setSelectLineColor(Color.parseColor("#ffffff"))//选中线颜色
                .setSelectLineTextSize(25)//选中线大小
                .setLinePadding(50)//设置歌词行间距
                .setNormalRowColor(getResources().getColor(R.color.white))//普通字体颜色
                .setHeightRowColor(getResources().getColor(R.color.yellow))//高亮字体颜色
                .setNormalRowTextSize(DisplayUtils.sp2px(this, 60))//正常行字体大小
                .setHeightLightRowTextSize(DisplayUtils.sp2px(this, 60))//高亮行字体大小
                .setTrySelectRowTextSize(DisplayUtils.sp2px(this, 60))//尝试选中行字体大小
                .setTimeTextColor(Color.parseColor("#ffffff"))//时间字体颜色
                .setTrySelectRowColor(Color.parseColor("#55ffffff"));//尝试选中字体颜色
        mLrcView.commitLrcSettings();
        mLrcView.setLrcData(lrcRows);
        if (lrcFile!=null){
            messageFlag=true;
        }else{
            messageFlag=false;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        // bindService成功后回调onServiceConnected函数
        // 通过IBinder获取Service对象,实现Activity与Service的绑定
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            musicService = (MusicService)((MusicService.MusicBinder)binder).getService();

        }
        // 解除绑定
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder=(DownloadService.DownloadBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void ConnectService(){
        Intent intent=new Intent(this,MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void ConnectDownload(){
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,downloadConnection,BIND_AUTO_CREATE);
        Log.d("TAG","connect");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) // 音量增大键响应重做
        {
            int seekPosition=seekBar_volume.getProgress();
            if(seekBar_volume.getProgress()<MaxSound)
                seekPosition++;
            seekBar_volume.setProgress(seekPosition);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekPosition, 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)// 音量减小键响应重做
        {
            int seekPosition=seekBar_volume.getProgress();
            if(seekBar_volume.getProgress()>0)
                seekPosition--;
            seekBar_volume.setProgress(seekPosition);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekPosition, 0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getViewHeight(View view){
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int height = view.getMeasuredHeight();
        Log.d("DialogTag","height:"+height);
        return height;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(musicService.mediaPlayer!=null)
            unbindService(serviceConnection);
        unbindService(downloadConnection);
        seekBar.setEnabled(false);
        mhandler.removeCallbacksAndMessages(null);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
