package com.hiber.bean;

import android.view.View;

/*
 * Created by qianli.ma on 2019/2/21 0021.
 */
public class PermissBean {
    
    private View permissView;// 自定义视图
    private StringBean stringBean;// 默认情况下的字符内容对象

    public PermissBean() {
    }

    public PermissBean(View view, StringBean stringBean) {
        this.permissView = view;
        this.stringBean = stringBean;
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
}
