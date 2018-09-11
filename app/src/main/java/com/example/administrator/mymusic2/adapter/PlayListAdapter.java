package com.example.administrator.mymusic2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.R;

import java.util.List;

/**
 * Created by Administrator on 2018/3/9.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder>{
    private List<LocalMusic> mPlayMusicList;
    private PlayListAdapter.OnRecyclerViewListener onRecyclerViewListener;
    private int defItem=-1;
    private String url=null;
    private Context mContext;

    public void setDefSelect(String url,int position) {
        this.url=url;
        this.defItem=position;
    }

    public interface OnRecyclerViewListener{
        void onItemClick(View view,int position);
    }
    public void setOnRecyclerViewListener(PlayListAdapter.OnRecyclerViewListener mOnItemClickListener){
        this.onRecyclerViewListener=mOnItemClickListener;
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView play_num,play_music_name,play_music_singer;
        ImageView delete,icon;
        LinearLayout layout_play;
        public ViewHolder(View view){
            super(view);
            play_num=(TextView)view.findViewById(R.id.play_num);
            play_music_name=(TextView)view.findViewById(R.id.play_music_name);
            play_music_singer=(TextView)view.findViewById(R.id.play_music_singer);
            delete=(ImageView)view.findViewById(R.id.delete);
            icon=(ImageView)view.findViewById(R.id.icon_change);
            layout_play=(LinearLayout)view.findViewById(R.id.layout_play);
        }
    }
    public PlayListAdapter(List<LocalMusic> PlayMusicList){
        mPlayMusicList=PlayMusicList;
        Log.d("DialogTAG","initAdapter");
    }
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item,parent,false);
        mContext=parent.getContext();
        final PlayListAdapter.ViewHolder holder=new PlayListAdapter.ViewHolder(view);
        holder.setIsRecyclable(false);
        return holder;
    }
    public void onBindViewHolder(final PlayListAdapter.ViewHolder holder, int position){
        LocalMusic playedMusic=mPlayMusicList.get(position);
        holder.play_num.setText((position+1)+"");
        holder.play_music_singer.setText(playedMusic.getMusicSinger());
        holder.play_music_name.setText(playedMusic.getMusicName());

        if((playedMusic.getUrl().equals(url))&&(position==defItem)){
            holder.play_music_name.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.play_music_singer.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.icon.setVisibility(View.VISIBLE);
            holder.play_num.setVisibility(View.GONE);
        }

        if(onRecyclerViewListener!=null){
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.delete,position);
                }
            });
            holder.layout_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.layout_play,position);
                }
            });
        }
    }
    public int getItemCount(){
        return mPlayMusicList.size();
    }
}
