package com.hiber.hiber;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiber.cons.Cons;
import com.hiber.tools.Lgg;
import com.hiber.tools.backhandler.FragmentBackHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by qianli.ma on 2018/7/23 0023.
 */

public abstract class RootFrag extends Fragment implements FragmentBackHandler {

    private View inflateView;
    private int layoutId;
    public FragmentActivity activity;
    public Unbinder unbinder;

    /*
     * fragment缓存: 记录从哪个fragment跳转过来
     * 以解决在permission没有完全通过时(此时Eventbus没有注册), 获取不到上一个跳转过来的fragment的情况
     * 因为当前界面有可能是从其他很多不同的fragment跳转过来的
     */
    public static Class lastFrag;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onAttach()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onCreateView()");
        // 1.填入layoutId
        layoutId = onInflateLayout();
        // 2.填充视图
        inflateView = View.inflate(activity, layoutId, null);
        // 3.绑定butterknife
        unbinder = ButterKnife.bind(this, inflateView);
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":return inflateView");
        // 4.加载完视图后的操作--> 由子类重写
        initViewFinish();
        return inflateView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onResume()");
        if (!EventBus.getDefault().isRegistered(this)) {
            // 4.注册订阅
            EventBus.getDefault().register(this);
            Lgg.t(Cons.TAG).vv(getClass().getSimpleName() + ":eventbus register");
        }
    }
    
    /**
     * 获取其他fragment跳转过来的fragbean
     *
     * @param bean fragbean
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getData(FragBean bean) {
        /*
         * 重要: 移除传输完成的粘性事件
         * 这里为什么要移除？因为在fragment相互跳转时
         * poststicky对象会创建多个, 而且传递的数据都是Fragbean类型
         * 这样会导致往后每个fragment创建的订阅者 @Subcribe(...)
         * 都会接收到前面其他fragment跳转传输的事件
         * 这些事件实际上是与当前fragment无关的, 如果在压力测试下
         * 会造成内存溢出
         *
         * */
        EventBus.getDefault().removeStickyEvent(bean);
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":getData()");
        Object attach = bean.getAttach();
        String whichFragmentStart = bean.getCurrentFragmentClass().getSimpleName();
        String targetFragment = bean.getTargetFragmentClass().getSimpleName();
        Lgg.t(Cons.TAG).vv("whichFragmentStart: " + whichFragmentStart);
        Lgg.t(Cons.TAG).vv("targetFragment: " + targetFragment);
        // 确保现在运行的是目标fragment
        if (getClass().getSimpleName().equalsIgnoreCase(targetFragment)) {
            Lgg.t(Cons.TAG).vv("whichFragmentStart <equal to> targetFragment");
            onNexts(attach, inflateView, whichFragmentStart);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onPause()");
        if (EventBus.getDefault().isRegistered(this) && isReloadData()) {
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":eventbus unregister");
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean onBackPressed() {
        boolean isDispathcherBackPressed = onBackPresss();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onBackPressed()--> isDispathcherBackPressed == " + isDispathcherBackPressed);
        return isDispathcherBackPressed;
    }

    /* -------------------------------------------- abstract -------------------------------------------- */


    /**
     * @return 1.填入layoutId
     */
    public abstract int onInflateLayout();

    /**
     * 2.你的业务逻辑
     *
     * @param yourBean           你的自定义附带对象(请执行强转)
     * @param view               填充视图
     * @param whichFragmentStart 由哪个fragment发起的跳转
     */
    public abstract void onNexts(Object yourBean, View view, String whichFragmentStart);

    /**
     * @return 3.点击返回键
     */
    public abstract boolean onBackPresss();

    /* -------------------------------------------- impl -------------------------------------------- */

    /**
     * 首次初始化视图完成后的操作
     */
    public void initViewFinish() {

    }

    /**
     * 是否在页面恢复时重新拉取数据
     *
     * @return true:默认(T:会触发eventbus注销并在下次重新注册, 间接触发onNexts()的重复执行)
     */
    public boolean isReloadData() {
        return true;
    }

    /* -------------------------------------------- helper -------------------------------------------- */

    /**
     * 跳转fragment
     *
     * @param current        当前
     * @param target         目标
     * @param object         附带
     * @param isTargetReload 是否重载视图
     */
    public void toFrag(Class current, Class target, Object object, boolean isTargetReload) {
        try {
            RootMAActivity activity = (RootMAActivity) getActivity();
            if (activity != null) {
                lastFrag = current;// 保存到缓存
                activity.toFrag(current, target, object, isTargetReload);
            } else {
                Lgg.t(Cons.TAG).ee("RootHiber--> toFrag() error: RootMAActivity is null");
            }
        } catch (Exception e) {
            Lgg.t(Cons.TAG).ee("Rootfrag error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 跳转fragment
     *
     * @param current        当前
     * @param target         目标
     * @param object         附带
     * @param isTargetReload 是否重载视图
     * @param delayMilis     延迟秒数
     */
    public void toFrag(Class current, Class target, Object object, boolean isTargetReload, int delayMilis) {
        try {
            RootMAActivity activity = (RootMAActivity) getActivity();
            if (activity != null) {
                Thread ta = new Thread(() -> {
                    try {
                        Thread.sleep(delayMilis);
                        lastFrag = current;// 保存到缓存
                        activity.runOnUiThread(() -> activity.toFrag(current, target, object, isTargetReload));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                ta.start();

            } else {
                Lgg.t(Cons.TAG).ee("RootHiber--> toFrag() error: RootMAActivity is null");
            }
        } catch (Exception e) {
            Lgg.t(Cons.TAG).ee("Rootfrag error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param target 移除某个fragment
     */
    public void removeFrag(Class target) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.removeFrag(target);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> removeFrag() error: RootMAActivity is null");
        }
    }

    /**
     * 结束当前Activit
     */
    public void finish() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.finishOver();
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> finish() error: RootMAActivity is null");
        }
    }

    /**
     * 杀死APP
     */
    public void kill() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.kill();
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> kill() error: RootMAActivity is null");
        }
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public void toast(String tip, int duration) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toast(tip, duration);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toast() error: RootMAActivity is null");
        }
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public void toast(@StringRes int tip, int duration) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toast(tip, duration);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toast() error: RootMAActivity is null");
        }
    }

    /**
     * 跳转(默认方式)
     *
     * @param context  当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity context, Class<?> clazz, boolean isFinish) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toActivity(context, clazz, isFinish);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 跳转(隐式)
     *
     * @param context  当前环境
     * @param action   目标
     * @param isFinish 是否默认方式
     */
    public void toActivityImplicit(Activity context, String action, boolean isFinish) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toActivityImplicit(context, action, isFinish);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toActivityImplicit() error: RootMAActivity is null");
        }
    }

    /**
     * 跳转(自定义方式)
     *
     * @param activity 当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity activity, Class<?> clazz, boolean isSigleTop, boolean isFinish, boolean overpedding, int delay) {
        RootMAActivity rootMAActivity = (RootMAActivity) getActivity();
        if (rootMAActivity != null) {
            rootMAActivity.toActivity(activity, clazz, isSigleTop, isFinish, overpedding, delay);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 跳转(隐式)
     *
     * @param activity    上下文
     * @param action      目标
     * @param isSigleTop  独立站
     * @param isFinish    是否结束当前
     * @param overpedding F:消除转场闪烁 T:保留转场闪烁
     * @param delay       延迟
     */
    public void toActivityImplicit(Activity activity, String action, boolean isSigleTop, boolean isFinish, boolean overpedding, int delay) {
        RootMAActivity rootMAActivity = (RootMAActivity) getActivity();
        if (rootMAActivity != null) {
            rootMAActivity.toActivityImplicit(activity, action, isSigleTop, isFinish, overpedding, delay);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toActivityImplicit() error: RootMAActivity is null");
        }
    }

    /**
     * @return 获取attach Activity
     */
    public FragmentActivity getActivitys() {
        return activity;
    }

    /**
     * 获取fragment调度器
     *
     * @return fragment调度器
     */
    public FraHelpers getFragmentHelper() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        return activity != null ? activity.getFragmentHelper() : null;
    }
}
