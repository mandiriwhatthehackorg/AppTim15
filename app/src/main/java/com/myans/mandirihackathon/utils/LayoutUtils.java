package com.myans.mandirihackathon.utils;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

public class LayoutUtils {


    public static GridLayoutManager createGridLayoutManager(Context context, int orientation, int spanCount) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(
                context,
                spanCount,
                orientation,
                false);

        return gridLayoutManager;
    }

    public static LinearLayoutManager createLinearLayoutManager(Context context, int orientation) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, orientation, false);
        return linearLayoutManager;
    }



}
