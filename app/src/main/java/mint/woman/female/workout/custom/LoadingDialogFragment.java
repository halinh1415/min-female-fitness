package mint.woman.female.workout.custom;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.DoWorkoutActivity;
import mint.woman.female.workout.databinding.DialogLoadingBinding;

public class LoadingDialogFragment extends DialogFragment {
    DialogLoadingBinding binding;
    String mLottieFile = "loading.json";
    String content = "Loading...";

    public LoadingDialogFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLottieFile = getArguments().getString("json");
            content = getArguments().getString("content");
        }
    }

    public static LoadingDialogFragment newInstance(String jsonFile, String content) {
        LoadingDialogFragment dialogLoading = new LoadingDialogFragment();
        Bundle arg = new Bundle();
        arg.putString("json", jsonFile);
        arg.putString("content", content);
        dialogLoading.setArguments(arg);
        return dialogLoading;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogLoadingBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mLottieFile.isEmpty()) {
            binding.ltMusicLoad.setAnimation(mLottieFile);
            binding.ltMusicLoad.playAnimation();
        }else {
            binding.ltMusicLoad.setAnimation("loading.json");
            binding.ltMusicLoad.playAnimation();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
