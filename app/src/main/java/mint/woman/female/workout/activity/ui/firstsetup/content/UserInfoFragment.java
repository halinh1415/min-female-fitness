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
import mint.woman.female.workout.custom.SuffixTextView;
import com.zjun.widget.RuleView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class UserInfoFragment extends Fragment {
    @BindView(R.id.rule_weight)
    RuleView mRuleWeight;

    @BindView(R.id.rule_height)
    RuleView mRuleHeight;
    @BindView(R.id.tv_weight_value)
    SuffixTextView mUserWeight;
    @BindView(R.id.tv_height_value)
    SuffixTextView mUserHeight;
    @BindView(R.id.rdKg)
    RadioButton rdKg;
    @BindView(R.id.rdKg1)
    RadioButton rdKg1;
    @BindView(R.id.rdLb)
    RadioButton rdLb;
    @BindView(R.id.rdLb1)
    RadioButton rdLb1;
    boolean isKg = true;
    float weight = 65.0f;
    float height = 175.0f;
    SetupViewModel setupViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel = new ViewModelProvider(getActivity()).get(SetupViewModel.class);
        if (isKg) {
            mUserWeight.setSuffix("kg");
            mUserHeight.setSuffix("cm");

            mRuleWeight.setValue(35f, 200f, weight, 0.1f, 10);
            mRuleHeight.setValue(90f, 250f, height, 0.1f, 10);
        } else {
            mUserWeight.setSuffix("lb");
            mUserHeight.setSuffix("ft");
            mRuleWeight.setValue(35f, 440f, weight, 0.1f, 10);
            mRuleHeight.setValue(2.9f, 8.2f, height, 0.1f, 10);
        }
        mRuleHeight.setOnValueChangedListener(new RuleView.OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                height = value;
                mUserHeight.setText(String.format(" %.1f", value));
            }
        });

        mRuleWeight.setOnValueChangedListener(new RuleView.OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                weight = value;
                mUserWeight.setText(String.format(" %.1f", value));
            }
        });

    }

    @OnClick({R.id.rdKg, R.id.rdKg1, R.id.rdLb, R.id.rdLb1})
    void onClickUnit(View view) {
        updateValue();
        switch (view.getId()) {
            case R.id.rdKg:
            case R.id.rdKg1:
                rdKg.setChecked(true);
                rdKg1.setChecked(true);
                if (!isKg) {
                    changeUnit();
                }
                break;
            case R.id.rdLb:
            case R.id.rdLb1:
                rdLb.setChecked(true);
                rdLb1.setChecked(true);
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
            mUserWeight.setSuffix("kg");
            mUserHeight.setSuffix("cm");
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
            mRuleWeight.setValue(35f, 200f, weight, 0.1f, 10);
            mRuleHeight.setValue(80f, 250f, height, 0.1f, 10);
        } else {
            mUserWeight.setSuffix("lb");
            mUserHeight.setSuffix("ft");
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

            mRuleWeight.setValue(35f, 440f, weight, 0.1f, 10);
           // mRuleHeight.setValue(2.5f, 8.2f, height, 0.1f, 10);
            mRuleHeight.setValue(2.5f, 10f, height, 0.1f, 10);
        }
    }

    private void updateValue() {
    }
}