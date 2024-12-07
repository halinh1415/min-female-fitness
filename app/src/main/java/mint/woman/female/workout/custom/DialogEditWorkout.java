package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.DialogMyWorkoutRepsLayoutBinding;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.MyPreference;

public class DialogEditWorkout extends DialogFragment {
    DialogMyWorkoutRepsLayoutBinding binding;

    OnEditFragmentInteractionListener mListener;
    PlanObject.ActionObject actionObject;
    MyPreference myPreference;

    public DialogEditWorkout() {

    }

    public static DialogEditWorkout newInstance(PlanObject.ActionObject actionObject) {
        DialogEditWorkout dialogEditWorkout = new DialogEditWorkout();
        Bundle arg = new Bundle();
        arg.putParcelable("ACTION", actionObject);
        dialogEditWorkout.setArguments(arg);
        return dialogEditWorkout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (getArguments() != null) {
            actionObject = getArguments().getParcelable("ACTION");
        }
        myPreference = MyPreference.newInstance(getContext());
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogMyWorkoutRepsLayoutBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.npkRepsNumber.setMaxValue(300);
        binding.npkRepsNumber.setMinValue(10);
        binding.npkRepsNumber.setValue(actionObject.time);
        binding.npkRepsNumber.setOnValueChangedListener((numberPicker, i, i1) -> {
            actionObject.time = i1;
        });
        binding.btnCancel.setOnClickListener(this::onClick);
        binding.btnOk.setOnClickListener(this::onClick);
    }


    @SuppressLint("SetTextI18n")
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_ok:

                dismiss();
                mListener.onUpdateAction(actionObject);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEditFragmentInteractionListener) {
            mListener = (OnEditFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnEditFragmentInteractionListener {
        void onUpdateAction(PlanObject.ActionObject actionObject);
    }
}
