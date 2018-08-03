package com.hiber.bean;
/*
 * Created by qianli.ma on 2018/8/1 0001.
 */

import java.util.Arrays;

public class RootProperty {

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

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public int getColorStatusBar() {
        return colorStatusBar;
    }

    public void setColorStatusBar(int colorStatusBar) {
        this.colorStatusBar = colorStatusBar;
    }

    public String getProjectDirName() {
        return projectDirName;
    }

    public void setProjectDirName(String projectDirName) {
        this.projectDirName = projectDirName;
    }

    public int getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(int permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public Class[] getFragmentClazzs() {
        return fragmentClazzs;
    }

    public void setFragmentClazzs(Class[] fragmentClazzs) {
        this.fragmentClazzs = fragmentClazzs;
    }

    public boolean isSaveInstanceState() {
        return isSaveInstanceState;
    }

    public void setSaveInstanceState(boolean saveInstanceState) {
        isSaveInstanceState = saveInstanceState;
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public int getContainId() {
        return containId;
    }

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
        sb.append("\n}");
        return sb.toString();
    }
}
