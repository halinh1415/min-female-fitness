package mint.woman.female.workout.custom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.PolicyActivity;
import mint.woman.female.workout.activity.ui.settings.CoachOptionActivity;
import mint.woman.female.workout.activity.ui.sounds.SoundsActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.utils.MyPreference;


public class DialogSoundSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,SharedPreferences.OnSharedPreferenceChangeListener {


    WorkoutViewModel workoutViewModel;
    MyPreference myPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound_settings, rootKey);
        myPreference = new MyPreference(getContext());
        Preference music = findPreference("BACK_GROUND_MUSIC");
        music.setSummary(MyPreference.newInstance(getContext()).getMusicName());
        findPreference("BACK_GROUND_MUSIC").setOnPreferenceClickListener(this);
        findPreference("COACH").setOnPreferenceClickListener(this);
        Preference coachPreference = findPreference("COACH");
        coachPreference.setSummary(myPreference.getVoiceModelName(myPreference.getLanguage()));
        workoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "BACK_GROUND_MUSIC_FILE":
//                ListPreference backGroundMusic = (ListPreference) findPreference("BACK_GROUND_MUSIC");
//                workoutViewModel.setMusicSong(backGroundMusic.getValue());
                break;
            case "BACK_GROUND_MUSIC":
                ListPreference backGroundMusic = (ListPreference) findPreference("BACK_GROUND_MUSIC");
                workoutViewModel.setMusicSong(backGroundMusic.getValue());
                break;
            case "MUSIC_ON":
                SwitchPreference musicOn = findPreference("MUSIC_ON");
                workoutViewModel.setMusicOn(musicOn.isChecked());
                break;
            case "MUSIC_VOLUME":
                SeekBarPreference seekBarPreference = (SeekBarPreference) findPreference("MUSIC_VOLUME");
                int soundVolume = seekBarPreference.getValue();
                float volume = (float) (1 - (Math.log(100 - soundVolume) / Math.log(100)));
                workoutViewModel.setMusicVolume(volume);
                break;

            case "ST_TTS_LANGUAGE":
                ListPreference ttsLanguage = (ListPreference) findPreference("ST_TTS_LANGUAGE");
                workoutViewModel.setLanguage(ttsLanguage.getValue());
                break;

        }
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
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "BACK_GROUND_MUSIC":
                startActivity(new Intent(getActivity(), SoundsActivity.class));
                break;
            case "COACH":
                startActivity(new Intent(getActivity(), CoachOptionActivity.class));
                break;

        }
        return false;
    }
}
