package com.hiber.cons;

/*
 * Created by Administrator on 2021/01/022.
 */
public enum  PERTYPE {
    SUPER("SUPER"),// 超管窗口
    NORMAL("NORMAL"),// 普通权限窗口
    ;

    public String perType;

    PERTYPE(String perType) {

        this.perType = perType;
    }
}
