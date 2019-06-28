package com.hiber.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiber.hiber.R;


public class ToastWidget extends RelativeLayout {

    private LinearLayout ll_toast;// 布局
    private TextView tv_toast;// 内容

    public ToastWidget(Context context) {
        super(context, null);
        View inflate = inflate(context, R.layout.widget_toast, this);
        ll_toast = inflate.findViewById(R.id.ll_toast);
        tv_toast = inflate.findViewById(R.id.tv_toast);
        ll_toast.setBackground(getResources().getDrawable(R.drawable.toast_corner));
    }

    /**
     * 渐变显示
     */
    public ToastWidget setContent(String content) {
        tv_toast.setText(content);
        return this;
    }
}
