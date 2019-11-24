package com.xm.comment_utils.encry;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5通用类
 */
public class MD5 {

    /**
     * 16位加密
     * @param text
     * @param key
     * @return
     * @throws Exception
     */
    public static String md516(String text, String key){
        return md5(text,key).substring(8,24);
    }

    /**
     * MD5方法
     *
     * @param text 明文
     * @param key  密钥
     * @return 密文
     * @throws Exception
     */
    public static String md5(String text, String key){
        //加密后的字符串
        String encodeStr = DigestUtils.md5Hex(text + key);
        return encodeStr;
    }

    /**
     * MD5验证方法
     *
     * @param text 明文
     * @param key  密钥
     * @param md5  密文
     * @return true/false
     * @throws Exception
     */
    public static boolean verify(String text, String key, String md5){
        //根据传入的密钥进行验证
        String md5Text = md5(text, key);
        if (md5Text.equalsIgnoreCase(md5)) {
            return true;
        }
        return false;
    }
}