package com.example.administrator.mymusic2.gson.musicGson;

import java.util.List;

/**
 * Created by Administrator on 2018/4/17.
 */

public class Lrc {
    private String count;
    private String code;
    private List<result> result;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<result> getResult() {
        return result;
    }

    public void setResult(List<result> result) {
        this.result = result;
    }
}
