package mint.woman.female.workout.retrofit;

import android.content.Context;

public class ApiUtils {
    public static final String BASE_URL = "https://min-fitness.uk/min-fitness/female_workout/";
    public static final String BASE_APP_URL= "https://min-fitness.uk/";
    public static final String BASE_URL_IMAGES = "https://min-fitness.uk/min-fitness/female_workout/images/";
    public static final String BASE_URL_BRAND = "https://min-fitness.uk/min-fitness/female_workout/";

    public static SOService getSOService(Context context) {
        return RetrofitClient.getClient(context, BASE_URL).create(SOService.class);
    }
}
