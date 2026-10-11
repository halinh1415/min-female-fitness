package mint.woman.female.workout.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;

import mint.woman.female.workout.custom.DialogDemoWorkout;
import mint.woman.female.workout.databinding.FragmentRestBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.WorkoutVideoCache;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.MyPreference;

@SuppressLint("NonConstantResourceId")
public class RestFragment extends Fragment implements View.OnClickListener {
    FragmentRestBinding binding;
//    @BindView(R.id.videoView)
//    Custo binding.videoView  binding.videoView;
//    @BindView(R.id.txt_workout_count)
//    TextView binding.txtWorkoutCount;
//    @BindView(R.id.tv_exercise_name)
//    TextView  binding.tvExerciseName;
//    @BindView(R.id.txt_exercise_count)
//    TextView  binding.txtExerciseCount;
//    @BindView(R.id.rest_progress)
//    TextView binding.restProgress;
//    @BindView(R.id.adView)
//    AdView mAdBanner;
//    @BindView(R.id.txt_break)
//    TextView binding.txtBreak;
//    @BindView(R.id.tv_sub)
//    TextView binding.tvSub;
    private int mRestTime, mCurrentTime;
    private String exerciseProgressCount;
    private MyPreference myPreference;
    private CountDownTimer countDownTimer;
    private String videoPath;
    private ExerciseObject mExerciseObject;
    private PlanObject.ActionObject mActionObject;
    private OnRestFragmentInteractionListener mListener;

    private WorkoutViewModel mWorkoutViewModel;

    public RestFragment() {
    }

    public static RestFragment newInstance(PlanObject.ActionObject actionObject, int restTime, String exerciseProgressCount) {
        RestFragment fragment = new RestFragment();
        Bundle args = new Bundle();
        args.putParcelable("action_object", actionObject);
        args.putInt("rest", restTime);
        args.putString("count", exerciseProgressCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            mActionObject = getArguments().getParcelable("action_object");
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover, DoWorkoutActivity.version).get(mActionObject.actionId);
            mRestTime = getArguments().getInt("rest");
            mCurrentTime = mRestTime;
            exerciseProgressCount = getArguments().getString("count");
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("ACTION_ID", mActionObject.actionId);
            crashlytics.setCustomKey("TIME", mActionObject.time);
        }


        myPreference = new MyPreference(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Bọc trong FrameLayout để xoay màn hình nạp lại đúng layout dọc / ngang và cập nhật binding
        FrameLayout frameLayout = new FrameLayout(requireContext());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Giải mã video ở luồng nền (trước đây chạy trên main thread mỗi lần sang bài -> nguy cơ ANR).
        WorkoutVideoCache.prepare(requireContext(), DoWorkoutActivity.discover,
                FitnessApplication.getApplication().getVersion(), mActionObject.actionId, video -> {
                    if (video == null || binding == null || getView() == null) return;
                    videoPath = video.getPath();
                    binding.videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
                    binding.videoView.setVideoURI(Uri.parse(videoPath));
                    binding.videoView.start();
                });

        if (mRestTime == 123) {
            readyToGo = true;
            mRestTime = 15;
            mCurrentTime = 15;
        }
        bindContent();
        initTimer(mCurrentTime);
        // if (mListener != null) mListener.startRestTime(mRestTime);
        requestNewAdBanner();
    }

    /** Lần đầu vào buổi tập (rest = 123): tiêu đề "Sẵn sàng" thay cho "Nghỉ". */
    private boolean readyToGo = false;

    /** Đổ dữ liệu + gắn sự kiện vào view hiện tại (gọi lại sau khi xoay màn hình). */
    @SuppressLint("SetTextI18n")
    private void bindContent() {
        if (readyToGo) binding.txtBreak.setText(getString(R.string.wp_ready_to_go));
        binding.tvExerciseName.setText(mExerciseObject.getName());
        if (mActionObject.getUnit().contains("s")) {
            binding.tvSub.setVisibility(View.GONE);
            binding.txtExerciseCount.setText(FitnessConstUtils.toTime(mActionObject.time));
        } else {
            binding.txtExerciseCount.setText("x " + mActionObject.time);
            if (mExerciseObject.getAlternation()) {
                binding.tvSub.setVisibility(View.VISIBLE);
                binding.tvSub.setText(getString(R.string.wp_each_side) + " x" + (mActionObject.time / 2));
            } else {
                binding.tvSub.setVisibility(View.GONE);
            }
        }
        binding.txtWorkoutCount.setText(exerciseProgressCount);
        binding.restProgress.setText(FitnessConstUtils.toTime(mCurrentTime));
        updateRing(false);
        binding.txtSkip.setOnClickListener(this);
        binding.txtAddTime.setOnClickListener(this);
        binding.tvExerciseName.setOnClickListener(this);
    }

    /** Vòng đếm ngược: phần còn lại / tổng thời gian nghỉ (kể cả +15s). */
    private void updateRing(boolean animate) {
        int total = Math.max(1, mRestTime);
        int value = Math.max(0, Math.min(100, mCurrentTime * 100 / total));
        binding.restRing.setProgressCompat(value, animate);
    }

    private void requestNewAdBanner() {
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.adView.loadAd(adRequest);
        }
    }

    private void initTimer(int time) {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long l) {
                if (mCurrentTime != (int) (l / 1000)) {
                    mCurrentTime = (int) (l / 1000);

                    binding.restProgress.setText(FitnessConstUtils.toTime(mCurrentTime));
                    updateRing(true);
                    if (mListener != null) {
                        mListener.updateRestTime(mCurrentTime);
                    }
                }

            }

            @Override
            public void onFinish() {
                mCurrentTime = 0;
                binding.restProgress.setText(FitnessConstUtils.toTime(0));
                updateRing(true);
                if (mListener != null) {
                    mListener.onFinishRest();
                }
            }
        };
        countDownTimer.start();
    }


    public void onPauseRest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
         binding.videoView.pause();
    }

    public void onResumeRest() {
        initTimer(mCurrentTime);
        // videoPath chỉ có sau khi luồng nền giải mã xong; chưa xong thì callback tự phát video.
        if (videoPath != null) {
            binding.videoView.setVideoURI(Uri.parse(videoPath));
            binding.videoView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
            });
            binding.videoView.start();
        }
        bindContent();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }
    private void showDialogDemoWorkout() {
        if (!isAdded() || getParentFragmentManager().findFragmentByTag("images") != null) return;

        DialogDemoWorkout dialogDemoWorkout = DialogDemoWorkout.newInstance(mActionObject.actionId);
        dialogDemoWorkout.show(getParentFragmentManager(), "images");
    }
    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRestFragmentInteractionListener) {
            mListener = (OnRestFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if ( binding.videoView != null) {
             binding.videoView.stopPlayback();
        }
    }

    private void releaseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        binding = FragmentRestBinding.inflate(inflater, viewGroup, false);
        viewGroup.addView(binding.getRoot());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        bindContent();
        requestNewAdBanner();
        if (videoPath == null) return;
        binding.videoView.setVideoURI(Uri.parse(videoPath));
        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
            }
        });
        binding.videoView.start();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_skip:
                releaseTimer();
                if (mListener != null) {
                    mListener.onFinishRest();
                }
                break;
            case R.id.txt_add_time:
                mRestTime += 15;
                mCurrentTime += 15;
                binding.restProgress.setText(FitnessConstUtils.toTime(mCurrentTime));
                updateRing(true);
                initTimer(mCurrentTime);
                break;
            case R.id.tv_exercise_name:
                mWorkoutViewModel.setPause(true);
                showDialogDemoWorkout();
                break;
        }
    }

    public interface OnRestFragmentInteractionListener {
        void onFinishRest();

        void updateRestTime(int time);
    }
}
