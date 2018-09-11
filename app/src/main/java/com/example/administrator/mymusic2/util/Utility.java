package com.example.administrator.mymusic2.util;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.mymusic2.datebase.locationDatebase.City;
import com.example.administrator.mymusic2.datebase.locationDatebase.County;
import com.example.administrator.mymusic2.datebase.locationDatebase.Province;

import com.example.administrator.mymusic2.gson.musicGson.Lrc;
import com.example.administrator.mymusic2.gson.weatherGson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/3.
 */

public class Utility {//解析获得的数据
    //解析和处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    Log.d("TAG","province.lenth:"+allProvinces.length());
                    Province province=new Province();
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId){
        //解析和处理服务器返回的市级数据
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    City city=new City();
                    JSONObject cityObject=allCities.getJSONObject(i);
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyResponse(String response,int cityId){
        //解析和处理服务器返回的县级数据
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    County county=new County();
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static Lrc handleLrcResponse(String response){
        try{
            Gson gson=new Gson();
            Lrc lrc=gson.fromJson(response,Lrc.class);
            return lrc;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String handleCoverResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONObject result=jsonObject.getJSONObject("result");
            String cover_result=result.getString("cover").replaceAll("cover","album-cover");
            return cover_result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
