package com.hiber.cons;
/*
 * Created by qianli.ma on 2018/8/2 0002.
 */

public class Cons {

    /**
     * 默认日志标记
     */
    public static String TAG = "Hiber";// 日志标记

    /**
     * 用于定位页面跳转行为
     */
    public static String TRACK = "RootTrack";// 流程标记

    /**
     * 用于定位弹出吐司的界面定位
     */
    public static String TOAST = "RootToast";// 吐司定位标记

    /**
     * 权限流程标记
     */
    public static String TAG2 = "PermissW";// 权限流程标记

    /**
     * Activity统一管理流程
     */
    public static String TAG3 = "ActivityLife";// 权限流程标记

    /**
     * 默认的工程目录名
     */
    public static String RootDir = "hiber";// 项目根目录名

    public static final int REQ_SUPER_INIT = 11010;// 超管权限请求码(初始化)
    public static final int REQ_SUPER_CLICK = 11020;// 超管权限请求码(点击时)
    public static final int REQ_SUPER_NONE = 11030;// 超管权限请求码(默认)
}
