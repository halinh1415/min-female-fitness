package mint.woman.female.workout.activity.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.activity.premium.PremiumActivity;
import mint.woman.female.workout.activity.premium.PremiumViewModel;
import mint.woman.female.workout.activity.ui.custom.MyWorkoutActivity;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.activity.ui.home.HomeFragment;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.settings.SettingFragment;
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.custom.DialogRating;
import mint.woman.female.workout.custom.DialogUserLogIn;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.FileUtil;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;


public class YogaActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener, SettingFragment.OnSettingsListener, UserUpdateCallback {

    private static final String TAG = "YogaActivity";
    public static boolean isFirst = true;
    public final static int RC_SIGN_IN = 1201;
    public final static int RC_ACCESS_FITNESS = 1211;
    private AppBarConfiguration mAppBarConfiguration;
    ProgramObject mCurrentCategory;
    private MyPreference myPreference;
    int flagAds;

    TextToSpeech tts;
    PremiumViewModel premiumViewModel;
    HistoryViewModel historyViewModel;

    //   GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_report2, R.id.nav_diet_fragment, R.id.nav_setting).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        initView();
        File file = new File(getFilesDir(), "music");
        if (!file.exists()) {
            file.mkdirs();
            FileUtil.copyAssetToInternalStorage(YogaActivity.this, "music/Default.mp3", "music/Default.mp3");
        }

    }

    private void initView() {
        premiumViewModel = new ViewModelProvider(this).get(PremiumViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        flagAds = 0;
        myPreference = MyPreference.newInstance(this);
        myPreference.increaseOpenCount();
        if (myPreference.getPlanProgress(20) > 0) {
            myPreference.setPlanProgress(879, myPreference.getPlanProgress(20));
            myPreference.setPlanProgress(20, 0);
        }
        if (myPreference.getPlanProgress(21) > 0) {
            myPreference.setPlanProgress(881, myPreference.getPlanProgress(21));
            myPreference.setPlanProgress(21, 0);
        }
        if (myPreference.getPlanProgress(22) > 0) {
            myPreference.setPlanProgress(880, myPreference.getPlanProgress(22));
            myPreference.setPlanProgress(22, 0);
        }
        if (myPreference.getPlanProgress(23) > 0) {
            myPreference.setPlanProgress(882, myPreference.getPlanProgress(23));
            myPreference.setPlanProgress(23, 0);
        }

        premiumViewModel.isPremium.observe(this, isPremium -> {
            myPreference.setPremiumMember(isPremium);
        });
        if (isFirst) {
            isFirst = false;

        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_SYNC)) {
            retrieveUserProgress(user.getUid());
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(YogaActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(YogaActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        if (myPreference.isRemind()) {
            createDailyNotify();
        }


        //checkAccount();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlarmReceiver.createNotificationChannels(YogaActivity.this);
                AlarmReceiver.scheduleLockscreenWidget(YogaActivity.this, myPreference.getTimeRemindDaily());
            }
        }

    }

    void createDailyNotify() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(YogaActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(YogaActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        myPreference.setRemind(false);
        AlarmReceiver.createNotificationChannels(YogaActivity.this);
        AlarmReceiver.scheduleLockscreenWidget(YogaActivity.this, myPreference.getTimeRemindDaily());
    }


    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty()) {
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
    public void onBackPressed() {
        if (myPreference.openCount() >= 2 && !myPreference.isRated() && myPreference.getShowRate()) {
            myPreference.setShowRate(false);
            DialogRating mDialogRate = new DialogRating();
            mDialogRate.show(getSupportFragmentManager(), "Rate");
            return;
        }
        if (myPreference.openCount() == 20 && myPreference.getShowRate()) {
            myPreference.setShowRate(false);
            DialogRating mDialogRate = new DialogRating();
            mDialogRate.show(getSupportFragmentManager(), "Rate");
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }

    @Override
    public void onLanguageChange() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(intent);
    }

    @Override
    public void onGetGoogleFit() {
        if (myPreference.isSyncGoogleFit()) {
            FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE).addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ).addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE).build();

            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                myPreference.setSyncGoogleFit(false);
                GoogleSignIn.requestPermissions(this, // your activity
                        RC_ACCESS_FITNESS, GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
            } else {
                accessGoogleFit();
            }
        } else {
            Toast.makeText(this, "Disconnected to Google Fit successfully!", Toast.LENGTH_SHORT).show();
        }
    }


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(), result -> onSignInResult(result));

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            SettingFragment settingFragment = (SettingFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
            if (settingFragment != null) {
                settingFragment.updateAccount(user);
                retrieveUserProgress(user.getUid());
            }

        } else {
            Toast.makeText(YogaActivity.this, getString(R.string.oops_sync_failed), Toast.LENGTH_SHORT).show();
        }
    }

    public void retrieveUserProgress(String userId) {
        // Get a reference to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(userId).child("programs");
        DatabaseReference userDiet = database.getReference("users").child(userId).child("diet");
        DatabaseReference userHistory = database.getReference("users").child(userId).child("history");

        // Attach a listener to read the data at our reference
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot programSnapshot : dataSnapshot.getChildren()) {
                    Integer progress = programSnapshot.getValue(Integer.class);
                    Integer programId = Integer.valueOf(programSnapshot.getKey());
                    if (progress != null) {
                        myPreference.setPlanProgress(programId, progress);
                    }
                }

                // Do something with the retrieved progress list, e.g., update UI
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting UserProgress failed, log a message
                Log.w("HAHA", "loadUserProgress:onCancelled", databaseError.toException());
            }
        });
        userDiet.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dietSnapshot : snapshot.getChildren()) {
                    Boolean isDone = dietSnapshot.getValue(Boolean.class);
                    if (isDone != null) {
                        int day = Integer.valueOf(dietSnapshot.getKey());
                        myPreference.setDietDone(day, isDone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot hisDataSnapshot : snapshot.getChildren()) {
                    HistoryItem historyItem = hisDataSnapshot.getValue(HistoryItem.class);
                    if (historyItem != null) {
                        historyViewModel.insertHistory(historyItem);
                        Log.d("HAHA", "retry " + historyItem.getName());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onSigOut() {
        FirebaseAuth.getInstance().signOut();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        SettingFragment settingFragment = (SettingFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        settingFragment.updateAccount(null);
    }

    @Override
    public void onContinueSync() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
// Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
        signInLauncher.launch(signInIntent);
    }

    @Override
    public void syncData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_SYNC)) {
            String userId = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(userId).child("programs");
            DatabaseReference userDiet = database.getReference("users").child(userId).child("diet");
            SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
            Map<String, ?> allEntries = preferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("PLAN_PROGRESS_")) {
                    int programId = Integer.parseInt(entry.getKey().replace("PLAN_PROGRESS_", ""));
                    int progress = (int) entry.getValue();
                    if (programId == 1 || programId == 879 || programId == 880 || programId == 881 || programId == 882 || programId == 1178){
                        userRef.child(String.valueOf(programId)).setValue(progress).addOnSuccessListener(aVoid -> {
                            Log.d("HAHA", "write progress success!");
                        }).addOnFailureListener(e -> {
                            Log.d("HAHA", "write progress fail!" + e.toString());
                        });
                    }

                }
                if (entry.getKey().startsWith("IS_DONE_DIET")) {
                    int day = Integer.parseInt(entry.getKey().replace("IS_DONE_DIET", ""));
                    boolean isDone = (boolean) entry.getValue();
                    userDiet.child(String.valueOf(day)).setValue(isDone).addOnSuccessListener(aVoid -> {
                        Log.d("HAHA", "write diet progress success!");
                    }).addOnFailureListener(e -> {
                        Log.d("HAHA", "write diet progress fail!" + e.toString());
                    });
                }
            }

            DatabaseReference userHistory = database.getReference("users").child(userId).child("history");
            historyViewModel.getAllHistory().observe(this, historyItems -> {
                for (HistoryItem history : historyItems) {
                    userHistory.child(String.valueOf(history.getId())).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                userHistory.child(String.valueOf(history.getId())).setValue(history).addOnSuccessListener(aVoid -> {
                                    Log.d("HAHA", "write history success!");
                                }).addOnFailureListener(e -> {
                                    Log.d("HAHA", "write history fail!" + e);
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ACCESS_FITNESS) {
            if (resultCode == Activity.RESULT_OK) {
                accessGoogleFit();
            } else {
                myPreference.setSyncGoogleFit(false);
                try {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    SettingFragment settingFragment = (SettingFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                    if (settingFragment != null) {
                        settingFragment.initValue(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }


    }

    private void accessGoogleFit() {
        Toast.makeText(this, "Connected to Google Fit successfully!", Toast.LENGTH_SHORT).show();
        myPreference.setSyncGoogleFit(true);
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            SettingFragment settingFragment = (SettingFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
            if (settingFragment != null) {
                settingFragment.initValue(true);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flagAds == 1) {
            flagAds = 0;
            gotoPlan();
        }

    }

    @Override
    public void onGotoPlan(ProgramObject programObject) {
        mCurrentCategory = programObject;
        //myDB.setCurrentId(ca);
        gotoPlan();
    }

    void gotoPlan() {
        flagAds = 2;
        if (mCurrentCategory == null) return;

        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(YogaActivity.this, ChallengeActivity.class);
            Bundle bundle = new Bundle();
            //bundle.putParcelable("PLAN", mCurrentCategory);
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (mCurrentCategory.getType() == 2) {
            Intent intent2 = new Intent(YogaActivity.this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            //bundle.putParcelable("PLAN", mCurrentCategory);
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            bundle.putInt("DAY_NUMBER", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }

    private String getVersionName() {
        String versionName = "1.0";
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;//Version Nam
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void showUpdateApp() {
        AlertDialog.Builder restart = new AlertDialog.Builder(this);
        restart.setTitle("Update New Version!");
        restart.setNegativeButton(getString(android.R.string.cancel), null);
        restart.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                gotoRate();
            }
        });
        restart.show();
    }

    public void gotoRate() {
        myPreference.setRated();
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    public void onRateShow() {
        DialogRating dialogPreRating = new DialogRating();
        dialogPreRating.show(getSupportFragmentManager(), "rate");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }


        super.onDestroy();
    }

    @Override
    public void onTestVoice(String locale) {
        tts = new TextToSpeech(this, i -> {
            if (i == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale(locale));
                tts.speak("This is test voice!", TextToSpeech.QUEUE_ADD, null);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fitness, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_my_workout:
                startActivity(new Intent(YogaActivity.this, MyWorkoutActivity.class));
                return true;
            case R.id.action_premium:
                startActivity(new Intent(YogaActivity.this, PremiumActivity.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}