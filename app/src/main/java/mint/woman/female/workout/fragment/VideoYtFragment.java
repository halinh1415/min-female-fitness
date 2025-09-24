package mint.woman.female.workout.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import mint.woman.female.workout.R;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.databinding.FragmentVideoYtBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoYtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoYtFragment extends Fragment {

    FragmentVideoYtBinding binding;
    private static final String ARG_URL = "video_url";

    // TODO: Rename and change types of parameters
    private String mExerciseUrl;
//    @BindView(R.id.youtube_player_view)
//    YouTubePlayerView youTubePlayerView;
//    @BindView(R.id.btn_open_yt)
//    Button mOpenYtBtn;


    public VideoYtFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment VideoYtFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoYtFragment newInstance(String url) {
        VideoYtFragment fragment = new VideoYtFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExerciseUrl = getArguments().getString(ARG_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =  FragmentVideoYtBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLifecycle().addObserver(binding.youtubePlayerView);



        String[] parts  = mExerciseUrl.split("&t=");
        String videoId = parts[0];

        // The second part is the time, if it exists
        String time = parts.length > 1 ? parts[1] : "0";
        float startTime = 0f;
        try {
            startTime = Float.parseFloat(time.replace("s", ""));
        }catch (Exception e){
            startTime = 0f;
            e.printStackTrace();
        }
        float finalTime = startTime;

        binding.youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                if (youTubePlayer!= null) {
                    //    DefaultPlayerUiController defaultPlayerUiController = new DefaultPlayerUiController(youTubePlayerView, youTubePlayer);
                    // youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.getRootView());
                    youTubePlayer.loadVideo(videoId, finalTime);
                    youTubePlayer.play();
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
                super.onError(youTubePlayer, error);
                Log.d("HAHA",""+error.toString());
                if (error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER) {
//                    String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
//                    intent.putExtra("force_fullscreen", true);
//                    startActivity(intent);
                    binding.youtubePlayerView.setVisibility(View.INVISIBLE);
                }
            }
        });
        binding.youtubePlayerView.enableBackgroundPlayback(true);
        binding.btnOpenYt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openYoutubeVideo(videoId);
            }
        });

        //  YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) view.findViewById(R.id.youtube_fragment);
    }
    void openYoutubeVideo(String videoId){
        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        intent.putExtra("force_fullscreen", true);
        startActivity(intent);
    }
    public void openYoutubeChannel() {
        FirebaseAnalytics.getInstance(getContext()).logEvent("click_open_youtube_channel_dm", new Bundle());
        AppOpenManager.getInstance().disableAdResumeByClickAction();

        String youtubeChannelId = "UCIUIxNiBFEqjHmnoPgpeKUw";

        // Try to open the YouTube app directly
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("vnd.youtube://channel/" + youtubeChannelId));

        // If YouTube app is not installed, open in browser
        if (intent.resolveActivity(getContext().getPackageManager()) == null) {
            intent.setData(Uri.parse("https://www.youtube.com/channel/" + youtubeChannelId));
        }

        try {
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        //  mYouTubePlayer.pause();
    }


}