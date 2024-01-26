package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;


public class YearOldSetupFragment extends Fragment {
    @BindView(R.id.npk_age)
    NumberPicker npkAge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_year_old_setup, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        npkAge.setMinValue(1);
        npkAge.setMaxValue(70);
        npkAge.setValue(20);
        MyPreference myPreference = new MyPreference(getActivity());
        npkAge.setOnValueChangedListener((numberPicker, i, value) -> myPreference.setAge(value));

    }
}