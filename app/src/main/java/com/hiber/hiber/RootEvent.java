package com.hiber.hiber;

import org.greenrobot.eventbus.EventBus;

/*
 * Created by qianli.ma on 2019/4/28 0028.
 */
public class RootEvent {

    /**
     * 发送eventbus事件
     *
     * @param obj     数据
     * @param isStick 是否为粘性
     */
    public static void sendEvent(Object obj, boolean isStick) {
        // 1.判断传送模式(stick or not)
        if (isStick) {
            EventBus.getDefault().postSticky(obj);
        } else {
            EventBus.getDefault().post(obj);
        }
    }
}
