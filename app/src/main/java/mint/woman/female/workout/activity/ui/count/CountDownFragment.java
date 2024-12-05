package mint.woman.female.workout.activity.ui.count;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.FragmentCountDownBinding;
import mint.woman.female.workout.utils.FitnessConstUtils;

public class CountDownFragment extends Fragment {



    CountDownTimer countDownTimer;
    FragmentCountDownBinding binding;
    int option = 0;

    public CountDownFragment() {
        // Required empty public constructor
    }


    public static CountDownFragment newInstance(int option){
        CountDownFragment countDownFragment = new CountDownFragment();
        Bundle args = new Bundle();
        args.putInt("OPTION",option);
        countDownFragment.setArguments(args);
        return countDownFragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            option = getArguments().getInt("OPTION");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         binding = FragmentCountDownBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.progressReady.setMax(10000);
        countDownTimer = new CountDownTimer(10000,10) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long l) {
                int curr = (int)(10000-l)/1000;
                binding.progressReady.setProgress((int) (10000-l));
                if (option == 2 ){
                    binding.tvCountDown.setText(FitnessConstUtils.toTime(10- curr));
                }else if (option == 1){
                    binding.tvCountDown.setText(String.format("%d/10",curr));
                }
            }

            @Override
            public void onFinish() {
                binding.progressReady.setProgress(0);
                countDownTimer.start();
            }
        };
        countDownTimer.start();
        switch (option){
            case 2:
                binding.btnPause.setVisibility(View.VISIBLE);
                binding.btnDone.setVisibility(View.GONE);
                binding.tvCountDown.setText("00:20");
                binding.tvCountTitle.setText(R.string.countdown);
                break;
            case 0:
                binding.btnPause.setVisibility(View.GONE);
                binding.btnDone.setVisibility(View.VISIBLE);
                binding.tvCountTitle.setText(R.string.no_counting);
                binding.tvCountDown.setText("x20");
                break;
            case 1:
                binding.tvCountDown.setText("8/10");
                binding.btnDone.setVisibility(View.GONE);
                binding.btnPause.setVisibility(View.VISIBLE);
                binding.tvCountTitle.setText(R.string.tempo_counting);
                break;
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer!= null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

    }

}