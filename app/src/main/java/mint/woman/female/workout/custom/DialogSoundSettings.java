package mint.woman.female.workout.custom;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;


public class DialogSoundSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    WorkoutViewModel workoutViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound_settings, rootKey);
        workoutViewModel = new ViewModelProvider(getActivity()).get(WorkoutViewModel.class);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
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

}
