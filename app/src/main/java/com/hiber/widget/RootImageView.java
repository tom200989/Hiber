package com.hiber.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * Created by qianli.ma on 2019/9/27 0027.
 */
@SuppressLint("AppCompatCustomView")
public class RootImageView extends ImageView {

    /**
     * 是否关闭硬件加速
     */
    public static boolean HARD_ACCERATE_CLOSE = true;

    public RootImageView(Context context) {
        this(context, null, 0);
    }

    public RootImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (HARD_ACCERATE_CLOSE) {
            setLayerType(1, (Paint) null);
        }
    }
}
