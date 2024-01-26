package mint.woman.female.workout.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.custom.CustomVideoView;
import mint.woman.female.workout.custom.DialogDemoWorkout;
import mint.woman.female.workout.custom.DialogSelectSpeed;
import mint.woman.female.workout.custom.DialogSound;
import mint.woman.female.workout.custom.PauseDialog;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint({"NonConstantResourceId", "UseSwitchCompatOrMaterialCode", "DefaultLocale", "SetTextI18n"})
public class PlayFragment extends Fragment implements View.OnClickListener {
    private boolean isPause = false;
    @BindView(R.id.videoView)
    CustomVideoView mVideoView;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.txt_time)
    TextView mExerciseTime;

    // @BindView(R.id.txt_pause)
    //ImageView mPause;
    @BindView(R.id.img_previous)
    TextView mPrevious;
    @BindView(R.id.img_next)
    TextView mNext;
    @BindView(R.id.progress_ready_bottom)
    ProgressBar mBottomProgressBar;
    @BindView(R.id.progress_ready)
    ProgressBar mReadyProgress;

    @BindView(R.id.txt_speed)
    TextView mSpeed;
    @BindView(R.id.img_done)
    ImageView mDoneImg;

    @BindView(R.id.tv_count_down)
    TextView mCountDownText;
    @BindView(R.id.tv_sub)
    TextView mSub;
    private CountDownTimer countDownTimer;
    private int crr, mCurrentTime, mTotalTime, progress, totalExercise;
    private MyPreference myPreference;
    private ExerciseObject mExerciseObject;
    private PlanObject.ActionObject mActionObject;
    private OnReadyFragmentInteractionListener mListener;
    private String path;

    private WorkoutViewModel mWorkoutViewModel;


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
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover).get(mActionObject.actionId);
        }
        setRetainInstance(false);
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        path = getContext().getCacheDir() + "/temp.mp4";
//        int resourceId = getContext().getResources().getIdentifier("video_" + mExerciseObject.getId(), "raw", getContext().getPackageName());
//        if (resourceId > 0) {
//            path = "android.resource://" + getContext().getPackageName() + "/" + resourceId;
//        }
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
        mCurrentTime = mActionObject.time;
        mTotalTime = mActionObject.time;
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        if (mActionObject.unit.contains("s")) {
            mDoneImg.setVisibility(View.INVISIBLE);
            int minutes = (mActionObject.time) / 60;
            int seconds = mCurrentTime % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            mExerciseName.setText(mExerciseObject.getName() + " x " + timeString);
            mExerciseTime.setText(timeString);
            mBottomProgressBar.setMax(mActionObject.time * 1000);
            mBottomProgressBar.setProgress(0);
            mReadyProgress.setMax(mActionObject.time * 1000);
            mReadyProgress.setProgress(0);
        } else {
            mDoneImg.setVisibility(View.VISIBLE);
            mExerciseName.setText(mExerciseObject.getName());
            mExerciseTime.setText("x" + mActionObject.time);
        }
//        String fm = "Begin! %s %d time";
//        if (mExerciseObject.getUnit().contains("s")) {
//            fm = listVoice.get(4);
//        } else {
//            fm = listVoice.get(3);
//        }

//        if (mListener != null)
//            mListener.onTextToSpeech(String.format(fm, mExerciseObject.getName(), mActionObject.time));
        countDown();
        if (progress == 0) {
            mPrevious.setEnabled(false);
            mPrevious.setAlpha(0.2f);
        } else if (progress > 0 && (progress < totalExercise - 1)) {
            mPrevious.setEnabled(true);
            mPrevious.setAlpha(1.0f);
            mNext.setEnabled(true);
            mNext.setAlpha(1.0f);
        }


    }


    public void onPauseReady() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mVideoView.pause();
    }

    @SuppressLint("SetTextI18n")
    public void onResumeReady() {
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        String textName = mExerciseObject.getName()  + "  x" + mActionObject.time + mExerciseObject.getUnit();
        mExerciseName.setText(textName);

        mCountDownText.setVisibility(View.INVISIBLE);
        if (mActionObject.unit.contains("s")) {
            mVideoView.setVideoURI(Uri.parse(path));
            mVideoView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                }
            });
            mVideoView.start();
            initTimer(mCurrentTime);
        } else {
            if (!myPreference.isAutoNext()) {
                mVideoView.setVideoURI(Uri.parse(path));
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.setLooping(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                });
                mVideoView.setOnCompletionListener(null);
            } else {
                mVideoView.setVideoURI(Uri.parse(path));
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(false);
                });
                mBottomProgressBar.setMax(mActionObject.time);
                mVideoView.setOnCompletionListener(mediaPlayer -> {
                    mCurrentTime++;
                    if (mCurrentTime >= mActionObject.time) {
                        mListener.onFinishReady();
                    } else {
                        mBottomProgressBar.setProgress((mCurrentTime + 1));
                        if (mListener != null) {
                            mListener.updateReady(mCurrentTime + 1);
                            mListener.updateProgressWorkout(((float) (mCurrentTime + 1) / mActionObject.time));
                        }
                        mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + mActionObject.time);


                        mVideoView.start();
                    }
                });
                mBottomProgressBar.setProgress(mCurrentTime + 1);
                if (mListener != null) mListener.updateReady(mCurrentTime + 1);
            }
            mVideoView.start();
        }
    }

    public void playExercise() {
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        mCountDownText.setVisibility(View.INVISIBLE);
        if (mActionObject.unit.contains("s")) {
            initTimer(mCurrentTime);
        } else {
            if (myPreference.isAutoNext()) {
                mVideoView.setVideoURI(Uri.parse(path));
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(false);
                });
                mBottomProgressBar.setMax(mActionObject.time);
                mCurrentTime = 0;
                mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + mActionObject.time);
                mVideoView.setOnCompletionListener(mediaPlayer -> {
                    mCurrentTime++;

                    if (mCurrentTime >= mActionObject.time) {
                        mListener.onFinishReady();
                    } else {
                        mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + mActionObject.time);
                        mBottomProgressBar.setProgress((mCurrentTime + 1));
                        if (mListener != null) {
                            mListener.updateReady(mCurrentTime + 1);
                            mListener.updateProgressWorkout(((float) (mCurrentTime + 1) / mActionObject.time));
                        }
                        mVideoView.start();
                    }
                });
                mBottomProgressBar.setProgress(mCurrentTime + 1);
                if (mListener != null) mListener.updateReady(mCurrentTime + 1);
                mVideoView.start();
            }else{
                mExerciseTime.setText("x" + mActionObject.time);
            }
        }
    }

    public void initTimer(int time) {
        releaseTimer();
        mBottomProgressBar.setMax(mTotalTime * 1000);
        mBottomProgressBar.setProgress((mTotalTime - mCurrentTime) * 1000);
        countDownTimer = new CountDownTimer(time * 1000, 10) {
            @Override
            public void onTick(long l) {
                if (mCurrentTime != (int) l / 1000) {
                    mCurrentTime = (int) l / 1000;
                    int minutes = (mCurrentTime % 3600) / 60;
                    int seconds = mCurrentTime % 60;
                    String timeString = String.format("%02d:%02d", minutes, seconds);
                    mExerciseTime.setText(timeString);
                    float progress = (float) (mTotalTime - mCurrentTime) / mTotalTime;
                    if (mListener != null) {
                        mListener.updateProgressWorkout(progress);
                        mListener.updateReady(mCurrentTime);
                    }
                }
                mReadyProgress.setProgress((int) (mTotalTime * 1000 - l));
                mBottomProgressBar.setProgress((int) (mTotalTime * 1000 - l));
            }

            @Override
            public void onFinish() {
                mCurrentTime = 0;
                mBottomProgressBar.setProgress(mTotalTime * 1000);
                if (mListener != null) {
                    mListener.onFinishReady();
                }
            }
        };
        countDownTimer.start();
    }

    private void countDown() {
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
            }
        });
        mVideoView.start();
        crr = 0;
        //playExercise();
        //mCountDownText.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(3800, 10) {
            @Override
            public void onTick(long l) {
                if (crr != (int) (l / 1000)) {
                    crr = (int) l / 1000;
                    if (crr > 0) {
                        try {
                          //  mCountDownText.setText("" + crr);
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
                                  //  mCountDownText.clearAnimation();
                                 //   mCountDownText.startAnimation(fadeOut);
                                 //   fadeOut.setFillAfter(true);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            //mCountDownText.clearAnimation();
                            //mCountDownText.startAnimation(anim);
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
                //mCountDownText.setVisibility(View.INVISIBLE);
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
        mListener = null;
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    private void releaseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.img_previous,
            R.id.img_next, R.id.btn_pause, R.id.txt_speed, R.id.tv_exercise_name, R.id.img_setting, R.id.img_rotate,R.id.img_done})
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
                DialogDemoWorkout dialogDemoWorkout = DialogDemoWorkout.newInstance(mActionObject.actionId);
                dialogDemoWorkout.show(getParentFragmentManager(), "images");
                break;
            case R.id.img_setting:
                mWorkoutViewModel.setPause(true);
                DialogSound soundSettings = new DialogSound();
                soundSettings.show(getParentFragmentManager(), "sound_settings");
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
        View subview = inflater.inflate(R.layout.fragment_play, viewGroup);
        ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        String textName = mExerciseObject.getName() + "  x" + mActionObject.time + mExerciseObject.getUnit();
        mExerciseName.setText(textName);
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
            }
        });
        mVideoView.start();
        if (mActionObject.unit.contains("s")) {
            mDoneImg.setVisibility(View.INVISIBLE);
            initTimer(mCurrentTime);
        } else {
            mDoneImg.setVisibility(View.VISIBLE);
            mExerciseTime.setText("x" + mActionObject.time);
            if (myPreference.isAutoNext()) {
                mVideoView.setVideoURI(Uri.parse(path));
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(false);
                });
                mBottomProgressBar.setMax(mActionObject.time);
                mCurrentTime = 0;
                mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + mActionObject.time);
                mVideoView.setOnCompletionListener(mediaPlayer -> {
                    mCurrentTime++;
                    mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + mActionObject.time);
                    if (mCurrentTime >= mActionObject.time) {
                        mListener.onFinishReady();
                    } else {
                        mBottomProgressBar.setProgress((mCurrentTime + 1));
                        if (mListener != null) {
                            mListener.updateReady(mCurrentTime + 1);
                            mListener.updateProgressWorkout(((float) (mCurrentTime + 1) / mActionObject.time));
                        }

                        mVideoView.start();
                    }
                });
                mBottomProgressBar.setProgress(mCurrentTime + 1);
                if (mListener != null) mListener.updateReady(mCurrentTime + 1);
                mVideoView.start();
            }

        }
        mCountDownText.setVisibility(View.GONE);
        mBottomProgressBar.setMax(mActionObject.time * 1000);
        mBottomProgressBar.setProgress(0);
        mReadyProgress.setMax(mActionObject.time * 1000);
        mReadyProgress.setProgress(0);
        mVideoView.start();
        if (progress == 0) {
            mPrevious.setEnabled(false);
            mPrevious.setAlpha(0.2f);
        }
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
