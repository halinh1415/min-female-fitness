package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.databinding.FragmentGenderSetupBinding;
import mint.woman.female.workout.utils.MyPreference;




public class GenderSetupFragment extends Fragment {

    FragmentGenderSetupBinding binding;


    MyPreference myPreference;
    public GenderSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGenderSetupBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myPreference = new MyPreference(getActivity());
        binding.lnFemale.setOnClickListener(this::setFemale);
        binding.lnMale.setOnClickListener(this::setMale);
    }


    void setMale(View view){
        binding.lnMale.setAlpha(1.0f);
        binding.lnFemale.setAlpha(0.3f);
        myPreference.setGender(0);
    }

    void setFemale(View view){
        binding.lnMale.setAlpha(.3f);
        binding.lnFemale.setAlpha(1.0f);
        myPreference.setGender(1);
    }
}