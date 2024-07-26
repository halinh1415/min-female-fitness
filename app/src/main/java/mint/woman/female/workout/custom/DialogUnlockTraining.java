package mint.woman.female.workout.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.DialogUnlockTrainingBinding;

public class DialogUnlockTraining extends DialogFragment {
    private OnUnlockTrainingListener mListener;
    DialogUnlockTrainingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogUnlockTrainingBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.imgBackActivity.setOnClickListener(view1 -> {
            if (mListener != null) {
                dismiss();
                mListener.backActivity();
            }
        });
        binding.lnWatchVideo.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.watchVideo();
                binding.imWatch.setVisibility(View.GONE);
                binding.pgLoading.setVisibility(View.VISIBLE);
                binding.lnWatchVideo.setEnabled(false);
            }
        });
        binding.lnGoPremium.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.goPremium();
            }
        });
    }

    public void loadDone(){
        binding.imWatch.setVisibility(View.VISIBLE);
        binding.pgLoading.setVisibility(View.GONE);
        binding.lnWatchVideo.setEnabled(true);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUnlockTrainingListener) {
            mListener = (OnUnlockTrainingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUnlockTrainingListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setOnCancelListener(dialogInterface -> {
                if (mListener != null) mListener.onCancel();
            });
        }
    }



    public interface OnUnlockTrainingListener {
        void watchVideo();

        void goPremium();

        void backActivity();

        void onCancel();
    }
}
