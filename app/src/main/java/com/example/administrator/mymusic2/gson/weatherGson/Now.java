package com.example.administrator.mymusic2.gson.weatherGson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/12/5.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
        public String code;
    }
}
