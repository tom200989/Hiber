package com.hiber.bean;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

/*
 * Created by Administrator on 2021/02/025.
 * 该类是用于适配状态栏渐变色
 */
public class GradientBean {

    private GradientDrawable.Orientation orientation;
    private int[] colors;
    private Drawable drawable;

    public Drawable getDrawable() {
        return drawable;
    }

    public GradientBean setDrawable(Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public GradientDrawable.Orientation getOrientation() {
        return orientation;
    }

    public GradientBean setOrientation(GradientDrawable.Orientation orientation) {
        this.orientation = orientation;
        return this;
    }

    public int[] getColors() {
        return colors;
    }

    public GradientBean setColors(int[] colors) {
        this.colors = colors;
        return this;
    }
}
