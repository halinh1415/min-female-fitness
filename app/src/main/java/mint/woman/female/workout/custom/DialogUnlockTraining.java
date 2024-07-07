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

public class DialogUnlockTraining extends DialogFragment implements View.OnClickListener {
    private OnUnlockTrainingListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_unlock_training, container, false);
        view.findViewById(R.id.img_back_activity).setOnClickListener(this);
        view.findViewById(R.id.ln_go_premium).setOnClickListener(this);
        view.findViewById(R.id.ln_watch_video).setOnClickListener(this);
        return view;
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
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (mListener != null) mListener.onCancel();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back_activity:
                if (mListener != null) {
                    dismiss();
                    mListener.backActivity();
                }
                break;
            case R.id.ln_go_premium:
                if (mListener != null) {
                    mListener.goPremium();
                }
                break;
            case R.id.ln_watch_video:
                if (mListener != null) {
                    mListener.watchVideo();
                }
                break;
        }
    }

    public interface OnUnlockTrainingListener {
        void watchVideo();

        void goPremium();

        void backActivity();

        void onCancel();
    }
}
