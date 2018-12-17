package com.hiber.hiber;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/*
 * Created by qianli.ma on 2018/12/17 0017.
 */
public class LanguageHelper {

    /**
     * 获取当前语言
     *
     * @return 当前语言, 如"ru,es"
     */
    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 设置语言
     *
     * @param context  上下文
     * @param language 目标语言
     * @param country  目标区域
     */
    public static void setLanguage(Context context, String language, @Nullable String country) {
        Locale locale;
        if (!TextUtils.isEmpty(country)) {
            locale = new Locale(language);
        } else {
            locale = new Locale(language, country);
        }
        Resources resources = context.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context.getApplicationContext().createConfigurationContext(config);
        }
        Locale.setDefault(locale);
        resources.updateConfiguration(config, dm);
    }
}
