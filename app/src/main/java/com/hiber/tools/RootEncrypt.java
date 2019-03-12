package com.hiber.tools;
/*
 * Created by qianli.ma on 2018/8/8 0008.
 */


import com.hiber.cons.Cons;
import com.hiber.tools.encoder.Aes128;
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
            Lgg.t(Cons.TAG).vv("Des-encrypt error: " + e.getMessage());
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
            Lgg.t(Cons.TAG).vv("Des-descrypt error: " + e.getMessage());
        }
        return miwen;
    }

    /**
     * AES加密(CBC)
     *
     * @param ori 原文
     * @param key 密钥
     * @param iv  向量
     * @return 密文
     */
    public static String aes_encrypt_CBC(String ori, byte[] key, byte[] iv) {
        try {
            return Aes128.encrypt(ori, key, iv, Aes128.Transformation_CBC);
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Cons.TAG).vv("Aes-encrypt CBC error: " + e.getMessage());
        }
        return "";
    }

    /**
     * AES加密(CFB)
     *
     * @param ori 原文
     * @param key 密钥
     * @param iv  向量
     * @return 密文
     */
    public static String aes_encrypt_CFB(String ori, byte[] key, byte[] iv) {
        try {
            return Aes128.encrypt(ori, key, iv, Aes128.Transformation_CFB);
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Lgg.TAG).vv("Aes-encrypt CFB error: " + e.getMessage());
        }
        return "";
    }

    /**
     * AES解密(CBC)
     *
     * @param ori 密文
     * @param key 密钥
     * @param iv  向量
     * @return 原文
     */
    public static String aes_decrypt_CBC(String ori, byte[] key, byte[] iv) {
        try {
            return Aes128.decrypt(ori, key, iv, Aes128.Transformation_CBC);
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Cons.TAG).vv("Aes-descrypt error: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * AES解密(CFB)
     *
     * @param ori 密文
     * @param key 密钥
     * @param iv  向量
     * @return 原文
     */
    public static String aes_decrypt_CFB(String ori, byte[] key, byte[] iv) {
        try {
            return Aes128.decrypt(ori, key, iv, Aes128.Transformation_CFB);
        } catch (Exception e) {
            e.printStackTrace();
            Lgg.t(Cons.TAG).vv("Aes-descrypt error: " + e.getMessage());
        }
        return "";
    }
}
