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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.activity.ui.custom.MyWorkoutActivity;
import mint.woman.female.workout.activity.ui.home.HomeFragment;
import mint.woman.female.workout.activity.ui.settings.SettingFragment;
import mint.woman.female.workout.custom.DialogRating;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.util.Locale;


public class YogaActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener, SettingFragment.OnSettingsListener {

    private static final String TAG = "YogaActivity";
    public static boolean isFirst = true;
    public final static int RC_SIGN_IN = 1201;
    public final static int RC_ACCESS_FITNESS = 1211;
    private AppBarConfiguration mAppBarConfiguration;
    ProgramObject mCurrentCategory;
    private MyPreference myPreference;
    int flagAds;
    ConsentInformation consentInformation;

    TextToSpeech tts;

    //   GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_explore, R.id.nav_diet, R.id.nav_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        initView();

        requestTCM();


    }

    private void initView() {

        flagAds = 0;
        myPreference = MyPreference.newInstance(this);
        myPreference.increaseOpenCount();

        if (isFirst) {
            isFirst = false;

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

    private void requestTCM() {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("0EAD9B8D367671ED9B8FF80FFEF0C46A")
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()

                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        //  consentInformation.reset();
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(TAG, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }

                                //  startLoadAd();
                            }
                    );

                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG, "consent XXX " + String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                });

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
            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                    .build();

            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                myPreference.setSyncGoogleFit(false);
                GoogleSignIn.requestPermissions(
                        this, // your activity
                        RC_ACCESS_FITNESS,
                        GoogleSignIn.getLastSignedInAccount(this),
                        fitnessOptions);
            } else {
                accessGoogleFit();
            }
        } else {
            Toast.makeText(this, "Disconnected to Google Fit successfully!", Toast.LENGTH_SHORT).show();
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
        if (mCurrentCategory == null)
            return;

        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(YogaActivity.this, ChallengeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("PLAN", mCurrentCategory);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (mCurrentCategory.getType() == 2) {
            Intent intent2 = new Intent(YogaActivity.this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("PLAN", mCurrentCategory);
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
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
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
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale(locale));
                    tts.speak("This is test voice!", TextToSpeech.QUEUE_ADD, null);
                }
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

        }
        return super.onOptionsItemSelected(item);
    }

}