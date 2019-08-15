package com.hiber.cons;

/*
 * Created by qianli.ma on 2019/8/14 0014.
 */
public enum TimerState {

    /**
     * 开启
     */
    ON(1),

    /**
     * 关闭全部
     */
    OFF_ALL(2),

    /**
     * 关闭全部(但保留当前)
     */
    OFF_ALL_BUT_KEEP_CURRENT(3),

    /**
     * 开启但在pause时停止
     */
    ON_BUT_OFF_WHEN_PAUSE(4);

    private Integer timerState;

    TimerState(int timerState) {
        this.timerState = timerState;
    }
}
