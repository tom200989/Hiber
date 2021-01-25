package com.hiber.hiber;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiber.bean.PermissBean;
import com.hiber.bean.SkipBean;
import com.hiber.bean.StringBean;
import com.hiber.bean.SuperBean;
import com.hiber.cons.Cons;
import com.hiber.cons.PERTYPE;
import com.hiber.cons.SUPERMISSION;
import com.hiber.cons.TimerState;
import com.hiber.impl.PermissedListener;
import com.hiber.impl.RootEventListener;
import com.hiber.impl.SuperPermissListener;
import com.hiber.tools.Lgg;
import com.hiber.tools.TimerHelper;
import com.hiber.tools.backhandler.FragmentBackHandler;
import com.hiber.ui.PermissFragment;

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

    /*
     * fragment缓存: 记录从哪个fragment跳转过来
     * 该标记位由开发人员进行调用, 由于在开发当中涉及到很多时机
     * 框架并不能统一记录上一个跳转过来的位置, 其中影响最大的是推送场景
     * 例如从A--> B, 此时切换至后台, 推送到达, 但推送指定的目的是C,
     * 那么lastfrag如果被框架锁死, 则无法知道具体要回退到哪个页面
     * 因此决定由开发人员自行设置
     */
    public static Class lastFrag;

    // 定时器统一管理处
    private static List<TimerHelper> timerList = new ArrayList<>();
    private TimerHelper timerHelper;// 定时器
    public int timer_delay = 0;// 默认延迟时间
    public int timer_period = 3000;// 默认间隙时间
    public TimerState timerState;// 用户可改变的定时器状态:(ON,OFF_ALL,OFF_ALL_KEEP_CURRENT,ON_BUT_OFF_WHEN_PAUSE)

    private static final String TAG = "RootFrag";
    public FragmentActivity activity;
    private Unbinder unbinder;
    private View inflateView;
    private int layoutId;
    protected static String whichFragmentStart;// 由哪个页面跳转过来

    // 权限相关
    private int permissedCode = 0x101;
    private String[] initPermisseds;// 初始化需要申请的权限
    private String[] clickPermisseds;// 点击时需要申请的权限
    private String[] tmp_click_permissions;// 点击时需要申请的权限(临时, 用于超管权限后使用)
    private static final int ACTION_DEFAULT = 0;// 默认情况
    private static final int ACTION_DENY = -1;// 拒绝情况
    private static final int ACTION_PASS = 1;// 同意情况
    private HashMap<HashMap<String, Integer>, Integer> permissedActionMap;// < < 权限 , 权限状态 > , 用户行为 >
    public PermissedListener permissedListener;// 权限申请监听器
    private View permissView;// 权限自定义制图
    private StringBean stringBean;// 权限默认字符内容
    private PermissBean permissbean;
    private boolean isOnResume = true;// 是否允许onResume继续执行(受限于超管页面的操作)

    /**
     * Eventbus 泛型字节码集合
     */
    protected List<Class> eventClazzs = new ArrayList<>();

    /**
     * Eventbus 数据回调监听器集合
     */
    protected List<RootEventListener> eventListeners = new ArrayList<>();


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
        // 4.加载完视图后的操作--> 由子类重写
        initViewFinish(inflateView);
        // 5.外部定义了 [初始化申请超管权限]
        if (requestSUPermission().supermission == SUPERMISSION.INIT & isNeedSuPermiss()) {
            // TODO: 2021/01/022  弹窗
            showPermissFrag(PERTYPE.SUPER, null);
            return inflateView;
        }
        // 6.初始化其他权限
        initOtherPermiss();
        return inflateView;
    }

    /**
     * 初始化其他权限
     */
    private void initOtherPermiss() {
        String[] tempInitPermisseds = initPermissed();
        initPermisseds = tempInitPermisseds == null ? new String[]{} : tempInitPermisseds;
        initPermissedActionMap(initPermisseds);
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":return inflateView & init permissed");
    }

    /**
     * 是否符合申请超管权限条件
     *
     * @return T:需要申请 F:不需要申请
     */
    private boolean isNeedSuPermiss() {
        // TODO: 2021/01/022  
        // Android R以下 - 不需申请
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":initSuPermis(): Android R以下无需申请");
            return false;
        }

        // 已有超管权限 - 不再申请
        if (Environment.isExternalStorageManager()) {
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":initSuPermis(): 当前已有超管权限");
            return false;
        }

        // 没有提供包名 - 无法申请
        String rootPkgName = ((RootMAActivity) activity).packageName;
        if (TextUtils.isEmpty(rootPkgName)) {
            toast("检测到APP申请了超管权限, 但没有提供包名, 请外部提供包名", 5000);
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":initSuPermis(): 检测到APP申请了超管权限, 但没有提供包名, 请外部提供包名");
            return false;
        }

        return true;
    }


    /**
     * 接收自定义的Eventbus数据
     *
     * @param object 多元化数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onRootEvent(Object object) {
        if (!(object instanceof FragBean)) {
            getDataHandler(object);
        }
    }

    /**
     * 接收到Eventbus之后的处理
     *
     * @param object 泛型对象
     */
    protected void getDataHandler(Object object) {
        // 1.检测集合是否为空
        if (eventClazzs != null & eventListeners != null) {
            // 2.检测用户是否需要监听回调
            if (eventClazzs.size() > 0 & eventListeners.size() > 0) {
                for (int i = 0; i < eventClazzs.size(); i++) {
                    // 3.获取到外部指定的泛型
                    Class eventClazz = eventClazzs.get(i);
                    // 4.判断Eventbus接收到的数据是否符合用户传递的类型
                    if (eventClazz.isAssignableFrom(object.getClass())) {
                        // 5.获取监听器
                        RootEventListener listener = eventListeners.get(i);
                        if (listener != null) {
                            // 6.用户是否指定了［仅仅作用域当前界面］的条件
                            if (listener.isCurrentPageEffectOnly()) {/* 指定当前页面才起作用 */
                                // 6.1.检测［当前屏幕的页面］是否等于［接收数据的页面］
                                if (!isHidden()) {
                                    // 6.2.移除stick包
                                    EventBus.getDefault().removeStickyEvent(object);
                                    // 6.3.将数据回调到外部
                                    listener.getData(object);
                                }
                            } else {/* 不指定当前页面才起作用--> 即全部注册的页面均发生作用 */
                                EventBus.getDefault().removeStickyEvent(object);
                                listener.getData(object);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化权限状态与用户Action
     *
     * @param permisseds 需要初始的权限组
     */
    private void initPermissedActionMap(String[] permisseds) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":initPermissedActionMap()");
        if (permissedActionMap == null) {
            permissedActionMap = new HashMap<>();
        }
        permissedActionMap.clear();
        if (permisseds != null && permisseds.length > 0) {
            for (String permission : permisseds) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put(permission, PackageManager.PERMISSION_DENIED);
                permissedActionMap.put(map, ACTION_DEFAULT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOnResume) return;
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onResume()");
        /* 1.初始化检查权限 */
        if (isReqPermissed(initPermisseds)) {
            // 1.1.处理未申请权限
            handlePermissed(false);
        } else {
            // 1.2.因点击申请时将initPermissions置空--> 初始化权限申请行为将不被重复触发
            // TOAT: 2018/12/21 疑问：是否有必要把初始化权限申请全部通过之后的回调提供给开发人员？目前不提供
            //if (permissedListener != null && initPermisseds != null) {
            //permissedListener.permissionResult(true,null);
            //}

            // 当从后台回来 && isReloadData没有设置为true(即不会走onNext) && 当前Fragment处于显示状态的情况下 -- 从这里启动定时器
            /* 注意: 这里加isHidden()的原因是因为防止以下情况:
             * Frag A设置了ON, Frag B设置了OFF_ALL_X
             * 然后从Frag B跳转到Frag A, 然后Frag A切换到后台再Frag A和Frag B会同时执行OnResume()
             * 那么设置了OFF_ALL的会把ON的杀掉, 造成定时器不启动的感觉
             * 所以要利用isHidden()来过滤, 以当前显示给用户的界面为准
             * */
            if (!isReloadData() & EventBus.getDefault().isRegistered(this) & !isHidden()) {
                beginTimer();
            }

            // 1.2.初始化权限全部通过 || 点击申请即使不通过 --> 也不影响数据初始化
            if (!EventBus.getDefault().isRegistered(this)) {
                // stick包存在--> 首次加载--> 执行注册
                EventBus.getDefault().register(this);
            }
        }

        /* 触发点击申请权限行为 */
        if (isReqPermissed(clickPermisseds)) {
            handlePermissed(true);
        } else {
            // 点击申请权限全部通过--> 接口回调
            if (permissedListener != null && clickPermisseds != null && clickPermisseds.length > 0) {
                // TOAT: 此处先清空权限集合是为了处理［开发人员在监听回调中再次设置权限监听的情况］
                clickPermisseds = new String[]{};// 防止重新进入该页面重复执行业务逻辑
                // 点击权限全部通过--> 执行你的业务逻辑（如启动照相机）
                permissedListener.permissionResult(true, null);

            }
        }
    }

    /**
     * 处理未申请的权限
     *
     * @param isClickPermissed 是否为点击申请
     */
    private void handlePermissed(boolean isClickPermissed) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":handlePermissed()");
        // 如果用户在同意后到system setting做了取消操作，需要把权限状态更新一下
        Collection<Integer> values = permissedActionMap.values();
        if (values.contains(ACTION_DEFAULT)) {// 默认情况(初始化）--> 直接请求权限申请
            if (initPermisseds != null && initPermisseds.length > 0) {
                requestPermissions(initPermisseds, permissedCode);
                Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":call system requestPermissions()");
            }

        } else {// 非默认情况（用户已通过系统框进行操作）--> 重新封装（记录拒绝的权限状态）
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":user had click the permissed pop window");
            checkPermissedState(isClickPermissed ? clickPermisseds : initPermisseds);
            List<String> denyPermissions = new ArrayList<>();
            Set<HashMap<String, Integer>> hashMaps = permissedActionMap.keySet();
            for (HashMap<String, Integer> map : hashMaps) {
                Set<Map.Entry<String, Integer>> entries = map.entrySet();
                for (Map.Entry<String, Integer> entry : entries) {
                    if (entry.getValue() == PackageManager.PERMISSION_DENIED) {
                        denyPermissions.add(entry.getKey());
                    }
                }
            }

            // 显示自定义权限弹窗
            showPermissFrag(PERTYPE.NORMAL, denyPermissions);

            // 点击申请情况--> 将点击权限设置为空
            if (isClickPermissed) {
                clickPermisseds = new String[]{};
            }
        }
    }

    /**
     * 显示权限视窗(fragment)
     *
     * @param perType         窗口类型(超管、普通权限)
     * @param denyPermissions 权限组
     */
    private void showPermissFrag(PERTYPE perType, List<String> denyPermissions) {
        // * 普通权限窗口
        if (perType == PERTYPE.NORMAL) {
            // 接受并处理外部重写的自定义contentView
            preparePermissView();
            // 采用fragment方案代替以上方案 20190306
            Lgg.t(Cons.TAG).ii("prepare the permissInnerbean");
            PermissInnerBean permissInnerBean = new PermissInnerBean();
            permissInnerBean.setLayoutId(layoutId);
            permissInnerBean.setPermissView(permissView);
            permissInnerBean.setStringBean(stringBean);
            permissInnerBean.setPermissedListener(permissedListener);
            permissInnerBean.setDenyPermissons(denyPermissions.toArray(new String[denyPermissions.size()]));
            permissInnerBean.setCurrentFrag(getClass());
            // 启动权限视窗fragment
            Lgg.t(Cons.TAG).ii("start to the PermissFragment");
            new Handler().postDelayed(() -> toFrag(getClass(), PermissFragment.class, permissInnerBean, true), 0);
        }

        // * 超管权限窗口
        if (perType == PERTYPE.SUPER) {
            // TODO: 2021/01/022  弹窗
            // 采用fragment方案代替以上方案 20190306
            Lgg.t(Cons.TAG).ii("prepare the SuperInnerBean");
            SuperBean superBean = requestSUPermission();
            SuperInnerBean superInnerBean = new SuperInnerBean();
            superInnerBean.setLayoutId(layoutId);
            superInnerBean.setSuperView(superBean.getSuperView());
            superInnerBean.setStringBean(superBean.getSuperStringBean());
            superInnerBean.setCurrentFrag(getClass());
            superInnerBean.setSupermission(superBean.getSupermission());
            superInnerBean.setListener(new SuperPermissListener() {
                @Override
                public void initSuperPermissPass() {// (初始化申请)用户打开了超管权限
                    RootFrag.this.isOnResume = true;
                    initOtherPermiss();// 继续申请其他权限
                }

                @Override
                public void clickSuperPermissPass() {// (点击申请)用户打开了超管权限
                    RootFrag.this.isOnResume = true;
                    clickOtherPermiss(tmp_click_permissions);// 继续申请点击其他权限
                }

                @Override
                public void superPermissStillClose() {// 用户没有打开超管权限 - 弹窗
                    RootFrag.this.isOnResume = false;
                    showPermissFrag(PERTYPE.SUPER, null);
                }

                @Override
                public void superPermissCancel() {// 用户点击了弹框的[取消]按钮
                    // TODO: 2021/01/022  弹窗 - 回调业务
                    RootFrag.this.isOnResume = false;
                    superBean.getBussinessListener().clickSuperCancel();
                }
            });
            // 启动权限视窗fragment
            Lgg.t(Cons.TAG).ii("start to the PermissFragment");
            new Handler().postDelayed(() -> toFrag(getClass(), PermissFragment.class, superInnerBean, true), 0);
        }
    }

    /**
     * 处理外部重写的自定义contentView
     */
    private void preparePermissView() {
        Lgg.t(Cons.TAG).ii("Rootfrag: preparePermissView()");
        // PermissBean permissBean = overWritePermissedView();
        if (permissbean != null) {
            permissView = permissbean.getPermissView();
            stringBean = permissbean.getStringBean();
        }
    }

    /**
     * 申请权限回调
     *
     * @param requestCode  回调码
     * @param permissions  权限组
     * @param grantResults 权限组状态
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onRequestPermissionsResult()");
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":permissions[length]" + permissions.length);
        checkPermissedState(permissions);// 检查权限当前的最新状态
    }

    /**
     * 检查权限当前的最新状态
     *
     * @param permissions 需要检查的权限组
     * @apiNote onRequestPermissionsResult()
     * @apiNote handlePermissed()
     */
    private void checkPermissedState(String[] permissions) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":checkPermissedState()");
        HashMap<HashMap<String, Integer>, Integer> tempHashMap = new HashMap<>();
        for (String permission : permissions) {

            int permissedState;// 系统返回的权限状态
            int userAction;// 权限框弹出后用户的行为

            // 检查用户操作权限框后的拒绝状态
            boolean isDenied = PermissionChecker.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED;
            permissedState = isDenied ? PackageManager.PERMISSION_DENIED : PackageManager.PERMISSION_GRANTED;
            userAction = isDenied ? ACTION_DENY : ACTION_PASS;
            // 重新赋值更新Map状态
            Set<Map.Entry<HashMap<String, Integer>, Integer>> entries = permissedActionMap.entrySet();
            for (Map.Entry<HashMap<String, Integer>, Integer> entry : entries) {
                HashMap<String, Integer> permissedMap = entry.getKey();
                for (String permissionName : permissedMap.keySet()) {
                    if (permissionName.equalsIgnoreCase(permission)) {
                        HashMap<String, Integer> map = new HashMap<>();
                        map.put(permission, permissedState);
                        tempHashMap.put(map, userAction);
                    }
                }
            }
        }
        permissedActionMap = tempHashMap;
    }

    /**
     * 是否需要执行请求权限操作
     *
     * @param permissions 权限组
     * @return T：需要
     */
    private boolean isReqPermissed(String[] permissions) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":isReqPermissed()");
        if (permissions == null) {
            return false;
        }
        // 1.循环检查权限
        List<Integer> permissionInt = new ArrayList<>();
        for (String permission : permissions) {
            permissionInt.add(PermissionChecker.checkSelfPermission(activity, permission));
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
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":getData()");
        Object attachs = bean.getAttach();
        whichFragmentStart = bean.getCurrentFragmentClass().getSimpleName();
        String targetFragment = bean.getTargetFragmentClass().getSimpleName();
        Lgg.t(Cons.TAG).vv("whichFragmentStart: " + whichFragmentStart);
        Lgg.t(Cons.TAG).vv("targetFragment: " + targetFragment);
        // 确保现在运行的是目标fragment
        if (getClass().getSimpleName().equalsIgnoreCase(targetFragment)) {
            Lgg.t(Cons.TAG).vv("whichFragmentStart <equal to> targetFragment");
            onNexts(attachs, inflateView, whichFragmentStart);// 抽象
            beginTimer();// 开始启动定时器
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (isHidden()) {
            if (timerState == TimerState.ON_BUT_OFF_WHEN_HIDE | timerState == TimerState.ON_BUT_OFF_WHEN_HIDE_AND_PAUSE) {
                clearTimer(timerHelper);
            }
        }
    }

    /**
     * 启动定时器
     */
    private void beginTimer() {

        if (timerState == TimerState.ON //
                    | timerState == TimerState.ON_BUT_OFF_WHEN_PAUSE //
                    | timerState == TimerState.ON_BUT_OFF_WHEN_HIDE //
                    | timerState == TimerState.ON_BUT_OFF_WHEN_HIDE_AND_PAUSE) {// 开启 | 开启但pause要停止
            clearTimer(timerHelper);

        } else if (timerState == TimerState.OFF_ALL) {// 关闭全部
            clearAllTimer();

        } else if (timerState == TimerState.OFF_ALL_BUT_KEEP_CURRENT) {// 关闭全部(但不包含当前)
            clearAllTimer();

        } else if (timerState == TimerState.OFF_ALL_BUT_KEEP_CURRENT_OFF_WHEN_PAUSE) {// 关闭全部(但不包含当前) 但pause时停止当前
            clearAllTimer();
        }

        if (timerState == TimerState.OFF_ALL_BUT_KEEP_CURRENT // 关闭全部(但不包含当前)
                    | timerState == TimerState.ON // 开启
                    | timerState == TimerState.ON_BUT_OFF_WHEN_PAUSE // 开启但pause要停止
                    | timerState == TimerState.ON_BUT_OFF_WHEN_HIDE_AND_PAUSE // 开启但pause以及hide的时候要停止
                    | timerState == TimerState.OFF_ALL_BUT_KEEP_CURRENT_OFF_WHEN_PAUSE // 关闭全部(但不包含当前) 但pause时停止当前
        ) {
            // 2.创建新的定时器
            timerHelper = new TimerHelper(activity) {
                @Override
                public void doSomething() {
                    setTimerTask();
                }
            };
            // 3.添加到管理处
            timerList.add(timerHelper);
            // 4.检查合理性
            checkDelayPeriod();
            // 5.正式启动
            timerHelper.start(timer_delay, timer_period);
        }
    }

    /**
     * 检查延迟和间隔的合理性
     */
    private void checkDelayPeriod() {
        timer_delay = Math.max(timer_delay, 0);
        timer_period = timer_period <= 0 ? 3000 : timer_period;
    }

    /**
     * 清除定时器
     *
     * @param timerHelper 清除的对象
     */
    private void clearTimer(TimerHelper timerHelper) {
        if (timerHelper != null) {
            timerHelper.stop();
            timerList.remove(timerHelper);
            timerHelper = null;
        }
    }

    /**
     * 清理全部定时器
     */
    private void clearAllTimer() {
        for (TimerHelper timerHelper : timerList) {
            if (timerHelper != null) {
                timerHelper.stop();
                timerHelper = null;
            }
        }
        timerList.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":onPause()");
        if (EventBus.getDefault().isRegistered(this) && isReloadData()) {
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":eventbus unregister");
            EventBus.getDefault().unregister(this);
        }
        // 如果用户设置了该标记位 -- 则pause停止定时器
        if (timerState == TimerState.ON_BUT_OFF_WHEN_PAUSE // 
                    | timerState == TimerState.OFF_ALL_BUT_KEEP_CURRENT_OFF_WHEN_PAUSE // 
                    | timerState == TimerState.ON_BUT_OFF_WHEN_HIDE_AND_PAUSE) {//
            clearTimer(timerHelper);
        }
    }

    @Override
    public void onDestroy() {
        // 解决跨module时, eventbus没有注销而导致从其他module返回时, 会被之前的eventbus重复响应的问题
        // 因此在fragment彻底被销毁时, 需要把eventbus完全注销
        EventBus.getDefault().unregister(this);
        // 停止和清理定时器
        clearTimer(timerHelper);
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        // 其他重写情况
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

    /* -------------------------------------------- override -------------------------------------------- */

    /**
     * 由外部重写初始化权限
     *
     * @return 需要申请的权限组（可以为null, 为null即不申请)
     * @apiNote 重写
     */
    public String[] initPermissed() {
        return new String[]{};
    }

    /**
     * 首次初始化视图完成后的操作
     */
    public void initViewFinish(View inflateView) {

    }

    /**
     * 是否在页面恢复时重新拉取数据
     *
     * @return true:默认(T:会触发eventbus注销并在下次重新注册, 间接触发onNexts()的重复执行)
     */
    public boolean isReloadData() {
        return true;
    }

    /**
     * 提供给外部设定的任务
     */
    public void setTimerTask() {
        // 这里用户做的定时任务逻辑
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 申请超级管理员权限 (适配Android R(11),(SDK = 30))
     *
     * @return 默认为 NONE: 不申请
     */
    public SuperBean requestSUPermission() {
        // TODO: 2021/01/022  弹窗
        SuperBean superBean = new SuperBean();
        superBean.setSuperView(null);
        superBean.setSupermission(SUPERMISSION.NONE);
        superBean.setSuperStringBean(new StringBean());
        superBean.setBussinessListener(null);
        return superBean;
    }

    /**
     * 发送Eventbus事件
     *
     * @param obj     数据
     * @param isStick T:粘性
     */
    public void sendEvent(Object obj, boolean isStick) {
        ((RootMAActivity) activity).sendEvent(obj, isStick);
    }

    /**
     * 设置Eventbus数据接收监听器
     *
     * @param clazz             数据类型, 如 XXX.class
     * @param rootEventListener 数据回调监听器
     * @param <T>               泛型
     */
    public <T> void setEventListener(Class<T> clazz, RootEventListener<T> rootEventListener) {
        if (clazz != null && rootEventListener != null) {
            eventClazzs.add(clazz);
            eventListeners.add(rootEventListener);
        } else {
            if (clazz == null) {
                toast(R.string.EVENT_BUS_CLASS_IS_NULL, 5000);
            } else {
                toast(R.string.EVENT_BUS_LISTENER_IS_NULL, 5000);
            }
        }
    }

    /**
     * 点击申请权限
     *
     * @param permissions 需要申请的权限组
     */
    public void clickPermissed(String[] permissions) {
        tmp_click_permissions = permissions;
        // TODO: 2021/01/022  申请超管权限
        if (requestSUPermission().supermission == SUPERMISSION.CLICK & isNeedSuPermiss()) {
            // TODO: 2021/01/022  弹窗
            showPermissFrag(PERTYPE.SUPER, null);
            return;
        }
        // 申请其他权限
        clickOtherPermiss(permissions);
    }

    /**
     * 点击时其他权限操作
     *
     * @param permissions 需要申请的权限组
     */
    private void clickOtherPermiss(String[] permissions) {
        Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":clickPermissed()");
        initPermisseds = new String[]{};// 1.该步防止初始化权限重复申请
        clickPermisseds = permissions == null ? new String[]{} : permissions;
        if (isReqPermissed(clickPermisseds)) {// 2.点击权限申请
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":to request permissed");
            initPermissedActionMap(clickPermisseds);
            if (clickPermisseds != null && clickPermisseds.length > 0) {
                requestPermissions(clickPermisseds, permissedCode);
            }

        } else {
            Lgg.t(Cons.TAG).vv("Method--> " + getClass().getSimpleName() + ":no need to request permissed");
            // TOAT: 此处先清空权限集合是为了处理［开发人员在监听回调中再次设置权限监听的情况］
            tmp_click_permissions = clickPermisseds = new String[]{};
            if (permissedListener != null) {
                permissedListener.permissionResult(true, null);
            }
        }
    }

    /**
     * 设置permissbean
     *
     * @param permissbean 权限对象
     */
    public void setPermissBean(PermissBean permissbean) {
        this.permissbean = permissbean;
    }

    /**
     * 普通跳转
     *
     * @param current        当前
     * @param target         目标
     * @param attach         附带
     * @param isTargetReload 是否重载视图
     * @param needKills      需要移除的fragment
     */
    public void toFrag(Class current, Class target, Object attach, boolean isTargetReload, Class... needKills) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toFrag(current, target, attach, isTargetReload, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("RootFrag--> toFrag() error: RootMAActivity is null");
        }
    }

    /**
     * 同module + 同Activity: 普通跳转(延迟)
     *
     * @param current        当前
     * @param target         目标
     * @param attach         附带
     * @param isTargetReload 是否重载视图
     * @param delayMilis     延迟毫秒数
     * @param needKills      需要移除的fragment
     */
    public void toFrag(Class current, Class target, Object attach, boolean isTargetReload, int delayMilis, Class... needKills) {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.toFrag(current, target, attach, isTargetReload, delayMilis, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toFrag() error: RootMAActivity is null");
        }
    }

    /**
     * 同module + 不同Activity 跳转
     *
     * @param current        当前的fragment
     * @param targetAC       目标Activity
     * @param target         目标fragment
     * @param attach         附件
     * @param isTargetReload 是否重载目标fragment
     * @param needKills      需要移除的fragment
     */
    public void toFragActivity(Class current, Class targetAC, Class target, Object attach, boolean isTargetReload, Class... needKills) {

        RootMAActivity rootMAActivity = (RootMAActivity) activity;
        if (rootMAActivity != null) {
            rootMAActivity.toFragActivity(current, targetAC, target, attach, isTargetReload, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("Rootfrag--> toFragActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 同module + 不同Activity 跳转(带延时和自定义结束当前)
     *
     * @param current        当前的fragment
     * @param targetAC       目标Activity
     * @param target         目标fragment
     * @param attach         附件
     * @param isTargetReload 是否重载目标fragment
     * @param isFinish       是否结束当前AC
     * @param delay          延迟毫秒数
     * @param needKills      需要移除的fragment
     */
    public void toFragActivity(Class current, Class targetAC, Class target, Object attach, boolean isTargetReload, boolean isFinish, int delay, Class... needKills) {
        RootMAActivity rootMAActivity = (RootMAActivity) activity;
        if (rootMAActivity != null) {
            rootMAActivity.toFragActivity(current, targetAC, target, attach, isTargetReload, isFinish, delay, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("Rootfrag--> toFragActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 不同module + 不同Activity 跳转
     *
     * @param current        当前的fragment
     * @param activityClass  目标Activity的action
     * @param target         目标fragment
     * @param attach         附件
     * @param isTargetReload 是否重载目标fragment
     * @param needKills      需要移除的fragment
     */
    public void toFragModule(Class current, String activityClass, String target, Object attach, boolean isTargetReload, Class... needKills) {
        RootMAActivity rootMAActivity = (RootMAActivity) activity;
        if (rootMAActivity != null) {
            rootMAActivity.toFragModule(current, activityClass, target, attach, isTargetReload, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("Rootfrag--> toFragActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 不同module + 不同Activity 跳转 (带延时和自定义结束当前)
     *
     * @param current        当前的fragment
     * @param activityClass  目标Activity的action
     * @param target         目标fragment
     * @param attach         附件
     * @param isTargetReload 是否重载目标fragment
     * @param isFinish       是否结束当前AC
     * @param delay          延迟毫秒数
     * @param needKills      需要移除的fragment
     */
    public void toFragModule(Class current, String activityClass, String target, Object attach, boolean isTargetReload, boolean isFinish, int delay, Class... needKills) {
        RootMAActivity rootMAActivity = (RootMAActivity) activity;
        if (rootMAActivity != null) {
            rootMAActivity.toFragModule(current, activityClass, target, attach, isTargetReload, isFinish, delay, needKills);
        } else {
            Lgg.t(Cons.TAG).ee("Rootfrag--> toFragActivity() error: RootMAActivity is null");
        }
    }

    /**
     * 结束当前Activit
     */
    public void finishActivity() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.finish();
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
     * 清理Activity
     *
     * @param keepActivitys 需要保留的Activity
     */
    public void killActivitys(Class... keepActivitys) {
        ActivityHelper.killActivitys(keepActivitys);
    }

    /**
     * 清理全部的Activity
     */
    public void killAllActivitys() {
        ActivityHelper.killAllActivity();
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyBoard() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.hideKeyBoard();
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> hideKeyBoard() error: RootMAActivity is null");
        }
    }

    /**
     * 显示软键盘
     */
    public void showKeyBoard() {
        RootMAActivity activity = (RootMAActivity) getActivity();
        if (activity != null) {
            activity.showKeyBoard();
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> showKeyBoard() error: RootMAActivity is null");
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
            activity.toast(tip, duration, getClass());
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
            activity.toast(tip, duration, getClass());
        } else {
            Lgg.t(Cons.TAG).ee("RootHiber--> toast() error: RootMAActivity is null");
        }
    }

    /**
     * 获取图片资源 (为了适配android 9.0 ~ 10.0)
     *
     * @param resId 图片资源
     * @return drawable
     */
    public Drawable getRootDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(activity, resId);
    }

    /**
     * 统一获取字符资源 (为了适配android 9.0 ~ 10.0)
     *
     * @param resId 图片资源
     * @return drawable
     */
    public String getRootString(@StringRes int resId) {
        return activity.getString(resId);
    }

    /**
     * 获取颜色资源 (为了适配android 9.0 ~ 10.0)
     *
     * @param resId 颜色资源
     * @return color
     */
    public int getRootColor(@ColorRes int resId) {
        return ContextCompat.getColor(activity, resId);
    }

    /**
     * 获取SD卡根路径
     *
     * @param dirName 目录名
     * @return 根路径
     */
    public String getSdPath(String dirName) {
        return ((RootMAActivity) activity).getSdPath(dirName);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    /**
     * 设置权限监听接口
     *
     * @param permissedListener 权限监听接口
     */
    public void setPermissedListener(PermissedListener permissedListener) {
        this.permissedListener = permissedListener;
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 封装skipbean
     *
     * @param current        当前的fragment
     * @param activityClass  目标Activity的action
     * @param target         目标Fragment
     * @param attach         附件
     * @param isTargetReload 是否重载对象Fragment
     * @param isFinish       是否结束当前Activity
     * @return skipbean
     */
    private SkipBean getSkipbean(Class current, Class activityClass, Class target, Object attach, boolean isTargetReload, boolean isFinish) {
        SkipBean skipBean = new SkipBean();

        boolean isCurrentFrag = Fragment.class.isAssignableFrom(current);
        boolean isTargetFrag = Fragment.class.isAssignableFrom(target);
        if (isCurrentFrag & isTargetFrag) {
            skipBean.setCurrentFragmentClassName(current.getName());
            skipBean.setTargetFragmentClassName(target.getName());

        } else if (isCurrentFrag & !isTargetFrag) {
            skipBean.setCurrentFragmentClassName(current.getName());
            skipBean.setTargetFragmentClassName(current.getName());

        } else if (!isCurrentFrag & isTargetFrag) {
            skipBean.setCurrentFragmentClassName(getClass().getName());
            skipBean.setTargetFragmentClassName(target.getName());

        } else {
            skipBean.setCurrentFragmentClassName(getClass().getName());
            skipBean.setTargetFragmentClassName(getClass().getName());

        }

        skipBean.setTargetActivityClassName(activityClass.getName());
        skipBean.setAttach(attach);
        skipBean.setTargetReload(isTargetReload);
        skipBean.setCurrentACFinish(isFinish);
        return skipBean;
    }
}
