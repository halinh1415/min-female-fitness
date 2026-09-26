package mint.woman.female.workout.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.count.CountManagerActivity;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.custom.DialogDemoWorkout;
import mint.woman.female.workout.custom.DialogSelectSpeed;
import mint.woman.female.workout.custom.PauseDialog;
import mint.woman.female.workout.databinding.FragmentPlayBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.WorkoutVideoDecoder;


@SuppressLint({"NonConstantResourceId", "UseSwitchCompatOrMaterialCode", "DefaultLocale", "SetTextI18n"})
public class PlayFragment extends Fragment implements View.OnClickListener {
    FragmentPlayBinding binding;
    private CountDownTimer countDownTimer;
    private int crr, mCurrentTime, mTotalTime, progress, totalExercise;
    private MyPreference myPreference;
    private ExerciseObject mExerciseObject;
    private PlanObject.ActionObject mActionObject;
    private OnReadyFragmentInteractionListener mListener;
    private String path;

    private WorkoutViewModel mWorkoutViewModel;
    private int REP = 12;


    public PlayFragment() {

    }


    public static PlayFragment newInstance(PlanObject.ActionObject actionObject, int progress, int totalExercise) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putParcelable("action_object", actionObject);
        args.putInt("progress", progress);
        args.putInt("total", totalExercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActionObject = getArguments().getParcelable("action_object");
            progress = getArguments().getInt("progress");
            totalExercise = getArguments().getInt("total");
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover,DoWorkoutActivity.version).get(mActionObject.actionId);
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("ACTION_ID", mActionObject.actionId);
            crashlytics.setCustomKey("TIME", mActionObject.time);
        }
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        path = getContext().getCacheDir() + "/temp.mp4";
        myPreference = new MyPreference(getContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mExerciseObject == null){
            mListener.onFinishReady();
            return;
        }
        REP = mActionObject.time;
        if (mExerciseObject.getAlternation() && mActionObject.getUnit().isEmpty()) {
            REP = mActionObject.time / 2;
        }
        mTotalTime = mActionObject.time;
        binding.txtSpeed.setText("" + myPreference.getSpeed() + "x");
        loadVideo();
        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);

        });

        binding.tvExerciseName.setText(mExerciseObject.getName());
        if (mActionObject.getUnit().contains("s")) {
            mCurrentTime = mActionObject.time;
            binding.btnPause.setVisibility(View.VISIBLE);
            binding.imgDone.setVisibility(View.GONE);
            binding.tvSub.setVisibility(View.INVISIBLE);
            binding.txtTime.setText(FitnessConstUtils.toTime(REP));
            binding.progressReadyBottom.setMax(mActionObject.time * 1000);
            binding.progressReadyBottom.setProgress(0);
            binding.progressReady.setMax(mActionObject.time * 1000);
            binding.progressReady.setProgress(0);
        } else {
            mCurrentTime = 0;
            if (mExerciseObject.getAlternation()) {
                binding.tvSub.setVisibility(View.VISIBLE);
                binding.tvSub.setText(getString(R.string.wp_each_side)+" x"+REP);
            } else {
                binding.tvSub.setVisibility(View.INVISIBLE);
            }
            binding.txtTime.setText("x" + REP);
            if (myPreference.getCountOption() == 1) {
                binding.btnPause.setVisibility(View.VISIBLE);
                binding.imgDone.setVisibility(View.GONE);
                binding.videoView.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.setLooping(false);
                });
                binding.videoView.setOnCompletionListener(mediaPlayer -> {
                    new Handler().postDelayed(() -> binding.videoView.start(), mExerciseObject.getSpeed());
                });
                binding.txtTime.setText(proToSpan(""+0,"/"+REP));
                //binding.txtTime.setText("0/" + REP);
            }else {
                binding.btnPause.setVisibility(View.GONE);
                binding.imgDone.setVisibility(View.VISIBLE);
            }
            binding.tvExerciseName.setText(mExerciseObject.getName());

        }
        binding.videoView.start();
        if (!myPreference.isSpeakerOn()) {
            countDown();
        }
        //  countDown();
        if (progress == 0) {
            binding.imgPrevious.setEnabled(false);
             binding.imgPrevious.setAlpha(0.2f);
        } else if (progress > 0 && (progress < totalExercise - 1)) {
             binding.imgPrevious.setEnabled(true);
             binding.imgPrevious.setAlpha(1.0f);
            binding.imgNext.setEnabled(true);
            binding.imgNext.setAlpha(1.0f);
        }
        binding.imgPrevious.setOnClickListener(this);
        binding.imgNext.setOnClickListener(this);
        binding.btnPause.setOnClickListener(this);
        binding.txtSpeed.setOnClickListener(this);
        binding.tvExerciseName.setOnClickListener(this);
        binding.imgSetting.setOnClickListener(this);
        binding.imgRotate.setOnClickListener(this);
        binding.imgDone.setOnClickListener(this);

    }


    public void onPauseReady() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (binding.videoView != null) {
            binding.videoView.setOnCompletionListener(null);
            binding.videoView.stopPlayback();
        }
    }

    SpannableStringBuilder proToSpan(String pro, String total){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString span1 = new SpannableString(pro);
        span1.setSpan(new RelativeSizeSpan(1.5f), 0, span1.length(), 0); // Adjust the size as needed
      //  span1.setSpan(new StyleSpan(Typeface.BOLD), 0, span1.length(), 0);
        builder.append(span1);
        builder.append(total);

        return builder;
    }
    public void onResumeReady() {
        Log.d("HAHA", "onResumeReady");
        binding.txtSpeed.setText("" + myPreference.getSpeed() + "x");
        binding.tvExerciseName.setText(mExerciseObject.getName());
        binding.tvCountDown.setVisibility(View.INVISIBLE);
        loadVideo();
        playExercise();

    }

    /**
     * temp.mp4 do RestFragment giải mã ở luồng nền; bỏ qua nghỉ ngay thì có thể chưa xong,
     * nên chờ giải mã rồi mới gán video.
     */
    private void loadVideo() {
        Context context = getContext();
        if (context == null) return;
        WorkoutVideoDecoder.whenReady(context, available -> {
            if (!available || !isAdded() || getView() == null) return;
            binding.videoView.setVideoURI(Uri.parse(path));
        });
    }

    public void playExercise() {
        binding.txtSpeed.setText("" + myPreference.getSpeed() + "x");
        binding.tvCountDown.setVisibility(View.INVISIBLE);
         binding.progressReady.setMax(REP);
         binding.progressReady.setProgress((mCurrentTime));
         binding.progressReadyBottom.setMax(REP);
         binding.progressReadyBottom.setProgress((mCurrentTime));
     //   binding.videoView.setVideoURI(Uri.parse(path));
        if (mActionObject.getUnit().contains("s")) {

            binding.btnPause.setVisibility(View.VISIBLE);
            binding.imgDone.setVisibility(View.GONE);
            binding.videoView.setOnPreparedListener(mediaPlayer -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                }
                mediaPlayer.setLooping(true);
            });
            if (!binding.videoView.isPlaying()) {
                binding.videoView.start();
            }
            initTimer(mCurrentTime);
        } else {
            if (mExerciseObject.getAlternation()) {
                binding.tvSub.setVisibility(View.VISIBLE);
                binding.tvSub.setText(getString(R.string.wp_each_side)+" x"+REP);
            } else {
                binding.tvSub.setVisibility(View.INVISIBLE);
            }

            if (myPreference.getCountOption() == 1) {
                binding.txtTime.setText("" + (mCurrentTime + 1) + "/" + REP);
                binding.btnPause.setVisibility(View.VISIBLE);
                binding.imgDone.setVisibility(View.GONE);

                binding.videoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(false);
                });
                binding.txtTime.setText(proToSpan(""+mCurrentTime,"/"+REP));
                binding.videoView.setOnCompletionListener(mediaPlayer -> {
                    mCurrentTime++;
                    if (mCurrentTime > REP) {
                        mListener.onFinishReady();
                    } else {
                        binding.txtTime.setText(proToSpan(""+mCurrentTime,"/"+REP));
                         binding.progressReadyBottom.setProgress((mCurrentTime));
                         binding.progressReady.setProgress((mCurrentTime));
                        if (mListener != null) {
                            mListener.updateReady(mCurrentTime);
                            mListener.updateProgressWorkout(((float) (mCurrentTime) / REP));
                        }
                        new Handler().postDelayed(() -> binding.videoView.start(), mExerciseObject.getSpeed());
                        // binding.videoView.start();
                    }
                });
                //mListener.updateReady(mCurrentTime + 1);
                 binding.progressReadyBottom.setProgress(mCurrentTime + 1);
                if (!binding.videoView.isPlaying()) {
                    binding.videoView.start();
                }
            } else {
                binding.txtTime.setText("x" + REP);
                binding.videoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(true);
                });
                if (!binding.videoView.isPlaying()) {
                    binding.videoView.start();
                }
                binding.btnPause.setVisibility(View.GONE);
                binding.imgDone.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initTimer(int time) {
        releaseTimer();
         binding.progressReadyBottom.setMax(mTotalTime * 1000);
         binding.progressReady.setMax(mTotalTime * 1000);
         binding.progressReadyBottom.setProgress((mTotalTime - mCurrentTime) * 1000);
        countDownTimer = new CountDownTimer(time * 1000, 10) {
            @Override
            public void onTick(long l) {
                if (mCurrentTime != (int) l / 1000) {
                    mCurrentTime = (int) l / 1000;
                    binding.txtTime.setText(FitnessConstUtils.toTime(mCurrentTime));
                    float progress = (float) (mTotalTime - mCurrentTime) / mTotalTime;
                    if (mListener != null) {
                        mListener.updateProgressWorkout(progress);
                        mListener.updateReady(mCurrentTime);
                    }
                }
                 binding.progressReady.setProgress((int) (mTotalTime * 1000 - l));
                 binding.progressReadyBottom.setProgress((int) (mTotalTime * 1000 - l));
            }

            @Override
            public void onFinish() {
                mCurrentTime = 0;
                 binding.progressReadyBottom.setProgress(mTotalTime * 1000);
                if (mListener != null) {
                    mListener.onFinishReady();
                }
            }
        };
        countDownTimer.start();
    }

    public void countDown() {
        crr = 0;
        //playExercise();
        //binding.tvCountDown.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(3800, 10) {
            @Override
            public void onTick(long l) {
                if (crr != (int) (l / 1000)) {
                    crr = (int) l / 1000;
                    if (crr > 0) {
                        try {
                          //  binding.tvCountDown.setText("" + crr);
                            Animation anim = new TranslateAnimation(0, 0, -200, 100);
                            anim.setDuration(450);
                            Animation fadeOut = new AlphaAnimation(1, 0);
                            fadeOut.setDuration(250);
                            anim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                  //  binding.tvCountDown.clearAnimation();
                                 //   binding.tvCountDown.startAnimation(fadeOut);
                                 //   fadeOut.setFillAfter(true);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            //binding.tvCountDown.clearAnimation();
                            //binding.tvCountDown.startAnimation(anim);
                           // if (mListener != null) mListener.updateReady(crr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onFinish() {
                //binding.tvCountDown.setVisibility(View.INVISIBLE);
                playExercise();

            }
        };
        countDownTimer.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReadyFragmentInteractionListener) {
            mListener = (OnReadyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (binding.videoView != null) {
            binding.videoView.stopPlayback();
        }
    }

    private void releaseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

    }
    private void showDialogDemoWorkout() {
        if (!isAdded() || getParentFragmentManager().findFragmentByTag("images") != null) return;

        DialogDemoWorkout dialogDemoWorkout = DialogDemoWorkout.newInstance(mActionObject.actionId);
        dialogDemoWorkout.show(getParentFragmentManager(), "images");
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_previous:
                if (progress > 0) {
                    releaseTimer();
                    if (mListener != null) {
                        mListener.onPreviousExercise();
                    }
                }

                break;
            case R.id.img_done:
            case R.id.img_next:
                releaseTimer();
                if (mListener != null) {
                    mListener.onNextExercise();
                }
                break;
            case R.id.btn_pause:
                mWorkoutViewModel.setPause(true);
                PauseDialog pauseDialog = PauseDialog.newInstance(mActionObject, getString(R.string.txt_next_of_exercise) + " " + (progress + 1) + "/" + totalExercise);
                pauseDialog.show(getParentFragmentManager(), "Pause");
                break;


            case R.id.txt_speed:
                mWorkoutViewModel.setPause(true);
                DialogSelectSpeed speedDialog = DialogSelectSpeed.newInstance(mActionObject.actionId);
                speedDialog.show(getParentFragmentManager(), "speed");

                break;
            case R.id.tv_exercise_name:
                mWorkoutViewModel.setPause(true);
               showDialogDemoWorkout();
                break;
            case R.id.img_setting:
                mWorkoutViewModel.setPause(true);
                startActivity(new Intent(getActivity(), CountManagerActivity.class));
//                DialogSound soundSettings = new DialogSound();
//                soundSettings.show(getParentFragmentManager(), "sound_settings");
                break;
            case R.id.img_rotate:
                int currentOrientation = getActivity().getRequestedOrientation();
                if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
        }
    }


    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        binding = FragmentPlayBinding.inflate(getLayoutInflater());
        viewGroup.addView(binding.getRoot());
        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        onResumeReady();
        if (progress == 0) {
             binding.imgPrevious.setEnabled(false);
             binding.imgPrevious.setAlpha(0.2f);
        }
        binding.imgPrevious.setOnClickListener(this);
        binding.imgNext.setOnClickListener(this);
        binding.btnPause.setOnClickListener(this);
        binding.txtSpeed.setOnClickListener(this);
        binding.tvExerciseName.setOnClickListener(this);
        binding.imgSetting.setOnClickListener(this);
        binding.imgRotate.setOnClickListener(this);
        binding.imgDone.setOnClickListener(this);
    }

    public interface OnReadyFragmentInteractionListener {
        void updateReady(int timeLeft);

        void updateProgressWorkout(float progress);

        void onFinishReady();

        void onNextExercise();

        void onPreviousExercise();

        void onTextToSpeech(String voice);

    }
}
