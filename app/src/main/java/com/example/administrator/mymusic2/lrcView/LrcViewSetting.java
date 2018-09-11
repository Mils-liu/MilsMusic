package com.example.administrator.mymusic2.lrcView;

import android.graphics.Color;

/**
 * created by ： bifan-wei
 */

public class LrcViewSetting {

    public int LinePadding = 40;// in px
    public int RowPaddingLeft = 10;//行左padding
    public int RowPaddingRight = 10;//行右padding

    public int TimeTextPaddingRight = 10;// 时间文字距右距离
    public int TimeTextPaddingLeft = 10;// 时间文字距左距离
    public int SelectLinePaddingTextTop = 10;// 时间线距离时间文字
    public int SelectLinePaddingTextLeft = 10;// 时间线距离时间文字
    public int SelectLinePaddingLeft = 10;// 时间线距离左边
    public int SelectLinePaddingRight= 10;// 时间线距离右边

    public int NormalRowTextSize = 47;// in px
    public int HeightLightRowTextSize = 67;// in px
    public int TrySelectRowTextSize = 67;// in px
    public int MessagePaintTextSize = 27;// in px
    public int SelectLineTextSize = 27;// in px
    public int TimeTextSize = 27;// in px
    public int TriangleWidth = 0;// in px

    public int NormalRowColor = Color.WHITE;// 正常行字体颜色
    public int HeightRowColor = Color.YELLOW;// 高亮行字体颜色
    public int TrySelectRowColor = Color.GRAY;// 尝试选择行字体颜色
    public int MessageColor = Color.YELLOW;// 信息字体颜色
    public int SelectLineColor = Color.GRAY;// 选择线颜色
    public int TimeTextColor = Color.GRAY;// 选择线颜色
    public Boolean ShowTimeText = true;
    public Boolean ShowTriangle = true;
    public Boolean ShowSelectLine = true;


    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setLinePadding(int linePadding) {
        LinePadding = linePadding;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setRowPaddingLeft(int rowPaddingLeft) {
        RowPaddingLeft = rowPaddingLeft;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setRowPaddingRight(int rowPaddingRight) {
        RowPaddingRight = rowPaddingRight;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTimeTextPaddingRight(int timeTextPaddingRight) {
        TimeTextPaddingRight = timeTextPaddingRight;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTimeTextPaddingLeft(int timeTextPaddingLeft) {
        TimeTextPaddingLeft = timeTextPaddingLeft;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLinePaddingTextTop(int selectLinePaddingTextTop) {
        SelectLinePaddingTextTop = selectLinePaddingTextTop;
        return this;
    }



    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLinePaddingTextLeft(int selectLinePaddingTextLeft) {
        SelectLinePaddingTextLeft = selectLinePaddingTextLeft;
        return this;
    }
    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLinePaddingLeft(int selectLinePaddingLeft) {
        SelectLinePaddingLeft= selectLinePaddingLeft;
        return this;
    }


    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLinePaddingRight(int selectLinePaddingRight) {
        SelectLinePaddingRight = selectLinePaddingRight;
        return this;
    }
    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTriangleWidth(int triangleWidth) {
        TriangleWidth = triangleWidth;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setNormalRowTextSize(int normalRowTextSize) {
        NormalRowTextSize = normalRowTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setHeightLightRowTextSize(int heightLightRowTextSize) {
        HeightLightRowTextSize = heightLightRowTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTrySelectRowTextSize(int trySelectRowTextSize) {
        TrySelectRowTextSize = trySelectRowTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setMessagePaintTextSize(int messagePaintTextSize) {
        MessagePaintTextSize = messagePaintTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLineTextSize(int selectLineTextSize) {
        SelectLineTextSize = selectLineTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTimeTextSize(int timeTextSize) {
        TimeTextSize = timeTextSize;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setNormalRowColor(int normalRowColor) {
        NormalRowColor = normalRowColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setHeightRowColor(int heightRowColor) {
        HeightRowColor = heightRowColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTrySelectRowColor(int trySelectRowColor) {
        TrySelectRowColor = trySelectRowColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setMessageColor(int messageColor) {
        MessageColor = messageColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setSelectLineColor(int selectLineColor) {
        SelectLineColor = selectLineColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setTimeTextColor(int timeTextColor) {
        TimeTextColor = timeTextColor;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setShowTimeText(Boolean showTimeText) {
        ShowTimeText = showTimeText;
        return this;
    }
    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setShowTriangle(Boolean showTriangle) {
        ShowTriangle = showTriangle;
        return this;
    }

    public com.example.administrator.mymusic2.lrcView.LrcViewSetting setShowSelectLine(Boolean showSelectLine) {
        ShowSelectLine = showSelectLine;
        return this;
    }
}
