package mint.woman.female.workout.retrofit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final long CACHE_SIZE = 10 * 1024 * 1024;
    private static final int READ_TIMEOUT = 20000;
    private static final int WRITE_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String TIME_CACHE_ONLINE = "public, max-age = 6000";// 1 minute
    private static final String TIME_CACHE_OFFLINE = "public, only-if-cached, max-stale = 604800";//1 day
    private static Retrofit sRetrofit;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static OkHttpClient initClient(final Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .cache(new Cache(context.getCacheDir(), CACHE_SIZE))
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (isNetworkAvailable(context)) {
                        request = request
                                .newBuilder()
                                .header(CACHE_CONTROL, TIME_CACHE_ONLINE)
                                .build();
                    } else {
                        request = request
                                .newBuilder()
                                .header(CACHE_CONTROL, TIME_CACHE_OFFLINE)
                                .build();
                    }
                    HttpUrl httpUrl = request.url()

                            .newBuilder()
                            .addQueryParameter("music.json", "")
//                            .addQueryParameter("yogaapp/get_exercises.php", "")
//                            .addQueryParameter("yogaapp/get_explore.php", "")
                            .build();
                    Request.Builder requestBuilder = request
                            .newBuilder()
                            .url(httpUrl);
                    return chain.proceed(requestBuilder.build());
                });
//                .addNetworkInterceptor(chain -> {
//                    Response response = chain.proceed(chain.request());
//                    if (response.isSuccessful()) {
//                        Response.Builder newResponse = response.newBuilder();
//                        String contentType = response.header("Content-Type");
//
//                        String cryptedStream = response.body().string();
//                        String decrypted = null;
//                        if (contentType != null && contentType.contains("json") && cryptedStream != null && newResponse != null) {
//                            decrypted = Java_AES_Cipher.decrypt("0123456789abcdef", cryptedStream);
//                            try {
//                                newResponse.body(ResponseBody.create(MediaType.parse(contentType), decrypted));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        return newResponse.build();
//                    }
//                    return response;
//                });
        return builder.build();
    }

    public static Retrofit getClient(Context context, String baseUrl) {
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(initClient(context))
                    //   .addConverterFactory(SimpleXmlConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sRetrofit;
    }
}