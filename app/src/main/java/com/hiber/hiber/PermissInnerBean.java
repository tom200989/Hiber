package com.hiber.hiber;

import android.view.View;

import com.hiber.bean.StringBean;
import com.hiber.impl.PermissedListener;

/*
 * Created by qianli.ma on 2019/3/4 0004.
 */
public class PermissInnerBean {

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
    private View view;
    
    /**
     * 权限默认字符内容
     */
    private StringBean stringBean;

    /**
     * 当前的fragment
     */
    private Class currentFrag;

    public PermissInnerBean() {
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

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
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
