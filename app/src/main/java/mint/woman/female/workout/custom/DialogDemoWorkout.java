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
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;


import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.databinding.DialogDemoLayoutBinding;
import mint.woman.female.workout.fragment.VideoDemoFragment;
import mint.woman.female.workout.fragment.VideoYtFragment;
import mint.woman.female.workout.model.Customfields;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.ExerciseHelper;
import mint.woman.female.workout.utils.FitnessConstUtils;


public class DialogDemoWorkout extends DialogFragment{
    DialogDemoLayoutBinding binding;
    ExerciseObject mExerciseObject;
//    @BindView(R.id.tv_exercise_name)
//    TextView mExerciseName;
//    @BindView(R.id.tv_exercise_description)
//    TextView mExerciseDescription;
//
//    @BindView(R.id.tabLayout)
//    TabLayout mTabLayout;
//    @BindView(R.id.viewPager)
//    CustomViewPager mViewPager;
//    @BindView(R.id.chipGroup)
//    ChipGroup chipGroup;
//    @BindView(R.id.img_muscle)
//    ImageView imgMuscle;
//    @BindView(R.id.cv_muscle)
//    View cvMuscle;
    private WorkoutViewModel mWorkoutViewModel;

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
            mExerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(DoWorkoutActivity.discover,DoWorkoutActivity.version).get(id);
        }
//        dialog.setOnShowListener(dialogInterface -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
//            setupFullHeight(bottomSheetDialog);
//            loadExercise();
//        });
        return dialog;
    }


    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
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
        binding = DialogDemoLayoutBinding.inflate(getLayoutInflater());
//        FrameLayout frameLayout = new FrameLayout(getActivity());
//        populateViewForOrientation(inflater, frameLayout);
        return binding.getRoot();
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {

        viewGroup.removeAllViewsInLayout();
        binding = DialogDemoLayoutBinding.inflate(getLayoutInflater());
        //viewGroup.addView(binding.getRoot());

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
        binding.viewPager.setAdapter(mAdapter);
        binding.tabLayout.setupWithViewPager( binding.viewPager);
        binding.tvExerciseName.setText(mExerciseObject.getName());
        binding.tvExerciseDescription.setText(mExerciseObject.getIntroduce());
        try {
            Customfields customfields = mExerciseObject.getCustomfields();
            if (customfields == null) {
                binding.cvMuscle.setVisibility(View.GONE);
                return;
            }

            MuscleMapView muscleMap = binding.muscleMap;
            for (String major : customfields.getMuscleMajor()) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, binding.chipGroup, false);

                int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(major));
                if (resId > 0) {
                    chip.setText(getString(resId));
                    binding.chipGroup.addView(chip);
                    muscleMap.bindChip(chip, major);
                }

            }
            for (String minor : customfields.getMuscleMinor()) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, binding.chipGroup, false);
                chip.setChipIconResource(R.drawable.ic_red_2);
                int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(minor));
                if (resId > 0) {
                    chip.setText(getString(resId));
                    binding.chipGroup.addView(chip);
                    muscleMap.bindChip(chip, minor);
                }
            }
            muscleMap.setMuscleGroups(customfields.getMuscleMajor(), customfields.getMuscleMinor());
        }catch (Exception e){
            e.printStackTrace();
        }

        binding.btnClose.setOnClickListener(view1 -> {
            dismiss();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);
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
        binding.viewPager.setAdapter(mAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tvExerciseName.setText(mExerciseObject.getName());
        binding.tvExerciseDescription.setText(mExerciseObject.getIntroduce());

    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mWorkoutViewModel.setPause(false);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }



    class VideoDemoAdapter extends FragmentPagerAdapter {


        public VideoDemoAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 1) return VideoYtFragment.newInstance(ExerciseHelper.getInstance().getUrl(mExerciseObject.getId()));
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
