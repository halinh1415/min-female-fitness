package mint.woman.female.workout.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static boolean doesFileExist(Context context, String fileName) {
        // Get the path to the app's internal files directory
        File internalFilesDir = context.getFilesDir();

        // Create a File object with the desired file name
        File file = new File(internalFilesDir, fileName);

        // Check if the file exists
        return file.exists();
    }
    public static void unzipFromAssets(Context context, String zipFileName, String outputDirectory) {

        try {
            InputStream inputStream = context.getAssets().open(zipFileName);
            File directory = new File(outputDirectory);

            // Create output directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(outputDirectory, ze.getName());
                if (ze.isDirectory()) {
                    file.mkdirs();
                } else {
                    FileOutputStream fout = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                }
                zis.closeEntry();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getLocaleApp(Context context) {
        String loc = MyPreference.newInstance(context).getLanguage();
        Locale locale = new Locale(loc);
        if (locale.getLanguage().contains("zh")) {
            return locale.getLanguage();
        }
        String language = locale.getLanguage();
        return language.substring(0, 2);
    }
    private static String convertByteArrayToString(byte[] byteArray) {
        // Use the String constructor that takes a byte array and character encoding
        return new String(byteArray, StandardCharsets.UTF_8);
    }

}