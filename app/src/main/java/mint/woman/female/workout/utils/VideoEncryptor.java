package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;

public class VideoEncryptor {

    public static void encryptVideo(String inputFile, String outputFile, String key) {
        try {
            byte[] keyBytes = key.getBytes("UTF-8");
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile);
                 CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) > 0) {
                    cos.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void encryptByte(byte[] data, String filePath,String key) {
        try {
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] newData=  cipher.doFinal(data);
            File file = new File(filePath);
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(newData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void encryptVideoFromAssets(Context context, String inputFile, String outputFile, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            InputStream fis = context.getAssets().open(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);

            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) > 0) {
                    cos.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void decryptVideoFromAssets(Context context, String inputFile, String outputFile, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            InputStream fis = context.getAssets().open(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                fos.write(decryptedBytes);
            }
            byte[] decryptedBytes = cipher.doFinal();
            fos.write(decryptedBytes);

            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void decryptVideo(String inputFile, String outputFile, String key) {
        try {
            Log.d("HAHA","decrypt "+inputFile);
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                fos.write(decryptedBytes);
            }
            byte[] decryptedBytes = cipher.doFinal();
            fos.write(decryptedBytes);

            fis.close();
            fos.close();
        } catch (Exception e) {
            Log.d("HAHA",e.toString());
            e.printStackTrace();
        }
    }
    public static byte[] decryptGif(AssetManager assetManager,String inputFile, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            InputStream fis = assetManager.open(inputFile);
          //  FileInputStream fis = new FileInputStream(inputStream);
            byte[] arr = inputToByte(fis);
            byte[] decryptedBytes = cipher.doFinal(arr);

            fis.close();
            return decryptedBytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static byte[] decryptInput(String inputFile, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            Key secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream fis = new FileInputStream(inputFile);
            //  FileInputStream fis = new FileInputStream(inputStream);
            byte[] arr = inputToByte(fis);
            byte[] decryptedBytes = cipher.doFinal(arr);

            fis.close();
            return decryptedBytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static final byte[] inputToByte(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }
}
