package com.example.administrator.mymusic2.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.adapter.PlayListAdapter;
import com.example.administrator.mymusic2.util.UnitUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/9.
 */

public class DialogFragment_playMusic extends DialogFragment{
    private View view;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private MusicReceiver receiver;
    private ImageView image_mode_play;
    private TextView text_mode_play,add_warning,play_num;
    private LinearLayout layout_getHeight;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    static public List<LocalMusic> playlist=new ArrayList<>();
    static public int Mode_Play=1000;
    static public int Position_Play=-1;
    static public int Total=0;
    private PlayListAdapter adapter;

    private int height=0;
    private int offset=0;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.CustomDatePickerDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // must be called before set content
        dialog.setContentView(R.layout.dialog_play);
        dialog.setCanceledOnTouchOutside(true);

        Log.d("DialogTAG","onCreateDialog");

        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        return dialog;
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.dialog_play, null);

        Log.d("DialogTAG","onCreateView");

        LinearLayout delete_all=(LinearLayout)view.findViewById(R.id.delete_all);
        final LinearLayout play_mode=(LinearLayout)view.findViewById(R.id.layout_mode_play);
        image_mode_play=(ImageView)view.findViewById(R.id.image_mode_play);
        text_mode_play=(TextView)view.findViewById(R.id.text_mode_play);
        add_warning=(TextView)view.findViewById(R.id.add_warning);
        layout_getHeight=(LinearLayout)view.findViewById(R.id.layout_getheight);
        play_num=(TextView)view.findViewById(R.id.play_num);
        if(Total==0)
            add_warning.setVisibility(View.VISIBLE);
        else
            add_warning.setVisibility(View.GONE);
        recyclerView=(RecyclerView)view.findViewById(R.id.recyclerView_played);
        linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter=new PlayListAdapter(playlist);
        if(Total>0&&Position_Play!=-1){
            adapter.setDefSelect(playlist.get(Position_Play).getUrl(),Position_Play);
        }
        recyclerView.setAdapter(adapter);

        intentFilter=new IntentFilter();
        intentFilter.addAction("Shake");
        receiver=new MusicReceiver();
        localBroadcastManager=LocalBroadcastManager.getInstance(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,intentFilter);

        setMode_Play();

        delete_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlist.clear();
                adapter.notifyDataSetChanged();
                Position_Play=-1;
                Total=0;

                add_warning.setVisibility(View.VISIBLE);

                Intent intent=new Intent();
                intent.setAction("Delete_All");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMode_Play();
            }
        });
        adapter.setOnRecyclerViewListener(new PlayListAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()){
                    case R.id.delete:
                        Log.d("PTAG","position:"+position);
                        if(position!=-1){
                            playlist.remove(position);
                            if(position==Position_Play){
                                if(Total>1){
                                    if(position==Total-1)
                                        Position_Play--;
                                    Intent intent=new Intent();
                                    intent.setAction("Delete");
                                    localBroadcastManager.sendBroadcast(intent);
                                }else {
                                    Position_Play--;
                                    add_warning.setVisibility(View.VISIBLE);
                                    Intent intent=new Intent();
                                    intent.setAction("Delete_All");
                                    localBroadcastManager.sendBroadcast(intent);
                                }
                            }else if(position<Position_Play){
                                Position_Play--;
                            }
                            Total--;
                            if(Position_Play!=-1)
                                adapter.setDefSelect(playlist.get(Position_Play).getUrl(),Position_Play);
                            else{
                                adapter.setDefSelect(null,-1);
                                if(Total==0)
                                    add_warning.setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                            setMode_Play();
                        }
                        break;
                    case R.id.layout_play:
                        Intent intent=new Intent();
                        intent.putExtra("url",playlist.get(position).getUrl());
                        intent.putExtra("song",playlist.get(position).getMusicName());
                        intent.putExtra("singer",playlist.get(position).getMusicSinger());
                        intent.setAction("MusicUrl");
                        adapter.setDefSelect(playlist.get(position).getUrl(),position);
                        adapter.notifyDataSetChanged();
                        Log.d("TAG","ChoosePosition:"+position+"");
                        Position_Play=position;
                        localBroadcastManager.sendBroadcast(intent);
                        break;
                }
            }
        });
        return view;
    }
    private void changeMode_Play(){
        Intent intent=new Intent();
        intent.setAction("ModeChange");
        localBroadcastManager.sendBroadcast(intent);
        if(Mode_Play==1000){
            image_mode_play.setImageResource(R.drawable.music_cycle_one);
            text_mode_play.setText(getResources().getString(R.string.cycle_one)+"("+Total+")");
            Mode_Play=1001;
        }else if(Mode_Play==1001){
            image_mode_play.setImageResource(R.drawable.music_cycle_random);
            text_mode_play.setText(getResources().getString(R.string.cycle_random)+"("+Total+")");
            Mode_Play=1002;
        }else if(Mode_Play==1002){
            image_mode_play.setImageResource(R.drawable.music_cycle_all);
            text_mode_play.setText(getResources().getString(R.string.cycle_all)+"("+Total+")");
            Mode_Play=1000;
        }
    }
    private void setMode_Play(){
        if(Mode_Play==1000){
            image_mode_play.setImageResource(R.drawable.music_cycle_all);
            text_mode_play.setText(getResources().getString(R.string.cycle_all)+"("+Total+")");
        }else if(Mode_Play==1001){
            image_mode_play.setImageResource(R.drawable.music_cycle_one);
            text_mode_play.setText(getResources().getString(R.string.cycle_one)+"("+Total+")");
        }else if(Mode_Play==1002){
            image_mode_play.setImageResource(R.drawable.music_cycle_random);
            text_mode_play.setText(getResources().getString(R.string.cycle_random)+"("+Total+")");
        }
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

    public class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action=="Shake"){
                Log.d("Dialog","shake");
                adapter.setDefSelect(playlist.get(Position_Play).getUrl(),Position_Play);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void onStart() {
        super.onStart();
        getDialog().getWindow().setWindowAnimations(
                R.style.DialogAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height=outMetrics.widthPixels;
        getDialog().getWindow().
                setLayout(RecyclerView.LayoutParams.MATCH_PARENT,height);
        height=height-80;//Dialog总高度减去标题栏高度
        if(Total>0){
            int itemHeight=getViewHeight(recyclerView)/Total/2;//获得RecyclerViewItem的高度
            offset=height/2-itemHeight;
        }
        Log.d("DialogTag","offset:"+offset);

        linearLayoutManager.scrollToPositionWithOffset(Position_Play,offset);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        Log.d("Dialog","DialogDestroy");
    }
}
