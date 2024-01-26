package mint.woman.female.workout.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    @BindView(R.id.txt_kg)
    TextView mKg;
    @BindView(R.id.txt_lbs)
    TextView mLbs;
    @BindView(R.id.txt_inc)
    TextView mInc;
    @BindView(R.id.txt_cm)
    TextView mCm;
    @BindView(R.id.txt_ft)
    TextView mFt;
    @BindView(R.id.txt_inc1)
    TextView mInc1;
    @BindView(R.id.txt_cm1)
    TextView mCm1;
    @BindView(R.id.txt_ft1)
    TextView mFt1;
    @BindView(R.id.txt_bmi_cal)
    TextView mBmiCal;
    @BindView(R.id.edt_weight)
    TextView edtWeight;
    @BindView(R.id.edt_height)
    TextView edtHeight;
    @BindView(R.id.edt_waist)
    TextView edtWaist;
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
        mWeightViewModel = new ViewModelProvider(getActivity()).get(WeightViewModel.class);
        updateColor();
    }

    @SuppressLint("DefaultLocale")
    public void updateColor() {
        myPreference = new MyPreference(getContext());
        if (myPreference.isKg()) {
            mKg.setBackgroundResource(R.drawable.background_text_selected);
            mLbs.setBackgroundResource(R.drawable.background_text_unselected);
        } else {
            mKg.setBackgroundResource(R.drawable.background_text_unselected);
            mLbs.setBackgroundResource(R.drawable.background_text_selected);
        }
        switch (myPreference.uHeight()) {
            case 0:
                mCm.setBackgroundResource(R.drawable.background_text_selected);
                mInc.setBackgroundResource(R.drawable.background_text_unselected);
                mFt.setBackgroundResource(R.drawable.background_text_unselected);
                mCm1.setBackgroundResource(R.drawable.background_text_selected);
                mInc1.setBackgroundResource(R.drawable.background_text_unselected);
                mFt1.setBackgroundResource(R.drawable.background_text_unselected);
                break;
            case 1:
                mCm.setBackgroundResource(R.drawable.background_text_unselected);
                mInc.setBackgroundResource(R.drawable.background_text_unselected);
                mFt.setBackgroundResource(R.drawable.background_text_selected);
                mCm1.setBackgroundResource(R.drawable.background_text_unselected);
                mInc1.setBackgroundResource(R.drawable.background_text_unselected);
                mFt1.setBackgroundResource(R.drawable.background_text_selected);
                break;
            case 2:
                mCm.setBackgroundResource(R.drawable.background_text_unselected);
                mInc.setBackgroundResource(R.drawable.background_text_selected);
                mFt.setBackgroundResource(R.drawable.background_text_unselected);
                mCm1.setBackgroundResource(R.drawable.background_text_unselected);
                mInc1.setBackgroundResource(R.drawable.background_text_selected);
                mFt1.setBackgroundResource(R.drawable.background_text_unselected);
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
        edtWaist.setText(contentWaist);
        edtHeight.setText(contentHeight);
        bmiView.setGender(myPreference.getGender());

        mWeightViewModel.getUser().observe(getActivity(),userObject->{
            bmiView.setWeight(userObject.getWeight());
            bmiView.setHeight(userObject.getHeight()/100);
            mBmiStatus.setText(bmiView.getBodyDescription());
            mBmiCal.setText(String.format("BMI: %.1f (Kg/m2)", bmiView.getBmiValue()));
        });

//        bmiView.setWeight(myPreference.getCurrentWeightKG());
       // bmiView.setHeight(myPreference.getCurrentHeightCm() / 100);

    }




    @OnClick({R.id.txt_lbs, R.id.edt_weight, R.id.edt_height,R.id.edt_waist, R.id.txt_kg, R.id.txt_ft, R.id.txt_cm, R.id.txt_inc, R.id.txt_ft1, R.id.txt_cm1, R.id.txt_inc1, R.id.txt_edit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_lbs:
                myPreference.changeKgUnit(false);
//                if (mListener != null) {
//                    mListener.onWeightChange();
//                }
                mWeightViewModel.setIsKg(false);
                updateColor();
                break;
            case R.id.edt_waist:
            case R.id.edt_weight:
            case R.id.txt_edit:
            case R.id.edt_height:
                mDialogWeight = new DialogWeight();
                mDialogWeight.show(getParentFragmentManager(), "enter_weight");
                break;
            case R.id.txt_kg:
                myPreference.changeKgUnit(true);
//                if (mListener != null) {
//                    mListener.onWeightChange();
//                }
                mWeightViewModel.setIsKg(true);
                updateColor();
                break;
            case R.id.txt_cm1:
            case R.id.txt_cm:
                myPreference.changeHeightUnit(0);
//                if (mListener != null) {
//                    mListener.onWeightChange();
//                }
                mWeightViewModel.setIsKg(true);
                updateColor();
                break;
            case R.id.txt_inc1:
            case R.id.txt_inc:
                myPreference.changeHeightUnit(2);
//                if (mListener != null) {
//                    mListener.onWeightChange();
//                }
                updateColor();
                break;
            case R.id.txt_ft1:
            case R.id.txt_ft:
                myPreference.changeHeightUnit(1);
//                if (mListener != null) {
//                    mListener.onWeightChange();
//                }
                mWeightViewModel.setIsKg(false);
                updateColor();
                break;
            default:
                break;
        }
    }


}
