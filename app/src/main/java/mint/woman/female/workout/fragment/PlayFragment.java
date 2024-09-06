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
import android.widget.ProgressBar;
import android.widget.TextView;

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
import mint.woman.female.workout.custom.CustomVideoView;
import mint.woman.female.workout.custom.DialogDemoWorkout;
import mint.woman.female.workout.custom.DialogSelectSpeed;
import mint.woman.female.workout.custom.PauseDialog;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.MyPreference;

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
    View mPrevious;
    @BindView(R.id.img_next)
    View mNext;
    @BindView(R.id.progress_ready_bottom)
    ProgressBar mBottomProgressBar;
    @BindView(R.id.progress_ready)
    ProgressBar mReadyProgress;

    @BindView(R.id.txt_speed)
    TextView mSpeed;
    @BindView(R.id.img_done)
    View mDoneImg;
    @BindView(R.id.btn_pause)
    View btnPause;

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
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);

        });

        mExerciseName.setText(mExerciseObject.getName());
        if (mActionObject.getUnit().contains("s")) {
            mCurrentTime = mActionObject.time;
            btnPause.setVisibility(View.VISIBLE);
            mDoneImg.setVisibility(View.GONE);
            mSub.setVisibility(View.INVISIBLE);
            mExerciseTime.setText(FitnessConstUtils.toTime(REP));
            mBottomProgressBar.setMax(mActionObject.time * 1000);
            mBottomProgressBar.setProgress(0);
            mReadyProgress.setMax(mActionObject.time * 1000);
            mReadyProgress.setProgress(0);
        } else {
            mCurrentTime = 0;
            if (mExerciseObject.getAlternation()) {
                mSub.setVisibility(View.VISIBLE);
                mSub.setText(getString(R.string.td_each_side)+" x"+REP);
            } else {
                mSub.setVisibility(View.INVISIBLE);
            }
            mExerciseTime.setText("x" + REP);
            if (myPreference.getCountOption() == 1) {
                btnPause.setVisibility(View.VISIBLE);
                mDoneImg.setVisibility(View.GONE);
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.setLooping(false);
                });
                mVideoView.setOnCompletionListener(mediaPlayer -> {
                    new Handler().postDelayed(() -> mVideoView.start(), mExerciseObject.getSpeed());
                });
                mExerciseTime.setText(proToSpan(""+0,"/"+REP));
                //mExerciseTime.setText("0/" + REP);
            }else {
                btnPause.setVisibility(View.GONE);
                mDoneImg.setVisibility(View.VISIBLE);
            }
            mExerciseName.setText(mExerciseObject.getName());

        }
        mVideoView.start();
        if (!myPreference.isSpeakerOn()) {
            countDown();
        }
        //  countDown();
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
        if (mVideoView != null) {
            mVideoView.setOnCompletionListener(null);
            mVideoView.stopPlayback();
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
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        mExerciseName.setText(mExerciseObject.getName());
        mCountDownText.setVisibility(View.INVISIBLE);
        mVideoView.setVideoURI(Uri.parse(path));
        playExercise();

    }

    public void playExercise() {
        mSpeed.setText("" + myPreference.getSpeed() + "x");
        mCountDownText.setVisibility(View.INVISIBLE);
        mReadyProgress.setMax(REP);
        mReadyProgress.setProgress((mCurrentTime));
        mBottomProgressBar.setMax(REP);
        mBottomProgressBar.setProgress((mCurrentTime));
     //   mVideoView.setVideoURI(Uri.parse(path));
        if (mActionObject.getUnit().contains("s")) {

            btnPause.setVisibility(View.VISIBLE);
            mDoneImg.setVisibility(View.GONE);
            mVideoView.setOnPreparedListener(mediaPlayer -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                }
                mediaPlayer.setLooping(true);
            });
            if (!mVideoView.isPlaying()) {
                mVideoView.start();
            }
            initTimer(mCurrentTime);
        } else {
            if (mExerciseObject.getAlternation()) {
                mSub.setVisibility(View.VISIBLE);
                mSub.setText(getString(R.string.td_each_side)+" x"+REP);
            } else {
                mSub.setVisibility(View.INVISIBLE);
            }

            if (myPreference.getCountOption() == 1) {
                mExerciseTime.setText("" + (mCurrentTime + 1) + "/" + REP);
                btnPause.setVisibility(View.VISIBLE);
                mDoneImg.setVisibility(View.GONE);

                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(false);
                });
                mExerciseTime.setText(proToSpan(""+mCurrentTime,"/"+REP));
                mVideoView.setOnCompletionListener(mediaPlayer -> {
                    mCurrentTime++;
                    if (mCurrentTime > REP) {
                        mListener.onFinishReady();
                    } else {
                        mExerciseTime.setText(proToSpan(""+mCurrentTime,"/"+REP));
                        mBottomProgressBar.setProgress((mCurrentTime));
                        mReadyProgress.setProgress((mCurrentTime));
                        if (mListener != null) {
                            mListener.updateReady(mCurrentTime);
                            mListener.updateProgressWorkout(((float) (mCurrentTime) / REP));
                        }
                        new Handler().postDelayed(() -> mVideoView.start(), mExerciseObject.getSpeed());
                        // mVideoView.start();
                    }
                });
                //mListener.updateReady(mCurrentTime + 1);
                mBottomProgressBar.setProgress(mCurrentTime + 1);
                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                }
            } else {
                mExerciseTime.setText("x" + REP);
                mVideoView.setOnPreparedListener(mediaPlayer -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
                    }
                    mediaPlayer.setLooping(true);
                });
                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                }
                btnPause.setVisibility(View.GONE);
                mDoneImg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initTimer(int time) {
        releaseTimer();
        mBottomProgressBar.setMax(mTotalTime * 1000);
        mReadyProgress.setMax(mTotalTime * 1000);
        mBottomProgressBar.setProgress((mTotalTime - mCurrentTime) * 1000);
        countDownTimer = new CountDownTimer(time * 1000, 10) {
            @Override
            public void onTick(long l) {
                if (mCurrentTime != (int) l / 1000) {
                    mCurrentTime = (int) l / 1000;
                    mExerciseTime.setText(FitnessConstUtils.toTime(mCurrentTime));
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

    public void countDown() {
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
        View subview = inflater.inflate(R.layout.fragment_play, viewGroup);
        ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        onResumeReady();
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
