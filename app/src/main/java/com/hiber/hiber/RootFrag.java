package com.hiber.hiber;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiber.cons.Cons;
import com.hiber.tools.Lgg;
import com.hiber.tools.backhandler.FragmentBackHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Object attach;
    private String whichFragmentStart;

    // 权限相关
    private int permissedCode = 0x101;
    private String[] initPermisseds;// 初始化需要申请的权限
    private String[] clickPermisseds;// 点击时需要申请的权限
    public static final int ACTION_DEFAULT = 0;// 默认情况
    public static final int ACTION_DENY = -1;// 拒绝情况
    public static final int ACTION_PASS = 1;// 同意情况
    private HashMap<HashMap<String, Integer>, Integer> permissedMap;
    PermissedListener permissedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onAttach()");
    }

    /**
     * 由外部重写初始化权限
     *
     * @return 需要申请的权限组（可以为null, 为null即不申请)
     */
    public String[] initPermissed() {
        return null;
    }

    /**
     * 点击申请权限
     *
     * @param permissions 需要申请的权限组
     */
    public void clickPermissed(String[] permissions) {
        initPermisseds = null;// 1.该步防止初始化权限重复申请
        clickPermisseds = permissions;
        if (isReqPermissed(clickPermisseds)) {// 2.点击权限申请
            initPermissionMap(clickPermisseds);
            requestPermissions(clickPermisseds, permissedCode);
        } else {
            if (permissedListener != null) {
                permissedListener.permissionResult(true, null);
            }
            clickPermisseds = null;
        }

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
        initPermisseds = initPermissed();
        initPermissionMap(initPermisseds);
        return inflateView;
    }

    /**
     * 初始化权限状态与用户Action
     *
     * @param permissions 需要初始的权限组
     */
    private void initPermissionMap(String[] permissions) {
        if (permissedMap == null) {
            permissedMap = new HashMap<>();
        }
        permissedMap.clear();
        for (String permission : permissions) {
            HashMap<String, Integer> map = new HashMap<>();
            map.put(permission, PackageManager.PERMISSION_DENIED);
            permissedMap.put(map, ACTION_DEFAULT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // 1.初始化检查权限
        if (isReqPermissed(initPermisseds)) {
            // 1.1.处理未申请权限
            handlePermissed(false);
        } else {
            // 1.2.因点击申请时将initPermissions置空--> 初始化权限申请行为将不被重复触发
            // TODO: 2018/12/21 疑问：是否有必要把初始化权限申请全部通过之后的回调提供给开发人员？
            //if (permissedListener != null && initPermisseds != null) {
            //permissedListener.permissionResult(true,null);
            //}

            // 1.2.初始化权限全部通过 || 点击申请即使不通过 --> 也不影响数据初始化
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }

        if (isReqPermissed(clickPermisseds)) {// 触发点击申请权限行为
            handlePermissed(true);
        } else {
            // 点击申请权限全部通过--> 接口回调
            if (permissedListener != null && clickPermisseds != null) {
                // 点击权限全部通过--> 执行你的业务逻辑（如启动照相机）
                permissedListener.permissionResult(true, null);
                clickPermisseds = null;// 防止重新进入该页面重复执行业务逻辑
            }
        }
    }

    /**
     * 处理未申请的权限
     *
     * @param isClickPermissed 是否为点击申请
     */
    private void handlePermissed(boolean isClickPermissed) {
        // 如果用户在同意后到system setting做了取消操作，需要把权限状态更新一下
        checkPermissedState(isClickPermissed ? clickPermisseds : initPermisseds);
        Collection<Integer> values = permissedMap.values();
        if (values.contains(ACTION_DEFAULT)) {// 默认情况(初始化）--> 直接请求权限申请
            requestPermissions(initPermisseds, permissedCode);
        } else {// 非默认情况（用户已通过系统框进行操作）--> 重新封装（记录拒绝的权限状态）
            List<String> denyPermissions = new ArrayList<>();
            Set<HashMap<String, Integer>> hashMaps = permissedMap.keySet();
            for (HashMap<String, Integer> map : hashMaps) {
                Set<Map.Entry<String, Integer>> entries = map.entrySet();
                for (Map.Entry<String, Integer> entry : entries) {
                    if (entry.getValue() == PackageManager.PERMISSION_DENIED) {
                        denyPermissions.add(entry.getKey());
                    }
                    break;
                }
            }
            // 把拒绝的权限接口对外提供
            if (permissedListener != null) {
                permissedListener.permissionResult(false, denyPermissions.toArray(new String[denyPermissions.size()]));
            }

            // 点击申请情况--> 将点击权限设置为空
            if (isClickPermissed) {
                clickPermisseds = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissedState(permissions);// 检查权限当前的最新状态
    }

    /**
     * 检查权限当前的最新状态
     *
     * @param permissions 需要检查的权限组
     * @call onRequestPermissionsResult()
     * @call handlePermissed()
     */
    private void checkPermissedState(String[] permissions) {
        for (String permission : permissions) {

            int permissedState;// 系统返回的权限状态
            int userAction;// 权限框弹出后用户的行为

            // 检查用户操作权限框后的拒绝状态
            boolean isDenied = PermissionChecker.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED;
            permissedState = isDenied ? PackageManager.PERMISSION_DENIED : PackageManager.PERMISSION_GRANTED;
            userAction = isDenied ? ACTION_DENY : ACTION_PASS;
            // 重新赋值更新Map状态
            Set<Map.Entry<HashMap<String, Integer>, Integer>> entries = permissedMap.entrySet();
            for (Map.Entry<HashMap<String, Integer>, Integer> entry : entries) {
                HashMap<String, Integer> key = entry.getKey();
                if (key.containsKey(permission)) {
                    key.put(permission, permissedState);
                    permissedMap.remove(entry);
                    permissedMap.put(key, userAction);
                    break;
                }
            }
        }
    }

    /**
     * 权限监听接口
     */
    interface PermissedListener {
        void permissionResult(boolean isPassAllPermission, String[] denyPermissions);
    }

    /**
     * 设置权限监听接口
     *
     * @param permissedListener 权限监听接口
     */
    public void setPermissedListener(PermissedListener permissedListener) {
        this.permissedListener = permissedListener;
    }


    /**
     * 是否需要执行请求权限操作
     *
     * @param permissions 权限组
     * @return T：需要
     */
    private boolean isReqPermissed(String[] permissions) {
        if (permissions == null) {
            return false;
        }
        // 1.循环检查权限
        List<Integer> permissionInt = new ArrayList<>();
        for (String permission : permissions) {
            permissionInt.add(PermissionChecker.checkSelfPermission(getContext(), permission));
        }

        // 2.判断是否有未通过的权限
        for (Integer permissionDenied : permissionInt) {
            if (permissionDenied == PackageManager.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取其他fragment跳转过来的fragbean
     *
     * @param bean fragbean
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getData(FragBean bean) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":getData()");
        attach = bean.getAttach();
        whichFragmentStart = bean.getCurrentFragmentClass().getSimpleName();
        String targetFragment = bean.getTargetFragmentClass().getSimpleName();
        Lgg.t(Cons.TAG).vv("whichFragmentStart: " + whichFragmentStart);
        Lgg.t(Cons.TAG).vv("targetFragment: " + targetFragment);
        // 确保现在运行的是目标fragment
        if (getClass().getSimpleName().equalsIgnoreCase(targetFragment)) {
            Lgg.t(Cons.TAG).vv("whichFragmentStart <equal to> targetFragment");
            onNexts(attach, inflateView, whichFragmentStart);
            permissedMap.clear();
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
     * 跳转到别的fragment
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

    @Override
    public void onStop() {
        super.onStop();
    }
}
