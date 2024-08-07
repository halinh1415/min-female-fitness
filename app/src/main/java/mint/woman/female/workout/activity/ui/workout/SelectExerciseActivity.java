package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.adapter.SelectAdapter;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SelectExerciseActivity extends AppCompatActivity implements SelectAdapter.EventSelectExercise, SearchView.OnQueryTextListener {

    //InterstitialAd mInterstitialAd;
    final static String all_equipment = "Punch, Kick, Combo, Block";
    MyPreference myPreference;
    AdView mAdBanner;
    Boolean flagAds = false;
    Menu menu;
    RecyclerView mRecyclerView;
    JsonHelper mJsonHelper;
    Bundle mBundle;
    List<ExerciseObject> mAllExercise;
    SelectAdapter mAdapter;
    ArrayList<Integer> mActionList;

    ActionBar mActionBar;
    String filterValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_exercise);
        initData();
        initView();
    }

    private void initData() {
//        mExTypeList = new ArrayList<>();
//        mExTypeList.add(new ExerciseTypeObject(getString(R.string.txt_kick_boxing), getResources().getIntArray(R.array.break_dance_filter), true));
//        mExTypeList.add(new ExerciseTypeObject(getString(R.string.txt_kick_boxing), getResources().getIntArray(R.array.dab_popping_filter), true));
    }

    private void initView() {
        myPreference = new MyPreference(this);
        mActionBar = getSupportActionBar();
        mActionList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.rc_select_exercise);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            int parentType = mBundle.getInt("PARENT");
            if (parentType == 0) {
                setTitle(getString(R.string.txt_select_exercise));
            } else {
                setTitle(getString(R.string.txt_exercise));
            }
            mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
            mAllExercise = mJsonHelper.getAllExercise(0,1001);
            mAdapter = new SelectAdapter(this, parentType, this);
            mRecyclerView.setAdapter(mAdapter);

            DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            mRecyclerView.addItemDecoration(divider);
            filterValue = "all";
            filterType(filterValue);
        }
        requestNewAdBanner();
        //requestNewInterstitialAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_exercise, menu);
        this.menu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.txt_search_hint));
        searchView.setOnQueryTextListener(this);
        // searchView.setIconified(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int type = 0;
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mn_all_pose:
                filterValue = "all";
                break;
            case R.id.mn_punch:
                filterValue = "punch";
                break;
            case R.id.mn_kick:
                filterValue = "kick";
                break;
            case R.id.mn_combo:
                filterValue = "combo";
                break;
            case R.id.mn_block:
                filterValue = "block";
                break;
            case R.id.mn_warm:
                filterValue = "warm";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        filterType(filterValue);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        Bundle b = new Bundle();
        b.putIntegerArrayList("ADD_ACTIONS", mActionList);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }


    //    @Override
//    protected void onResume() {
//        super.onResume();
//        if (flagAds){
//            flagAds = false;
//            super.onBackPressed();
//        }
//    }
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
    public void onAdd(int action) {
        mActionList.add(action);
        if (mActionBar != null) {
            mActionBar.setSubtitle("" + mActionList.size() + " " + getString(R.string.txt_exercise));
        }
    }

    @Override
    public void onRemove(int action) {
        int index = mActionList.indexOf(action);
        if (index >= 0) {
            mActionList.remove(index);
        }
        if (mActionBar != null) {
            mActionBar.setSubtitle("" + mActionList.size() + " " + getString(R.string.txt_exercise));
        }
    }

    @Override
    public void onDetail(int actionId) {
        Intent detail = new Intent(SelectExerciseActivity.this, ExerciseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("actionId", actionId);
        detail.putExtras(bundle);
        startActivity(detail);
    }

    @Override
    public void onGotoPremium() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1111) {
            Bundle extras = data.getExtras();
            boolean premiumMember = extras.getBoolean("PREMIUM_MEMBER", false);
            if (premiumMember) {
                filterType(filterValue);
            }
        }
    }

    void filterType(String type) {
        List<ExerciseObject> filterList = new ArrayList<>();

        if (type.equals("all")) {
            filterList.addAll(mAllExercise);
        } else {
            for (ExerciseObject exercise : mAllExercise) {


            }
        }


        // filterList.addAll(mAllExercise);
//        Collections.sort(filterList, new Comparator<ExerciseObject>() {
//            @Override
//            public int compare(ExerciseObject exerciseObject, ExerciseObject t1) {
//                int cmp = exerciseObject.getType().compareTo(t1.getType());
//                int pre = exerciseObject.getPremium() - t1.getPremium();
//                if (cmp != 0) return -cmp;
//                return pre == 0 ? exerciseObject.getId() - t1.getId() : pre;
//
//            }
//        });

        mAdapter.setData(filterList);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        List<ExerciseObject> filterList = new ArrayList<>();
        for (ExerciseObject exercise : mAllExercise
        ) {
            if (exercise.getName().toLowerCase().contains(s.toLowerCase())) {
                filterList.add(exercise);
            }
        }
        mAdapter.setData(filterList);
        return false;
    }

}
