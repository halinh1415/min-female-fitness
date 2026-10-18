package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    boolean timed, perSide;
    String unit;
    int step, low, high;
    final Handler repeatHandler = new Handler(Looper.getMainLooper());
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
            unit = getArguments().getString("unit");
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

    public static DialogEditExercise newInstance(int actionId, int time, int min, int max, String unit) {
        DialogEditExercise dialogEditExercise = newInstance(actionId, time, min, max);
        dialogEditExercise.getArguments().putString("unit", unit);
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
        // Đơn vị lấy theo kế hoạch (giống danh sách và màn tập), dữ liệu bài tập chỉ là dự phòng.
        if (unit == null) unit = mExerciseObject.getUnit();
        timed = unit.contains("s");
        perSide = mExerciseObject.getAlternation() && unit.isEmpty();
        if (perSide) {
            time = time / 2;
        }
        binding.tvEachSide.setVisibility(perSide ? View.VISIBLE : View.GONE);
        // Bài tính giờ bước 5 giây (5s..10 phút); bài đếm reps bước 1 trong khoảng min..max được truyền vào.
        step = timed ? 5 : 1;
        low = timed ? 5 : Math.max(1, min);
        high = timed ? 600 : (perSide ? Math.max(low, max / 2) : max);
        time = Math.max(low, Math.min(high, time));
        showValue();

        binding.tvExerciseDescription.setText(mExerciseObject.getIntroduce());
        binding.imgClose.setOnClickListener(view1 -> dismiss());
        binding.btnCancel.setOnClickListener(view1 -> dismiss());
        bindRepeat(binding.imMinus, -1);
        bindRepeat(binding.imPlus, 1);
        binding.btnReplace.setOnClickListener(this::onReplace);
    }

    // Giữ nút +/- để tăng/giảm liên tục.
    @SuppressLint("ClickableViewAccessibility")
    private void bindRepeat(View button, int direction) {
        Runnable repeat = new Runnable() {
            @Override
            public void run() {
                if (change(direction)) {
                    repeatHandler.postDelayed(this, 80);
                }
            }
        };
        button.setOnClickListener(v -> change(direction));
        button.setOnLongClickListener(v -> {
            repeatHandler.post(repeat);
            return true;
        });
        button.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                repeatHandler.removeCallbacks(repeat);
            }
            return false;
        });
    }

    private boolean change(int direction) {
        int next;
        if (direction > 0) {
            next = (time / step + 1) * step;
        } else {
            next = ((time + step - 1) / step - 1) * step;
        }
        next = Math.max(low, Math.min(high, next));
        if (next == time) return false;
        time = next;
        showValue();
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void showValue() {
        binding.tvRep.setText(timed ? FitnessConstUtils.toTime(time) : "× " + time);
        binding.imMinus.setEnabled(time > low);
        binding.imMinus.setAlpha(time > low ? 1f : 0.4f);
        binding.imPlus.setEnabled(time < high);
        binding.imPlus.setAlpha(time < high ? 1f : 0.4f);
    }

    void onReplace(View view) {
        if (editExerciseListener != null) {
            editExerciseListener.onChange(actionId, perSide ? time * 2 : time, unit);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        repeatHandler.removeCallbacksAndMessages(null);
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void setEditExerciseListener(EditExerciseListener editExerciseListener) {
        this.editExerciseListener = editExerciseListener;
    }
}
