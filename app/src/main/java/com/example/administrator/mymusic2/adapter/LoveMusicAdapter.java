package com.example.administrator.mymusic2.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.mymusic2.R;
import com.example.administrator.mymusic2.datebase.musicDatebase.LoveMusic;

import java.util.List;

/**
 * Created by Administrator on 2018/3/15.
 */

public class LoveMusicAdapter extends RecyclerView.Adapter<LoveMusicAdapter.ViewHolder>{
    private List<LoveMusic> mLoveMusicList;
    private LoveMusicAdapter.OnRecyclerViewListener onRecyclerViewListener;
    private String url=null;

    public void setDefSelect(String url) {
        this.url=url;
        notifyDataSetChanged();
    }
    public interface OnRecyclerViewListener{
        void onItemClick(View view, int position);
    }
    public void setOnRecyclerViewListener(LoveMusicAdapter.OnRecyclerViewListener mOnItemClickListener){
        this.onRecyclerViewListener=mOnItemClickListener;
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView musicAdd,musicDelete_love;
        TextView musicSinger,musicName;
        LinearLayout layout_Love;
        public ViewHolder(View view){
            super(view);
            musicAdd=(ImageView)view.findViewById(R.id.love_add);
            musicDelete_love=(ImageView) view.findViewById(R.id.delete_love);
            musicSinger=(TextView)view.findViewById(R.id.love_music_singer);
            musicName=(TextView)view.findViewById(R.id.love_music_name);
            layout_Love=(LinearLayout)view.findViewById(R.id.layout_love);
        }
    }
    public LoveMusicAdapter(List<LoveMusic> LoveMusicList){
        mLoveMusicList=LoveMusicList;
    }
    public LoveMusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.lovemusic_item,parent,false);
        final LoveMusicAdapter.ViewHolder holder=new LoveMusicAdapter.ViewHolder(view);
        holder.setIsRecyclable(false);//取消复用
        return holder;
    }
    public void onBindViewHolder(final LoveMusicAdapter.ViewHolder holder, int position){
        LoveMusic loveMusic=mLoveMusicList.get(position);
        holder.musicSinger.setText(loveMusic.getMusicSinger());
        holder.musicName.setText(loveMusic.getMusicName());

        if(loveMusic.getUrl().equals(url)){
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
            holder.layout_Love.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.layout_Love,position);

                }
            });
            holder.musicDelete_love.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();
                    onRecyclerViewListener.onItemClick(holder.musicDelete_love,position);
                }
            });
        }
    }
    public int getItemCount(){
        return mLoveMusicList.size();
    }
}
