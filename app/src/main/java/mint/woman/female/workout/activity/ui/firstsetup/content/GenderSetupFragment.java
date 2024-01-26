package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class GenderSetupFragment extends Fragment {


    @BindView(R.id.lnFemale)
    View mFemale;
    @BindView(R.id.lnMale)
    View mMale;
    MyPreference myPreference;
    public GenderSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gender_setup, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myPreference = new MyPreference(getActivity());
    }

    @OnClick({R.id.lnMale})
    void setMale(){
        mMale.setAlpha(1.0f);
        mFemale.setAlpha(0.3f);
        myPreference.setGender(0);
    }
    @OnClick({R.id.lnFemale})
    void setFemale(){
        mMale.setAlpha(.3f);
        mFemale.setAlpha(1f);
        myPreference.setGender(1);
    }
}