package mint.woman.female.workout.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.fragment.VideoDemoFragment;
import mint.woman.female.workout.fragment.VideoYtFragment;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DialogDemoWorkout extends DialogFragment implements View.OnClickListener {
    ExerciseObject mExerciseObject;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.tv_exercise_description)
    TextView mExerciseDescription;
    @BindView(R.id.tv_sub)
    TextView tvSub;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewPager)
    CustomViewPager mViewPager;
    //    @BindView(R.id.videoView)
//    CustomVideoView mVideoView;
    private MediaPlayer mediaPlayer;
    private WorkoutViewModel mWorkoutViewModel;
    private MyPreference myPreference;

    public DialogDemoWorkout() {

    }

    public static DialogDemoWorkout newInstance(int actionId) {
        DialogDemoWorkout dialogExerciseOverView = new DialogDemoWorkout();
        Bundle arg = new Bundle();
        arg.putInt("actionId", actionId);
        dialogExerciseOverView.setArguments(arg);
        return dialogExerciseOverView;
    }

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        if (getArguments() != null) {
//            mExerciseObject = getArguments().getParcelable("EXERCISE_OBJECT");
//        }
//        return dialog;
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            int id = getArguments().getInt("actionId");
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover).get(id);
        }
//        dialog.setOnShowListener(dialogInterface -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
//            setupFullHeight(bottomSheetDialog);
//            loadExercise();
//        });
        return dialog;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.dialog_demo_layout, viewGroup);
        ButterKnife.bind(this, subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        loadExercise();
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoDemoAdapter mAdapter = new VideoDemoAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mExerciseName.setText(mExerciseObject.getName());
        mExerciseDescription.setText(mExerciseObject.getIntroduce());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);
        setRetainInstance(true);
        myPreference = new MyPreference(getContext());
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
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


    void loadExercise() {
//        int resourceId = getContext().getResources().getIdentifier("" + mExerciseObject.video, "raw", getContext().getPackageName());
//        String path = "android.resource://" + getContext().getPackageName() + "/" + resourceId;
//        mVideoView.setVideoURI(Uri.parse(path));
//        mVideoView.setOnPreparedListener(mediaPlayer ->{
//            mediaPlayer.setLooping(true);
//        } );
//        mVideoView.start();

        VideoDemoAdapter mAdapter = new VideoDemoAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mExerciseName.setText(mExerciseObject.getName());
        mExerciseDescription.setText(mExerciseObject.getIntroduce());

    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mWorkoutViewModel.setPause(false);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @OnClick(R.id.btn_close)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                //   mWorkoutViewModel.setPause(false);
                dismiss();
                break;
        }
    }

    class VideoDemoAdapter extends FragmentPagerAdapter {


        public VideoDemoAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 1) return VideoYtFragment.newInstance(mExerciseObject.getVideoUrl());
            else return VideoDemoFragment.newInstance(mExerciseObject.getImagePath());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return "Animation";
            else return "Video";
        }
    }

}
