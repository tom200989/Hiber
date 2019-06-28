package com.hiber.tools;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.hiber.hiber.RootMAActivity;

/**
 * 吐司的工具类
 * Created by haide.yin(haide.yin@tcl.com) on 2019/6/21 13:24.
 */
public class ToastUtil {

    private static TimerHelper timerHelper;
    private static int DUR = 500;// 过渡动画时长

    public static void showSelfToast(RootMAActivity rootMAActivity, String tip, int duration) {

        // 1.清除隐藏的定时器
        if (timerHelper != null) {
            timerHelper.stop();
            timerHelper = null;
        }
        // 2.重新设置显示动画
        TextView tvToast = rootMAActivity.tvToast;
        tvToast.setText(tip);
        Animation showAnim = setToastAnimation(true, tvToast);
        showAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvToast.setAlpha(1);
                hideToast(rootMAActivity, tvToast, duration);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏吐司
     *
     * @param rootMAActivity 域
     * @param tvToast        控件
     * @param duration       时长 -- 决定显示时间
     */
    private static void hideToast(RootMAActivity rootMAActivity, TextView tvToast, int duration) {
        timerHelper = new TimerHelper(rootMAActivity) {
            @Override
            public void doSomething() {
                Animation animation = setToastAnimation(false, tvToast);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tvToast.setAlpha(0);
                        tvToast.clearAnimation();
                        if (timerHelper != null) {
                            timerHelper.stop();
                            timerHelper = null;
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        };
        timerHelper.startDelay(duration);
    }

    /**
     * 设置吐司动画
     *
     * @param isShow  T:渐显 F:渐隐
     * @param tvToast 显示控件
     * @return 动画
     */
    private static Animation setToastAnimation(boolean isShow, TextView tvToast) {
        tvToast.clearAnimation();
        AlphaAnimation al = new AlphaAnimation(isShow ? 0 : 1, isShow ? 1 : 0);
        al.setFillAfter(true);
        al.setDuration(DUR);
        tvToast.setAnimation(al);
        al.startNow();
        tvToast.startAnimation(al);
        return al;
    }
}
