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
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import mint.woman.female.workout.utils.ExerciseFeedbackStore;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;

import mint.woman.female.workout.custom.DialogDemoWorkout;
import mint.woman.female.workout.databinding.FragmentRestBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.WorkoutVideoCache;
import mint.woman.female.workout.utils.WorkoutDataValidation;
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
    private int exerciseDiscover, exerciseVersion;
    private PlanObject.ActionObject mActionObject;
    private OnRestFragmentInteractionListener mListener;

    private WorkoutViewModel mWorkoutViewModel;

    public RestFragment() {
    }

    public static RestFragment newInstance(PlanObject.ActionObject actionObject, int restTime, String exerciseProgressCount) {
        RestFragment fragment = new RestFragment();
        Bundle args = new Bundle();
        args.putParcelable("action_object", actionObject);
        args.putInt("exercise_discover", DoWorkoutActivity.discover);
        args.putInt("exercise_version", DoWorkoutActivity.version);
        args.putInt("rest", restTime);
        args.putString("count", exerciseProgressCount);
        fragment.setArguments(args);
        return fragment;
    }

    /** Hỏi 👍/👎 cho động tác vừa tập xong (chỉ khi đi tiếp, không khi lùi lại). */
    public RestFragment withFeedback(int previousActionId) {
        if (getArguments() != null) getArguments().putInt("prev_action", previousActionId);
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            mActionObject = getArguments().getParcelable("action_object");
            exerciseDiscover = getArguments().getInt("exercise_discover", DoWorkoutActivity.discover);
            exerciseVersion = getArguments().getInt("exercise_version", DoWorkoutActivity.version);
            // Older saved fragments have no catalog arguments. Resolve from the activity intent,
            // since static workout fields may still contain defaults after process recreation.
            if (!getArguments().containsKey("exercise_discover")
                    || !getArguments().containsKey("exercise_version")) {
                int programId = requireActivity().getIntent().getIntExtra("PROGRAM_ID", 0);
                ProgramObject program = FitnessApplication.getApplication().getProgram(programId);
                if (program != null) {
                    exerciseDiscover = program.getDiscover();
                    exerciseVersion = program.getVersion();
                }
                getArguments().putInt("exercise_discover", exerciseDiscover);
                getArguments().putInt("exercise_version", exerciseVersion);
            }
            if (mActionObject != null) {
                mExerciseObject = FitnessApplication.getApplication().mJsonHelper
                        .getHashMapExercise(exerciseDiscover, exerciseVersion).get(mActionObject.actionId);
            }
            mRestTime = getArguments().getInt("rest");
            mCurrentTime = mRestTime;
            exerciseProgressCount = getArguments().getString("count");
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            if (mActionObject != null) {
                crashlytics.setCustomKey("ACTION_ID", mActionObject.actionId);
                crashlytics.setCustomKey("TIME", mActionObject.time);
            }
        }


        myPreference = new MyPreference(getContext());
    }

    private void bindFeedback() {
        // Màn ngang không có hàng này.
        if (binding.layoutFeedback == null || getArguments() == null) return;
        int previousId = getArguments().getInt("prev_action", 0);
        ExerciseObject previous = previousId > 0
                ? FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(exerciseDiscover, exerciseVersion).get(previousId) : null;
        if (previous == null) {
            binding.layoutFeedback.setVisibility(View.GONE);
            return;
        }
        binding.layoutFeedback.setVisibility(View.VISIBLE);
        binding.tvFeedback.setText(getString(R.string.fb_how_was, previous.getName()));
        showFeedback(ExerciseFeedbackStore.get(requireContext(), exerciseDiscover, previousId));
        binding.btnLike.setOnClickListener(v -> toggleFeedback(previousId, ExerciseFeedbackStore.LIKE));
        binding.btnDislike.setOnClickListener(v -> toggleFeedback(previousId, ExerciseFeedbackStore.DISLIKE));
    }

    private void toggleFeedback(int actionId, int value) {
        int current = ExerciseFeedbackStore.get(requireContext(), exerciseDiscover, actionId);
        int next = current == value ? ExerciseFeedbackStore.NONE : value;
        ExerciseFeedbackStore.set(requireContext(), exerciseDiscover, actionId, next);
        showFeedback(next);
        if (next == ExerciseFeedbackStore.DISLIKE) {
            Toast.makeText(getContext(), R.string.fb_dislike_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void showFeedback(int value) {
        if (binding == null || binding.btnLike == null) return;
        int accent = ContextCompat.getColor(requireContext(), R.color.dm_accent);
        binding.btnLike.setSelected(value == ExerciseFeedbackStore.LIKE);
        binding.btnDislike.setSelected(value == ExerciseFeedbackStore.DISLIKE);
        binding.btnLike.setColorFilter(value == ExerciseFeedbackStore.LIKE ? accent : Color.WHITE);
        binding.btnDislike.setColorFilter(value == ExerciseFeedbackStore.DISLIKE ? accent : Color.WHITE);
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
        if (mActionObject == null || mExerciseObject == null) {
            int programId = requireActivity().getIntent().getIntExtra("PROGRAM_ID", -1);
            ProgramObject program = FitnessApplication.getApplication().getProgram(programId);
            WorkoutDataValidation.report(requireContext(), "rest", program == null ? programId : program.getId(),
                    exerciseDiscover, exerciseVersion,
                    requireActivity().getIntent().getIntExtra("DAY_NUMBER", -1), -1,
                    mActionObject == null ? -1 : mActionObject.actionId,
                    mActionObject == null ? "null_action" : "missing_exercise");
            // Do not continue to a workout screen with an unavailable exercise.
            requireActivity().finish();
            return;
        }
        // Giải mã video ở luồng nền (trước đây chạy trên main thread mỗi lần sang bài -> nguy cơ ANR).
        WorkoutVideoCache.prepare(requireContext(), exerciseDiscover,
                exerciseVersion, mActionObject.actionId, video -> {
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
        if (binding == null || mActionObject == null || mExerciseObject == null) return;
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
        bindFeedback();
        binding.txtAddTime.setOnClickListener(this);
        // Màn "Sẵn sàng" (trước động tác đầu) không phải nghỉ: ẩn nút chỉnh thời gian nghỉ.
        binding.txtEditRest.setVisibility(readyToGo ? View.GONE : View.VISIBLE);
        binding.txtEditRest.setOnClickListener(v -> showEditRest());
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
        if (binding == null || mActionObject == null || mExerciseObject == null) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long l) {
                if (binding == null) return;
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
                if (binding == null) return;
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
        if (binding != null) binding.videoView.pause();
    }

    public void onResumeRest() {
        if (binding == null || mActionObject == null || mExerciseObject == null) return;
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
        if (mActionObject == null || mExerciseObject == null || !isAdded() || getParentFragmentManager().findFragmentByTag("images") != null) return;

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
        if (binding != null) binding.videoView.stopPlayback();
    }

    @Override
    public void onDestroyView() {
        releaseTimer();
        if (binding != null) binding.videoView.stopPlayback();
        binding = null;
        super.onDestroyView();
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
        if (getView() == null || mActionObject == null || mExerciseObject == null) return;
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

    /** Chọn thời gian nghỉ ngay trên màn nghỉ: áp dụng cho lần nghỉ này và các lần sau (lưu REST_TIME). */
    private void showEditRest() {
        if (getContext() == null) return;
        if (countDownTimer != null) countDownTimer.cancel();
        String[] names = getResources().getStringArray(R.array.arr_rest_name);
        String[] values = getResources().getStringArray(R.array.arr_rest_value);
        int checked = java.util.Arrays.asList(values).indexOf(String.valueOf(myPreference.getRestTime()));
        androidx.appcompat.app.AlertDialog restDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.txt_rest_time)
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    int seconds = Integer.parseInt(values[which]);
                    myPreference.setRestTime(seconds);
                    mRestTime = seconds;
                    mCurrentTime = seconds;
                    if (binding != null) {
                        binding.restProgress.setText(FitnessConstUtils.toTime(seconds));
                        updateRing(false);
                    }
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> {
                    if (binding != null && isAdded()) initTimer(mCurrentTime);
                })
                .show();
        mint.woman.female.workout.utils.ImmersiveHelper.keepDialog(restDialog);
    }

    public interface OnRestFragmentInteractionListener {
        void onFinishRest();

        void updateRestTime(int time);
    }
}
