package com.hiber.bean;

import java.io.Serializable;

/*
 * Created by qianli.ma on 2019/2/25 0025.
 */
public class SkipBean implements Serializable {

    /**
     * 当前fragment绝对路径
     */
    private String currentFragmentClassName;

    /**
     * 目标Activity绝对路径
     */
    private String targetActivityClassName;

    /**
     * 目标fragment绝对路径
     */
    private String targetFragmentClassName;

    /**
     * 需要携带的附件
     */
    private Object attach;

    /**
     * 是否重载目标页面
     */
    private boolean isTargetReload;

    /**
     * 是否结束当前的Activity
     */
    private boolean isCurrentACFinish;

    /**
     * 附件的字节码
     */
    private Class attachClass;

    public Class getAttachClass() {
        return attachClass;
    }

    public void setAttachClass(Class attachClass) {
        this.attachClass = attachClass;
    }

    public SkipBean() {
    }

    public String getCurrentFragmentClassName() {
        return currentFragmentClassName;
    }

    public void setCurrentFragmentClassName(String currentFragmentClassName) {
        this.currentFragmentClassName = currentFragmentClassName;
    }

    public boolean isCurrentACFinish() {
        return isCurrentACFinish;
    }

    public void setCurrentACFinish(boolean currentACFinish) {
        isCurrentACFinish = currentACFinish;
    }

    public boolean isTargetReload() {
        return isTargetReload;
    }

    public void setTargetReload(boolean targetReload) {
        isTargetReload = targetReload;
    }

    public String getTargetActivityClassName() {
        return targetActivityClassName;
    }

    public void setTargetActivityClassName(String targetActivityClassName) {
        this.targetActivityClassName = targetActivityClassName;
    }

    public String getTargetFragmentClassName() {
        return targetFragmentClassName;
    }

    public void setTargetFragmentClassName(String targetFragmentClassName) {
        this.targetFragmentClassName = targetFragmentClassName;
    }

    public Object getAttach() {
        return attach;
    }

    public void setAttach(Object attach) {
        this.attach = attach;
    }
}
