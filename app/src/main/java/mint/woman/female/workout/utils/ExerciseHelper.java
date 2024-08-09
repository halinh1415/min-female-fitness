package mint.woman.female.workout.utils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseHelper {
    private static ExerciseHelper instance;
    public static synchronized ExerciseHelper getInstance() {
        if (instance == null) {
            instance = new ExerciseHelper();
        }
        return instance;
    }
    public String getUrl(int actionId){
        String json = FirebaseRemoteConfig.getInstance().getString("demo_url");
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(""+actionId))
                return jsonObject.getString(""+actionId);
            else return "a";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "a";
    }

}
