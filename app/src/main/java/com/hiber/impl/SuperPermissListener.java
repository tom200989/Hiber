package com.hiber.impl;

/*
 * Created by Administrator on 2021/01/022.
 * 用于RootFrag接收PermitFrag回调用户操作超管权限界面后的接口
 */
public interface SuperPermissListener {
    void initSuperPermissPass();// 初始化申请超管通过

    void clickSuperPermissPass();// 点击申请超管通过

    void superPermissStillClose();// 超管权限依然关闭

    void superPermissCancel();// 点击cancel
}
