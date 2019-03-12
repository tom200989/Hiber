package com.hiber.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.hiber.bean.StringBean;
import com.hiber.cons.Cons;
import com.hiber.hiber.PermissInnerBean;
import com.hiber.hiber.R;
import com.hiber.hiber.RootFrag;
import com.hiber.impl.PermissedListener;
import com.hiber.tools.Lgg;
import com.hiber.widget.PermisWidget;

import java.util.Arrays;

/*
 * Created by qianli.ma on 2019/3/4 0004.
 */
public class PermissFragment extends RootFrag {

    private PermisWidget permisWidget;

    @Override
    public int onInflateLayout() {
        Lgg.t(Cons.TAG).ii("PermissFragment onInflateLayout");
        return R.layout.frag_permission;
    }

    @Override
    public void onNexts(Object yourBean, View view, String whichFragmentStart) {
        // 初始化视图
        permisWidget = view.findViewById(R.id.wd_permiss);
        // 获取数据
        if (yourBean instanceof PermissInnerBean) {
            // 接收数据
            Lgg.t(Cons.TAG).ii("parse the PermissInnerBean");
            PermissInnerBean permissInnerBean = (PermissInnerBean) yourBean;
            String[] denyPermissons = permissInnerBean.getDenyPermissons();
            PermissedListener permissedListener = permissInnerBean.getPermissedListener();
            StringBean stringBean = permissInnerBean.getStringBean();
            View permissView = permissInnerBean.getPermissView();
            Class currentFrag = permissInnerBean.getCurrentFrag();
            int layoutId = permissInnerBean.getLayoutId();

            // 设置上一个frag的图层以实现半透明效果
            permisWidget.setLastFragView(layoutId);

            // 初始化视图
            Lgg.t(Cons.TAG).ii("permisWidget.setPermissView");
            permisWidget.setPermissView(permissView, stringBean, Arrays.asList(denyPermissons));

            // 设置Cancel点击事件
            permisWidget.setOnClickCancelListener(() -> {
                // 10.2.关闭窗口
                toFrag(getClass(), currentFrag, null, false);
                // 10.3.接口回调
                if (permissedListener != null) {
                    permissedListener.permissionResult(false, denyPermissons);
                }
                Lgg.t(Cons.TAG2).ii("PermissFragment: click cancel callback outside");
            });

            // 设置OK点击事件
            permisWidget.setOnClickOkListener(() -> {
                // 10.1.关闭窗口(此处一定要先跳转, 否则会被setting页面覆盖)
                toFrag(getClass(), currentFrag, null, false);
                // 10.2.前往setting界面
                toSetting(activity);
                // 10.3.日志打印
                Lgg.t(Cons.TAG2).ii("PermissFragment: click ok callback outside");
            });
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
        Lgg.t(Cons.TAG2).ii("PermissFragment: to system setting ui");
    }

    @Override
    public boolean onBackPresss() {
        Lgg.t(Cons.TAG).ii("click permission fragment");
        return true;
    }

    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 此步一定要让外部传递的自定义view解除与parent的绑定关系否则报出如下异常
        // The specified child already has a parent. You must call removeView() on the child's parent first.
        permisWidget.removeView();
    }
}
