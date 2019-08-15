package com.hiber.tools;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Created by qianli.ma on 2019/8/12 0012.
 */
@SuppressWarnings({"unchecked", "CommitPrefEdits"})
public class ShareUtils {

    public static String spName = "WifiLink";// 默认名
    private static SharedPreferences.Editor editor;
    private static SharedPreferences sp;
    private static Context cts;
    private static String TAG = "ShareUtils";

    /**
     * 初始化shareperfrence相关属性
     *
     * @param context 域
     */
    public static void init(Context context) {
        cts = context;
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /**
     * 存入 (存值仅支持[int][float][long][boolean][String])
     *
     * @param key   key
     * @param value value
     */
    public static void set(String key, Object value) {

        // 0.判断类型是否正确
        if (!isCorrectType(value)) {
            Lgg.t(TAG).ee("Please set the correct type value like [int][float][long][boolean][String]");
            throw new RuntimeException("Please set the correct type value like [int][float][long][boolean][String]");

        } else {

            // 1.判断是否有初始化
            if (cts == null) {
                Lgg.t(TAG).ee("Please call ShareUtils.init() First");
                throw new RuntimeException("Please call ShareUtils.init() First");

            } else {
                // 2.按照类型提交
                if (value instanceof Integer) {
                    editor.putInt(key, (int) value);

                } else if (value instanceof Float) {
                    editor.putFloat(key, (float) value);

                } else if (value instanceof Long) {
                    editor.putLong(key, (long) value);

                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);

                } else if (value instanceof String) {
                    editor.putString(key, (String) value);

                }
                editor.apply();
            }
        }
    }

    /**
     * 取出 (默认值仅支持[int][float][long][boolean][String])
     *
     * @param key          key
     * @param defaultValue default value
     * @param <T>          指定类型
     * @return 返回指定类型对象
     */
    public static <T> T get(String key, T defaultValue) {

        try {

            // 0.判断类型是否正确
            if (!isCorrectType(defaultValue)) {
                Lgg.t(TAG).ee("Please set the correct type value like [int][float][long][boolean][String]");
                throw new RuntimeException("Please set the correct type value like [int][float][long][boolean][String]");

            } else {

                // 1.判断是否有初始化
                if (cts == null) {
                    Lgg.t(TAG).ee("Please call ShareUtils.init() First");
                    throw new RuntimeException("Please call ShareUtils.init() First");

                } else {

                    // 2.开始获取
                    if (defaultValue instanceof Integer) {
                        int value = sp.getInt(key, (Integer) defaultValue);
                        return (T) (Integer) value;

                    } else if (defaultValue instanceof Float) {
                        float value = sp.getFloat(key, (Float) defaultValue);
                        return (T) (Float) value;

                    } else if (defaultValue instanceof Long) {
                        long value = sp.getLong(key, (Long) defaultValue);
                        return (T) (Long) value;

                    } else if (defaultValue instanceof Boolean) {
                        boolean value = sp.getBoolean(key, (Boolean) defaultValue);
                        return (T) (Boolean) value;

                    } else if (defaultValue instanceof String) {
                        String value = sp.getString(key, (String) defaultValue);
                        return (T) value;

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(TAG).ee("Please set the correct defalut value type like [int][float][long][boolean][String]");
            throw new RuntimeException("Please set the correct defalut value type like [int][float][long][boolean][String]");
        }

        return null;
    }

    /**
     * 判断传入的类型是否为基本类型
     *
     * @param value 对象
     */
    private static boolean isCorrectType(Object value) {
        if (value != null) {
            boolean isInt = value instanceof Integer;
            boolean isFloat = value instanceof Float;
            boolean isLong = value instanceof Long;
            boolean isBoolean = value instanceof Boolean;
            boolean isString = value instanceof String;
            return isInt || isFloat || isLong || isBoolean || isString;
        } else {
            return false;
        }
    }
}
