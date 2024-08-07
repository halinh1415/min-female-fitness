package mint.woman.female.workout.activity.ui.diet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.fragment.ShoppingListFragment;
import mint.woman.female.workout.model.ProductObject;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;
import java.util.Locale;

public class ShoppingListActivity extends AppCompatActivity {

    JsonHelper mJsonHelper;
    ViewPager mProductVp;
    TabLayout tabLayout;
    List<ProductObject> standardProducts, vegetarianProducts;
    MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);
        mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
        mProductVp = findViewById(R.id.vp_products);
        tabLayout = findViewById(R.id.tab_products);

        standardProducts = mJsonHelper.getListProducts(true);
        vegetarianProducts = mJsonHelper.getListProducts(false);

        mProductVp.setAdapter(new ShoppingListVpAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(mProductVp);
        mProductVp.setCurrentItem(myPreference.getPagerSaved(), true);
        mProductVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                myPreference.setPagerSaved(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


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

    class ShoppingListVpAdapter extends FragmentPagerAdapter {


        public ShoppingListVpAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ShoppingListFragment.newInstance(standardProducts.get(position), vegetarianProducts.get(position));
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.txt_week_count) + " " + (position + 1);
        }
    }
}
