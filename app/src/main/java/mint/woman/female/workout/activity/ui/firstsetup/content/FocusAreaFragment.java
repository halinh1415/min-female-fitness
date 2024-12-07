package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.FragmentFocusAreaBinding;


public class FocusAreaFragment extends Fragment {
    FragmentFocusAreaBinding binding;




    public FocusAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFocusAreaBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnChooseAbs.setOnClickListener(this::choose);
        binding.btnUnchooseAbs.setOnClickListener(this::choose);
        binding.btnChooseArm.setOnClickListener(this::choose);
        binding.btnUnchooseArm.setOnClickListener(this::choose);
        binding.btnChooseButt.setOnClickListener(this::choose);
        binding.btnUnchooseButt.setOnClickListener(this::choose);
        binding.btnChooseThigh.setOnClickListener(this::choose);
        binding.btnUnchooseThigh.setOnClickListener(this::choose);
        binding.btnChooseFullBody.setOnClickListener(this::choose);
        binding.btnUnchooseFullBody.setOnClickListener(this::choose);
    }

    @SuppressLint("NonConstantResourceId")
    void choose(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_abs:
                binding.btnChooseFullBody.setVisibility(View.GONE);
                binding.btnUnchooseFullBody.setVisibility(View.VISIBLE);
                binding.btnUnchooseAbs.setVisibility(View.VISIBLE);
                binding.btnChooseAbs.setVisibility(View.GONE);
                break;
            case R.id.btn_unchoose_abs:
                binding.btnUnchooseAbs.setVisibility(View.GONE);
                binding.btnChooseAbs.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_choose_arm:
                binding.btnChooseFullBody.setVisibility(View.GONE);
                binding.btnUnchooseFullBody.setVisibility(View.VISIBLE);
                binding.btnChooseArm.setVisibility(View.GONE);
                binding.btnUnchooseArm.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_arm:
                binding.btnChooseArm.setVisibility(View.VISIBLE);
                binding.btnUnchooseArm.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_butt:
                binding.btnChooseFullBody.setVisibility(View.GONE);
                binding.btnUnchooseFullBody.setVisibility(View.VISIBLE);
                binding.btnChooseButt.setVisibility(View.GONE);
                binding.btnUnchooseButt.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_butt:
                binding.btnChooseButt.setVisibility(View.VISIBLE);
                binding.btnUnchooseButt.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_thigh:
                binding.btnChooseFullBody.setVisibility(View.GONE);
                binding.btnUnchooseFullBody.setVisibility(View.VISIBLE);
                binding.btnChooseThigh.setVisibility(View.GONE);
                binding.btnUnchooseThigh.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_thigh:
                binding.btnChooseThigh.setVisibility(View.VISIBLE);
                binding.btnUnchooseThigh.setVisibility(View.GONE);
                break;
            case R.id.btn_unchoose_full_body:
                binding.btnChooseFullBody.setVisibility(View.VISIBLE);
                binding.btnUnchooseFullBody.setVisibility(View.GONE);
                binding.btnChooseAbs.setVisibility(View.VISIBLE);
                binding.btnChooseButt.setVisibility(View.VISIBLE);
                binding.btnChooseThigh.setVisibility(View.VISIBLE);
                binding.btnChooseArm.setVisibility(View.VISIBLE);
                binding.btnUnchooseAbs.setVisibility(View.GONE);
                binding.btnUnchooseArm.setVisibility(View.GONE);
                binding.btnUnchooseThigh.setVisibility(View.GONE);
                binding.btnUnchooseButt.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_full_body:
                binding.btnChooseFullBody.setVisibility(View.GONE);
                binding.btnUnchooseFullBody.setVisibility(View.VISIBLE);
                binding.btnChooseAbs.setVisibility(View.GONE);
                binding.btnChooseButt.setVisibility(View.GONE);
                binding.btnChooseThigh.setVisibility(View.GONE);
                binding.btnChooseArm.setVisibility(View.GONE);
                binding.btnUnchooseAbs.setVisibility(View.VISIBLE);
                binding.btnUnchooseArm.setVisibility(View.VISIBLE);
                binding.btnUnchooseThigh.setVisibility(View.VISIBLE);
                binding.btnUnchooseButt.setVisibility(View.VISIBLE);
                break;
        }
    }
}