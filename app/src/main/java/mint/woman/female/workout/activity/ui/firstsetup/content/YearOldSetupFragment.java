package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import mint.woman.female.workout.databinding.FragmentYearOldSetupBinding;
import mint.woman.female.workout.utils.MyPreference;




public class YearOldSetupFragment extends Fragment {
    FragmentYearOldSetupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentYearOldSetupBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.npkAge.setMinValue(1);
        binding.npkAge.setMaxValue(70);
        binding.npkAge.setValue(20);
        MyPreference myPreference = new MyPreference(getActivity());
        binding.npkAge.setOnValueChangedListener((numberPicker, i, value) -> myPreference.setAge(value));

    }
}