package com.hiber.cons;

/*
 * Created by Administrator on 2021/01/022.
 * 超级管理员权限 - 文件的超权操作
 */
public enum SUPERMISSION {

    INIT(Cons.REQ_SUPER_INIT), // 初始化申请
    CLICK(Cons.REQ_SUPER_CLICK),// 点击时申请
    NONE(Cons.REQ_SUPER_NONE),// 不申请
    ;

    public int permission;

    SUPERMISSION(int permission) {
        this.permission = permission;
    }
}
