package mint.woman.female.workout.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.fragment.VideoDemoFragment;
import mint.woman.female.workout.fragment.VideoYtFragment;
import mint.woman.female.workout.model.Customfields;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
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

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewPager)
    CustomViewPager mViewPager;
    @BindView(R.id.chipGroup)
    ChipGroup chipGroup;
    @BindView(R.id.img_muscle)
    ImageView imgMuscle;
    private WorkoutViewModel mWorkoutViewModel;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    Bitmap body;

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
        Customfields customfields = mExerciseObject.getCustomfields();
        if (customfields == null) {
            return;
        }

        for (String major : customfields.getMuscleMajor()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, chipGroup, false);

            int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(major));
            if (resId >0){
                chip.setText(getString(resId));
                chipGroup.addView(chip);
            }

        }
        for (String minor : customfields.getMuscleMinor()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, chipGroup, false);
            chip.setChipIconResource(R.drawable.ic_red_2);
            int resId = FitnessConstUtils.getMuscleId(Integer.parseInt(minor));
            if (resId >0){
                chip.setText(getString(resId));
                chipGroup.addView(chip);
            }
        }
        body = BitmapFactory.decodeResource(getResources(),
                R.drawable.img_body);

        imgMuscle.setImageBitmap(body);
        executor.execute(() -> {
            body = BitmapFactory.decodeResource(getResources(), R.drawable.img_body);
            for (String major : customfields.getMuscleMajor()) {
                int mus = FitnessConstUtils.getMuscle(Integer.parseInt(major), true);
                body = overlay(body, BitmapFactory.decodeResource(getResources(), mus));

            }
            for (String minor : customfields.getMuscleMinor()) {
                int mus = FitnessConstUtils.getMuscle(Integer.parseInt(minor), false);
                body = overlay(body, BitmapFactory.decodeResource(getResources(), mus));
            }
            handler.post(() -> {
                imgMuscle.setImageBitmap(body);
            });
        });
    }

    public Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
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
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mExerciseName.setText(mExerciseObject.getName());
        mExerciseDescription.setText(mExerciseObject.getIntroduce());

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
