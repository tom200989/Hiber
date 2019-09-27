package com.hiber.hiber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Process;
import android.widget.Toast;

import com.hiber.bean.SkipBean;
import com.hiber.cons.Cons;
import com.hiber.tools.Lgg;
import com.hiber.tools.ToastUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by qianli.ma on 2018/7/24 0024.
 */
public class RootHelper {

    private static final String TAG1 = Cons.TAG;
    private static final String TOAST_TAG = Cons.TOAST;

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
     * @param page     由哪个fragment或者Activity弹出
     */
    protected static void toast(Context context, String tip, int duration, Class page) {
        show(context, tip, duration, page);
    }

    /**
     * 吐司定位
     *
     * @param isSystem  是否为系统打印
     * @param whichPage 哪个页面打印的
     * @param content   内容是什么
     */
    private static void toastPos(boolean isSystem, Class whichPage, String content) {
        String type = isSystem ? "系统" : "自定义";
        String frag = whichPage.getSimpleName();
        StringBuilder builder = new StringBuilder();
        builder.append("RootLog\n");
        builder.append("--------------------------- 吐司定位 ---------------------------").append("\n");
        builder.append("Type: ").append(type).append("\n");
        builder.append("page: ").append(frag).append("\n");
        builder.append("content: ").append(content).append("\n");
        builder.append("----------------------------------------------------------------");
        builder.append("\n");
        Lgg.w(TAG1, builder.toString());
        Lgg.w(TOAST_TAG, builder.toString());
    }

    /**
     * @param context  环境
     * @param tip      提示
     * @param duration 时长
     * @param page     由哪个fragment或者Activity弹出
     */
    @SuppressLint("ShowToast")
    private static void show(Context context, String tip, int duration, Class page) {
        String threadName = Thread.currentThread().getName();
        if (threadName.equalsIgnoreCase("main")) {
            if (isNotificationOpen(context)) {// 系统通知开启 -- 使用系统吐司
                setToastSytem(Toast.makeText(context, tip, Toast.LENGTH_LONG), duration);
                if (Lgg.LOG_FLAG == Lgg.SHOW_ALL) {// 外部是否需要关闭定位
                    toastPos(true, page, tip);
                }
            } else {// 否则使用自定义吐司
                ToastUtil.showSelfToast((RootMAActivity) context, tip, duration);
                if (Lgg.LOG_FLAG == Lgg.SHOW_ALL) {// 外部是否需要关闭定位
                    toastPos(false, page, tip);
                }
            }
        } else {
            RootMAActivity activity = (RootMAActivity) context;
            activity.runOnUiThread(() -> {
                if (isNotificationOpen(context)) {// 系统通知开启 -- 使用系统吐司
                    setToastSytem(Toast.makeText(context, tip, Toast.LENGTH_LONG), duration);
                    if (Lgg.LOG_FLAG == Lgg.SHOW_ALL) {// 外部是否需要关闭定位
                        toastPos(true, page, tip);
                    }
                } else {// 否则使用自定义吐司
                    ToastUtil.showSelfToast((RootMAActivity) context, tip, duration);
                    if (Lgg.LOG_FLAG == Lgg.SHOW_ALL) {// 外部是否需要关闭定位
                        toastPos(false, page, tip);
                    }
                }
            });
        }
    }

    /**
     * 开启系统自带吐司并设置时长
     *
     * @param toast    吐司
     * @param duration 时长
     */
    private static void setToastSytem(Toast toast, int duration) {
        toast.show();
        // TOGO 2019/9/3 0003 以下代码备用, 以免有产品需求是要求延长toast时长
        // final Timer timer = new Timer();
        // timer.schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         toast.show();
        //     }
        // }, 0, 7000);
        // new Timer().schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         toast.cancel();
        //         timer.cancel();
        //     }
        // }, duration);
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

    /**
     * 检查通知有没有开启(T:开启)
     */
    private static boolean isNotificationOpen(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// 7.0
            return ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;

            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
                int value = (Integer) opPostNotificationValue.get(Integer.class);
                return (Integer) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) == 0;
            } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException | RuntimeException | ClassNotFoundException ignored) {
                return true;
            }
        } else {
            return true;
        }
    }

}
