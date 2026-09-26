package mint.woman.female.workout.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AppEncrypt {
    private static void a(StringBuffer stringBuffer, byte b) {
        stringBuffer.append("0123456789abcdef".charAt((b >> 4) & 15));
        stringBuffer.append("0123456789abcdef".charAt(b & 15));
    }

    public static String getName(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes(), 0, str.getBytes().length);
            return c(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String c(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer(bArr.length * 2);
        for (byte b : bArr) {
            a(stringBuffer, b);
        }
        return stringBuffer.toString();
    }
}