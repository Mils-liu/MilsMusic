package com.example.administrator.mymusic2.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.mymusic2.datebase.musicDatebase.LocalMusic;
import com.example.administrator.mymusic2.R;

import java.util.List;

/**
 * Created by Administrator on 2018/3/8.
 */

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.ViewHolder>{
    private List<LocalMusic> mLocalMusicList;
    private OnRecyclerViewListener onRecyclerViewListener;
    private String url=null;

    public interface OnRecyclerViewListener{
        void onItemClick(View view,int position);
    }
    public void setOnRecyclerViewListener(OnRecyclerViewListener mOnItemClickListener){
        this.onRecyclerViewListener=mOnItemClickListener;
    }
    public void setDefSelect(String url) {
        this.url=url;
        notifyDataSetChanged();
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView musicAdd,musicAdd_love,icon;
        TextView musicSinger,musicName;
        LinearLayout layout_local;
        public ViewHolder(View view){
            super(view);
            musicAdd=(ImageView)view.findViewById(R.id.add);
            musicAdd_love=(ImageView) view.findViewById(R.id.add_love);
            musicSinger=(TextView)view.findViewById(R.id.music_singer);
            musicName=(TextView)view.findViewById(R.id.music_name);
            layout_local=(LinearLayout)view.findViewById(R.id.layout_local);
        }
    }
    public LocalMusicAdapter(List<LocalMusic> LocalMusicList){
        mLocalMusicList=LocalMusicList;
    }
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.localmusic_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.setIsRecyclable(false);//取消复用
        return holder;
    }
    public void onBindViewHolder(final ViewHolder holder,int position){
        LocalMusic localMusic=mLocalMusicList.get(position);
        holder.musicSinger.setText(localMusic.getMusicSinger());
        holder.musicName.setText(localMusic.getMusicName());
        Log.d("TAG","AdapterPosition:"+position);
        if(localMusic.getUrl().equals(url)){
            holder.musicAdd.setImageResource(R.drawable.icon);
        }

        if(onRecyclerViewListener!=null){
            holder.musicAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.musicAdd,position);
                }
            });
            holder.layout_local.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.layout_local,position);
                }
            });
            holder.musicAdd_love.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.musicAdd_love,position);
                }
            });
        }
    }
    public int getItemCount(){
        return mLocalMusicList.size();
    }
}
