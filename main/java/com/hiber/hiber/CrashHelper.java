package com.hiber.hiber;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.hiber.cons.Cons;
import com.hiber.tools.CrashHanlder;
import com.hiber.tools.Lgg;
import com.hiber.ui.ActivityNotFound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
                // action设置错误
                String errTrace = getExceptionTrace(ex);
                if (errTrace.contains("No Activity found to handle Intent")) {
                    // 过滤信息
                    String acError = app.getString(R.string.ACTION_ERR);
                    String des = String.format(acError, getTargetACAction(errTrace));
                    Lgg.t(Cons.TAG).ee("CrashHelper--> " + des);
                    // 显示Activity窗体
                    showErrWindow(context, des);
                } else {
                    showErrWindow(context, errTrace);
                }
                
                // 系统打印(无效--> 因为printStream已经被getExceptionTrace()消费
                // ex.printStackTrace();
            }
        };
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 获取异常的trace信息
     *
     * @param ex 异常实体
     * @return trace字符信息
     */
    private String getExceptionTrace(Throwable ex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ex.printStackTrace(ps);
        String errTrace = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        try {
            baos.close();
        } catch (IOException e) {
            Lgg.t(Cons.TAG).ee("CrashHelper--> close error:" + e.getMessage());
        }
        return errTrace;
    }

    /**
     * 从错误信息中抽取出出错的目标action
     *
     * @param errMsg 错误信息
     */
    private String getTargetACAction(String errMsg) {
        int start = errMsg.indexOf("{");
        int last = errMsg.indexOf("}");
        String sub = errMsg.substring(start + 1, last);
        return sub.replace("act=", "")// 裁剪[act=]
                       .replace("(has extras)", "")// 裁剪[(has extras)]
                       .replace(" ", "");// 裁剪空格
    }

    /**
     * 显示窗体
     *
     * @param context 环境
     * @param des     描述
     */
    private void showErrWindow(Context context, String des) {
        Intent intent = new Intent(context, ActivityNotFound.class);
        intent.putExtra(ActivityNotFound.ERROR_INTENT, des);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 独立的任务栈(一定要设)
        context.startActivity(intent);
        // (此处可能会导致先前的log打印停止,出现控制没有Log的信息)
        System.exit(0);// 关闭已奔溃的app进程

    }
}
