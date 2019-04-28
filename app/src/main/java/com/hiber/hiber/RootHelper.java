package com.hiber.hiber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.widget.Toast;

import com.hiber.bean.SkipBean;
import com.hiber.tools.Lgg;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by qianli.ma on 2018/7/24 0024.
 */
public class RootHelper {

    /**
     * kill app
     */
    protected static void kill() {
        Process.killProcess(Process.myPid());
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    protected static void toast(Context context, String tip, int duration) {
        show(context, tip, duration);
    }

    /**
     * @param context  环境
     * @param tip      提示
     * @param duration 时长
     */
    @SuppressLint("ShowToast")
    private static void show(Context context, String tip, int duration) {
        String threadName = Thread.currentThread().getName();
        if (threadName.equalsIgnoreCase("main")) {
            setToastDuration(Toast.makeText(context, tip, Toast.LENGTH_LONG), duration);
        } else {
            Activity activity = (Activity) context;
            activity.runOnUiThread(() -> setToastDuration(Toast.makeText(activity, tip, Toast.LENGTH_LONG), duration));
        }
    }

    /**
     * 开启时长
     *
     * @param toast    吐司
     * @param duration 时长
     */
    private static void setToastDuration(Toast toast, int duration) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 2000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, duration);
    }

    /**
     * 跳转
     *
     * @param clazz           目标
     * @param isSingleTop     独立任务栈
     * @param isFinish        结束当前
     * @param overridepedding F:消除转场闪烁 T:保留转场闪烁
     * @param delay           延迟
     */
    @Deprecated
    private static void toActivity(final Activity activity,// 上下文
                                   final Class<?> clazz,// 目标
                                   final boolean isSingleTop,// 独立任务栈
                                   final boolean isFinish,// 结束当前
                                   boolean overridepedding, // 转场
                                   final int delay, // 延迟
                                   final SkipBean skipBean // 是否传递数据

    ) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Intent intent = new Intent(activity, clazz);
                        // 传递序列化
                        if (skipBean != null) {
                            intent.putExtra(RootMAActivity.INTENT_NAME, skipBean);
                        }
                        // 独立任务栈
                        if (isSingleTop) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        }
                        // 启动
                        activity.startActivity(intent);
                        // 转场(务必在启动后才可调用)
                        if (!overridepedding) {
                            activity.overridePendingTransition(0, 0);
                        }
                        // 结束当前(务必在启动后才可调用)
                        if (isFinish) {
                            activity.finish();
                        }
                        Lgg.t(Lgg.TAG).ii("RootMAActivity:toActivity(): " + clazz.getSimpleName());
                    });
                }
            } catch (Exception e) {
                Lgg.t(Lgg.TAG).ee("RootMAActivity:toActivity():error: " + e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }

    /**
     * 跳转(隐式)
     *
     * @param activity        上下文
     * @param action          目标
     * @param isSingleTop     独立任务栈
     * @param isFinish        结束当前
     * @param overridepedding F:消除转场闪烁 T:保留转场闪烁
     * @param delay           延迟
     */
    protected static void toActivityImplicit(final Activity activity,// 上下文
                                             final String action,// 目标
                                             final boolean isSingleTop,// 独立任务栈
                                             final boolean isFinish,// 结束当前
                                             boolean overridepedding, // 转场
                                             final int delay, // 延迟
                                             final SkipBean skipBean // 是否传递数据
    ) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Intent intent = new Intent();
                        intent.setAction(action);
                        // 传递序列化
                        if (skipBean != null) {
                            intent.putExtra(RootMAActivity.INTENT_NAME, skipBean);
                        }
                        // 独立任务栈
                        if (isSingleTop) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        }
                        // 启动
                        activity.startActivity(intent);
                        // 转场(务必在启动后才可调用)
                        if (!overridepedding) {
                            activity.overridePendingTransition(0, 0);
                        }
                        // 结束当前(务必在启动后才可调用)
                        if (isFinish) {
                            activity.finish();
                        }
                        Lgg.t(Lgg.TAG).ii("RootMAActivity:toActivity() Implicit: " + action);
                    });
                }
            } catch (Exception e) {
                Lgg.t(Lgg.TAG).ee("RootMAActivity:toActivity():error: " + e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }


}
