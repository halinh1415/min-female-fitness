package mint.woman.female.workout.fragment;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.databinding.FragmentVideoDemoBinding;
import mint.woman.female.workout.utils.WorkoutVideoCache;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoDemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoDemoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VIDEO = "video_demo";
    private static final String ARG_ACTION_ID = "action_id";

    // TODO: Rename and change types of parameters
    private String mVideoName;
    /** > 0: video lấy từ WorkoutVideoCache của buổi tập; 0: dùng temp.mp4 (màn chi tiết bài tập). */
    private int mActionId;
    FragmentVideoDemoBinding binding;
    String path;

    public VideoDemoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param videoName Parameter 1.
     * @return A new instance of fragment VideoDemoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoDemoFragment newInstance(String videoName) {
        VideoDemoFragment fragment = new VideoDemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO, videoName);
        fragment.setArguments(args);
        return fragment;
    }

    /** Dùng trong màn tập: video đã/đang giải mã ở WorkoutVideoCache, không còn temp.mp4. */
    public static VideoDemoFragment newInstance(String videoName, int actionId) {
        VideoDemoFragment fragment = newInstance(videoName);
        fragment.getArguments().putInt(ARG_ACTION_ID, actionId);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVideoName = getArguments().getString(ARG_VIDEO);
            mActionId = getArguments().getInt(ARG_ACTION_ID);
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        playVideo();
        super.onConfigurationChanged(newConfig);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
     
        binding = FragmentVideoDemoBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_video_demo, viewGroup);
       // ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onResume() {
        super.onResume();
        playVideo();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playVideo();
    }

    private void playVideo() {
        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
        });
        if (mActionId <= 0) {
            path = getContext().getCacheDir() + "/temp.mp4";
        } else {
            File video = WorkoutVideoCache.fileFor(requireContext(), DoWorkoutActivity.discover,
                    FitnessApplication.getApplication().getVersion(), mActionId);
            path = video.getPath();
            if (video.length() == 0) {
                WorkoutVideoCache.prepare(requireContext(), DoWorkoutActivity.discover,
                        FitnessApplication.getApplication().getVersion(), mActionId, ready -> {
                            if (ready == null || binding == null || getView() == null) return;
                            binding.videoView.setVideoURI(Uri.parse(path));
                            binding.videoView.start();
                        });
                return;
            }
        }
        binding.videoView.setVideoURI(Uri.parse(path));
        binding.videoView.start();
    }
}
