package com.example.administrator.mymusic2.util;

import android.content.Context;

/**
 * Created by Administrator on 2018/5/17.
 */

public class DisplayUtils {

    public static int  sp2px(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }
}
