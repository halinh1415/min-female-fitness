package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.fragment.BMIFragment;
import mint.woman.female.workout.fragment.WeightViewModel;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class DialogWeight extends DialogFragment {



    @BindView(R.id.rdKg)
    RadioButton rdKg;
    @BindView(R.id.rdLb)
    RadioButton rdLb;


    @BindView(R.id.edt_weight)
    SuffixEditText edtWeight;
    @BindView(R.id.edt_height)
    SuffixEditText edtHeight;
    @BindView(R.id.edt_height_2)
    SuffixEditText edtHeight2;
    @BindView(R.id.edt_waist)
    SuffixEditText edtWaist;
    @BindView(R.id.edt_waist2)
    SuffixEditText edtWaist2;
    HistoryViewModel mHistoryViewModel;
    WeightViewModel mWeightViewModel;
    MyPreference myPreference;
    private float weight = 0.0f, height = 0.0f, waist = 0.0f;
    boolean isKg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_weight, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHistoryViewModel = new ViewModelProvider(getActivity()).get(HistoryViewModel.class);
        mWeightViewModel = new ViewModelProvider(getActivity()).get(WeightViewModel.class);

        mWeightViewModel.getIsKg().observe(getViewLifecycleOwner(),isKgL->{
            if (isKgL) {
                edtWeight.setText(String.format("%.1f", weight).replace(",", "."));
                edtHeight.setText(String.format("%.1f", height).replace(",", "."));
                edtWaist.setText(String.format("%.1f", waist).replace(",", "."));
                edtWaist2.setText("");
                edtHeight2.setText("");
                edtHeight2.setVisibility(View.GONE);
                edtWaist2.setVisibility(View.GONE);
                edtWeight.setSuffix("KG");
                edtHeight.setSuffix("CM");
                edtWaist.setSuffix("CM");
                rdKg.setChecked(true);
            } else {
                rdLb.setChecked(true);
                edtHeight2.setVisibility(View.VISIBLE);
                edtWaist2.setVisibility(View.VISIBLE);
                edtWeight.setText(String.format("%.1f", weight * 2.20462262f).replace(",", "."));
                Log.d("HAHA","height = "+height);
                int feetPart = (int) Math.floor(height / 12f / 2.54f);
                float inchesPart = (float) Math.ceil((height / 2.54f) - (feetPart * 12f));
                edtHeight.setText(String.format("%d", feetPart));
                edtHeight2.setText(String.format("%.1f", inchesPart).replace(",", "."));

                Log.d("HAHA","waist = "+waist);
                int ft = (int) Math.floor(waist / 12f / 2.54f);
                float in = (float) Math.ceil((waist / 2.54f) - (ft * 12f));


                edtWaist.setText(String.format("%d", ft));
                edtWaist2.setText(String.format("%.1f", in).replace(",", "."));
                edtWeight.setSuffix("LB");
                edtHeight.setSuffix("FT");
                edtHeight2.setSuffix("IN");
                edtWaist.setSuffix("FT");
                edtWaist2.setSuffix("IN");
            }
        });
        myPreference = new MyPreference(getActivity());
        weight = myPreference.getCurrentWeightKG();
        height = myPreference.getCurrentHeightCm();
        waist = myPreference.getCurrentWaistCm();
        isKg = myPreference.isKg();
        mWeightViewModel.setIsKg( myPreference.isKg());
        mWeightViewModel.setUser(new UserObject(1100,weight,height,waist));


    }

    @OnClick({R.id.rdKg, R.id.rdLb})
    void onClickUnit(View view) {
        updateValue();
        switch (view.getId()) {
            case R.id.rdKg:
                rdKg.setChecked(true);
                if (!isKg){
                    changeUnit();
                }
                break;
            case R.id.rdLb:
                rdLb.setChecked(true);
                if (isKg){
                    changeUnit();
                }
                break;
        }

    }

    @SuppressLint("DefaultLocale")
    void changeUnit() {
        isKg = !isKg;
        if (isKg) {
            edtWeight.setText(String.format("%.1f", weight).replace(",", "."));
            edtHeight.setText(String.format("%.1f", height).replace(",", "."));
            edtWaist.setText(String.format("%.1f", waist).replace(",", "."));
            edtWaist2.setText("");
            edtHeight2.setText("");
            edtHeight2.setVisibility(View.GONE);
            edtWaist2.setVisibility(View.GONE);
            edtWeight.setSuffix("KG");
            edtHeight.setSuffix("CM");
            edtWaist.setSuffix("CM");
            rdKg.setChecked(true);
        } else {
            rdLb.setChecked(true);
            edtHeight2.setVisibility(View.VISIBLE);
            edtWaist2.setVisibility(View.VISIBLE);
            edtWeight.setText(String.format("%.1f", weight * 2.20462262f).replace(",", "."));
            Log.d("HAHA","height = "+height);
            int feetPart = (int) Math.floor(height / 12f / 2.54f);
            float inchesPart = (float) Math.ceil((height / 2.54f) - (feetPart * 12f));
            edtHeight.setText(String.format("%d", feetPart));
            edtHeight2.setText(String.format("%.1f", inchesPart).replace(",", "."));

            Log.d("HAHA","waist = "+waist);
            int ft = (int) Math.floor(waist / 12f / 2.54f);
            float in = (float) Math.ceil((waist / 2.54f) - (ft * 12f));


            edtWaist.setText(String.format("%d", ft));
            edtWaist2.setText(String.format("%.1f", in).replace(",", "."));
            edtWeight.setSuffix("LB");
            edtHeight.setSuffix("FT");
            edtHeight2.setSuffix("IN");
            edtWaist.setSuffix("FT");
            edtWaist2.setSuffix("IN");
        }

    }
    void updateValue(){
        try {
            float temp = Float.parseFloat(edtWaist.getText().toString().replace(",", "."));
            float tempHeight = Float.parseFloat(edtHeight.getText().toString().replace(",", "."));
            float tempWeight = Float.parseFloat(edtWeight.getText().toString().replace(",", "."));
            if (isKg) {
                waist = temp;
                height = tempHeight;
                weight =  tempWeight;
            } else {
                float temp2 = Float.parseFloat(edtWaist2.getText().toString().replace(",", "."));
                float temp2Height = Float.parseFloat(edtHeight2.getText().toString().replace(",", "."));
                waist = temp * 30.48f + 2.54f * temp2;
                height = tempHeight * 30.48f + 2.54f * temp2Height;
                weight =  tempWeight * 0.45359237f;
            }

        }catch (Exception e){
            Log.d("HAHA",e.toString());
            e.printStackTrace();
        }
        int a = 0;
    }

    @OnTextChanged(R.id.edt_weight)
    void onWeightChange(CharSequence charSequence, int i, int i1, int i2) {
        try {
            if (!charSequence.toString().isEmpty()) {
                float temp = Float.parseFloat(charSequence.toString().replace(",", "."));
                if (isKg) {
                    weight = temp;
                } else {
                    weight = temp / 2.20462262f;
                }
                // updateUnit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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







    @OnClick({R.id.btn_enter_weight, R.id.btn_cancel_weight})
    void onClick(View view) {
        updateValue();
        switch (view.getId()) {
            case R.id.btn_enter_weight:
                if (weight < 30f || weight > 150f) {
                    edtWeight.setError(getString(R.string.txt_weight_invalid));
                    return;
                }
                if (height < 80f || height > 250f) {
                    edtHeight.setError(getString(R.string.txt_height_invalid));
                    edtHeight2.setError(getString(R.string.txt_height_invalid));
                    return;
                }
                if (waist < 50 || waist > 200f) {
                    edtWaist.setError(getString(R.string.txt_waist_invalid));
                    edtWaist2.setError(getString(R.string.txt_waist_invalid));
                    return;
                }
                myPreference.setCurrentWeight(weight);
                myPreference.setCurrentHeight(height);
                myPreference.setCurrentWaist(waist);
//                BMIFragment bmiFragment = (BMIFragment) getActivity().getSupportFragmentManager().findFragmentByTag("BMI_FRAGMENT");
//                bmiFragment.updateColor();
                UserObject userObject = new UserObject();
                userObject.setDate(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()));
                userObject.setWeight(myPreference.getCurrentWeightKG());
                userObject.setHeight(myPreference.getCurrentHeightCm());
                userObject.setWaist(myPreference.getCurrentWaistCm());
                mHistoryViewModel.insertUser(userObject);
                mWeightViewModel.setIsKg(isKg);
                myPreference.setUnitKg(isKg);
                mWeightViewModel.setUser(userObject);
                this.dismiss();
                break;
            case R.id.btn_cancel_weight:
                this.dismiss();
                break;
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
