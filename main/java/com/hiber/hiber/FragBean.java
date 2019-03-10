package com.hiber.hiber;

/**
 * Created by qianli.ma on 2018/7/23 0023.
 */

public class FragBean {
    public Class currentFragmentClass;// 是哪个fragment发起的跳转
    public Class targetFragmentClass;// 将前往那个fragment
    public Object attach;// 附件(额外自定义数据对象)

    /**
     * @return 获取即将前往的fragment字节码
     */
    public Class getTargetFragmentClass() {
        return targetFragmentClass;
    }

    /**
     * 设置即将前往的fragment字节码
     * @param targetFragmentClass 即将前往的fragment字节码
     */
    public void setTargetFragmentClass(Class targetFragmentClass) {
        this.targetFragmentClass = targetFragmentClass;
    }

    /**
     * @return 获取当前fragment字节码
     */
    public Class getCurrentFragmentClass() {
        return currentFragmentClass;
    }

    /**
     * 设置当前fragment字节码
     * @param currentFragmentClass 当前fragment字节码
     */
    public void setCurrentFragmentClass(Class currentFragmentClass) {
        this.currentFragmentClass = currentFragmentClass;
    }

    /**
     * @return 获取附件
     */
    public Object getAttach() {
        return attach;
    }

    /**
     * 设置附件
     * @param attach 附件(可以为任意对象)
     */
    public void setAttach(Object attach) {
        this.attach = attach;
    }

    @Override
    public String toString() {
        return "FragBean{" + "currentFragmentClass=" + currentFragmentClass + ", attach=" + attach + '}';
    }
}
