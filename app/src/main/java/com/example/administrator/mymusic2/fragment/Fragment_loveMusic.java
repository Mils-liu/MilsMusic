package com.example.administrator.mymusic2.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.adapter.LoveMusicAdapter;
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

public class Fragment_loveMusic extends Fragment{
    private List<LoveMusic> loveList=new ArrayList<>();
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private MusicReceiver receiver;
    private LoveMusicAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_love,container,false);

        initLoveMusic();
        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.recyclerView_love);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter=new LoveMusicAdapter(loveList);
        recyclerView.setAdapter(adapter);

        intentFilter=new IntentFilter();
        intentFilter.addAction("LoveMusicChange");
        intentFilter.addAction("MusicUrl");
        intentFilter.addAction("Delete_All");
        intentFilter.addAction("SongChange");
        receiver=new MusicReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,intentFilter);

        localBroadcastManager=LocalBroadcastManager.getInstance(getActivity());

        adapter.setOnRecyclerViewListener(new LoveMusicAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()){
                    case R.id.love_add:
                        LoveMusic loveMusic=loveList.get(position);
                        playlist.add(new LocalMusic(loveMusic.getMusicName(),
                                loveMusic.getMusicSinger(),loveMusic.getUrl(),loveMusic.getAlbum_id(),false));
                        Total++;
                        Toast.makeText(getActivity(),"已添加到下一首播放",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.layout_love:
                        Intent intent=new Intent();
                        intent.putExtra("url",loveList.get(position).getUrl());
                        intent.putExtra("song",loveList.get(position).getMusicName());
                        intent.putExtra("singer",loveList.get(position).getMusicSinger());
                        intent.setAction("MusicUrl");
                        Position_Play=position;
                        localBroadcastManager.sendBroadcast(intent);
                        adapter.setDefSelect(loveList.get(position).getUrl());

                        playlist.clear();
                        initPlayMusic();
                        Position_Play=position;
                        break;
                    case R.id.delete_love:
                        Toast.makeText(getActivity(),"删除成功",Toast.LENGTH_SHORT).show();
                        DataSupport.deleteAll(LoveMusic.class,"url=?",loveList.get(position).getUrl());
                        loveList.remove(position);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });
        return view;
    }
    private void initLoveMusic(){
        List<LoveMusic> musics= DataSupport.findAll(LoveMusic.class);
        loveList.clear();
        for(LoveMusic music:musics){
            LoveMusic loveMusic=new LoveMusic();
            loveMusic.setMusicName(music.getMusicName());
            loveMusic.setMusicSinger(music.getMusicSinger());
            loveMusic.setUrl(music.getUrl());
            loveList.add(loveMusic);
        }
    }
    private void initPlayMusic(){
        Total=0;
        List<LoveMusic> musics= DataSupport.findAll(LoveMusic.class);
        for(LoveMusic music:musics){
            playlist.add(new LocalMusic(music.getMusicName(), music.getMusicSinger(),music.getUrl(),music.getAlbum_id(),false));
            Total++;
        }
    }

    public class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("LoveMusicChange")){
                initLoveMusic();
                adapter.notifyDataSetChanged();
            }
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }
}
