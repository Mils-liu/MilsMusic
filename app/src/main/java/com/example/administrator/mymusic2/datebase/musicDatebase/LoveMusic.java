package com.example.administrator.mymusic2.datebase.musicDatebase;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2018/3/15.
 */

public class LoveMusic extends DataSupport{
    private String musicName;
    private String musicSinger;
    private String url;
    private int album_id;

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicSinger() {
        return musicSinger;
    }

    public void setMusicSinger(String musicSinger) {
        this.musicSinger = musicSinger;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
