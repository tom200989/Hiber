package com.hiber.hiber.language;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.hiber.tools.Sgg;

import java.util.Locale;

public class LangHelper {

    /*
     * 注意:
     * 从 Android N (7.0 , sdk = 24) 开始
     * 切换语言必须同时修改Activity以及Application的配置
     * */

    /**
     * 切换语言(由APP选项用户切换)
     *
     * @param context 上下文
     * @param lang    语言
     * @param cn      国家
     */
    public static void transfer(Context context, @NonNull String lang, @Nullable String cn) {
        // 1.提交语言到APP缓存
        Sgg.getInstance(context).putString(LangAttr.LANGUAGE, lang);
        Sgg.getInstance(context).putString(LangAttr.COUNTRY, cn == null ? "" : cn);
        Sgg.getInstance(context).putBoolean(LangAttr.IS_FROM_APP, true);
        // 2.从Activity方更新语言配置
        updateConfiguration(context, lang, cn);
        // 3.从Application方更新语言配置
        if (context instanceof Activity) {
            updateConfiguration(context.getApplicationContext(), lang, cn);
        }
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 初始化
     *
     * @param context 上下文
     */
    static void init(Context context) {
        // 1.从「系统」或者「APP缓存」拿到语言对象
        Locale locale = getLocale(context);
        String lang = locale.getLanguage();
        String cn = locale.getCountry();
        // 1.1.提交语言到APP缓存
        Sgg.getInstance(context).putString(LangAttr.LANGUAGE, lang);
        Sgg.getInstance(context).putString(LangAttr.COUNTRY, cn);
        // 2.从Activity方更新语言配置
        updateConfiguration(context, lang, cn);
        // 3.从Application方更新语言配置
        if (context instanceof Activity) {
            updateConfiguration(context.getApplicationContext(), lang, cn);
        }
    }

    /**
     * (核心) 更新语言配置
     *
     * @param context 上下文
     * @param lang    需要切换的语言
     * @param cn      地区
     */
    @SuppressLint("ObsoleteSdkInt")
    private static void updateConfiguration(Context context, String lang, String cn) {
        // 1.创建语言对象--> 如果不提供国家码则按照母系语种切换
        Locale locale;
        // 1.1.统一国家码大写
        if (TextUtils.isEmpty(cn)) {
            locale = new Locale(lang);
        } else {
            locale = new Locale(lang, cn.toUpperCase());
        }
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        // 2.根据版本进行参数设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
        Locale.setDefault(locale);// 该句必须要重新刷新
    }

    /**
     * 获取context
     * android N (7.0) 以上需要在Application:attachBaseContext()方法中绑定
     *
     * @param context 原始上下文,如: Application.this
     * @return 绑定语言的上下文
     */
    public static Context getContext(Context context) {
        // android N 以上需要在Application:attachBaseContext()方法中绑定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale locale = getLocale(context);
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(locale);
            configuration.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(configuration);
        }
        return context;
    }

    /**
     * 获取由APP保存的语言对象(以自己选择的优先)
     *
     * @param context 上下文
     * @return APP保存的语言对象
     */
    public static Locale getLocale(Context context) {

        Locale locale;
        // 1.获取系统语言(获取方式分为 >= 7.0 & < 7.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }

        // 2.获取上次APP保存的语言 (APP内部设置的语言优先)
        boolean isAppLang = Sgg.getInstance(context).getBoolean(LangAttr.IS_FROM_APP, false);
        if (isAppLang) {
            String lang = Sgg.getInstance(context).getString(LangAttr.LANGUAGE, locale.getLanguage());
            String cn = Sgg.getInstance(context).getString(LangAttr.COUNTRY, locale.getCountry());
            if (TextUtils.isEmpty(cn)) {
                locale = new Locale(lang);
            } else {
                locale = new Locale(lang, cn.toUpperCase());
            }
            return locale;// APP缓存的语言
        } else {
            return locale;// 系统语言
        }
    }
}
