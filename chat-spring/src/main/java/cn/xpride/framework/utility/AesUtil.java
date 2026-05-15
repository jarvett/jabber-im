package cn.xpride.framework.utility;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    //java默认的
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {
        try {
            return decrypt(data, key, iv, AES_CBC_PKCS5_PADDING, null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
        try {
            return encrypt(data, key, iv, AES_CBC_PKCS5_PADDING, null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 解密
     *
     * @param data
     * @param key
     * @param iv   一般使用CBC会用到
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key, byte[] iv, String algorithm, String provider) throws Exception {
        Cipher cipher = getCipher(algorithm, provider);
        algorithm = suffixAlgorithm(algorithm);
        if (null != iv) {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, algorithm), new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, algorithm));
        }
        return cipher.doFinal(data);
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @param iv   一般使用CBC会用到
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key, byte[] iv, String algorithm, String provider) throws Exception {
        Cipher cipher = getCipher(algorithm, provider);
        String keyAlgorithm = suffixAlgorithm(algorithm);
        if (null != iv) {
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, keyAlgorithm), new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, keyAlgorithm));
        }
        return cipher.doFinal(data);
    }

    private static Cipher getCipher(String algorithm, String provider) throws Exception {
        Cipher cipher;
        if (null != provider && !provider.isEmpty()) {
            cipher = Cipher.getInstance(algorithm, provider);
        } else {
            cipher = Cipher.getInstance(algorithm);
        }
        return cipher;
    }

    private static String suffixAlgorithm(String algorithm) {
        if (null != algorithm && algorithm.contains("/")) {
            return algorithm.substring(0, algorithm.indexOf("/"));
        } else {
            return algorithm;
        }
    }

}
