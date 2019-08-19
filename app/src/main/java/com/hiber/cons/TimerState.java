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
     * 需要开启, 但Hide以及pause的时候停止
     */
    ON_BUT_OFF_WHEN_HIDE_AND_PAUSE(2),

    /**
     * 开启但在pause时停止当前
     */
    ON_BUT_OFF_WHEN_PAUSE(3),

    /**
     * 需要开启,但在Hide的时候要停止
     */
    ON_BUT_OFF_WHEN_HIDE(4),

    /**
     * 直接关闭全部
     */
    OFF_ALL(5),

    /**
     * 关闭全部, 但保留当前
     */
    OFF_ALL_BUT_KEEP_CURRENT(6),

    /**
     * 开启但在pause时停止全部
     */
    OFF_ALL_BUT_KEEP_CURRENT_OFF_WHEN_PAUSE(7);


    private Integer timerState;

    TimerState(int timerState) {
        this.timerState = timerState;
    }
}
