package com.example.administrator.mymusic2.lrcView;

import com.example.administrator.mymusic2.lrcView.LrcRow;

/**
 * created by ： bifan-wei
 *  歌词拖动监听
 */

public interface ILrcViewListener {
    void onSeek(LrcRow currentLrcRow, long CurrentSelectedRowTime);
    void onClick();
}
