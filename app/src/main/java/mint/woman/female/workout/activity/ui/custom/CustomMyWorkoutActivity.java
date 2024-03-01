package mint.woman.female.workout.activity.ui.custom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.ExerciseDetailActivity;
import mint.woman.female.workout.activity.ui.workout.SelectExerciseActivity;
import mint.woman.female.workout.common.adapter.CustomWorkoutAdapter;
import mint.woman.female.workout.common.data.CustomWorkoutDataProvider;
import mint.woman.female.workout.custom.DialogEditWorkout;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.VideoEncryptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomMyWorkoutActivity extends AppCompatActivity implements DialogEditWorkout.OnEditFragmentInteractionListener {
    @BindView(R.id.rc_custom_workout)
    RecyclerView mRecyclerView;
    @BindView(R.id.adView)
    AdView mAdBanner;

    CustomWorkoutDataProvider mProvider;
    CustomWorkoutAdapter customWorkoutAdapter;
    JsonHelper mJsonHelper;
    List<ExerciseObject> mAllExercise;
    List<PlanObject> mPlanObjects;
    boolean flagEdit = false;
    Bundle mBundle;
    int mCustomId, mCurrentDay, currentGroup, currentPosition;
    Database mDatabase;
    ProgramObject myWorkoutObject;
    MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_my_workout);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBundle = getIntent().getExtras();
        mDatabase = FitnessApplication.getApplication().mDatabase;
        mCurrentDay = 0;
        if (mBundle != null) {
            mCustomId = mBundle.getInt("CUSTOM_ID");
            mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
            if (mCustomId <= 0) {
                int id = mBundle.getInt("PROGRAM_ID",0);
                myWorkoutObject = FitnessApplication.getApplication().getProgram(id);
            } else {
                myWorkoutObject = mDatabase.getMyWorkoutObject(mCustomId);
            }

            mAllExercise = mJsonHelper.getAllExercise(myWorkoutObject.getDiscover());
            mPlanObjects = mJsonHelper.getListPlan(myWorkoutObject.getPlan(),myWorkoutObject.getDiscover());
            mProvider = new CustomWorkoutDataProvider();
            mProvider.setData(mPlanObjects);
            GeneralItemAnimator animator = new DraggableItemAnimator();
            RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
            RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);
            mRecyclerViewExpandableItemManager.setDefaultGroupsExpandedState(true);

            customWorkoutAdapter = new CustomWorkoutAdapter(this, mProvider, mRecyclerViewExpandableItemManager, flagEdit);
            customWorkoutAdapter.setEventListener(new CustomWorkoutAdapter.EventListener() {
                @Override
                public void onExerciseDetail(int actionId) {
                    Intent detail = new Intent(CustomMyWorkoutActivity.this, ExerciseDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("actionId", actionId);
                    detail.putExtras(bundle);
                    startActivity(detail);
                }

                @Override
                public void onAddExerciseInDay(int day) {
                    mCurrentDay = day;
                    Intent chooseExercise = new Intent(CustomMyWorkoutActivity.this, SelectExerciseActivity.class);
                    chooseExercise.putExtras(mBundle);
                    startActivityForResult(chooseExercise, 1212);
                }

                @Override
                public void onUpdateChildItem(int groupPosition, int childPosition) {
                    showAlertRepsEdit(groupPosition, childPosition);
                }
            });
            RecyclerView.Adapter mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(customWorkoutAdapter);
            mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mWrappedAdapter);
            mRecyclerView.setAdapter(mWrappedAdapter);
            mRecyclerView.setItemAnimator(animator);
            mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
            mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
        }
        requestNewAdBanner();
    }

    void requestNewAdBanner() {
        mAdBanner.setVisibility(View.GONE);
        if (MyPreference.newInstance(this).isShowAds() && MyPreference.newInstance(this).getOK()) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1212) {
            Bundle extras = data.getExtras();
            ArrayList<Integer> addList = extras.getIntegerArrayList("ADD_ACTIONS");
            if (addList != null && addList.size() > 0) {
                for (Integer action : addList) {
                    PlanObject.ActionObject actionObject = new PlanObject.ActionObject();
                    actionObject.actionId = action;
                    actionObject.time = 30;
                    mProvider.addChildItem(mCurrentDay, actionObject);
                }
                customWorkoutAdapter.notifyDataSetChanged();
            }

        }
    }

    private void showAlertRepsEdit(int groupPosition, int childPosition) {
        PlanObject.ActionObject actionObject = mProvider.getChildItem(groupPosition, childPosition);
        currentGroup = groupPosition;
        currentPosition = childPosition;
        DialogEditWorkout dialogEditWorkout = DialogEditWorkout.newInstance(actionObject);
        dialogEditWorkout.show(getSupportFragmentManager(), "ACTION_EDITOR");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_custom_workout, menu);

        menuItem = menu.findItem(R.id.action_copy_to_all);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                flagEdit = !flagEdit;
                if (flagEdit) {
                    menuItem.setVisible(false);
                    item.setTitle(R.string.txt_edit);
                  //  mJsonHelper.writeFileToData(myWorkoutObject.getPlan(), mProvider.convertPlanToJsonString());
                    VideoEncryptor.encryptByte(mProvider.convertPlanToJsonString().getBytes(), getFilesDir().getPath() + "/i/" + myWorkoutObject.getPlan(), "DesignMinFitness");
                    Toast.makeText(this, "Update plan " + myWorkoutObject.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    menuItem.setVisible(true);
                    item.setTitle(R.string.txt_save);
                }
                customWorkoutAdapter.setFlagEdit(flagEdit);
                return true;
            case R.id.action_copy_to_all:
                mProvider.copyToAll();
                customWorkoutAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() && lang.length() > 2) {
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
    public void onUpdateAction(PlanObject.ActionObject actionObject) {
        mProvider.updateItem(currentGroup, currentPosition, actionObject);
        customWorkoutAdapter.notifyDataSetChanged();
    }
}
