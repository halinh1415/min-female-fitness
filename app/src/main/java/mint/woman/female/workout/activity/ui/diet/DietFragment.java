package mint.woman.female.workout.activity.ui.diet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.GridSpacingItemDecoration;
import mint.woman.female.workout.utils.MyPreference;

public class DietFragment extends Fragment implements DietAdapter.IDietClick {

    @BindView(R.id.rc_diet)
    RecyclerView mMealRc;
    @BindView(R.id.progressBar)
    ProgressBar mProgressDiet;
    @BindView(R.id.tv_program_progress)
    TextView mProgressTxt;
    MyPreference myPreference;
    int progress;
    DietAdapter mDietAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);
        ButterKnife.bind(this,view);
        return view;
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
        mMealRc.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mMealRc.setAdapter(mDietAdapter);
        mMealRc.setNestedScrollingEnabled(false);
        mMealRc.addItemDecoration(new GridSpacingItemDecoration(3, 10, false));
        mProgressDiet.setMax(30);
        mProgressDiet.setProgress(progress);
        mProgressTxt.setText("" + (30 - progress) + " " + getString(R.string.txt_day_left));
        if (30 == progress) {
            showFullDiet();
        }

    }
    private void showFullDiet() {
        AlertDialog.Builder doneAlert = new AlertDialog.Builder(getContext());
        doneAlert.setTitle(getString(R.string.txt_finish));
        doneAlert.setMessage(getString(R.string.txt_are_you_want_to_reset));

        doneAlert.setPositiveButton(getString(R.string.txt_ok), (dialogInterface, i) -> {
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
    @OnClick(R.id.btn_shopping_list)
    void onClick() {
        Intent intent = new Intent(getContext(), ShoppingListActivity.class);
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