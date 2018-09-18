package com.example.administrator.mymusic2.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.adapter.LocalMusicAdapter;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.datebase.musicDatebase.LoveMusic;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.Position_Play;
import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.Total;
import static com.example.administrator.mymusic2.fragment.DialogFragment_playMusic.playlist;

/**
 * Created by Administrator on 2018/3/8.
 */

public class Fragment_localMusic extends Fragment{
    private List<LocalMusic> localList=new ArrayList<>();
    private LocalBroadcastManager localBroadcastManager;
    private LinearLayout layout_local;
    private LocalMusicAdapter adapter;
    private IntentFilter intentFilter;
    private MusicReceiver receiver;
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local,container,false);
        super.onCreate(savedInstanceState);
        Log.d("FragmentLife","onCreateView");
        layout_local=(LinearLayout)view.findViewById(R.id.layout_local);

        initLocalMusic();
        receiver=new MusicReceiver();
        intentFilter=new IntentFilter();
        intentFilter.addAction("MusicUrl");
        intentFilter.addAction("Delete_All");
        intentFilter.addAction("SongChange");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,intentFilter);
        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter=new LocalMusicAdapter(localList);
        if(Position_Play!=-1){
            adapter.setDefSelect(playlist.get(Position_Play).getUrl());
        }
        recyclerView.setAdapter(adapter);

        localBroadcastManager=LocalBroadcastManager.getInstance(getActivity());

        adapter.setOnRecyclerViewListener(new LocalMusicAdapter.OnRecyclerViewListener(){
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()){
                    case R.id.add:
                        playlist.add(localList.get(position));
                        Total++;
                        Toast.makeText(getActivity(),"已添加到下一首播放",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.layout_local:
                        Intent intent=new Intent();
                        intent.putExtra("url",localList.get(position).getUrl());
                        intent.putExtra("song",localList.get(position).getMusicName());
                        intent.putExtra("singer",localList.get(position).getMusicSinger());
                        intent.setAction("MusicUrl");
                        Position_Play=position;
                        localBroadcastManager.sendBroadcast(intent);

                        playlist.clear();
                        initPlayMusic();
                        Position_Play=position;
                        break;
                    case R.id.add_love:
                        AddtoLove(position);
                        break;
                }
            }
        });
        return view;
    }
    private void initLocalMusic(){
        localList.clear();
        Log.d("LocalMusic","initLocalMusic");
        Cursor cursor=null;
        try{
            cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if(cursor!=null){
                while (cursor.moveToNext()){
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    //歌曲的名称：MediaStore.Audio.Media.TITLE
                    String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    //歌曲的歌手名：MediaStore.Audio.Media.ARTIST
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    //歌曲文件的路径：MediaStore.Audio.Media.DATA
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    //歌曲的总播放时长：MediaStore.Audio.Media.DURATION
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    //歌曲的专辑ID
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    //歌曲文件的大小：MediaStore.Audio.Media.SIZE
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    if(duration>=1000*60)
                        localList.add(new LocalMusic(tilte,artist,url,album_id,false));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null)
                cursor.close();
        }
    }

    private void initPlayMusic(){
        Total=0;
        for(LocalMusic localMusic:localList){
            playlist.add(localMusic);
            Total++;
        }
    }

    private void AddtoLove(int position){
        boolean flag=false;
        List<LoveMusic> musics= DataSupport.findAll(LoveMusic.class);//获得数据库“收藏的音乐”表
        for(LoveMusic music:musics){//判断被点击添加的音乐是否已被收藏
            if(music.getUrl().equals(localList.get(position).getUrl()))
                flag=true;
        }
        if(flag==true)//已被收藏
            Toast.makeText(getActivity(),"已在我的音乐中",Toast.LENGTH_SHORT).show();
        else {//未被收藏，执行收藏操作
            LoveMusic loveMusic=new LoveMusic();
            //添加音乐信息
            LocalMusic localMusic=localList.get(position);
            loveMusic.setMusicSinger(localMusic.getMusicSinger());
            loveMusic.setMusicName(localMusic.getMusicName());
            loveMusic.setUrl(localMusic.getUrl());
            loveMusic.setAlbum_id(localMusic.getAlbum_id());
            //保存
            loveMusic.save();
            //通过本地广播通知“收藏音乐”列表刷新音乐数据
            Intent intent1=new Intent();
            intent1.setAction("LoveMusicChange");
            localBroadcastManager.sendBroadcast(intent1);
            Toast.makeText(getActivity(),"添加成功",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("Life","Local_visible");
        } else {
            Log.d("Life","Local_invisible");
        }
    }

    public class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("MusicUrl")){
                String url = intent.getStringExtra("url");
                adapter.setDefSelect(url);
            }
            if(action.equals("Delete_All")){
                adapter.setDefSelect(null);
            }
            if(action.equals("SongChange")){
                adapter.setDefSelect(playlist.get(Position_Play).getUrl());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FragmentLife","onDestroy");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("FragmentLife","onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FragmentLife","onCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("FragmentLife","onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("FragmentLife","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FragmentLife","onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FragmentLife","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("FragmentLife","onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("FragmentLife","onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("FragmentLife","onDetach");
    }
}
