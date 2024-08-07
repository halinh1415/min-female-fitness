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
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DietActivity extends AppCompatActivity {

    @BindView(R.id.rc_diet)
    RecyclerView mMealRc;
    @BindView(R.id.img_banner)
    ImageView mBanner;
    @BindView(R.id.progressBar)
    ProgressBar mProgressDiet;
    @BindView(R.id.tv_program_progress)
    TextView mProgressTxt;
    @BindView(R.id.adView)
    AdView mAdBanner;
    MyPreference myPreference;
    int progress;
    DietAdapter mDietAdapter;
    InterstitialAd mInterstitialAd;
    boolean flagAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/images/diet_banner.jpg")).into(mBanner);
        mDietAdapter = new DietAdapter();
        mMealRc.setLayoutManager(new GridLayoutManager(this, 3));
        mMealRc.setAdapter(mDietAdapter);
        mMealRc.setNestedScrollingEnabled(false);
        mProgressDiet.setMax(30);
        mProgressDiet.setProgress(progress);
        mProgressTxt.setText("" + (30 - progress) + " " + getString(R.string.txt_day_left));
        if (30 == progress) {
            showFullDiet();
        }

    }

    @OnClick(R.id.btn_shopping_list)
    void onClick() {
        Intent intent = new Intent(DietActivity.this, ShoppingListActivity.class);
        startActivity(intent);
    }

    void requestNewAdBanner() {
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
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class DietViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txt_diet_day)
            TextView mDay;
            @BindView(R.id.rl_diet)
            View bg;
            @BindView(R.id.img_diet_done)
            ImageView mIconDone;

            DietViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.rl_diet)
            void onClick(View view) {
                gotoMeals(getAdapterPosition());
            }
        }
    }
}
