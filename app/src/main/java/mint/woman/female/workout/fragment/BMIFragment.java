package mint.woman.female.workout.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.BMIView;
import mint.woman.female.workout.custom.DialogWeight;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint("NonConstantResourceId")
public class BMIFragment extends Fragment {

    @BindView(R.id.bmiView)
    BMIView bmiView;
    @BindView(R.id.txt_status_bmi)
    TextView mBmiStatus;
    @BindView(R.id.rdKg)
    RadioButton rdKg;
    @BindView(R.id.rdKg2)
    RadioButton rdKg2;
    @BindView(R.id.rdLb)
    RadioButton rdLb;
    @BindView(R.id.rdLb2)
    RadioButton rdLb2;

    @BindView(R.id.rgUnit2)
    RadioGroup rgUnit2;

    @BindView(R.id.txt_bmi_cal)
    TextView mBmiCal;
    @BindView(R.id.edt_weight)
    TextView edtWeight;
    @BindView(R.id.edt_height)
    TextView edtHeight;
    private MyPreference myPreference;
    private DialogWeight mDialogWeight;
    private WeightViewModel mWeightViewModel;
    //private OnBMIFragmentInteractionListener mListener;

    public BMIFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bmi, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myPreference = new MyPreference(getContext());
        mWeightViewModel = new ViewModelProvider(getActivity()).get(WeightViewModel.class);
//        mWeightViewModel.getUser().observe(getViewLifecycleOwner(),userObject -> {
//            updateColor();
//        });

        mWeightViewModel.getUser().observe(getViewLifecycleOwner(),userObject->{

            updateColor();
            bmiView.setWeight(userObject.getWeight());
            bmiView.setHeight(userObject.getHeight()/100);
            mBmiStatus.setText(bmiView.getBodyDescription());
            mBmiCal.setText(String.format("BMI: %.1f (Kg/m2)", bmiView.getBmiValue()));
        });
        mWeightViewModel.setIsKg(myPreference.isKg());
        mWeightViewModel.getIsKg().observe(getViewLifecycleOwner(),aBoolean -> {
            updateColor();
        });
    }

    @SuppressLint("DefaultLocale")
    public void updateColor() {

        if (myPreference.isKg()) {
            rdKg.setChecked(true);
            rdKg2.setChecked(true);
        } else {
            rdLb.setChecked(true);
            rdLb2.setChecked(true);

        }

        String stringWeight = String.format("%.1f %s", myPreference.getCurrentWeight(), myPreference.getWeightUnit());
        SpannableString contentWeight = new SpannableString(stringWeight);
        contentWeight.setSpan(new UnderlineSpan(), 0, stringWeight.length(), 0);
        edtWeight.setText(contentWeight);

        String stringHeight = String.format("%.1f %s", myPreference.getCurrentHeight(), myPreference.getHeightUnit());
        SpannableString contentHeight = new SpannableString(stringHeight);
        contentHeight.setSpan(new UnderlineSpan(), 0, stringHeight.length(), 0);



        String stringWaist = String.format("%.1f %s", myPreference.getCurrentWaist(),myPreference.getHeightUnit());
        stringWaist = stringWaist.replace(",", ".");
        SpannableString contentWaist = new SpannableString(stringWaist);
        contentWaist.setSpan(new UnderlineSpan(), 0, contentWaist.length(), 0);
        edtHeight.setText(contentHeight);
        bmiView.setGender(myPreference.getGender());


//        bmiView.setWeight(myPreference.getCurrentWeightKG());
       // bmiView.setHeight(myPreference.getCurrentHeightCm() / 100);

    }




    @OnClick({R.id.edt_weight, R.id.edt_height, R.id.txt_edit})
    public void onClick(View view) {
        mDialogWeight = new DialogWeight();
        mDialogWeight.show(getParentFragmentManager(), "enter_weight");
    }

    @OnClick({R.id.rdKg, R.id.rdKg2, R.id.rdLb, R.id.rdLb2})
    void onClickUnit(View view) {
        Log.d("HAHA","Click: "+view.getId());
        switch (view.getId()) {
            case R.id.rdKg:
            case R.id.rdKg2:
                Log.d("HAHA","set kg true");
                rdKg.setChecked(true);
                rdKg2.setChecked(true);
                myPreference.setUnitKg(true);
                mWeightViewModel.setIsKg(true);
                break;
            case R.id.rdLb:
            case R.id.rdLb2:
                Log.d("HAHA","set kg false");
                rdLb.setChecked(true);
                rdLb2.setChecked(true);
                myPreference.setUnitKg(false);
                mWeightViewModel.setIsKg(false);
                break;
        }
    }


}
