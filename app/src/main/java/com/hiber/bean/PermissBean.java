package com.hiber.bean;

import android.view.View;

/*
 * Created by qianli.ma on 2019/2/21 0021.
 */
public class PermissBean {
    
    private View view;
    private StringBean stringBean;

    public PermissBean() {
    }

    public PermissBean(View view, StringBean stringBean) {
        this.view = view;
        this.stringBean = stringBean;
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
}
