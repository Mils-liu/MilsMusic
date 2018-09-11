package com.example.administrator.mymusic2.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.util.HttpUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public Boolean Permissionflag=false;//判断是否成功获取权限
    private ImageView cover_welcome;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){//如果版本号大于或等于21
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//让状态栏也可以显示活动的布局
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明色
        }
        setContentView(R.layout.activity_main);
        cover_welcome=(ImageView)findViewById(R.id.cover_welcome);
        loadBingPic();//加载欢迎界面图片，从必应获取图片数据
        getPermission();//获取权限
        LitePal.getDatabase();//若无数据库则创建数据库
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent=new Intent(MainActivity.this,MusicActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);//延迟3000ms执行跳转操作
    }

    private void getPermission(){
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);//若不被授权就添加在List集合中
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);//若不被授权就添加在List集合中
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);//若不被授权就添加在List集合中
        }
        if(!permissionList.isEmpty()){//判断集合是否为空
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);//将集合转变为数组
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            Permissionflag=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Permissionflag=true;
                }else {
                    Permissionflag=false;
                }
                break;
            default:
        }
    }

    private void loadBingPic(){
        String requestBingPic="http:/guolin.tech/api/bing_pic";//必应图片接口
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//成功获取数据
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(cover_welcome);//加载图片到控件
                    }
                });
            }
        });
    }
}
