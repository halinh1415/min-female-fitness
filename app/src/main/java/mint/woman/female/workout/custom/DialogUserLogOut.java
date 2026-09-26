package mint.woman.female.workout.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback;
import mint.woman.female.workout.databinding.DialogUserLogOutBinding;

public class DialogUserLogOut extends BottomSheetDialogFragment {
    DialogUserLogOutBinding binding;
    private UserUpdateCallback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogUserLogOutBinding.inflate(getLayoutInflater());
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Glide.with(getContext()).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(binding.imIcon);
            binding.tvUserName.setText(user.getDisplayName());
            binding.tvUserEmail.setText(user.getEmail());
        }
        binding.imClose.setOnClickListener(view1 -> dismiss());
        binding.tvLogout.setOnClickListener(view1 -> logOutConfirm());
        binding.tvCancel.setOnClickListener(view1 -> dismiss());
    }

    private void logOutConfirm() {
        AlertDialog.Builder confirm = new AlertDialog.Builder(getContext());
        confirm.setMessage(getString(R.string.login_out_sure));
        confirm.setPositiveButton(getString(R.string.OK), (dialogInterface, i) -> {

            if (callback != null){
                callback.onSigOut();
            }
            dismiss();
        });
        confirm.setNegativeButton(getString(R.string.txt_cancel), (dialogInterface, i) -> dismiss());
        confirm.show();
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
