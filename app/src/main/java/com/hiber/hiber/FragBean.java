package com.hiber.hiber;

/**
 * Created by qianli.ma on 2018/7/23 0023.
 */

public class FragBean {
    public Class currentFragmentClass;// 是哪个fragment发起的跳转
    public Class targetFragmentClass;// 将前往那个fragment
    public Object attach;// 附件(额外自定义数据对象)

    public Class getTargetFragmentClass() {
        return targetFragmentClass;
    }

    public void setTargetFragmentClass(Class targetFragmentClass) {
        this.targetFragmentClass = targetFragmentClass;
    }

    public Class getCurrentFragmentClass() {
        return currentFragmentClass;
    }

    public void setCurrentFragmentClass(Class currentFragmentClass) {
        this.currentFragmentClass = currentFragmentClass;
    }

    public Object getAttach() {
        return attach;
    }

    public void setAttach(Object attach) {
        this.attach = attach;
    }

    @Override
    public String toString() {
        return "FragBean{" + "currentFragmentClass=" + currentFragmentClass + ", attach=" + attach + '}';
    }
}
