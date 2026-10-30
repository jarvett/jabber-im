package cn.xpride.framework.base;

import cn.xpride.framework.utility.AesUtil;
import cn.xpride.framework.utility.CustomBase62;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class MyTokenUtil {

    private static final String secret = "2021";

    private static byte[] key = md5();
    private static byte[] iv = new byte[16];

    static {
        for (int i = 0; i < key.length; i++) {
            iv[key.length - i - 1] = key[i];
        }
    }

    private static byte[] md5() {
        byte[] bytes = MyTokenUtil.secret.getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        int read;
        byte[] buffer = new byte[8192];
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return digest.digest();
        } catch (Exception e) {
            return null;
        }
    }

    private static final String userIdKey = "i";
    private static final String userTypeKey = "t";
    private static final String expiresAtStampKey = "e";

    public static MyToken saveToken(Long userId, Integer userType, Integer timeout) {
        Date expiresAt = Date.from(LocalDateTime.now().plusMinutes(timeout).atZone(ZoneId.systemDefault()).toInstant());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(userIdKey, CustomBase62.convert(userId));
        tokenMap.put(userTypeKey, userType.toString());
        tokenMap.put(expiresAtStampKey, CustomBase62.convert(expiresAt.getTime()));
        String token = CustomBase62.encode(AesUtil.encrypt(objToFormStr(tokenMap).getBytes(), key, iv));
        return new MyToken(userId, userType, token, expiresAt.getTime());
    }

    public static MyToken getToken(String token) {
        try {
            Map<String, String> obj = formStrToObj(new String(AesUtil.decrypt(CustomBase62.decode(token), key, iv)));
            long et = CustomBase62.revert(obj.get(expiresAtStampKey));
            if (et + 5 * 60 * 1000L < System.currentTimeMillis()) {//失效超5分钟了
                return null;
            }
            MyToken jwtToken = new MyToken();
            jwtToken.setUserId(CustomBase62.revert(obj.get(userIdKey)));
            jwtToken.setUserType(Integer.parseInt(obj.get(userTypeKey)));
            jwtToken.setExpiresAtStamp(et);
            return jwtToken;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String objToFormStr(Map<String, String> tokenMap) {
        try {
            StringBuilder sb = new StringBuilder();
            List<String> keyList = new ArrayList<>(tokenMap.keySet());
            Collections.shuffle(keyList);
            for (String key : keyList) {
                sb.append(key).append('@').append(tokenMap.get(key)).append('&');
            }
            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } catch (Throwable ignore) {
            return "";
        }
    }

    private static Map<String, String> formStrToObj(String str) {
        Map<String, String> resMap = new HashMap<>();
        String[] reArray = str.split("&");
        for (String tmp : reArray) {
            int eqIndex = tmp.indexOf("@");
            resMap.put(tmp.substring(0, eqIndex), tmp.substring(eqIndex + 1));
        }
        return resMap;
    }

}
