package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
@SuppressLint({"SetTextI18n","NonConstantResourceId"})
public class PauseDialog extends DialogFragment {
    @BindView(R.id.videoView)
    CustomVideoView mVideoView;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.txt_exercise_count)
    TextView mExerciseCount;
    @BindView(R.id.txt_workout_count)
    TextView mProgress;
    @BindView(R.id.adView)
    AdView mAdBanner;

    String mProgressWorkoutString;
    private ExerciseObject mExerciseObject;
    private PlanObject.ActionObject mActionObject;
    private MyPreference myPreference;
    WorkoutViewModel mWorkoutViewModel;
    String path;

    public PauseDialog() {

    }

    public static PauseDialog newInstance(PlanObject.ActionObject actionObject, String progressWorkoutString) {
        PauseDialog pauseDialog = new PauseDialog();
        Bundle args = new Bundle();
        args.putParcelable("action_object", actionObject);
        args.putString("progress", progressWorkoutString);
        pauseDialog.setArguments(args);
        return pauseDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (getArguments() != null) {
            mProgressWorkoutString = getArguments().getString("progress");
            mActionObject = getArguments().getParcelable("action_object");
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover).get(mActionObject.actionId);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);
        myPreference = MyPreference.newInstance(getContext());
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
    }

    @OnClick({R.id.img_resume})
    public void onClick() {
        dismiss();
        mWorkoutViewModel.setPause(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // int resourceId = getContext().getResources().getIdentifier("video_" + mExerciseObject.getId(), "raw", getContext().getPackageName());
        path = getContext().getCacheDir() + "/temp.mp4";
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
        });
        mVideoView.start();
        mExerciseName.setText(mExerciseObject.getName());
        int minutes = (mActionObject.time) / 60;
        int seconds = mActionObject.time % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        if (mExerciseObject.getUnit().contains("s")){
            mExerciseCount.setText(timeString);
        }else {
            mExerciseCount.setText("x "+mActionObject.time);
        }
        mProgress.setText(mProgressWorkoutString);
        requestNewAdBanner();
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_pause, viewGroup);
        ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        mExerciseName.setText(mExerciseObject.getName());
        mExerciseCount.setText("x" + mActionObject.time + mExerciseObject.getUnit());
        mProgress.setText(mProgressWorkoutString);
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(myPreference.getSpeed()));
            }
        });
        mVideoView.start();
    }

    void requestNewAdBanner() {
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdBanner.loadAd(adRequest);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mWorkoutViewModel.setPause(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

}
