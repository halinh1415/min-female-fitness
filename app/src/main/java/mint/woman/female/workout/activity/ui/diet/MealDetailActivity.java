package mint.woman.female.workout.activity.ui.diet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.model.DietObject;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;
import java.util.Locale;


public class MealDetailActivity extends AppCompatActivity implements RadioButton.OnCheckedChangeListener {

    RecyclerView mMealsRc;
    ImageView mImgDone;
    int mDay;
    MyPreference myPreference;
    AdView mAdBanner;
    boolean flagAds = false;
    RadioButton mStandard, mVegetarian;
    MealAdapter mealAdapter;
    JsonHelper mJsonHelper;
    List<DietObject> mDietObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meal_detail);

        initView();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        requestNewAdBanner();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (flagAds) {
            flagAds = false;
            super.onBackPressed();
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

    private void initView() {

        Bundle bundle = getIntent().getExtras();
        mDay = bundle.getInt("DAY");
        mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
        mDietObjects = mJsonHelper.getListDiet();
        setTitle(getString(R.string.txt_day) + " " + mDay);
        mMealsRc = findViewById(R.id.rc_meals);
        mImgDone = findViewById(R.id.img_meal_done);
        mStandard = findViewById(R.id.rb_standard);
        mVegetarian = findViewById(R.id.rb_vegetarian);

        myPreference = new MyPreference(this);
        if (myPreference.isStandardDiet()) {
            mStandard.setChecked(true);
            mVegetarian.setChecked(false);
        } else {
            mStandard.setChecked(false);
            mVegetarian.setChecked(true);
        }
        mealAdapter = new MealAdapter(mDietObjects.get(mDay - 1));
        mMealsRc.setLayoutManager(new GridLayoutManager(this, 1));
        mMealsRc.setAdapter(mealAdapter);
        mImgDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogDone();
            }
        });

        mStandard.setOnCheckedChangeListener(this);
        mVegetarian.setOnCheckedChangeListener(this);
    }

    void requestNewAdBanner() {
        mAdBanner = findViewById(R.id.adView);
        mAdBanner.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdBanner.loadAd(adRequest);
            mAdBanner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdBanner.setVisibility(View.VISIBLE);
                }
            });
        }
    }



    private void showDialogDone() {
        AlertDialog.Builder doneAlert = new AlertDialog.Builder(this);
        doneAlert.setTitle(getString(R.string.txt_day) + " " + mDay + " " + getString(R.string.txt_completed));
        doneAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myPreference.setDietDone(mDay, true);
            }
        });
        doneAlert.setNegativeButton(android.R.string.cancel, null);
        doneAlert.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.rb_standard:
                if (b) {
                    myPreference.setStandardDiet(true);
                } else {
                    myPreference.setStandardDiet(false);
                }
                mDietObjects = mJsonHelper.getListDiet();
                mealAdapter.setMeals(mDietObjects.get(mDay - 1));
                break;
            case R.id.rb_vegetarian:
                break;
        }
    }

    class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
        private DietObject dietObject;

        public MealAdapter(DietObject dietObject) {
            this.dietObject = dietObject;
        }

        public void setMeals(DietObject dietObject) {
            this.dietObject = dietObject;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_item_layout, null);
            return new MealViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
            switch (position) {
                case 0:
                    holder.mIconMeal.setImageResource(R.drawable.ic_breakfast);
                    break;
                case 1:
                    holder.mIconMeal.setImageResource(R.drawable.ic_snack);
                    break;
                case 2:
                    holder.mIconMeal.setImageResource(R.drawable.ic_lunch);
                    break;
                case 3:
                    holder.mIconMeal.setImageResource(R.drawable.ic_dinner);
                    break;
            }
            holder.mNameMeal.setText(dietObject.meals.get(position).name);
            String recipes = "";
            for (int i = 0; i < dietObject.meals.get(position).recipes.size(); i++) {
                recipes = recipes + "-" + dietObject.meals.get(position).recipes.get(i) + "\n";
            }
            holder.mRecipes.setText(recipes);
        }

        @Override
        public int getItemCount() {
            return dietObject.meals.size();
        }

        class MealViewHolder extends RecyclerView.ViewHolder {
            ImageView mIconMeal;
            TextView mNameMeal;
            TextView mRecipes;

            public MealViewHolder(View itemView) {
                super(itemView);
                mIconMeal = itemView.findViewById(R.id.img_icon_meal);
                mNameMeal = itemView.findViewById(R.id.txt_meal_name);
                mRecipes = itemView.findViewById(R.id.txt_recipes);
            }
        }
    }
}
