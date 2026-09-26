package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.firstsetup.SetupViewModel;
import mint.woman.female.workout.databinding.FragmentUserInfoBinding;


public class UserInfoFragment extends Fragment {
    FragmentUserInfoBinding binding;
    boolean isKg = true;
    float weight = 65.0f;
    float height = 175.0f;
    SetupViewModel setupViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserInfoBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel = new ViewModelProvider(getActivity()).get(SetupViewModel.class);
        if (isKg) {
            binding.tvWeightValue.setSuffix("kg");
            binding.tvHeightValue.setSuffix("cm");

            binding.ruleWeight.setValue(35f, 200f, weight, 0.1f, 10);
            binding.ruleHeight.setValue(90f, 250f, height, 0.1f, 10);
        } else {
            binding.tvWeightValue.setSuffix("lb");
            binding.tvHeightValue.setSuffix("ft");
            binding.ruleWeight.setValue(35f, 440f, weight, 0.1f, 10);
            binding.ruleHeight.setValue(2.9f, 8.2f, height, 0.1f, 10);
        }
        binding.ruleHeight.setOnValueChangedListener(value -> {
            height = value;
            setupViewModel.setHeight(value);
            binding.tvHeightValue.setText(String.format(" %.1f", value));
        });

        binding.ruleWeight.setOnValueChangedListener(value -> {
            weight = value;
            setupViewModel.setWeight(value);
            binding.tvWeightValue.setText(String.format(" %.1f", value));
        });

        binding.rdKg.setOnClickListener(this::onClickUnit);
        binding.rdKg1.setOnClickListener(this::onClickUnit);
        binding.rdLb.setOnClickListener(this::onClickUnit);
        binding.rdLb1.setOnClickListener(this::onClickUnit);
    }

    void onClickUnit(View view) {
        updateValue();
        switch (view.getId()) {
            case R.id.rdKg:
            case R.id.rdKg1:
                binding.rdKg.setChecked(true);
                binding.rdKg1.setChecked(true);
                if (!isKg) {
                    changeUnit();
                }
                break;
            case R.id.rdLb:
            case R.id.rdLb1:
                binding.rdLb.setChecked(true);
                binding.rdLb1.setChecked(true);
                if (isKg) {
                    changeUnit();
                }
                break;
        }

    }

    private void changeUnit() {
        if (isKg) {
            weight = weight * 2.20462262f;
            height = height /30.48f;
        } else {
            weight = weight / 2.20462262f;
            height = height * 30.48f;
        }

        isKg = !isKg;
        setupViewModel.setKg(isKg);
        setupViewModel.setWeight(weight);
        setupViewModel.setHeight(height);
        if (isKg) {
            binding.tvWeightValue.setSuffix("kg");
            binding.tvHeightValue.setSuffix("cm");
            if (weight >200f){
                weight = 180f;
            }
            if (weight < 35f){
                weight = 40f;
            }
            if (weight > 80){
                weight = 180f;
            }
            if (height < 80f){
                height = 81f;
            }
            if (height > 250f){
                height = 200f;
            }
            binding.ruleWeight.setValue(35f, 200f, weight, 0.1f, 10);
            binding.ruleHeight.setValue(80f, 250f, height, 0.1f, 10);
        } else {
            binding.tvWeightValue.setSuffix("lb");
            binding.tvHeightValue.setSuffix("ft");
            if (weight > 440){
                weight = 400f;
            }
            if (weight < 2.5f){
                weight = 2.5f;
            }
            if (height < 2.5f){
                height = 2.6f;
            }
            if (height > 10f){
                height = 9.9f;
            }

            binding.ruleWeight.setValue(35f, 440f, weight, 0.1f, 10);
           // mRuleHeight.setValue(2.5f, 8.2f, height, 0.1f, 10);
            binding.ruleHeight.setValue(2.5f, 10f, height, 0.1f, 10);
        }
    }

    private void updateValue() {
    }
}