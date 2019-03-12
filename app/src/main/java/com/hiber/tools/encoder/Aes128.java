package com.hiber.tools.encoder;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Aes128 {

    /**
     * 编码格式
     */
    private static String UTF_8 = "utf-8";

    /**
     * 密钥格式
     */
    private static String KEY_SPEC = "AES";

    /**
     * 加密类型(CBC)
     */
    public static String Transformation_CBC = "AES/CBC/PKCS5Padding";

    /**
     * 加密类型(CFB)
     */
    public static String Transformation_CFB = "AES/CFB/PKCS5Padding";

    /**
     * AES加密
     *
     * @param ori            原文
     * @param key            密钥
     * @param iv             向量
     * @param transformation 加密类型(CBC/CFB)
     * @return 密文
     */
    public static String encrypt(String ori, byte[] key, byte[] iv, String transformation) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec skeySpec = new SecretKeySpec(key, KEY_SPEC);
        IvParameterSpec ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);
        byte[] en = cipher.doFinal(ori.getBytes(UTF_8));
        return new BASE64Encoder().encode(en);
    }

    /**
     * AES解密
     *
     * @param ori            密文
     * @param key            密钥
     * @param iv             向量
     * @param transformation 解密类型(CBC/CFB)
     * @return 原文
     */
    public static String decrypt(String ori, byte[] key, byte[] iv, String transformation) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, KEY_SPEC);
        Cipher cipher = Cipher.getInstance(transformation);
        IvParameterSpec ips = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);
        byte[] de = new BASE64Decoder().decodeBuffer(ori);
        byte[] oristr = cipher.doFinal(de);
        return new String(oristr, UTF_8);
    }
}
