package mint.woman.female.workout.activity.ui.settings;


import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import mint.woman.female.workout.BuildConfig;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.PolicyActivity;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.utils.MyPreference;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private OnSettingsListener mListener;

    GoogleApiClient mGoogleSignInClient;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);
        findPreference("ST_RATE").setOnPreferenceClickListener(this);
        findPreference("ST_FEEDBACK").setOnPreferenceClickListener(this);
        findPreference("ST_SHARE").setOnPreferenceClickListener(this);
        findPreference("TTS_ENGINE").setOnPreferenceClickListener(this);
        findPreference("PRIVACY_POLICY").setOnPreferenceClickListener(this);
        findPreference("TTS_INSTALL_VOICE").setOnPreferenceClickListener(this);
        //findPreference("SYNC_GOOGLE_FIT").setOnPreferenceClickListener(this);
        findPreference("ST_REMINDER").setOnPreferenceClickListener(this);
        findPreference("ST_YOUTUBE").setOnPreferenceClickListener(this);
        //  findPreference("BACK_UP").setOnPreferenceClickListener(this);
        Preference reminder = findPreference("ST_REMINDER");
        reminder.setSummary(FitnessApplication.getApplication().mDatabase.getReminderSet());
        Preference version = findPreference("VERSION_APP");
        version.setTitle("Version App: " + BuildConfig.VERSION_NAME);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        updateAccount(account);
    }


    public void initValue(boolean value) {
     //   SwitchPreference swGoogleFit = (SwitchPreference) findPreference("SYNC_GOOGLE_FIT");
        //swGoogleFit.setChecked(value);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "ST_RATE":
                if (mListener != null) {
                    mListener.onRateShow();
                }
                break;
            case "ST_FEEDBACK":
                sendEmail();
                break;
            case "ST_SHARE":
                shareApp();
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
            case "ST_YOUTUBE":
                openYoutubeChannel();
                break;
//            case "BACK_UP":
//                if (mListener != null) {
//                    mListener.singIn();
//                }
//
//                break;

        }
        return false;
    }
    public void openYoutubeChannel() {
        AppOpenManager.getInstance().disableAdResumeByClickAction();

        String youtubeChannelId = "UCIUIxNiBFEqjHmnoPgpeKUw";

        // Try to open the YouTube app directly
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("vnd.youtube://channel/" + youtubeChannelId));

        // If YouTube app is not installed, open in browser
        if (intent.resolveActivity(getContext().getPackageManager()) == null) {
            intent.setData(Uri.parse("https://www.youtube.com/channel/" + youtubeChannelId));
        }

        try {
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void updateAccount(GoogleSignInAccount account) {
//        Preference preference = findPreference("BACK_UP");
//        if (account!= null){
//            preference.setTitle(account.getDisplayName());
//        }else {
//            preference.setTitle("Back up!");
//        }

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
    }
}
