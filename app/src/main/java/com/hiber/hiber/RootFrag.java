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
     * @return true:默认
     */
    public boolean isReloadData() {
        return true;
    }

    /* -------------------------------------------- helper -------------------------------------------- */

    /**
     * 跳转到别的fragment
     *
     * @param current        当前
     * @param target         目标
     * @param object         附带
     * @param isTargetReload 是否重载视图
     */
    public void toFrag(Class current, Class target, Object object, boolean isTargetReload) {
        try {
            ((RootMAActivity) getActivity()).toFrag(current, target, object, isTargetReload);
        } catch (Exception e) {
            Lgg.t(Cons.TAG).ee("Rootfrag error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param target 移除某个fragment
     */
    public void removeFrag(Class target) {
        ((RootMAActivity) getActivity()).removeFrag(target);
    }

    /**
     * 结束当前Activit
     */
    public void finish() {
        ((RootMAActivity) getActivity()).finishOver();
    }

    /**
     * 杀死APP
     */
    public void kill() {
        ((RootMAActivity) getActivity()).kill();
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public void toast(String tip, int duration) {
        ((RootMAActivity) getActivity()).toast(tip, duration);
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public void toast(@StringRes int tip, int duration) {
        ((RootMAActivity) getActivity()).toast(tip, duration);
    }

    /**
     * 跳转(默认方式)
     *
     * @param context  当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity context, Class<?> clazz, boolean isFinish) {
        ((RootMAActivity) getActivity()).toActivity(context, clazz, isFinish);
    }

    /**
     * 跳转(自定义方式)
     *
     * @param activity 当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity activity, Class<?> clazz, boolean isSigleTop, boolean isFinish, boolean overpedding, int delay) {
        ((RootMAActivity) getActivity()).toActivity(activity, clazz, isSigleTop, isFinish, overpedding, delay);
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
        return ((RootMAActivity) getActivity()).getFragmentHelper();
    }
}
