package mint.woman.female.workout.custom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback;
import mint.woman.female.workout.databinding.DialogUserLogInBinding;

public class DialogUserLogIn extends BottomSheetDialogFragment {
    DialogUserLogInBinding binding;
    private UserUpdateCallback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogUserLogInBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.imClose.setOnClickListener(view1 -> dismiss());
        binding.tvContinue.setOnClickListener(view1 -> {
            if (callback!= null){
                callback.onContinueSync();
            }
            dismiss();
        });
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserUpdateCallback) {
            callback = (UserUpdateCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UserUpdateCallback");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
