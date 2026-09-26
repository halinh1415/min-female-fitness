package mint.woman.female.workout.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadVoiceWorker extends Worker {

    public DownloadVoiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileListPath = getInputData().getString("FILE_LIST_PATH");
        String base_url = getInputData().getString("BASE_URL");
        String base_path = getInputData().getString("BASE_PATH");
        if (fileListPath == null || fileListPath.isEmpty()) {
            Log.e("DownloadVoiceWorker", "FILE_LIST_PATH is missing.");
            return Result.failure();
        }
        if (!isNetworkAvailable()) {
            Log.e("DownloadVoiceWorker", "No network available.");
            return Result.failure();
        }
        String[] fileNames;
        // Tạo thread pool, ví dụ sử dụng 10 luồng
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            File file = new File(fileListPath);
            if (!file.exists()) {
                Log.e("DownloadVoiceWorker", "File list JSON does not exist: " + fileListPath);
                return Result.failure();
            }
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line);
            }
            br.close();

            String jsonContent = jsonBuilder.toString();
            JSONArray jsonArray = new JSONArray(jsonContent);
            fileNames = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                fileNames[i] = jsonArray.getString(i);
            }
            for (int i = 0; i < fileNames.length; i++) {
                final String fileName = fileNames[i];
                final int index = i;
                Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {

                        String fileUrl = base_url + fileName;
                        File outputFile = new File(base_path, fileName);

                        if (outputFile.exists()) {
                            Log.d("DownloadVoiceWorker", "File exists: " + outputFile.getAbsolutePath());
                            return true;
                        }

                        boolean success = downloadFile(fileUrl, outputFile);
                        return success;
                    }
                });
                futures.add(future);
            }

            // Đợi tất cả các task hoàn thành và cập nhật tiến trình
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Boolean success = futures.get(i).get();
                    setProgressAsync(new Data.Builder().putInt("PROGRESS", i + 1).build());
                    if (!success) {
                        Log.e("DownloadVoiceWorker", "Download failed for file: " + fileNames[i]);
                        // Bạn có thể chọn return Result.retry() nếu một file thất bại
                    }
                } catch (Exception e) {
                    Log.e("DownloadVoiceWorker", "Error downloading file: " + fileNames[i], e);
                    // Có thể chọn return Result.retry() nếu cần retry toàn bộ
                }
            }
        } catch (Exception e) {
            Log.e("DownloadVoiceWorker", "Download error", e);
            return Result.failure();
        } finally {
            executor.shutdown();
        }

        return Result.success();
    }

    private boolean downloadFile(String fileUrl, File outputFile) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setDoInput(true);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("DownloadVoiceWorker", "HTTP error: " + conn.getResponseCode() + " for URL: " + fileUrl);
                return false;
            }

            try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                 FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                Log.d("DownloadVoiceWorker", "Downloaded: " + outputFile.getAbsolutePath());
                return true;
            }
        } catch (IOException e) {
            Log.e("DownloadVoiceWorker", "Error downloading file: " + fileUrl, e);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Kiểm tra kết nối mạng.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
