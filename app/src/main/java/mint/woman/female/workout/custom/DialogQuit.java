package mint.woman.female.workout.custom;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;

import java.util.Random;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogQuit extends DialogFragment {

    WorkoutViewModel mWorkoutViewModel;
    @BindView(R.id.txt_quit_content)
    TextView tvQuitContent;

    public OnQuitListener mListener;

    public void setQuitListener(OnQuitListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_quit_workout, container, false);
        mWorkoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);
        ButterKnife.bind(this, view);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
//        dialog.setOnShowListener(dialogInterface -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
//            setupFullHeight(bottomSheetDialog);
//        });
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] arr = getContext().getResources().getStringArray(R.array.arr_quit);
        Random random = new Random();
        int index = random.nextInt(arr.length);
        tvQuitContent.setText(arr[index]);

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mWorkoutViewModel.setPause(false);
    }


    @OnClick({R.id.btn_quit, R.id.btn_no_quit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_quit:
                dismiss();
                if (mListener != null) mListener.onQuitWorkout();
                break;
            case R.id.btn_no_quit:
                dismiss();
                if (mListener != null) mListener.onNoQuitWorkout();
                break;
        }
    }

    public interface OnQuitListener {
        void onQuitWorkout();

        void onNoQuitWorkout();
    }
}
