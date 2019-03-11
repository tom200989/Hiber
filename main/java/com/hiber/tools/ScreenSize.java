package com.hiber.tools;

import android.app.Activity;
import android.content.Context;

public class ScreenSize {

    public static SizeBean getSize(Context context) {
        Activity activity = (Activity) context;
        SizeBean sizeBean = new SizeBean();
        sizeBean.width = activity.getWindowManager().getDefaultDisplay().getWidth();
        sizeBean.height = activity.getWindowManager().getDefaultDisplay().getHeight();
        return sizeBean;
    }

    public static class SizeBean {
        public int width;
        public int height;
    }

}
