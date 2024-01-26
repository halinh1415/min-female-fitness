package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.firstsetup.SetupViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReminderSetupFragment extends Fragment {

    @BindView(R.id.npkHour)
    NumberPicker npkHour;
    @BindView(R.id.npkMinute)
    NumberPicker npkMinute;
    SetupViewModel mSetupViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_reminder_setup, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSetupViewModel = new ViewModelProvider(getActivity()).get(SetupViewModel.class);
        npkHour.setMinValue(0);
        npkHour.setMaxValue(23);
        npkHour.setValue(20);
        npkMinute.setValue(0);
        npkHour.setFormatter(i -> String.format("%02d",i));
        npkMinute.setFormatter(i -> String.format("%02d",i));
        npkMinute.setMinValue(0);
        npkMinute.setMaxValue(59);
        npkHour.setOnValueChangedListener((numberPicker, i, value) -> mSetupViewModel.setHour(value));
        npkMinute.setOnValueChangedListener((numberPicker, i, value) -> mSetupViewModel.setMinute(value));
    }
}