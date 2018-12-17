package com.hiber.hiber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Window;

import com.hiber.bean.RootProperty;
import com.hiber.cons.Cons;
import com.hiber.tools.Lgg;
import com.hiber.tools.backhandler.BackHandlerHelper;
import com.hiber.tools.barcompat.StatusBarCompat;
import com.hiber.ui.DefaultFragment;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
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
     * 权限组 如: Manifest.permission.WRITE_EXTERNAL_STORAGE
     */
    private String[] permissions = // 默认以下权限需要申请
            {Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写SD卡
                    Manifest.permission.READ_EXTERNAL_STORAGE};// 读SD卡

    /**
     * 权限请求码
     */
    private int permissionCode = 0x100;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":onCreate()");
        // 1.获取初始化配置对象
        rootProperty = initProperty();
        if (rootProperty != null) {
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
            // 6.初始化权限
            initPermission();
        } else {
            toast("RootProperty is null \n app crash", 2500);
            Lgg.t(TAG).vv("RootProperty is null");
        }

    }

    /**
     * 分发配置
     *
     * @param rootProperty 配置
     */
    private void dispatherProperty(RootProperty rootProperty) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":dispatherProperty()");
        Lgg.t(TAG).vv(rootProperty.toString());
        isFullScreen = rootProperty.isFullScreen();
        colorStatusBar = rootProperty.getColorStatusBar() < 0 ? colorStatusBar : rootProperty.getColorStatusBar();
        layoutId = rootProperty.getLayoutId() <= 0 ? layoutId : rootProperty.getLayoutId();
        TAG = TextUtils.isEmpty(rootProperty.getTAG()) ? TAG : rootProperty.getTAG();
        isSaveInstanceState = rootProperty.isSaveInstanceState();
        permissions = rootProperty.getPermissions() == null | rootProperty.getPermissions().length <= 0 ? permissions : rootProperty.getPermissions();
        permissionCode = rootProperty.getPermissionCode() < 0 ? permissionCode : rootProperty.getPermissionCode();
        projectDirName = TextUtils.isEmpty(rootProperty.getProjectDirName()) ? projectDirName : rootProperty.getProjectDirName();
        containId = rootProperty.getContainId() <= 0 ? containId : rootProperty.getContainId();
        fragmentClazzs = rootProperty.getFragmentClazzs() == null | rootProperty.getFragmentClazzs().length <= 0 ? fragmentClazzs : rootProperty.getFragmentClazzs();
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
     * 初始化权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android N : 6.0 以下直接执行业务逻辑
            initFragment();
            Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initPermission() below Android N : 6.0");
        } else {
            // Android N : 6.0 以上申请权限
            if (isNeedToRequestPermissions()) {
                ActivityCompat.requestPermissions(this, permissions, permissionCode);
                Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initPermission() requestPermissions");
            } else {
                initFragment();
                Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initPermission() had pass permissions");
            }
        }
    }

    /**
     * 判断是否需要申请权限
     *
     * @return true:需要
     */
    private boolean isNeedToRequestPermissions() {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":isNeedToRequestPermissions()");
        // 1.循环检查权限
        List<Integer> permissionInt = new ArrayList<>();
        for (String permission : permissions) {
            permissionInt.add(ContextCompat.checkSelfPermission(this, permission));
        }

        // 2.判断是否有未通过的权限
        for (Integer permissionDenied : permissionInt) {
            if (permissionDenied == PackageManager.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionCode) {
            applyBasePermission(permissions, grantResults);
        }
    }

    /**
     * 申请基本的权限
     *
     * @param grantResults 权限组
     */
    private void applyBasePermission(String[] permissions, int[] grantResults) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":applyBasePermission()");
        // 1.把所有回调的权限状态添加到自定义集合
        List<Integer> ints = new ArrayList<>();
        for (int grantResult : grantResults) {
            ints.add(grantResult);
        }

        // 2.判断: 如果有任意一个权限未通过, 则杀死进程(或者自定义处理)
        if (ints.contains(PackageManager.PERMISSION_DENIED)) {
            // 如果返回为false--> 结束当前的activity
            if (!permissonNotPass()) {
                if (!isFinishing()) {
                    Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":finish()");
                    finish();
                }
            }
        } else {
            initFragment();
            createInstallRootDir(projectDirName);
        }
    }


    /**
     * 初始化fragment
     */
    private void initFragment() {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initFragment()");
        // 容器
        int contain = containId;
        Class firstFrag = fragmentClazzs[0];
        // 初始化fragment调度器
        initFragmentSchedule(contain, firstFrag);
    }

    /**
     * 初始化frahelper单例
     *
     * @param contain   容器
     * @param firstFrag 首屏
     */
    private void initFragmentSchedule(int contain, Class firstFrag) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":initFragmentSchedule()");
        if (fraHelpers == null) {
            synchronized (FraHelpers.class) {
                if (fraHelpers == null) {
                    fraHelpers = new FraHelpers(this, fragmentClazzs, firstFrag, contain);
                    Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":new FraHelpers()");
                }
            }
        } else {
            onNexts();
        }
    }

    /**
     * 创建安装包根目录
     */
    private void createInstallRootDir(String dirName) {
        Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":createInstallRootDir()");
        File sdcard = Environment.getExternalStorageDirectory();
        String installDirPath = sdcard.getAbsolutePath() + File.separator + dirName;
        File installDir = new File(installDirPath);
        if (!installDir.exists() | !installDir.isDirectory()) {
            boolean mkdir = installDir.mkdir();
            Lgg.t(TAG).vv("Method--> " + getClass().getSimpleName() + ":createInstallRootDir() == " + mkdir);
        }
    }

    /* -------------------------------------------- Normal method -------------------------------------------- */

    /**
     * 跳转到别的fragment
     *
     * @param classWhichFragmentStart 当前
     * @param targetFragmentClass     目标
     * @param attach                  额外附带数据对象
     * @param isTargetReload          是否重载视图
     */
    public void toFrag(Class classWhichFragmentStart, Class targetFragmentClass, Object attach, boolean isTargetReload) {
        FragBean fragBean = new FragBean();
        fragBean.setCurrentFragmentClass(classWhichFragmentStart);
        fragBean.setTargetFragmentClass(targetFragmentClass);
        fragBean.setAttach(attach == null ? "" : attach);
        // 1.先跳转
        fraHelpers.transfer(targetFragmentClass, isTargetReload);
        // 2.在传输(否则会出现nullPointException)
        EventBus.getDefault().postSticky(fragBean);
    }

    /**
     * 获取fragment调度器
     *
     * @return fragment调度器
     */
    public FraHelpers getFragmentHelper() {
        return fraHelpers;
    }

    /**
     * @param target 移除指定的fragment
     */
    public void removeFrag(Class target) {
        fraHelpers.remove(target);
    }

    /**
     * 结束当前Activit
     */
    public void finishOver() {
        RootHelper.finishOver(this);
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

    /**
     * 跳转(默认方式)
     *
     * @param activity 当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity activity, Class<?> clazz, boolean isFinish) {
        RootHelper.toActivity(activity, clazz, isFinish);
    }

    /**
     * 跳转(隐式)
     *
     * @param activity 当前环境
     * @param action    目标
     * @param isFinish 是否默认方式
     */
    public void toActivityImplicit(Activity activity, String action, boolean isFinish) {
        RootHelper.toActivityImplicit(activity, action, isFinish);
    }
    
    /**
     * 跳转(自定义方式)
     *
     * @param activity 当前环境
     * @param clazz    目标
     * @param isFinish 是否默认方式
     */
    public void toActivity(Activity activity, Class<?> clazz, boolean isSigleTop, boolean isFinish, boolean overpedding, int delay) {
        RootHelper.toActivity(activity, clazz, isSigleTop, isFinish, overpedding, delay);
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
        RootHelper.toActivityImplicit(activity, action, isSigleTop, isFinish, overpedding, delay);
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

    /* -------------------------------------------- impl -------------------------------------------- */

    /**
     * 由外部重写
     *
     * @return true:子类实现业务逻辑后不finish. false:finish
     */
    public boolean permissonNotPass() {
        return false;
    }

}
