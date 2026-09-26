package mint.woman.female.workout.activity.ui.workout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;

public class RestTimeActivity extends AppCompatActivity implements View.OnClickListener {

    InterstitialAd mInterstitialAd;
    AdView mAdBanner;
    MyPreference myPreference;
    boolean flagAds = false;
    ProgramObject mProgramObject;
    int indexDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_time);
        initView();
        requestNewAdBanner();
        requestNewInterstitialAd();
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null ) {
            flagAds = true;
            mInterstitialAd.show(RestTimeActivity.this);
        } else {
            super.onBackPressed();
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

    private void initView() {
        myPreference = new MyPreference(this);
        Bundle mBundle = getIntent().getExtras();
        mProgramObject = mBundle.getParcelable("PLAN");
        indexDay = mBundle.getInt("DAY_NUMBER");
        findViewById(R.id.btn_finish).setOnClickListener(this);
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

    void requestNewInterstitialAd() {
        if (myPreference.isShowAds() && myPreference.getOK()) {

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_finish:
                if (indexDay + 1 > myPreference.getPlanProgress(mProgramObject.getId())) {
                    myPreference.setPlanProgress(mProgramObject.getId(), indexDay + 1);
                }
                onBackPressed();
                break;
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
