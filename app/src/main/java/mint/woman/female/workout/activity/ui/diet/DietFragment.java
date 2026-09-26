package mint.woman.female.workout.activity.ui.diet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.GridSpacingItemDecoration;
import mint.woman.female.workout.databinding.FragmentDietBinding;
import mint.woman.female.workout.utils.MyPreference;

public class DietFragment extends Fragment implements DietAdapter.IDietClick {
    FragmentDietBinding binding;
    MyPreference myPreference;
    int progress;
    DietAdapter mDietAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDietBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        myPreference = new MyPreference(getContext());
        progress = myPreference.getProgressDiet();

        mDietAdapter = new DietAdapter(this);
        binding.rcDiet.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rcDiet.setAdapter(mDietAdapter);
        binding.rcDiet.setNestedScrollingEnabled(false);
        binding.rcDiet.addItemDecoration(new GridSpacingItemDecoration(3, 10, false));
        binding.progressBar.setMax(30);
        binding.progressBar.setProgress(progress);
        binding.tvProgramProgress.setText("" + (30 - progress) + " " + getString(R.string.txt_day_left));
        if (30 == progress) {
            showFullDiet();
        }

        binding.btnShoppingList.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ShoppingListActivity.class);
            startActivity(intent);
        });
    }
    private void showFullDiet() {
        AlertDialog.Builder doneAlert = new AlertDialog.Builder(getContext());
        doneAlert.setTitle(getString(R.string.txt_finish));
        doneAlert.setMessage(getString(R.string.txt_are_you_want_to_reset));

        doneAlert.setPositiveButton(getString(R.string.OK), (dialogInterface, i) -> {
            myPreference.resetDietProgress();

        });
        doneAlert.setNegativeButton(getString(R.string.txt_cancel), null);
        doneAlert.show();
    }

    private void gotoMeals(int position) {
        Intent intent = new Intent(getContext(), MealDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("DAY", position + 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }



    @Override
    public void goDay(int day) {
        Intent intent = new Intent(getContext(), MealDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("DAY", day + 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}