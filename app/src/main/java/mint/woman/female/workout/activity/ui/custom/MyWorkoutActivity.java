package mint.woman.female.workout.activity.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.activity.ui.workout.RestTimeActivity;
import mint.woman.female.workout.common.adapter.MyWorkoutAdapter;
import mint.woman.female.workout.common.data.CustomWorkoutDataProvider;
import mint.woman.female.workout.databinding.ActivityMyWorkoutBinding;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import com.minfitness.decode.Decoder;
import java.util.List;
import java.util.Locale;


@SuppressLint("NonConstantResourceId")
public class MyWorkoutActivity extends AppCompatActivity implements MyWorkoutAdapter.EventListener {


    ActivityMyWorkoutBinding binding;
    MyPreference myPreference;
    boolean flagAds = false, flagRefresh = false;
    MyWorkoutAdapter mAdapter;
    CustomWorkoutDataProvider mCustomWorkoutDataProvider;
    JsonHelper mJsonHelper;
    Database mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initData() {
        mDatabase = FitnessApplication.getApplication().mDatabase;
        mAdapter = new MyWorkoutAdapter();
        List<ProgramObject> list = mDatabase.getMyWorkoutList();
        mAdapter.setData(list);
        if (list.isEmpty()){
            binding.adView.setVisibility(View.GONE);
        }else {
            binding.adView.setVisibility(View.VISIBLE);
        }
        mAdapter.setEventListener(this);
        binding.rcMyWorkout.setLayoutManager(new GridLayoutManager(this, 1));
        binding.rcMyWorkout.setAdapter(mAdapter);
        binding.fbAdd.setOnClickListener(this::showAlertAdd);
    }


    private void initView() {
        myPreference = new MyPreference(this);
        mCustomWorkoutDataProvider = new CustomWorkoutDataProvider();
        mJsonHelper = FitnessApplication.getApplication().mJsonHelper;
        requestNewAdBanner();
    }



    public void showAlertAdd(View v1) {
        final AlertDialog.Builder dialogAdd = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_my_workout, null);
        final NumberPicker daysNumber = view.findViewById(R.id.npk_reps_number);
        daysNumber.setSaveFromParentEnabled(false);
        daysNumber.setSaveEnabled(true);
        daysNumber.setMinValue(1);
        daysNumber.setMaxValue(60);
        daysNumber.setValue(28);
        daysNumber.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        daysNumber.setWrapSelectorWheel(true);
        final EditText workoutName = view.findViewById(R.id.edt_my_workout_name);
        dialogAdd.setView(view);
        dialogAdd.setPositiveButton(getString(R.string.OK), (dialogInterface, i) -> {
            mCustomWorkoutDataProvider.generateDefaultPlan(daysNumber.getValue());
            if (workoutName.getText().toString().isEmpty()) {
                Toast.makeText(MyWorkoutActivity.this, "Please enter workout name!!!", Toast.LENGTH_SHORT).show();
            } else {
                ProgramObject myWorkoutObject = new ProgramObject();
                myWorkoutObject.setName(workoutName.getText().toString());
                myWorkoutObject.setPlan("custom_plan_"+mDatabase.getNewId()+".json");
                myWorkoutObject.setTotal(daysNumber.getValue());
                myWorkoutObject.setImage("custom_workout.webp");
                int newId = mDatabase.insertMyWorkout(myWorkoutObject);
                //myWorkoutObject.setPlan("custom_plan_" + newId + ".json");
               // myWorkoutObject.setId(newId);
               // mDatabase.updateMyWorkout(myWorkoutObject);
                Decoder.encryptStringToFile(mCustomWorkoutDataProvider.convertPlanToJsonString().getBytes(),getFilesDir().getPath() + "/i/" + myWorkoutObject.getPlan());
                //mJsonHelper.writeFileToData(myWorkoutObject.getPlan(), mCustomWorkoutDataProvider.convertPlanToJsonString());
                flagRefresh = true;
                Intent intent = new Intent(MyWorkoutActivity.this, CustomMyWorkoutActivity.class);
                myPreference.setPlanProgress(newId, 0);
                Bundle mBundle = new Bundle();
                mBundle.putInt("CUSTOM_ID", newId);
                intent.putExtras(mBundle);
                startActivity(intent);
                initData();

            }
        });
        dialogAdd.setNegativeButton(getString(R.string.txt_cancel), null);
        dialogAdd.show();

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (flagAds) {
            flagAds = false;
            super.onBackPressed();
        }
        if (flagRefresh) {
            flagRefresh = false;
            initData();
        }
    }

    void requestNewAdBanner() {
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.adView.loadAd(adRequest);
            binding.adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    binding.adView.setVisibility(View.GONE);
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
    public void onEdit(ProgramObject myWorkoutObject) {
        flagRefresh = true;
        Intent intent = new Intent(MyWorkoutActivity.this, CustomMyWorkoutActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putInt("CUSTOM_ID", myWorkoutObject.getId());
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    @Override
    public void onGotoWorkout(ProgramObject myWorkoutObject) {
        Bundle mBundle = new Bundle();
        mBundle.putInt("PROGRAM_ID", myWorkoutObject.getId());
        if (myWorkoutObject.getTotal() > 1) {
            Intent intent = new Intent(MyWorkoutActivity.this, ProgramActivity.class);
            intent.putExtras(mBundle);
            startActivity(intent);
        } else if (myWorkoutObject.getTotal() == 1) {
            List<PlanObject> dayPlanObjectList = mJsonHelper.getListPlan(myWorkoutObject.getPlan(),0,1001);
            if (dayPlanObjectList.get(0).mActionList.size() == 0) {
                flagRefresh = true;
                mBundle.putInt("DAY_NUMBER", 1);
                Intent intent = new Intent(MyWorkoutActivity.this, RestTimeActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
            } else {
                flagRefresh = true;
                Intent intent = new Intent(MyWorkoutActivity.this, PreviewActivity.class);
                mBundle.putInt("DAY_NUMBER", 0);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDeleteWorkout(ProgramObject myWorkoutObject) {
        showDialogDelete(myWorkoutObject.getId());

    }

    private void showDialogDelete(int id) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        // deleteDialog.setTitle(getString(R.string.txt_tips));
        deleteDialog.setTitle(getString(R.string.txt_delete_confirm));
        deleteDialog.setNegativeButton(getString(android.R.string.cancel), null);
        deleteDialog.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            mDatabase.deleteMyWorkout(id);
            mAdapter.setData(mDatabase.getMyWorkoutList());
            Toast.makeText(this, "Deleted this workout!", Toast.LENGTH_SHORT).show();
        });
        deleteDialog.show();
    }

}
