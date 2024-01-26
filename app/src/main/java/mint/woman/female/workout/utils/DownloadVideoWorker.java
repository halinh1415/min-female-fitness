package mint.woman.female.workout.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadVideoWorker extends Worker {
    public static final String KEY_URL = "URL_LIST";
    public static final String KEY_FILE_PATH = "FILE_PATH_LIST";

    public DownloadVideoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static boolean isConnected(URLConnection con) {
        try {
            con.setDoOutput(con.getDoOutput()); // throws IllegalStateException if connected
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        String[] urls = getInputData().getStringArray(KEY_URL);
        String[] filePaths = getInputData().getStringArray(KEY_FILE_PATH);

        try {
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                String filePath = filePaths[i];

                URL u = new URL(url);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();

                if (isConnected(conn)) {
                    DataInputStream stream = new DataInputStream(u.openStream());
                    byte[] buffer = new byte[contentLength];
                    stream.readFully(buffer);
                    stream.close();
                    DataOutputStream fos = new DataOutputStream(new FileOutputStream(new File(filePath)));
                    fos.write(buffer);
                    fos.flush();
                    fos.close();
                } else {
                    ListenableWorker.Result.failure();
                }
                setProgressAsync(new Data.Builder().putInt("PROGRESS", i).build());
            }
        } catch (FileNotFoundException e) {
            return ListenableWorker.Result.failure(); // swallow a 404
        } catch (IOException e) {
            return ListenableWorker.Result.failure(); //
        }
        return ListenableWorker.Result.success();
    }
}
