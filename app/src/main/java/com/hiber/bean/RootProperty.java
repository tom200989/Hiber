package com.hiber.bean;
/*
 * Created by qianli.ma on 2018/8/1 0001.
 */

import java.io.Serializable;
import java.util.Arrays;

public class RootProperty implements Serializable {

    /**
     * 是否设置为全屏
     */
    private boolean isFullScreen;

    /**
     * 状态栏颜色 如:R.color.xxx
     */
    private int colorStatusBar;

    /**
     * 工程默认目录名 如:aaa
     */
    private String projectDirName;

    /**
     * 权限响应码 如0x999
     */
    private int permissionCode;

    /**
     * 权限组
     */
    private String[] permissions;

    /**
     * fragments的字节码数组 如:aaa.class bbb.class
     */
    private Class[] fragmentClazzs;

    /**
     * 是否保存Activity状态, 建议不保存
     */
    private boolean isSaveInstanceState;

    /**
     * 日志TAG
     */
    private String TAG;

    /**
     * Activity布局ID
     */
    private int layoutId;

    /**
     * framelayout ID
     */
    private int containId;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 是否开启lint检查
     */
    private boolean setLint;

    public boolean isSetLint() {
        return setLint;
    }

    public void setSetLint(boolean setLint) {
        this.setLint = setLint;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 当前是否全屏
     *
     * @return 当前是否全屏
     */
    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * 设置全屏
     *
     * @param fullScreen true:全屏
     */
    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    /**
     * 获取状态栏颜色
     *
     * @return 获取状态栏颜色
     */
    public int getColorStatusBar() {
        return colorStatusBar;
    }

    /**
     * 设置状态栏颜色
     *
     * @param colorStatusBar 状态栏颜色
     */
    public void setColorStatusBar(int colorStatusBar) {
        this.colorStatusBar = colorStatusBar;
    }

    /**
     * 获取工程默认目录名
     *
     * @return 获取工程默认目录名
     */
    public String getProjectDirName() {
        return projectDirName;
    }

    /**
     * 设置工程默认目录名
     *
     * @param projectDirName 工程目录名
     */
    public void setProjectDirName(String projectDirName) {
        this.projectDirName = projectDirName;
    }

    /**
     * 获取权限请求码
     *
     * @return 获取权限请求码
     */
    public int getPermissionCode() {
        return permissionCode;
    }

    /**
     * 设置权限请求回调码
     *
     * @param permissionCode 权限请求回调码
     */
    public void setPermissionCode(int permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 获取权限数组
     *
     * @return 获取权限数组
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * 设置权限数组
     *
     * @param permissions 权限数组
     */
    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    /**
     * 获取fragment字节数组
     *
     * @return 获取fragment字节数组
     */
    public Class[] getFragmentClazzs() {
        return fragmentClazzs;
    }

    /**
     * 设置fragment字节数组
     *
     * @param fragmentClazzs fragment字节数组
     */
    public void setFragmentClazzs(Class[] fragmentClazzs) {
        this.fragmentClazzs = fragmentClazzs;
    }

    /**
     * 当前是否为「非保存Activity状态」
     *
     * @return 当前是否为「非保存Activity状态」
     */
    public boolean isSaveInstanceState() {
        return isSaveInstanceState;
    }

    /**
     * 设置为「非保存Activity状态」
     *
     * @param saveInstanceState 非保存Activity状态
     */
    public void setSaveInstanceState(boolean saveInstanceState) {
        isSaveInstanceState = saveInstanceState;
    }

    /**
     * 获取当前日志标记
     *
     * @return 获取当前日志标记
     */
    public String getTAG() {
        return TAG;
    }

    /**
     * 设置日志标记
     *
     * @param TAG 日志标记
     */
    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    /**
     * 获取当前的布局ID
     *
     * @return 获取当前的布局ID
     */
    public int getLayoutId() {
        return layoutId;
    }

    /**
     * 设置当前的布局ID
     *
     * @param layoutId 布局ID
     */
    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    /**
     * 获取当前布局里的framelayoutID
     *
     * @return 获取当前布局里的framelayoutID
     */
    public int getContainId() {
        return containId;
    }

    /**
     * 设置当前布局里的framelayoutID
     *
     * @param containId 当前布局里的framelayoutID
     */
    public void setContainId(int containId) {
        this.containId = containId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RootProperty{");
        sb.append("\n").append("\t").append("isFullScreen =").append(isFullScreen);
        sb.append("\n").append("\t").append("colorStatusBar =").append(colorStatusBar);
        sb.append("\n").append("\t").append("projectDirName ='").append(projectDirName).append('\'');
        sb.append("\n").append("\t").append("permissionCode =").append(permissionCode);
        sb.append("\n").append("\t").append("permissions =").append(permissions == null ? "null" : Arrays.asList(permissions).toString());
        sb.append("\n").append("\t").append("fragmentClazzs =").append(fragmentClazzs == null ? "null" : Arrays.asList(fragmentClazzs).toString());
        sb.append("\n").append("\t").append("isSaveInstanceState =").append(isSaveInstanceState);
        sb.append("\n").append("\t").append("TAG ='").append(TAG).append('\'');
        sb.append("\n").append("\t").append("layoutId =").append(layoutId);
        sb.append("\n").append("\t").append("containId =").append(containId);
        sb.append("\n").append("\t").append("packageName ='").append(packageName).append('\'');
        sb.append("\n").append("\t").append("setLint =").append(setLint);
        sb.append("\n}");
        return sb.toString();
    }
}
