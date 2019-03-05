package com.hiber.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.hiber.bean.StringBean;
import com.hiber.cons.Cons;
import com.hiber.hiber.FragBean;
import com.hiber.hiber.R;
import com.hiber.widget.PermisWidget;

import org.greenrobot.eventbus.EventBus;

/*
 * Created by qianli.ma on 2019/2/20 0020. 由于需要用户权限, 该方案取消(留待备用)
 */
@Deprecated 
public class PermissWindow {

    // 顶层窗体
    private WindowManager winManager;
    // 布局试图
    private View inflate;
    // 自定义弹出框
    private PermisWidget permisWidget;

    /**
     * 显示权限窗口
     *
     * @param context 环境
     */
    public void setVisibles(final Context context, @Nullable View view, @Nullable StringBean stringBean) {
        Lgg.t(Cons.TAG2).ii("PermissWindow: setVisibles() start");
        // 1.获取应用的Context
        final Context contextApplication = context.getApplicationContext();
        //2.获取WindowManager
        winManager = (WindowManager) contextApplication.getSystemService(Context.WINDOW_SERVICE);
        // 3.获取windown的属性对象
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        // 4.设置类型为TYPE_SYSTEM_ALERT(弹出框类型)
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 5.设置flag(该flag的作用是方便后期功能扩展,例如backpress的回退点击事件)
        /*如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件*/
        // FLAG_NOT_TOUCH_MODAL: 不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE:   悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        // 6.设置弹出框遮罩为透明
        params.format = PixelFormat.TRANSLUCENT;
        // 7.设置窗口大小
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        // 8.窗体位置
        params.gravity = Gravity.CENTER;
        // 9.填充视图
        initView(view, stringBean, contextApplication);
        // 10.设置点击事件
        initClick(context);
        // 11.添加到窗体
        winManager.addView(inflate, params);
        Lgg.t(Cons.TAG2).ii("PermissWindow: setVisibles() end");
    }

    /**
     * 初始化视图
     *
     * @param view               外部输入视图
     * @param stringBean         外部输入默认字符
     * @param contextApplication 全局环境
     */
    private void initView(@Nullable View view, @Nullable StringBean stringBean, Context contextApplication) {
        Lgg.t(Cons.TAG2).ii("PermissWindow: initView()");
        inflate = View.inflate(contextApplication, R.layout.layout_permission, null);
        permisWidget = inflate.findViewById(R.id.wd_permiss);
        permisWidget.setView(view, stringBean);
    }

    /**
     * 初始化点击
     *
     * @param context 环境
     */
    private void initClick(Context context) {

        // 点击Cancel
        permisWidget.setOnClickCancelListener(() -> {
            // 10.1.移除stick事件
            EventBus.getDefault().removeStickyEvent(FragBean.class);
            Lgg.t(Cons.TAG2).ii("PermissWindow: Eventbus unRegister");
            // 10.2.关闭窗口
            setGone();
            // 10.3.接口回调
            clickCancelNext();
            Lgg.t(Cons.TAG2).ii("PermissWindow: click cancel callback outside");
        });

        permisWidget.setOnClickOkListener(() -> {// 点击OK
            // 10.1.前往setting界面
            toSetting(context);
            // 10.2.关闭窗口
            setGone();
            // 10.3.接口回调
            clickOkNext();
            Lgg.t(Cons.TAG2).ii("PermissWindow: click ok callback outside");
        });
    }

    /**
     * 外部获取自定义权限面板
     *
     * @return 自定义权限面板
     */
    public PermisWidget getPermissWidget() {
        return permisWidget;
    }

    /**
     * 外部判断自定义权限面板是否显示
     *
     * @return T:显示
     */
    public boolean isPermissWidgetVisible() {
        return permisWidget.getVisibility() == View.VISIBLE;
    }

    /**
     * 移除权限组视图
     */
    public void setGone() {
        if (winManager != null & inflate != null) {
            winManager.removeViewImmediate(inflate);
            Lgg.t(Cons.TAG2).ii("PermissWindow: remove view");
            winManager = null;
            inflate = null;
        }
    }

    /**
     * 前往系统的设置页面
     */
    private void toSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Lgg.t(Cons.TAG2).ii("PermissWindow: to system setting ui");
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    private OnClickCancelListener onClickCancelListener;

    // Inteerface--> 接口OnClickCancelListener
    public interface OnClickCancelListener {
        void clickCancel();
    }

    // 对外方式setOnClickCancelListener
    public void setOnClickCancelListener(OnClickCancelListener onClickCancelListener) {
        this.onClickCancelListener = onClickCancelListener;
    }

    // 封装方法clickCancelNext
    private void clickCancelNext() {
        if (onClickCancelListener != null) {
            onClickCancelListener.clickCancel();
        }
    }

    private OnClickOkListener onClickOkListener;

    // Inteerface--> 接口OnClickOkListener
    public interface OnClickOkListener {
        void clickOk();
    }

    // 对外方式setOnClickOkListener
    public void setOnClickOkListener(OnClickOkListener onClickOkListener) {
        this.onClickOkListener = onClickOkListener;
    }

    // 封装方法clickOkNext
    private void clickOkNext() {
        if (onClickOkListener != null) {
            onClickOkListener.clickOk();
        }
    }
}
