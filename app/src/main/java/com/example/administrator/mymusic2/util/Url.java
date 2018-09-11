package com.example.administrator.mymusic2.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2018/4/27.
 */

public class Url {
    static boolean URL_exists=true;
    static boolean checkIfUrlExists(final String URLName) {
        URL_exists=true;
        try {
//设置此类是否应该自动执行 HTTP 重定向（响应代码为 3xx 的请求）。
            HttpURLConnection.setFollowRedirects(false);
//到 URL 所引用的远程对象的连接
            HttpURLConnection con = (HttpURLConnection) new URL(URLName)
                    .openConnection();
/* 设置 URL 请求的方法， GET POST HEAD OPTIONS PUT DELETE TRACE 以上方法之一是合法的，具体取决于协议的限制。*/
            con.setRequestMethod("HEAD");
//从 HTTP 响应消息获取状态码
// LogUtil.e("ryan","head "+con.getResponseCode());
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
                URL_exists=true;
            else
                URL_exists=false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return URL_exists;
    }

    public static boolean checkURL(String url){
        try {
            HttpURLConnection conn=(HttpURLConnection)new URL(url).openConnection();
            int code=conn.getResponseCode();
            System.out.println(">>>>>>>>>>>>>>>> "+code+" <<<<<<<<<<<<<<<<<<");
            if(code!=200){
                return false;
            }else{
                return true;
            }
        } catch (MalformedURLException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
