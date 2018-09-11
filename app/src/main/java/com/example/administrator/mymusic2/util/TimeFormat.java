package com.example.administrator.mymusic2.util;

/**
 * Created by Administrator on 2018/3/8.
 */

public class TimeFormat {
     public static String time(int t){
         String minutestr,secondstr;
         int time=t/1000;
         int minutes=time/60;
         if(minutes<10)
             minutestr="0"+String.valueOf(minutes);
         else
             minutestr=String.valueOf(minutes);
         int seconds=time%60;
         if(seconds<10)
             secondstr="0"+String.valueOf(seconds);
         else
             secondstr=String.valueOf(seconds);
         String times=minutestr+":"+secondstr;
         return times;
     }
}
