package com.example.administrator.mymusic2.datebase.musicDatebase;

/**
 * Created by Administrator on 2018/3/8.
 */

public class LocalMusic {
    private String musicName;
    private String musicSinger;
    private String url;
    private int album_id;
    private Boolean select;

    public LocalMusic(String musicName, String musicSinger, String url, int album_id,Boolean select){
        this.musicName=musicName;
        this.musicSinger=musicSinger;
        this.url=url;
        this.album_id=album_id;
        this.select=select;
    }
    public void setSelect(Boolean select) {this.select = select;}
    public Boolean getSelect() {return select;}
    public String getMusicName(){return musicName;}
    public String getMusicSinger(){return musicSinger;}
    public String getUrl(){return url;}
    public int getAlbum_id() {return album_id;}
}
