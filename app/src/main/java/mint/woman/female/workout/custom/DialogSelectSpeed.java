package mint.woman.female.workout.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.MyPreference;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogSelectSpeed extends BottomSheetDialogFragment {
    @BindView(R.id.npk_speed)
    NumberPicker mSpeedNpk;
    @BindView(R.id.videoView)
    CustomVideoView mVideoView;

    private ExerciseObject mExerciseObject;
    private String[] num = new String[]{"0.6x", "0.7x", "0.8x", "0.9x", "1.0x", "1.1x", "1.2x", "1.3x", "1.4x", "1.5x", "1.6x"};
    private float[] values = new float[]{0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f};
    private float currentSpeed = 1.0f;
    private MyPreference myPreference;

    MediaPlayer mediaPlayer;
    WorkoutViewModel mWorkoutViewModel;
    String path;

    public DialogSelectSpeed() {

    }

    public static DialogSelectSpeed newInstance(int actionId) {
        DialogSelectSpeed dialogSelectSpeed = new DialogSelectSpeed();
        Bundle arg = new Bundle();
        arg.putInt("actionId", actionId);
        dialogSelectSpeed.setArguments(arg);
        return dialogSelectSpeed;
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
        //  setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        myPreference = MyPreference.newInstance(getContext());
        mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt("actionId");
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(mWorkoutViewModel.getDiscover(),DoWorkoutActivity.version).get(id);
        }
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            setupFullHeight(bottomSheetDialog);
            loadExercise();
        });
        return dialog;
    }

    void loadExercise() {

        path = getContext().getCacheDir() + "/temp.mp4";
        currentSpeed = myPreference.getSpeed();

        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(currentSpeed));
            }
        });
        mVideoView.setZOrderOnTop(false);
        mVideoView.start();
        mSpeedNpk.setMaxValue(num.length - 1);
        mSpeedNpk.setMinValue(0);
        int index = getIndexOf(myPreference.getSpeed());
        mSpeedNpk.setValue(index);
        mSpeedNpk.setDisplayedValues(num);
        mSpeedNpk.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mSpeedNpk.setOnValueChangedListener((numberPicker, i, i1) -> {
            currentSpeed = values[i1];
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getContext(), Uri.parse(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mVideoView.setVideoURI(Uri.parse(path));
            mVideoView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(currentSpeed));
                }
            });
            mVideoView.start();
        });
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.dialog_choose_speed, viewGroup);
        ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        loadExercise();
    }

    private int getIndexOf(float speed) {
        for (int i = 0; i < values.length; i++) {
            if (speed == values[i]) return i;
        }
        return 0;
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return (int) (displayMetrics.heightPixels * 0.9f);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadExercise();
    }


    @OnClick({R.id.btn_cancel, R.id.btn_ok})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                //     mWorkoutViewModel.setPause(false);
                break;
            case R.id.btn_ok:
                myPreference.setSpeed(values[mSpeedNpk.getValue()]);
                dismiss();
                //    mWorkoutViewModel.setPause(false);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mWorkoutViewModel.setPause(false);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


}
