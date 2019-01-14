package com.hiber.hiber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.widget.Toast;

import com.hiber.tools.Lgg;

/**
 * Created by qianli.ma on 2018/7/24 0024.
 */

public class RootHelper {

    /**
     * 结束当前activity
     *
     * @param activity 当前activity
     */
    public static void finishOver(Activity activity) {
        activity.finish();
    }

    /**
     * kill app
     */
    public static void kill() {
        Process.killProcess(Process.myPid());
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public static void toast(Context context, String tip, int duration) {
        show(context, tip, duration);
    }

    /**
     * @param context  环境
     * @param tip      提示
     * @param duration 时长
     */
    private static void show(Context context, String tip, int duration) {
        if (duration == 0) {
            duration = 2000;
        }
        String threadName = Thread.currentThread().getName();
        if (threadName.equalsIgnoreCase("main")) {
            Toast.makeText(context, tip, duration).show();
        } else {
            Activity activity = (Activity) context;
            int finalDuration = duration;
            activity.runOnUiThread(() -> Toast.makeText(activity, tip, finalDuration).show());
        }
    }

    /**
     * 跳转(默认方式)
     *
     * @param activity 当前环境
     * @param clazz    目标
     * @param isFinish 是否结束当前
     */
    public static void toActivity(Activity activity, Class<?> clazz, boolean isFinish) {
        toActivity(activity, clazz, true, isFinish, false, 0);
    }

    /**
     * 跳转(隐式)
     *
     * @param activity 当前环境
     * @param action   目标
     * @param isFinish 是否结束当前
     */
    public static void toActivityImplicit(Activity activity, String action, boolean isFinish) {
        toActivityImplicit(activity, action, true, isFinish, false, 0);
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
    public static void toActivity(final Activity activity,// 上下文
                                  final Class<?> clazz,// 目标
                                  final boolean isSingleTop,// 独立任务栈
                                  final boolean isFinish,// 结束当前
                                  boolean overridepedding, // 转场
                                  final int delay) {// 延迟
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Intent intent = new Intent(activity, clazz);
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
    public static void toActivityImplicit(final Activity activity,// 上下文
                                          final String action,// 目标
                                          final boolean isSingleTop,// 独立任务栈
                                          final boolean isFinish,// 结束当前
                                          boolean overridepedding, // 转场
                                          final int delay) {// 延迟
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Intent intent = new Intent();
                        intent.setAction(action);
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
