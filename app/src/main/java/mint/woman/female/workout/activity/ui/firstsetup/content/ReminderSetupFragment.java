package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mint.woman.female.workout.activity.ui.firstsetup.SetupViewModel;
import mint.woman.female.workout.databinding.FragmentReminderSetupBinding;


public class ReminderSetupFragment extends Fragment {


    FragmentReminderSetupBinding binding;
    SetupViewModel mSetupViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReminderSetupBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSetupViewModel = new ViewModelProvider(getActivity()).get(SetupViewModel.class);
        binding.npkHour.setMinValue(0);
        binding.npkHour.setMaxValue(23);
        binding.npkHour.setValue(20);
        binding.npkMinute.setValue(0);
        binding.npkHour.setFormatter(i -> String.format("%02d", i));
        binding.npkMinute.setFormatter(i -> String.format("%02d", i));
        binding.npkMinute.setMinValue(0);
        binding.npkMinute.setMaxValue(59);
        binding.npkHour.setOnValueChangedListener((numberPicker, i, value) -> mSetupViewModel.setHour(value));
        binding.npkMinute.setOnValueChangedListener((numberPicker, i, value) -> mSetupViewModel.setMinute(value));
    }
}