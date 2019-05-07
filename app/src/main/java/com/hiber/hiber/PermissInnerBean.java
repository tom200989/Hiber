package com.hiber.hiber;

import android.support.annotation.LayoutRes;
import android.view.View;

import com.hiber.bean.StringBean;
import com.hiber.impl.PermissedListener;

import java.io.Serializable;

/*
 * Created by qianli.ma on 2019/3/4 0004.
 */
public class PermissInnerBean implements Serializable {

    /**
     * 被拒绝的权限组
     */
    private String[] denyPermissons;

    /**
     * 权限监听器
     */
    private PermissedListener permissedListener;

    /**
     * 权限自定义制图
     */
    private View permissView;

    /**
     * 权限默认字符内容
     */
    private StringBean stringBean;

    /**
     * 当前的fragment
     */
    private Class currentFrag;

    private @LayoutRes
    int layoutId;


    public PermissInnerBean() {
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public String[] getDenyPermissons() {
        return denyPermissons;
    }

    public void setDenyPermissons(String[] denyPermissons) {
        this.denyPermissons = denyPermissons;
    }

    public PermissedListener getPermissedListener() {
        return permissedListener;
    }

    public void setPermissedListener(PermissedListener permissedListener) {
        this.permissedListener = permissedListener;
    }

    public View getPermissView() {
        return permissView;
    }

    public void setPermissView(View permissView) {
        this.permissView = permissView;
    }

    public StringBean getStringBean() {
        return stringBean;
    }

    public void setStringBean(StringBean stringBean) {
        this.stringBean = stringBean;
    }

    public Class getCurrentFrag() {
        return currentFrag;
    }

    public void setCurrentFrag(Class currentFrag) {
        this.currentFrag = currentFrag;
    }
}
