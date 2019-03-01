package com.hiber.hiber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Window;

import com.hiber.bean.RootProperty;
import com.hiber.bean.SkipBean;
import com.hiber.cons.Cons;
import com.hiber.hiber.language.LangHelper;
import com.hiber.tools.Lgg;
import com.hiber.tools.backhandler.BackHandlerHelper;
import com.hiber.tools.barcompat.StatusBarCompat;
import com.hiber.ui.DefaultFragment;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/*
 * Created by qianli.ma on 2018/6/20 0020.
 */

@SuppressLint("Registered")
public abstract class RootMAActivity extends FragmentActivity {

    /**
     * 配置对象
     */
    private RootProperty rootProperty;

    /**
     * fragment调度器
     */
    public FraHelpers fraHelpers;

    /**
     * 日志标记
     */
    public String TAG = Cons.TAG;

    /**
     * 状态栏颜色ID 如:R.color.xxx
     */
    private int colorStatusBar = R.color.colorHiberAccent;

    /**
     * 布局ID 如:R.layout.xxx
     */
    private int layoutId = R.layout.activity_hiber;

    /**
     * 是否保存Activity状态 建议为false
     */
    private boolean isSaveInstanceState;

    /**
     * 项目目录
     */
    private String projectDirName = Cons.RootDir;

    /**
     * fragment容器ID 如:R.id.frame
     */
    private int containId = R.id.fl_hiber_contain;

    /**
     * fragment字节码数组 如:[fragment1.class,fragment2.class,...]
     */
    private Class[] fragmentClazzs = {DefaultFragment.class};

    /**
     * 是否需要全屏
     */
    private boolean isFullScreen = true;

    /**
     * 存储「frag绝对路径, frag字节码」
     */
    private HashMap<String, Class> classFragMap = new HashMap<>();

    // 记录当前的初始化状态
    private String FLAG_ONCREATED = "onCreate";
    private String FLAG_NEW_INTENT = "onNewIntent";
    private String FLAG_CURRENT = FLAG_ONCREATED;// 默认onCreated

    /**
     * 通过extra方式需要接收的信标符号
     */
    public static String INTENT_NAME = "SkipBean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 0.检测action与category是否符合规范
        boolean isActionCategoryMatch = checkActionCategory();
        if (isActionCategoryMatch) {// 0.1.符合条件则正常执行
            FLAG_CURRENT = FLAG_ONCREATED;
            Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":onCreate()");
            // 1.获取初始化配置对象
            rootProperty = initProperty();
            if (rootProperty != null) {// 属性对象不为空
                // 2.分发配置
                dispatherProperty(rootProperty);
                // 3.设置无标题栏(必须位于 super.onCreate(savedInstanceState) 之上)
                if (isFullScreen) {
                    requestWindowFeature(Window.FEATURE_NO_TITLE);
                }
                super.onCreate(savedInstanceState);
                // 4.填充视图
                setContentView(layoutId);
                // 5.设置状态栏颜色
                StatusBarCompat.setStatusBarColor(this, getResources().getColor(colorStatusBar), false);
                // 6.处理从其他组件传递过来的数据
                handleIntentExtra(getIntent());

            } else {// 属性对象为空
                String proErr = getString(R.string.ROOT_PROPERTY_ERR);
                toast(proErr, 5000);
                Lgg.t(TAG).vv(proErr);
            }

        } else {// 0.1.开发人员没有按照规定配置manifest
            String err = getString(R.string.INIT_ERR);
            toast(err, 5000);
            Lgg.t(TAG).vv(err);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        FLAG_CURRENT = FLAG_NEW_INTENT;
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":onNewIntent()");
        super.onNewIntent(intent);
        handleIntentExtra(intent);
    }

    /**
     * 处理从其他组件传递过来的数据
     *
     * @param intent 意图
     */
    private void handleIntentExtra(Intent intent) {
        // 1.获取序列流
        SkipBean skipBean = (SkipBean) intent.getSerializableExtra(INTENT_NAME);
        // 2.判断是否为自身AC
        String currentActivityClassName = getClass().getName();
        String targetActivityClassName = skipBean.getTargetActivityClassName();
        if (targetActivityClassName.equalsIgnoreCase(currentActivityClassName)) {
            // 是自身AC
            Class targetFragClass = searchFragClassByName(skipBean.getTargetFragmentClassName());
            int classFragIndex = searchFragIndexByClass(targetFragClass);
            Object attach = skipBean.getAttach();
            boolean isTargetReload = skipBean.isTargetReload();
            if (FLAG_CURRENT.equalsIgnoreCase(FLAG_NEW_INTENT)) {
                toFrag(getClass(), targetFragClass, attach, isTargetReload);
            } else {
                initFragment(classFragIndex, attach);
            }
        } else {
            // 不是自身AC(推送)
            try {
                Activity activity = this;
                boolean isSingleTop = false;
                boolean isFinish = false;
                boolean isOverridePending = false;
                int delay = 0;
                RootHelper.toActivityImplicit(activity, targetActivityClassName, isSingleTop, isFinish, isOverridePending, delay, skipBean);
            } catch (Exception e) {
                e.printStackTrace();
                String acError = getString(R.string.ACTION_ERR);
                String des = String.format(acError, targetActivityClassName);
                Lgg.t(Cons.TAG).ee(des);
                toast(des, 5000);
            }
        }

        // 恢复标记位
        FLAG_CURRENT = FLAG_ONCREATED;
    }

    /**
     * 检查开发人员是否配置了action以及category, action是否符合规范(绝对路径)
     *
     * @return T:符合
     */
    @SuppressLint("PrivateApi") // 消除「packageManager.getClass().getDeclaredMethod」的警告
    @SuppressWarnings("unchecked")// 消除「method.invoke(packageManager, getPackageName()」的警告
    private boolean checkActionCategory() {

        // 如果是小于Android 4.4, 则不能使用反射, PackageManager没有对应的API
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return true;
        }

        // > 4.4 , 使用反射调用PackageManager的获取全部intent-filter
        try {
            PackageManager packageManager = getPackageManager();
            Method method = packageManager.getClass().getDeclaredMethod("getAllIntentFilters", String.class);
            method.setAccessible(true);
            List<IntentFilter> intentFilters = (List<IntentFilter>) method.invoke(packageManager, getPackageName());
            for (IntentFilter intentFilter : intentFilters) {
                // 检测<action>中是否配置自身类的绝对路径--> 必须强制要求外部人员在action里配置的是自身AC的绝对路径
                boolean isSetActionBySelfName = intentFilter.hasAction(getClass().getName());
                // 检测<category>是否配置DEFAULT标签
                boolean isSetCategoryByDefault = intentFilter.hasCategory("android.intent.category.DEFAULT");
                // <action> 与 <category>必须同时符合条件
                if (isSetActionBySelfName & isSetCategoryByDefault) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String inflectErr = getString(R.string.INFLECT_ERR);
            Lgg.t(Cons.TAG).ee(inflectErr);
        }
        return false;
    }

    /**
     * 根据frag的绝对路径获取到对应的frag字节码
     *
     * @param targetFragmentClassName fragment的绝对路径
     * @return frag字节码
     */
    private Class searchFragClassByName(String targetFragmentClassName) {
        if (!TextUtils.isEmpty(targetFragmentClassName)) {
            for (String fragName : classFragMap.keySet()) {
                if (fragName.equalsIgnoreCase(targetFragmentClassName)) {
                    return classFragMap.get(fragName);
                }
            }
        }
        return fragmentClazzs[0];
    }

    /**
     * 根据frag字节码找到该字节码在数组中的索引
     *
     * @param currentClass frag字节码
     * @return 对应的索引
     */
    private int searchFragIndexByClass(Class currentClass) {
        for (int i = 0; i < fragmentClazzs.length; i++) {
            if (currentClass == fragmentClazzs[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 国际化语言必须实现的方法
     *
     * @param context 上下文
     */
    @Override
    protected void attachBaseContext(Context context) {
        // 国际化语言切换时必须使用以下方式 (需要把context与语言配置进行绑定)
        super.attachBaseContext(LangHelper.getContext(context));
    }

    /**
     * 分发配置
     *
     * @param rootProperty 配置
     */
    private void dispatherProperty(RootProperty rootProperty) {
        // 初始化赋值
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":dispatherProperty()");
        Lgg.t(TAG).vv(rootProperty.toString());
        isFullScreen = rootProperty.isFullScreen();
        colorStatusBar = rootProperty.getColorStatusBar() <= 0 ? colorStatusBar : rootProperty.getColorStatusBar();
        layoutId = rootProperty.getLayoutId() <= 0 ? layoutId : rootProperty.getLayoutId();
        TAG = TextUtils.isEmpty(rootProperty.getTAG()) ? TAG : rootProperty.getTAG();
        isSaveInstanceState = rootProperty.isSaveInstanceState();
        projectDirName = TextUtils.isEmpty(rootProperty.getProjectDirName()) ? projectDirName : rootProperty.getProjectDirName();
        containId = rootProperty.getContainId() <= 0 ? containId : rootProperty.getContainId();
        fragmentClazzs = rootProperty.getFragmentClazzs() == null || rootProperty.getFragmentClazzs().length <= 0 ? fragmentClazzs : rootProperty.getFragmentClazzs();
        // 将fragment转换成map形式
        saveClassMap(fragmentClazzs);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        /*
         * 重写这个方法是为了解决: 用户点击权限允许后界面无法继续加载的bug.
         * NoSaveInstanceStateActivityName(): 一般由第一个Activity进行实现
         * onSaveInstanceState()方法在获取权限时, 导致fragment初始化失败
         * 如果当前的Activity没有必要保存状态 (默认是: Activity被后台杀死后,系统会保存Activity状态)
         * 则不需要调用 「super.onSaveInstanceState(outState)」这个方法
         */
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":onSaveInstanceState() == " + isSaveInstanceState);
        if (isSaveInstanceState) {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            boolean isDispatcher = onBackClick();
            if (!isDispatcher) {
                // 如果fragment没有处理--> 则直接退出
                super.onBackPressed();
            }
        }
    }

    /**
     * 初始化fragment
     */
    private void initFragment(int initIndex, Object attach) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initFragment()");
        // 容器
        int contain = containId;
        Class firstFrag = fragmentClazzs[initIndex];
        // 初始化fragment调度器
        initFragmentSchedule(contain, firstFrag, attach);
    }

    /**
     * 初始化frahelper单例
     *
     * @param contain   容器
     * @param firstFrag 首屏
     */
    private void initFragmentSchedule(int contain, Class firstFrag, Object attach) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initFragmentSchedule()");
        if (fraHelpers == null) {
            synchronized (FraHelpers.class) {
                if (fraHelpers == null) {
                    fraHelpers = new FraHelpers(this, fragmentClazzs, firstFrag, contain, attach);
                    Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":new FraHelpers()");
                }
            }
        } else {
            onNexts();
        }
    }

    /**
     * 将字节码list转换为hashmap
     *
     * @param fragmentClazzs 需要转换的集合
     */
    private void saveClassMap(Class[] fragmentClazzs) {
        for (Class clz : fragmentClazzs) {
            String clzAbsoluteName = clz.getName();
            classFragMap.put(clzAbsoluteName, clz);
        }
    }

    /**
     * 封装传输对象
     *
     * @param classWhichFragmentStart 哪个fragment跳转的
     * @param targetFragmentClass     跳转到哪个目标Fragment
     * @param attach                  附件
     * @return 传输对象
     */
    private FragBean transferFragbean(Class classWhichFragmentStart, Class targetFragmentClass, Object attach) {
        // 1.创建一个新的传输对象
        FragBean fragBean = new FragBean();
        // 2.检测传递进来的参数是否为fragment类型
        boolean whichIsFragment = Fragment.class.isAssignableFrom(classWhichFragmentStart);
        boolean targetIsFragment = Fragment.class.isAssignableFrom(targetFragmentClass);
        if (whichIsFragment & targetIsFragment) {// 同时符合条件
            fragBean.setCurrentFragmentClass(classWhichFragmentStart);
            fragBean.setTargetFragmentClass(targetFragmentClass);

        } else if (whichIsFragment & !targetIsFragment) {// target不符合条件--> 使用which填充
            fragBean.setCurrentFragmentClass(classWhichFragmentStart);
            fragBean.setTargetFragmentClass(classWhichFragmentStart);

        } else if (!whichIsFragment & targetIsFragment) {// which不符合条件--> 使用target填充
            fragBean.setCurrentFragmentClass(targetFragmentClass);
            fragBean.setTargetFragmentClass(targetFragmentClass);

        } else {// 两个同时不为true--> 则默认跳转第一个
            fragBean.setCurrentFragmentClass(fragmentClazzs[0]);
            fragBean.setTargetFragmentClass(fragmentClazzs[0]);
        }
        // 3.设置附件
        fragBean.setAttach(attach == null ? "" : attach);
        // 4.返回封装后对象
        return fragBean;
    }

    /* -------------------------------------------- public method -------------------------------------------- */

    /**
     * 跳转到别的fragment
     *
     * @param classWhichFragmentStart 当前
     * @param targetFragmentClass     目标
     * @param attach                  额外附带数据对象
     * @param isTargetReload          是否重载视图
     */
    public void toFrag(Class classWhichFragmentStart, Class targetFragmentClass, Object attach, boolean isTargetReload) {
        // 0.转换并封装传输对象
        FragBean fragBean = transferFragbean(classWhichFragmentStart, targetFragmentClass, attach);
        // 1.先跳转
        fraHelpers.transfer(fragBean.getTargetFragmentClass(), isTargetReload);
        // 2.再传输(否则会出现nullPointException)
        EventBus.getDefault().postSticky(fragBean);
    }

    /**
     * 杀死APP
     */
    public void kill() {
        RootHelper.kill();
    }

    /**
     * 吐司提示
     *
     * @param tip      提示
     * @param duration 时长
     */
    public void toast(String tip, int duration) {
        RootHelper.toast(this, tip, duration);
    }

    /**
     * 吐司提示
     *
     * @param stringId 字符资源ID
     * @param duration 时长
     */
    public void toast(int stringId, int duration) {
        RootHelper.toast(this, getString(stringId), duration);
    }

    /* -------------------------------------------- abstract -------------------------------------------- */

    /**
     * 初始配置
     *
     * @return 配置对象
     */
    public abstract RootProperty initProperty();

    /**
     * 你的业务逻辑
     */
    public abstract void onNexts();

    /**
     * 回退键的点击事件
     *
     * @return true:自定义逻辑 false:super.onBackPress()
     */
    public abstract boolean onBackClick();

}
