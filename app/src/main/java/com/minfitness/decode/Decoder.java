package com.minfitness.decode;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class Decoder {
    public static boolean isSuccess = false;
    static {
        try {
            System.loadLibrary("workout");
            isSuccess = true;
        } catch (Throwable th) {
            isSuccess = false;
            th.printStackTrace();
        }
    }

    public static native byte[] encryptFromAssets(AssetManager assetManager, String filename);
    public static native byte[] decryptFromAssets(AssetManager assetManager, String filename);

    public static native byte[] decrypt(byte[] data);
    public static native byte[] encrypt(byte[] data);
    public static native String getAllData(AssetManager assetManager,String fileName);

    public static byte[] convertStreamToByteArray(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[1024];
        int length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
    public static void decryptToFile(AssetManager assetManager, String fileName,String out_path){
        if (isSuccess){
            try {
                FileOutputStream fos = new FileOutputStream(out_path);
                byte[] decryptBytes = decryptFromAssets(assetManager,fileName);
                fos.write(decryptBytes);

                fos.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void decryptInputToFile(String inputFile,String out_path){
        if (isSuccess){
            try {
                byte[] input = convertStreamToByteArray(new FileInputStream(inputFile));
                FileOutputStream fos = new FileOutputStream(out_path);
                byte[] decryptBytes = decrypt(input);
                fos.write(decryptBytes);

                fos.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void encryptStringToFile(byte[] data,String out_path){
        if (isSuccess){
            try {
                byte[] input = encrypt(data);
                FileOutputStream fos = new FileOutputStream(out_path);
               // byte[] decryptBytes = encrypt(input);
                fos.write(input);

                fos.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static byte[] decryptInput(String inputFile){
        if (isSuccess){
            try {
                byte[] input = convertStreamToByteArray(new FileInputStream(inputFile));
                return decrypt(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static native String getKey();
}
