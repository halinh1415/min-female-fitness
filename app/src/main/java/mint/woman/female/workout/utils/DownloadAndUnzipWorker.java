package mint.woman.female.workout.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadAndUnzipWorker extends Worker {

    private static final String TAG = "DownloadAndUnzipWorker";

    public DownloadAndUnzipWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }


    @Override
    public Result doWork() {
        try {
            // Download the zip file
            String zipUrl = getInputData().getString("audio_url");
            String destination = getInputData().getString("destination_folder");


            Log.d(TAG, "url =" + zipUrl);
            URL url = new URL(zipUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000); // 10 giây
            urlConnection.setReadTimeout(12000);  // 15 giây
            InputStream input = url.openStream();

            File zipFile = new File(getApplicationContext().getCacheDir(), "temp.zip");
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
            int fileLength = urlConnection.getContentLength();
            int totalBytesRead = 0;
            FileOutputStream output = new FileOutputStream(zipFile);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
                totalBytesRead += length;
                int progress = (int) ((totalBytesRead * 100L) / fileLength);
                setProgressAsync(new Data.Builder().putInt("PROGRESS", progress).build());
            }

            output.close();
            input.close();
            File audioFile = new File(getApplicationContext().getFilesDir(), destination);
            if (!audioFile.exists()) {
                audioFile.mkdirs();
            }

            // Unzip the file
            unzip(zipFile, getApplicationContext().getFilesDir().getPath() + "/" + destination);

            // Delete the temporary zip file
            zipFile.delete();
            Log.d(TAG, "download audio success ");
            return Result.success();
        } catch (Exception e) {
            Log.d(TAG, "download audio fail =" + e);
            e.printStackTrace();
            return Result.failure();
        }
    }
//    @NonNull
//    @Override
//    public Result doWork() {
//        try {
//            // Download the zip file
//            String zipUrl = getInputData().getString("audio_url");
//            String destination = getInputData().getString("destination_folder");
//
//
//            Log.d(TAG, "url =" + zipUrl);
//            URL u = new URL(zipUrl);
//            URLConnection conn = u.openConnection();
//            int contentLength = conn.getContentLength();
//            File zipFile = new File(getApplicationContext().getCacheDir(), "temp.zip");
//            if (!zipFile.exists()) {
//                zipFile.createNewFile();
//            }
//
//            FileOutputStream fos = new FileOutputStream(zipFile);
//
//
//            int length;
//
//
//            if (isConnected(conn)) {
//                DataInputStream stream = new DataInputStream(u.openStream());
//                byte[] buffer = new byte[contentLength];
//                stream.readFully(buffer);
//                stream.close();
//                fos.write(buffer);
//                fos.flush();
//                fos.close();
//            }
//
//
//            File audioFile = new File(getApplicationContext().getFilesDir(), destination);
//            if (!audioFile.exists()) {
//                audioFile.mkdirs();
//            }
//
//            // Unzip the file
//            unzip(zipFile, getApplicationContext().getFilesDir().getPath() + "/" + destination);
//
//            // Delete the temporary zip file
//            zipFile.delete();
//            Log.d(TAG, "download audio success ");
//            return Result.success();
//        } catch (Exception e) {
//            Log.d(TAG, "download audio fail =" + e);
//            e.printStackTrace();
//            return Result.failure();
//        }
//    }

    public static boolean isConnected(URLConnection con) {
        try {
            con.setDoOutput(con.getDoOutput()); // throws IllegalStateException if connected
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    private void unzip(File zipFile, String destinationFolder) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(destinationFolder, ze.getName());
                ensureZipPathSafety(file, destinationFolder);

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
        } catch (Exception e) {
            Log.d(TAG, "unzip except:" + e.toString());
            e.printStackTrace();
        }
    }

    public void ensureZipPathSafety(final File outputFile, final String destDirectory) throws Exception {
        String destDirCanonicalPath = (new File(destDirectory)).getCanonicalPath();
        String outputFileCanonicalPath = outputFile.getCanonicalPath();
        if (!outputFileCanonicalPath.startsWith(destDirCanonicalPath)) {
            throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", destDirectory));
        }
    }
}