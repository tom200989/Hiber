package com.hiber.hiber;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.hiber.cons.Cons;
import com.hiber.tools.CrashHanlder;
import com.hiber.tools.Lgg;

/*
 * Created by qianli.ma on 2019/3/5 0005.
 */
public class CrashHelper {

    /**
     * 设置全局异常埋点
     *
     * @param app 环境
     */
    public void setCrash(Application app) {
        new CrashHanlder(app) {
            @Override
            public void catchCrash(Context context, Thread thread, Throwable ex) {
                ex.printStackTrace();
                // action设置错误
                if (ex.getMessage().contains("No Activity found to handle Intent")) {
                    String acError = app.getString(R.string.ACTION_ERR);
                    String des = String.format(acError, getTargetACAction(ex));
                    Lgg.t(Cons.TAG).ee(des);
                    toast(app, des);
                } else {
                    throw new RuntimeException();
                }
            }
        };
    }

    /**
     * 吐司
     *
     * @param app    主体
     * @param cotent 内容
     */
    private void toast(Application app, String cotent) {
        Toast.makeText(app, cotent, Toast.LENGTH_LONG).show();
    }

    /**
     * 从错误信息中抽取出出错的目标action
     *
     * @param ex 错误信息体
     */
    private String getTargetACAction(Throwable ex) {
        String message = ex.getMessage();
        int start = message.indexOf("{");
        int last = message.indexOf("}");
        String sub = message.substring(start + 1, last);
        return sub.replace("act=", "")// 裁剪[act=]
                       .replace("(has extras)", "")// 裁剪[(has extras)]
                       .replace(" ", "");// 裁剪空格
    }
}
