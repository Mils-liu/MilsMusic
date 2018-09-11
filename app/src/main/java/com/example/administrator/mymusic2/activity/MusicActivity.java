package com.example.administrator.mymusic2.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.administrator.mymusic2.datebase.locationDatebase.City;
import com.example.administrator.mymusic2.datebase.locationDatebase.County;
import com.example.administrator.mymusic2.datebase.locationDatebase.Province;
import com.example.administrator.mymusic2.service.MusicService;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.fragment.DialogFragment_playMusic;
import com.example.administrator.mymusic2.fragment.Fragment_localMusic;
import com.example.administrator.mymusic2.fragment.Fragment_loveMusic;
import com.example.administrator.mymusic2.fragment.Fragment_recentMusic;
import com.example.administrator.mymusic2.gson.weatherGson.Weather;
import com.example.administrator.mymusic2.util.HttpUtil;
import com.example.administrator.mymusic2.util.Utility;
import com.skyfishjy.library.RippleBackground;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.Position_Play;
import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.Total;

/**
 * Created by Administrator on 2018/3/7.
 */

public class MusicActivity extends AppCompatActivity implements View.OnClickListener{
    private long exitTime=0;//音乐结束时刻
    public static String STATE="";//音乐状态
    static int INTENT=0;//判断是跳转到下个Activity还是退出程序
    public static Boolean SHAKE_STATE=false;//判断是否开启摇摇切歌

    //控件
    private Button toPlayList,music_play,music_left,music_right;
    private TextView song,singer;
    private SeekBar seekBar;
    private TabLayout mTabTl;
    private ViewPager mContentVp;
    private Fragment local,love,recent;
    private LinearLayout layout_toPlay;
    private MusicService musicService;//音乐后台
    private String lastUrl="";//当前正在播放的音乐url
    private Vibrator vibrator;//手机抖动控件
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView Image_home;
    private TextView weather_info,weather_pm25,weather_location,weather_temp;
    private ImageView weather_icon;
    private FrameLayout music_layout;

    private RippleBackground rippleBackground,rippleBackground_left,rippleBackground_right,rippleBackground_list;

    //本地广播
    private LocalMusicUrlReceiver receiver;
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private Boolean BroadcastState=false;

    //ViewPage等滑动界面
    private List<String> tabIndicators;
    private List<Fragment> tabFragments;
    private ContentPagerAdapter contentAdapter;

    //定位
    public LocationClient mLocationClient;
    private String location;
    private String province=null;
    private String city=null;
    private String county=null;
    private List<Province> provincesList;
    private List<City> cityList;
    private List<County> countyList;
    public Province selectedProvince;
    public City selectedCity;
    private Boolean queryFlag=true;//判断获取数据线程是否结束
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        init();//初始化控件及其他内容
        Log.d("ActivityTAG","MusicActivity");
        if(Build.VERSION.SDK_INT>=21){//如果版本号大于或等于21
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//让状态栏也可以显示活动的布局
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明色
        }

        local= new Fragment_localMusic();
        love=new Fragment_loveMusic();
        recent=new Fragment_recentMusic();
        mTabTl = (TabLayout) findViewById(R.id.tl_tab);
        mContentVp = (ViewPager) findViewById(R.id.vp_content);

        initContent();
        initTab();
        initLocation();

        mContentVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float
                    positionOffset, int positionOffsetPixels) {

            }
            //这里面就是监听
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //拖动进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(musicService.mediaPlayer!=null)
                    musicService.mediaPlayer.seekTo(progress);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectService();
        initLocalBroadcast();
        seekBar.setEnabled(true);
        if(Total!=0){
            Message message=new Message();
            message.what=0;
            mhandler.sendMessage(message);
            song.setText(DialogFragment_playMusic.playlist.
                    get(DialogFragment_playMusic.Position_Play).getMusicName());
            singer.setText(DialogFragment_playMusic.playlist.
                    get(DialogFragment_playMusic.Position_Play).getMusicSinger());
            if(STATE.equals(getResources().getString(R.string.pause)))
                music_play.setBackgroundResource(R.drawable.play);
            else
                music_play.setBackgroundResource(R.drawable.pause);
        }else{
            song.setText("Song");
            singer.setText("Singer");
            seekBar.setProgress(0);
            music_play.setBackgroundResource(R.drawable.play);
            STATE=getResources().getString(R.string.pause);
            seekBar.setEnabled(false);
        }
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    if ((musicService!=null)&&(musicService.mediaPlayer != null)) {
                        // 更新拖动条的当前进度
                        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
                        // 更新拖动条最大值
                        seekBar.setMax(musicService.mediaPlayer.getDuration());
                        if((musicService.mediaPlayer.getDuration()-musicService.mediaPlayer.getCurrentPosition()<150)
                                &&musicService.mediaPlayer.isPlaying()){
                            //音乐切换
                            if(DialogFragment_playMusic.Mode_Play==1000){
                                cycle_all("Right");//全部循环
                            }else if(DialogFragment_playMusic.Mode_Play==1001){
                                cycle_one();//单曲循环
                            }else if(DialogFragment_playMusic.Mode_Play==1002){
                                cycle_random();//随机播放
                            }
                            seekBar.setProgress(0);
                            break;
                        }
                    }
                    mhandler.sendEmptyMessageDelayed(0, 10);
                    break;
                case 1:
                    mhandler.sendEmptyMessageDelayed(0, 3000);
                    break;
            }
        }
    };

    private void init(){
        seekBar=(SeekBar)findViewById(R.id.music_seekBar);
        layout_toPlay=(LinearLayout)findViewById(R.id.toPlay);
        toPlayList=(Button)findViewById(R.id.list_play_music);
        music_play=(Button)findViewById(R.id.play_music);
        music_left=(Button)findViewById(R.id.left_music);
        music_right=(Button)findViewById(R.id.right_music) ;
        song=(TextView)findViewById(R.id.song);
        singer=(TextView)findViewById(R.id.singer);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        Image_home=(ImageView)findViewById(R.id.home);
        music_layout=(FrameLayout)findViewById(R.id.music_layout);

        navView=(NavigationView)findViewById(R.id.nav_view);
        View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
        weather_icon=(ImageView)headerLayout.findViewById(R.id.weather_icon);
        weather_info=(TextView)headerLayout.findViewById(R.id.weather_state);
        weather_location=(TextView)headerLayout.findViewById(R.id.weather_location);
        weather_temp=(TextView)headerLayout.findViewById(R.id.weather_temp);
        weather_pm25=(TextView)headerLayout.findViewById(R.id.weather_pm25);

        Image_home.setOnClickListener(this);
        layout_toPlay.setOnClickListener(this);
        toPlayList.setOnClickListener(this);
        music_play.setOnClickListener(this);
        music_left.setOnClickListener(this);
        music_right.setOnClickListener(this);

        rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground_left=(RippleBackground)findViewById(R.id.content_left);
        rippleBackground_right=(RippleBackground)findViewById(R.id.content_right);
        rippleBackground_list=(RippleBackground)findViewById(R.id.content_list);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_shake:
                        if(SHAKE_STATE){
                            SHAKE_STATE=false;
                            item.setIcon(R.drawable.shake_no);
                            Toast.makeText(MusicActivity.this,"已关闭摇摇切歌",Toast.LENGTH_SHORT).show();
                        }else {
                            SHAKE_STATE=true;
                            item.setIcon(R.drawable.shakel);
                            Toast.makeText(MusicActivity.this,"已开启摇摇切歌",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_finish:
                        unbindService(serviceConnection);
                        mLocationClient.stop();
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initWeather(){//获取定位城市天气信息
        SharedPreferences pref=getSharedPreferences("Weather",MODE_PRIVATE);
        String weatherId=pref.getString("weatherId","");
        Log.d("TAG","weatherId:"+weatherId);
        requestWeather(weatherId);
    }

    private void initLocalBroadcast(){//初始化本地广播
        if(BroadcastState==false){
            intentFilter=new IntentFilter();
            intentFilter.addAction("MusicUrl");
            intentFilter.addAction("Delete");
            intentFilter.addAction("Delete_All");
            intentFilter.addAction("Shake");
            receiver=new LocalMusicUrlReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver,intentFilter);
            localBroadcastManager=LocalBroadcastManager.getInstance(this);
            BroadcastState=true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.toPlay:
                if(!song.getText().equals("Song")){
                    INTENT=1;
                    Intent intent=new Intent(this,PlayActivity.class);
                    intent.putExtra("CurrentPosition",musicService.mediaPlayer.getCurrentPosition());
                    intent.putExtra("Duration",musicService.mediaPlayer.getDuration());
                    Log.d("MusicActivity","CurrentPosition:"+musicService.mediaPlayer.getCurrentPosition());
                    Log.d("MusicActivity","Duration:"+musicService.mediaPlayer.getDuration());
                    startActivity(intent);
                }else
                    Toast.makeText(MusicActivity.this,"请先添加歌曲",Toast.LENGTH_SHORT).show();
                break;
            case R.id.list_play_music:
                new DialogFragment_playMusic().show(getFragmentManager(),"dialog_fragment");
                /*Ripple(rippleBackground_list);*/
                break;
            case R.id.play_music:
                if((musicService.mediaPlayer!=null)&&(Total!=0)&&Position_Play!=-1){
                    if(STATE.equals(getResources().getString(R.string.play))){
                        musicService.mediaPlayer.pause();
                        music_play.setBackgroundResource(R.drawable.play);
                        mhandler.removeCallbacksAndMessages(null);
                        Message message=new Message();
                        message.what=1;
                        mhandler.sendMessage(message);
                        STATE=getResources().getString(R.string.pause);
                    }else if(STATE.equals(getResources().getString(R.string.pause))){
                        musicService.mediaPlayer.start();
                        music_play.setBackgroundResource(R.drawable.pause);
                        STATE=getResources().getString(R.string.play);
                        Message message=new Message();
                        message.what=0;
                        mhandler.sendMessage(message);
                    }
                    /*Ripple(rippleBackground);*/
                }else
                    Toast.makeText(MusicActivity.this,"请先添加歌曲",Toast.LENGTH_SHORT).show();
                break;
            case R.id.left_music:
                if((musicService.mediaPlayer!=null)&&(Total!=0)){
                    if(DialogFragment_playMusic.Mode_Play==1002)
                        cycle_random();
                    else
                        cycle_all("Left");
                }
                /*Ripple(rippleBackground_left);*/
                break;
            case R.id.right_music:
                if((musicService.mediaPlayer!=null)&&(Total!=0)){
                    if(DialogFragment_playMusic.Mode_Play==1002)
                        cycle_random();
                    else
                        cycle_all("Right");
                }
                /*Ripple(rippleBackground_right);*/
                break;
        }
    }

    private void initTab(){
        mTabTl.setTabMode(TabLayout.MODE_FIXED);
        mTabTl.setTabTextColors(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.white));
        mTabTl.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
        mTabTl.setupWithViewPager(mContentVp);
    }

    private void initContent(){
        tabIndicators = new ArrayList<>();
        tabIndicators.add("本地音乐");
        tabIndicators.add("我的音乐");
        tabIndicators.add("最近播放");
        tabFragments = new ArrayList<>();
        tabFragments.add(local);
        tabFragments.add(love);
        tabFragments.add(recent);
        contentAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mContentVp.setAdapter(contentAdapter);
    }

    //接受本地广播
    public class LocalMusicUrlReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("MusicUrl")){//播放音乐
                String url = intent.getStringExtra("url");
                String songStr=intent.getStringExtra("song");
                String singerStr=intent.getStringExtra("singer");
                if(musicService.mediaPlayer!=null){
                    if(!lastUrl.equals(url)){
                        musicService.mediaPlayer.reset();
                        musicService.setUrl(url);
                        musicService.star();
                        mhandler.removeCallbacksAndMessages(null);
                    }else {
                        musicService.setUrl(url);
                        musicService.star();
                    }
                    Message message=new Message();
                    message.what=0;
                    mhandler.sendMessage(message);
                    music_play.setBackgroundResource(R.drawable.pause);
                    song.setText(songStr);
                    singer.setText(singerStr);
                    STATE=getResources().getString(R.string.play);
                    seekBar.setEnabled(true);
                }
            }
            if(action.equals("Delete")){//删除指定音乐
                if(DialogFragment_playMusic.Mode_Play==1000)
                    cycle_all("");
                else if(DialogFragment_playMusic.Mode_Play==1002)
                    cycle_random();
            }
            if(action.equals("Delete_All")){//删除播放列表所有音乐
                mhandler.removeCallbacksAndMessages(null);
                seekBar.setProgress(0);
                musicService.mediaPlayer.reset();
                STATE=getResources().getString(R.string.pause);
                song.setText("Song");
                singer.setText("Singer");
                music_play.setBackgroundResource(R.drawable.play);
                seekBar.setEnabled(false);
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
                    Log.d("Shake","MusicShake");
                }else{
                    Toast.makeText(MusicActivity.this,"请先添加歌曲",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //连接MusicService
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
    private void ConnectService(){
        Intent intent=new Intent(this,MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            return tabIndicators.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabIndicators.get(position);
        }
    }

    public class MyLocationListener implements BDLocationListener {//定位监听
        public void onReceiveLocation(BDLocation bdLocation) {
            //获取省市县数据
            location=bdLocation.getProvince()+"-" +bdLocation.getCity()+"-"+bdLocation.getDistrict();
            Log.d("TAG",location);
            province=bdLocation.getProvince().substring(0,bdLocation.getProvince().length()-1);
            city=bdLocation.getCity().substring(0,bdLocation.getProvince().length()-1);
            county=bdLocation.getDistrict().substring(0,bdLocation.getProvince().length()-1);

            //获取天气信息
            if((province!=null)&&(city!=null)&&(county!=null)){
                queryProvinces();
                while (queryFlag){}
                Log.d("query","ProvinceFlag1:"+queryFlag+"");
                for(Province p:provincesList){
                    Log.d("TAG",p.getProvinceName());
                    if(p.getProvinceName().equals(province)){
                        selectedProvince=p;
                        Log.d("TAG","select:"+selectedProvince.getProvinceName());
                        break;
                    }
                    Log.d("query","ProvinceFlag:"+queryFlag+"");
                    queryFlag=true;
                    Log.d("query","ProvinceFlag:"+queryFlag+"");
                }
                queryCities();
                while (queryFlag){}
                Log.d("query","CityFlag1:"+queryFlag+"");
                for(City c:cityList){
                    Log.d("TAG","city:"+c.getCityName());
                    if(c.getCityName().equals(city)){
                        selectedCity=c;
                        Log.d("TAG","select:"+selectedCity.getCityName());
                        break;
                    }
                    queryFlag=true;
                    Log.d("query","CityFlag:"+queryFlag+"");
                }
                queryCounties();
                while (queryFlag){}
                for(County co:countyList){
                    Log.d("TAG",co.getCountyName());
                    if(co.getCountyName().equals(county)){
                        Log.d("TAG","select:"+co.getCountyName());
                        SharedPreferences.Editor editor=
                                getSharedPreferences("Weather",MODE_PRIVATE).edit();
                        editor.putString("weatherId",co.getWeatherId());
                        Log.d("TAG","co.id:"+co.getWeatherId());
                        editor.apply();
                        break;
                    }
                }
                initWeather();
            }
        }
    }

    private void initLocation(){//初始化百度地图
        setBaiduMap();
        if(queryFlag){
            requestLocation();
        }
    }

    private void setBaiduMap(){
        mLocationClient=new LocationClient(getApplicationContext());//实例化
        mLocationClient.registerLocationListener(new MusicActivity.MyLocationListener());//注册定位监听器
    }

    private void requestLocation(){
        setLocation();
        mLocationClient.start();
    }

    private void setLocation(){//设置定位属性
        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);//获取城市地址
        mLocationClient.setLocOption(option);
    }

    private void queryProvinces(){//获得中国所有省份
        provincesList= DataSupport.findAll(Province.class);
        Log.d("TAG","size:"+provincesList.size());
        if(provincesList.size()<34){
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
            Log.d("TAG","query");
        }else
            queryFlag=false;
    }
    private void queryCities(){//获得省份对应城市
        cityList=DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() == 0) {
            Log.d("TAG","citysize:"+cityList.size());
            String address="http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            Log.d("TAG","selectCode:"+selectedProvince.getProvinceCode());
            queryFromServer(address,"city");
        }else
            queryFlag=false;
        Log.d("TAG","citysize:"+cityList.size());
    }
    private void queryCounties(){//获得城市对应县城
        countyList=DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()==0){
            Log.d("TAG","CountySize:"+countyList.size());
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()
                    +"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }else
            queryFlag=false;
    }
    private void queryFromServer(String address, final String type){//从网路获取省市县数据
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                Log.d("TAG","response:"+responseText);
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    Log.d("TAG","city");
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    if("province".equals(type)){
                        queryProvinces();
                    }else if("city".equals(type)){
                        queryCities();
                    }else if("county".equals(type)){
                        queryCounties();
                    }
                }
            }
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MusicActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //双击退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit(){//退出程序
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            unbindService(serviceConnection);
            mLocationClient.stop();
            finish();
        }
    }

    private void Ripple(final RippleBackground ripple){
        ripple.startRippleAnimation();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                ripple.stopRippleAnimation();
            }
        }, 500);
    }

    private void cycle_all(String state){//列表循环
        musicService.mediaPlayer.setLooping(false);
        musicService.mediaPlayer.reset();
        if(state.equals("Right")){//下一首
            if (DialogFragment_playMusic.Position_Play==(Total-1)){
                DialogFragment_playMusic.Position_Play=-1;
            }
            DialogFragment_playMusic.Position_Play++;
        }else if(state.equals("Left")){//上一首
            if (DialogFragment_playMusic.Position_Play==0){
                DialogFragment_playMusic.Position_Play= Total;
            }
            DialogFragment_playMusic.Position_Play--;
        }
        musicService.setUrl(DialogFragment_playMusic.playlist.get(DialogFragment_playMusic.Position_Play).getUrl());
        musicService.mediaPlayer.start();
        song.setText(DialogFragment_playMusic.playlist.get(DialogFragment_playMusic.Position_Play).getMusicName());
        singer.setText(DialogFragment_playMusic.playlist.get(DialogFragment_playMusic.Position_Play).getMusicSinger());
        music_play.setBackgroundResource(R.drawable.pause);
        STATE=getResources().getString(R.string.play);

        SongChange();

        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
    }
    private void cycle_one(){//单曲循环
        musicService.mediaPlayer.setLooping(true);
        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
    }
    private void cycle_random(){//随机播放
        musicService.mediaPlayer.setLooping(false);
        int p=new Random().nextInt(Total);
        DialogFragment_playMusic.Position_Play=p;
        musicService.mediaPlayer.reset();
        musicService.setUrl(DialogFragment_playMusic.playlist.get(p).getUrl());
        musicService.mediaPlayer.start();
        song.setText(DialogFragment_playMusic.playlist.get(p).getMusicName());
        singer.setText(DialogFragment_playMusic.playlist.get(p).getMusicSinger());
        music_play.setBackgroundResource(R.drawable.pause);
        STATE=getResources().getString(R.string.play);

        SongChange();

        mhandler.removeCallbacksAndMessages(null);
        Message message=new Message();
        message.what=0;
        mhandler.sendMessage(message);
    }

    private void SongChange(){//当前播放的音乐发生变化
        Intent intent=new Intent();
        intent.setAction("SongChange");
        localBroadcastManager.sendBroadcast(intent);
    }

    public void requestWeather(final String weatherId){//获取天气信息
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+
                "&key=63660528a9dc4f968243a7f0669cbe26";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MusicActivity.this,"获取天气信息失败，请检查网络",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather= Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor=
                                    getSharedPreferences("Weather",MODE_PRIVATE).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeather(weather);//显示天气信息
                        }else {
                            Toast.makeText(MusicActivity.this,"获取天气信息失败，请检查网络",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeather(Weather weather){
        String degree=weather.now.temperature+"℃";
        String pm25=weather.aqi.city.pm25;
        String weatherInfo=weather.now.more.info;
        String weatherCode=weather.now.more.code;
        Log.d("MusicTAG","degree:"+degree);
        Log.d("MusicTAG","pm25:"+pm25);

        weather_temp.setText(degree);
        weather_location.setText(location);
        weather_info.setText(weatherInfo);
        weather_pm25.setText("PM2.5 "+pm25);
        setWeatherIcon(weatherCode);
    }

    private void setWeatherIcon(String weatherCode){
        String code="weather_"+weatherCode;
        weather_icon.setImageResource(getResource(code));
        Log.d("TAG","id:"+getResource(code));
    }

    public int getResource(String imageName){//将String转为对应的资源ID
        Context ctx=getBaseContext();
        int resId = getResources().getIdentifier(imageName, "drawable", ctx.getPackageName());
        //如果没有在"drawable"下找到imageName,将会返回0
        return resId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhandler.removeCallbacksAndMessages(null);
        unbindService(serviceConnection);
        mLocationClient.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(INTENT==1) {//判断是跳转到PlayActivity还是息屏
            unbindService(serviceConnection);
            mhandler.removeCallbacksAndMessages(null);
            INTENT=0;
            if(BroadcastState==true){
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
                BroadcastState=false;
            }
        }
    }
}
