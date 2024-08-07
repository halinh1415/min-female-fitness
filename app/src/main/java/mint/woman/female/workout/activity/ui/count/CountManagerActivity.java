package mint.woman.female.workout.activity.ui.count;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.custom.KKViewPager;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

public class CountManagerActivity extends AppCompatActivity {

    @BindView(R.id.vpCount)
    KKViewPager viewPager;
    @BindView(R.id.tv_count_info)
    TextView tvCountInfo;
    @BindView(R.id.tv_count_title)
    TextView tvCountTitle;
    MyPreference myPreference;
    int currentOption = 0;
    int[] titleCountIds = new int[]{ R.string.no_counting, R.string.tempo_counting,R.string.countdown};
    int[] infoCountIds = new int[]{ R.string.txt_count_down_info_2, R.string.txt_count_down_info_3,R.string.txt_count_down_info_1};


    WorkoutViewModel mWorkoutViewModel;
    //    @BindView(R.id.pager_container)
//    PagerContainer mContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_count_manager);

        ButterKnife.bind(this);
        initView();

    }

    private void initView() {
        myPreference = new MyPreference(this);
        mWorkoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        currentOption = myPreference.getCountOption();
        tvCountInfo.setText(getString(infoCountIds[currentOption]));
        tvCountTitle.setText(getString(titleCountIds[currentOption]));
        viewPager.setAdapter(new CountPagerAdapter(getSupportFragmentManager()));
        viewPager.setAnimationEnabled(true);
        viewPager.setFadeEnabled(true);
        viewPager.setFadeFactor(0.6f);
        viewPager.setPageMargin(20);
        viewPager.setPadding(200, 0, 200, 0);
        viewPager.setCurrentItem(currentOption);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                myPreference.setCountOption(position);
                tvCountInfo.setText(infoCountIds[position]);
                tvCountTitle.setText(getString(titleCountIds[position]));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //viewPager.setClipChildren(false);
    }


    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        Bundle b = new Bundle();
        b.putInt("OPTION", myPreference.getCountOption());
        intent.putExtras(b);
        setResult(currentOption != myPreference.getCountOption()?RESULT_OK:RESULT_CANCELED, intent);
        finish();
        mWorkoutViewModel.setPause(false);
        super.onBackPressed();

    }

    class CountPagerAdapter extends FragmentPagerAdapter {


        public CountPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return CountDownFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() ) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}