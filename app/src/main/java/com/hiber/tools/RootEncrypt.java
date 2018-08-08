package com.hiber.tools;
/*
 * Created by qianli.ma on 2018/8/8 0008.
 */

import com.hiber.tools.encoder.Des;

public class RootEncrypt {

    /**
     * 加密
     *
     * @param mingwen 明文
     * @return 密文
     */
    public static String des_encrypt(String mingwen) {
        try {
            return Des.encrypt(mingwen, "roothiber");
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Lgg.TAG).vv("encrypt error: " + e.getMessage());
        }
        return mingwen;
    }

    /**
     * 解密
     *
     * @param miwen 密文
     * @return 明文
     */
    public static String des_descrypt(String miwen) {
        try {
            return Des.decrypt(miwen, "roothiber");
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Lgg.TAG).vv("descrypt error: " + e.getMessage());
        }
        return miwen;
    }
}
