package com.hiber.hiber;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hiber.hiber.language.RootApp;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by qianli.ma on 2019/1/28 0028.
 */
public class ActivityHelper implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        RootApp.activities.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        RootApp.activities.remove(activity);
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 杀死除共有部分的MainActivity外的Activity
     *
     * @param keepActivitys 需要保持存活的Activity
     */
    protected static void killActivitys(Class... keepActivitys) {

        // 1.创建记录需要保留的Activity索引的集合
        List<Activity> keepTemp = new ArrayList<>();

        // 2.遍历找到需要保留的Activity
        for (Activity currActivity : RootApp.activities) {
            for (Class keep : keepActivitys) {
                // 2.1.比较类名
                String currentName = currActivity.getClass().getName();
                String keepName = keep.getName();
                if (currentName.equalsIgnoreCase(keepName)) {
                    keepTemp.add(currActivity);
                }
            }
        }

        // 3.从当前集合中清理出需要保留的部分
        for (Activity activity : keepTemp) {
            RootApp.activities.remove(activity);
        }

        // 4.将剩下的部分进行finish
        for (Activity activity : RootApp.activities) {
            activity.finish();
        }
    }

    /**
     * 清除全部的activity
     */
    protected static void killAllActivity() {
        // 0.先finish
        for (Activity activity : RootApp.activities) {
            activity.finish();
        }
        // 1.在清空集合
        RootApp.activities.clear();
    }
}
