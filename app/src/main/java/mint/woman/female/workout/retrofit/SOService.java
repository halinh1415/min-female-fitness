package mint.woman.female.workout.retrofit;

import java.util.List;

import mint.woman.female.workout.retrofit.MusicItem;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SOService {
    @GET("music.json")
    Call<List<MusicItem>> getMusic();
}
