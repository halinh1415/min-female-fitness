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
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.FitnessConstUtils;

public class CountDownFragment extends Fragment {


    @BindView(R.id.tv_count_title)
    TextView tvCountTitle;
    @BindView(R.id.btn_pause)
    View btnPause;
    @BindView(R.id.btn_done)
    View btnDone;
    @BindView(R.id.progress_ready)
    ProgressBar progressBar;
    CountDownTimer countDownTimer;
    @BindView(R.id.tv_count_down)
    TextView tvCountDown;
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
        View view = inflater.inflate(R.layout.fragment_count_down, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setMax(10000);
        countDownTimer = new CountDownTimer(10000,10) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long l) {
                int curr = (int)(10000-l)/1000;
                progressBar.setProgress((int) (10000-l));
                if (option == 2 ){
                    tvCountDown.setText(FitnessConstUtils.toTime(10- curr));
                }else if (option == 1){
                    tvCountDown.setText(String.format("%d/10",curr));
                }
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                countDownTimer.start();
            }
        };
        countDownTimer.start();
        switch (option){
            case 2:
                btnPause.setVisibility(View.VISIBLE);
                btnDone.setVisibility(View.GONE);
                tvCountDown.setText("00:20");
                tvCountTitle.setText(R.string.countdown);
                break;
            case 0:
                btnPause.setVisibility(View.GONE);
                btnDone.setVisibility(View.VISIBLE);
                tvCountTitle.setText(R.string.no_counting);
                tvCountDown.setText("x20");
                break;
            case 1:
                tvCountDown.setText("8/10");
                btnDone.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                tvCountTitle.setText(R.string.tempo_counting);
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