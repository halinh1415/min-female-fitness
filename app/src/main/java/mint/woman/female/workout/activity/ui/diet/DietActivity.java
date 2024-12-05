package mint.woman.female.workout.activity.ui.diet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.ActivityDietBinding;
import mint.woman.female.workout.databinding.ContentDietBinding;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;



public class DietActivity extends AppCompatActivity {
    ActivityDietBinding binding;
    ContentDietBinding contentDietBinding;

    MyPreference myPreference;
    int progress;
    DietAdapter mDietAdapter;
    InterstitialAd mInterstitialAd;
    boolean flagAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDietBinding.inflate(getLayoutInflater());
        contentDietBinding = binding.contentDiet;
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        requestNewAdBanner();
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

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void initView() {
        myPreference = new MyPreference(this);
        progress = myPreference.getProgressDiet();
        Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/images/diet_banner.jpg")).into(binding.imgBanner);
        mDietAdapter = new DietAdapter();
        contentDietBinding.rcDiet.setLayoutManager(new GridLayoutManager(this, 3));
        contentDietBinding.rcDiet.setAdapter(mDietAdapter);
        contentDietBinding.rcDiet.setNestedScrollingEnabled(false);
        binding.progressBar.setMax(30);
        binding.progressBar.setProgress(progress);
        binding.tvProgramProgress.setText("" + (30 - progress) + " " + getString(R.string.txt_day_left));
        if (30 == progress) {
            showFullDiet();
        }

        binding.btnShoppingList.setOnClickListener(this::goShoppingList);
    }


    void goShoppingList(View view) {
        Intent intent = new Intent(DietActivity.this, ShoppingListActivity.class);
        startActivity(intent);
    }

    void requestNewAdBanner() {
        binding.adView.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.adView.loadAd(adRequest);
            binding.adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    binding.adView.setVisibility(View.VISIBLE);
                }
            });
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
    protected void onResume() {
        super.onResume();
        if (flagAds) {
            flagAds = false;
            super.onBackPressed();
        }
    }

    private void showFullDiet() {
        AlertDialog.Builder doneAlert = new AlertDialog.Builder(this);
        doneAlert.setTitle("Finish!!!\nAre you want to reset?");
        doneAlert.setPositiveButton(getString(R.string.OK), (dialogInterface, i) -> {
            myPreference.resetDietProgress();
            onRestart();
        });
        doneAlert.setNegativeButton(getString(R.string.txt_cancel), null);
        doneAlert.show();
    }

    private void gotoMeals(int position) {
        Intent intent = new Intent(DietActivity.this, MealDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("DAY", position + 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    class DietAdapter extends RecyclerView.Adapter<DietAdapter.DietViewHolder> {
        @NonNull
        @Override
        public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diet_item_layout, null);
            return new DietViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull DietViewHolder holder, int position) {
            holder.mDay.setText(getString(R.string.txt_day) + " " + (position + 1));
            if (myPreference.isDietDone(position + 1)) {
                holder.mIconDone.setVisibility(View.VISIBLE);
                holder.bg.setBackgroundResource(R.drawable.background_diet_done);
            } else {
                holder.mIconDone.setVisibility(View.INVISIBLE);
                holder.bg.setBackgroundResource(R.drawable.background_diet_none);
            }
            holder.bg.setOnClickListener(view -> {
                gotoMeals(position);
            });
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class DietViewHolder extends RecyclerView.ViewHolder {
            TextView mDay;
            View bg;
            ImageView mIconDone;

            DietViewHolder(View itemView) {
                super(itemView);
                mDay = itemView.findViewById(R.id.txt_diet_day);
                bg = itemView.findViewById(R.id.rl_diet);
                mIconDone = itemView.findViewById(R.id.img_diet_done);

            }
        }
    }
}
