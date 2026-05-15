package cn.xpride.framework.utility;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;

public class HashUtil {

    public static String makeMd5(byte[] data){
        return makeHash(data,"MD5");
    }

    public static String makeSHA256(String text) {
        return makeHash(text.getBytes(),"SHA-256");
    }

    private static String makeHash(byte[] data, String algorithm){
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        StringBuilder output;
        int read;
        byte[] buffer = new byte[8192];
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            output = new StringBuilder(bigInt.toString(16));
            while (output.length() < 32) {
                output.insert(0, "0");
            }
        } catch (Exception e) {
            return "";
        }
        return output.toString();
    }

    public static String fileSizeConvert(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

}
