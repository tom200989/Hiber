package com.hiber.bean;

import android.view.View;

import com.hiber.cons.SUPERMISSION;
import com.hiber.impl.SuperPermitBussinessListener;

import java.io.Serializable;

/*
 * Created by qianli.ma on 2019/2/21 0021.
 */
public class SuperBean implements Serializable {

    public View superView;// 自定义视图(超管)
    public StringBean superStringBean;// 默认情况下的字符内容对象(超管)
    public SUPERMISSION supermission = SUPERMISSION.NONE;// 超管行为枚举(初始、点击、不操作)
    public SuperPermitBussinessListener bussinessListener;

    public SuperBean() {
    }

    public SuperBean(View view, StringBean superStringBean, SUPERMISSION supermission, SuperPermitBussinessListener bussinessListener) {
        this.superView = view;
        this.superStringBean = superStringBean;
        this.supermission = supermission;
        this.bussinessListener = bussinessListener;
    }

    public SuperPermitBussinessListener getBussinessListener() {
        return bussinessListener;
    }

    public SuperBean setBussinessListener(SuperPermitBussinessListener bussinessListener) {
        this.bussinessListener = bussinessListener;
        return this;
    }

    public View getSuperView() {
        return superView;
    }

    public void setSuperView(View superView) {
        this.superView = superView;
    }

    public StringBean getSuperStringBean() {
        return superStringBean;
    }

    public void setSuperStringBean(StringBean superStringBean) {
        this.superStringBean = superStringBean;
    }

    public SUPERMISSION getSupermission() {
        return supermission;
    }

    public SuperBean setSupermission(SUPERMISSION supermission) {
        this.supermission = supermission;
        return this;
    }
}
