package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.databinding.DialogReplaceExerciseBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import com.minfitness.decode.Decoder;

public class DialogEditExercise extends DialogFragment {
    DialogReplaceExerciseBinding binding;
//    @BindView(R.id.tv_exercise_name)
//    TextView tvExerciseName;
//    @BindView(R.id.tv_exercise_description)
//    TextView tvExerciseIntro;
//    @BindView(R.id.videoView)
//    ImageView videoView;
//    @BindView(R.id.tv_rep)
//    TextView binding.tvRep;
//    @BindView(R.id.tv_each_side)
//    TextView binding.tvEachSide;
    ExerciseObject mExerciseObject;
    EditExerciseListener editExerciseListener;

    int actionId;
    int time;
    int min, max;
    String path;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (getArguments() != null) {
            actionId = getArguments().getInt("id");
            time = getArguments().getInt("time");
            max = getArguments().getInt("max");
            min = getArguments().getInt("min");
        }
        return dialog;
    }

    public static DialogEditExercise newInstance(int actionId, int time, int min, int max) {
        DialogEditExercise dialogEditExercise = new DialogEditExercise();
        Bundle arg = new Bundle();
        arg.putInt("id", actionId);
        arg.putInt("time", time);
        arg.putInt("min", min);
        arg.putInt("max", max);
        dialogEditExercise.setArguments(arg);
        return dialogEditExercise;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      binding = DialogReplaceExerciseBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int resourceId = getContext().getResources().getIdentifier("video_" + actionId, "raw", getContext().getPackageName());
        if (resourceId > 0) {
            path = "android.resource://" + getContext().getPackageName() + "/" + resourceId;
        }
//        videoView.setVideoURI(Uri.parse(path));
//        videoView.setOnPreparedListener(mediaPlayer -> {
//            mediaPlayer.setLooping(true);
//        });
//        videoView.start();
        byte[] encrypt = Decoder.decryptFromAssets(getContext().getAssets(), "i/g/video_" +actionId);
        Glide.with(getContext()).asGif().load(encrypt).into(binding.videoView);
        mExerciseObject = FitnessApplication.getApplication().getHashMapExercise().get(actionId);
        binding.tvExerciseName.setText(mExerciseObject.getName());
        if (mExerciseObject.getAlternation() && mExerciseObject.getUnit().isEmpty()){

            time = time/2;
            binding.tvEachSide.setVisibility(View.VISIBLE);
        }else{
            time = time;

            binding.tvEachSide.setVisibility(View.GONE);
        }
        if (mExerciseObject.getUnit().isEmpty()){
            binding.tvRep.setText(""+time);
        }else {
            binding.tvRep.setText(FitnessConstUtils.toTime(time));
        }

        binding.tvExerciseDescription.setText(mExerciseObject.getIntroduce());
        binding.imMinus.setOnClickListener(view1 -> {
            if (time>2) {
                time--;
                if (mExerciseObject.getUnit().isEmpty()){
                    binding.tvRep.setText(""+time);
                }else {
                    binding.tvRep.setText(FitnessConstUtils.toTime(time));
                }
            }
        });
        binding.imPlus.setOnClickListener(view1 -> {
            if (time<200){
                time++;
                if (mExerciseObject.getUnit().isEmpty()){
                    binding.tvRep.setText(""+time);
                }else {
                    binding.tvRep.setText(FitnessConstUtils.toTime(time));
                }
            }
        });
        binding.btnReplace.setOnClickListener(view1 -> {
            if (editExerciseListener != null) {
                if (mExerciseObject.getAlternation()&& mExerciseObject.getUnit().isEmpty()){
                    editExerciseListener.onChange(actionId, time*2, mExerciseObject.getUnit());
                }else {
                    editExerciseListener.onChange(actionId, time, mExerciseObject.getUnit());
                }
            }
            dismiss();
        });
        binding.imgClose.setOnClickListener(view1 -> {
            dismiss();
        });
    }



    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void setEditExerciseListener(EditExerciseListener editExerciseListener) {
        this.editExerciseListener = editExerciseListener;
    }
}
