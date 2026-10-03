package mint.woman.female.workout.activity.ui.settings;


import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.health.connect.HealthConnectManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import mint.woman.female.workout.BuildConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.PolicyActivity;
import mint.woman.female.workout.activity.ui.badge.BadgeActivity;
import mint.woman.female.workout.activity.ui.main.HealthDataDialog;
import mint.woman.female.workout.activity.ui.sounds.SoundsActivity;
import mint.woman.female.workout.badge.BadgeRepository;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.custom.CustomPreference;
import mint.woman.female.workout.custom.DialogUserLogIn;
import mint.woman.female.workout.custom.DialogUserLogOut;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private OnSettingsListener mListener;
    private final SimpleDateFormat mFormatDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    public final static String SUPPORT_LANGUAGES = "en@ar@de@es@fa@fr@in_ID@it@ja@ko@nl@pl@pt@ru@tr@vi@zh_CN@zh_TW@th@uk@da@no@lv@fi@hr@ca@cs";

    CustomPreference preferenceBackUp;
    MyPreference myPreference;
    DialogUserLogOut dialogUserLogOut;
    DialogUserLogIn dialogUserLogIn;
    boolean isSync = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);
        myPreference = new MyPreference(getContext());
        findPreference("GO_PREMIUM").setOnPreferenceClickListener(this);
        findPreference("ACHIEVEMENTS").setOnPreferenceClickListener(this);
        findPreference("ST_RATE").setOnPreferenceClickListener(this);
        findPreference("ST_FEEDBACK").setOnPreferenceClickListener(this);
        findPreference("ST_SHARE").setOnPreferenceClickListener(this);
        findPreference("TTS_ENGINE").setOnPreferenceClickListener(this);
        findPreference("PRIVACY_POLICY").setOnPreferenceClickListener(this);
        findPreference("TTS_INSTALL_VOICE").setOnPreferenceClickListener(this);
        findPreference("SYNC_FIT").setOnPreferenceClickListener(this);

        findPreference("ST_REMINDER").setOnPreferenceClickListener(this);
        findPreference("BACK_UP").setOnPreferenceClickListener(this);
        Preference music = findPreference("BACK_GROUND_MUSIC");
        music.setSummary(MyPreference.newInstance(getContext()).getMusicName());
        findPreference("BACK_GROUND_MUSIC").setOnPreferenceClickListener(this);
        findPreference("RECOMMEND_APP").setOnPreferenceClickListener(this);
        findPreference("COACH").setOnPreferenceClickListener(this);
        Preference coachPreference = findPreference("COACH");
        coachPreference.setSummary(myPreference.getVoiceModelName(myPreference.getLanguage()));
        preferenceBackUp = findPreference("BACK_UP");
        Preference reminder = findPreference("ST_REMINDER");
        reminder.setSummary(FitnessApplication.getApplication().mDatabase.getReminderSet());
        Preference version = findPreference("VERSION_APP");
        ListPreference languagePreference = findPreference("ST_LANGUAGE");
        String[] languageEntryValues = SUPPORT_LANGUAGES.split("@");
        String[] languageEntries = new String[languageEntryValues.length];
        for (int i = 0; i < languageEntryValues.length; i++) {
            languageEntries[i] = getLanguageName(languageEntryValues[i]);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Preference fitSync = findPreference("SYNC_FIT");
            fitSync.setVisible(false);
        }
        languagePreference.setEntryValues(languageEntryValues);
        languagePreference.setEntries(languageEntries);
        version.setTitle("Version: " + BuildConfig.VERSION_NAME);
        boolean hasGMS = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(getContext()) == ConnectionResult.SUCCESS;
        if (hasGMS &&FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_SYNC)){
            preferenceBackUp.setVisible(true);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                preferenceBackUp.setTitle(user.getDisplayName());
                preferenceBackUp.setUrl(user.getPhotoUrl(),true);
                if (myPreference.getBackUpTime() > 0) {
                    preferenceBackUp.setSummary(String.format(getString(R.string.last_sync), mFormatDateTime.format(myPreference.getBackUpTime())));
                }
            }
            preferenceBackUp.setClickListener(() -> {
                if (user != null) {
                    showDialogLogOut();
                }
            });
        }else {
            preferenceBackUp.setVisible(false);
        }

    }
    void showDialogLogOut(){
        if (dialogUserLogOut == null){
            dialogUserLogOut = new DialogUserLogOut();
        }
        if (!dialogUserLogOut.isAdded()){
            dialogUserLogOut.show(getParentFragmentManager(), "logout");
        }
        updateAccount(FirebaseAuth.getInstance().getCurrentUser());

    }
    void showDialogLogIn(){
        if (dialogUserLogIn == null){
            dialogUserLogIn = new DialogUserLogIn();
        }
        if (!dialogUserLogIn.isAdded()){
            dialogUserLogIn.show(getParentFragmentManager(), "log_in");
        }
    }

    String getLanguageName(String lng) {
        String[] loc = lng.split("[_-]");
        Locale locale;
        if (loc.length > 1) {
            locale = new Locale(loc[0], loc[1]);
        } else {
            locale = new Locale(loc[0]);
        }
        return locale.getDisplayLanguage(locale);
    }

    public void initValue(boolean value) {
        SwitchPreference swGoogleFit = (SwitchPreference) findPreference("SYNC_FIT");
        swGoogleFit.setChecked(value);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "GO_PREMIUM":
                startActivity(new Intent(requireContext(),
                        mint.woman.female.workout.activity.ui.premium.PremiumActivity.class));
                break;
            case "ACHIEVEMENTS":
                startActivity(BadgeActivity.newIntent(requireContext()));
                break;
            case "SYNC_FIT":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                    SwitchPreference swGoogleFit = findPreference("SYNC_FIT");
                    HealthDataDialog healthDataDialog = new HealthDataDialog();
                    healthDataDialog.show(getActivity().getSupportFragmentManager(), "health_connect");
                    if (swGoogleFit.isChecked()){
                        swGoogleFit.setChecked(false);
//                    if (mListener != null) {
//                        mListener.onGetGoogleFit();
//                    }
                }else {
                    swGoogleFit.setChecked(true);
//                    if (mListener != null) {
//                        mListener.disableHealthConnect();
//                    }
                        // swGoogleFit.setTitle(getString(R.string.txt_sync_to_google_fit));
                    }
                } else {

                }
                break;
            case "ST_RATE":
                if (mListener != null) {
                    mListener.onRateShow();
                }
                break;
            case "RECOMMEND_APP":
                AppOpenManager.getInstance().disableAdResumeByClickAction();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=Min+Fitness")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Min+Fitness")));
                }
                break;
            case "ST_FEEDBACK":
                AppOpenManager.getInstance().disableAdResumeByClickAction();
                sendEmail();
                break;
            case "ST_SHARE":
                shareApp();
                break;
            case "COACH":
                startActivity(new Intent(getActivity(), CoachOptionActivity.class));
                break;
            case "TTS_INSTALL_VOICE":
                AppOpenManager.getInstance().disableAdResumeByClickAction();
                installVoiceData();
                break;
            case "TTS_ENGINE":
                AppOpenManager.getInstance().disableAdResumeByClickAction();
                openTTSSettings();
                break;
            case "PRIVACY_POLICY":
                AppOpenManager.getInstance().disableAdResumeByClickAction();
                startActivity(new Intent(getActivity(), PolicyActivity.class));
                break;
            case "ST_REMINDER":
                Navigation.findNavController(getView()).navigate(R.id.action_nav_setting_to_nav_reminder);
                break;
            case "BACK_GROUND_MUSIC":
                startActivity(new Intent(getActivity(), SoundsActivity.class));
                break;
            case "BACK_UP":
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    showDialogLogIn();
                } else {
                    if (!isSync) {
                        isSync = true;
                        myPreference.setBackUpTime(Calendar.getInstance().getTimeInMillis());
                        preferenceBackUp.setSummary(String.format(getString(R.string.last_sync), mFormatDateTime.format(myPreference.getBackUpTime())));
                        preferenceBackUp.setUrl(user.getPhotoUrl(), true);
                        if (mListener != null)
                            mListener.syncData();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.data_sync_done), Toast.LENGTH_SHORT).show();
                    }

                }
                break;

        }
        return false;
    }

    public void updateAccount(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            preferenceBackUp.setTitle(firebaseUser.getDisplayName());
            myPreference.setBackUpTime(Calendar.getInstance().getTimeInMillis());
            preferenceBackUp.setSummary(String.format(getString(R.string.last_sync), mFormatDateTime.format(myPreference.getBackUpTime())));
            preferenceBackUp.setUrl(firebaseUser.getPhotoUrl(),true);
        } else {
            preferenceBackUp.setTitle(getString(R.string.set_backup));
            preferenceBackUp.setSummary(getString(R.string.sign_in_tips));
            preferenceBackUp.setUrl(null,false);
        }
        preferenceBackUp.setClickListener(() -> {
            if (firebaseUser != null) {
                showDialogLogOut();
            }
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {

            case "SYNC_GOOGLE_FIT":
                if (mListener != null) {
                    mListener.onGetGoogleFit();
                }
                break;
            case "ST_LANGUAGE":
                JsonHelper.newInstance(getContext()).resetData();
                if (mListener != null) {
                    mListener.onLanguageChange();
                }

                break;
            case "THEME":
                ListPreference theme = findPreference("THEME");
                // if(mListener!= null) mListener.onThemeChange();
                switch (theme.getValue()) {
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    default:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
                break;

        }
    }

    private void installVoiceData() {
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.tts"/*replace with the package name of the target TTS engine*/);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e("HAHA", "Failed to install TTS data, no acitivty found for " + intent + ")");
        }
    }

    private void openTTSSettings() {
        try {
            //Open Android Text-To-Speech Settings
            if (Build.VERSION.SDK_INT >= 15) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shareApp() {
        AppOpenManager.getInstance().disableAdResumeByClickAction();
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName();
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: minfitfeedback@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Feedback] Female Workout");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        refreshAchievementSummary();
        updatePremiumCard();
    }

    /** Thẻ Go Premium chỉ hiện khi người dùng chưa mua Premium. */
    private void updatePremiumCard() {
        Preference premium = findPreference("GO_PREMIUM");
        if (premium != null) {
            premium.setVisible(!myPreference.isPremiumMember());
        }
    }

    /** Hien so huy hieu da dat ngay tren dong Achievements, vi du "7 of 29 earned". */
    private void refreshAchievementSummary() {
        Preference achievements = findPreference("ACHIEVEMENTS");
        if (achievements == null) return;

        BadgeRepository.load(requireContext(), board -> {
            if (!isAdded() || board.getTotalCount() == 0) return;
            achievements.setSummary(getString(
                    R.string.badge_summary, board.getUnlockedCount(), board.getTotalCount()));
        });
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsListener) {
            mListener = (OnSettingsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSettingsListener {
        void onRateShow();

        void onTestVoice(String locale);

        void onLanguageChange();

        void onGetGoogleFit();
        void disableHealthConnect();

        void syncData();
    }
}
