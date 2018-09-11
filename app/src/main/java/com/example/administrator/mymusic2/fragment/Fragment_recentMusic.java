package com.example.administrator.mymusic2.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.mymusic2.R;

/**
 * Created by Administrator on 2018/3/8.
 */

public class Fragment_recentMusic extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local,container,false);
        super.onCreate(savedInstanceState);
        return view;
    }
}
