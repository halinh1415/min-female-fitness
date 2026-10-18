package mint.woman.female.workout.custom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.sounds.SoundsActivity;
import mint.woman.female.workout.activity.ui.workout.WorkoutViewModel;
import mint.woman.female.workout.databinding.DialogMusicBinding;
import mint.woman.female.workout.utils.MyPreference;

/**
 * Nút nhạc ở màn tập: bật/tắt nhạc nền, đổi bài (mở SoundsActivity), chỉnh âm lượng.
 * Ghi thẳng vào các pref MUSIC_ON / MUSIC_VOLUME như màn Cài đặt; DoWorkoutActivity nghe
 * WorkoutViewModel để phát lại nhạc ngay.
 */
public class DialogMusic extends BottomSheetDialogFragment {

    private DialogMusicBinding binding;
    private MyPreference myPreference;
    private WorkoutViewModel workoutViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MusicSheet);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogMusicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myPreference = new MyPreference(requireContext());
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        binding.swMusic.setChecked(myPreference.isMusicOn());
        binding.swMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            myPreference.setMusic(isChecked);
            workoutViewModel.setMusicOn(isChecked);
            showState();
        });
        binding.layoutSong.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SoundsActivity.class)));
        binding.seekVolume.setProgress(PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getInt("MUSIC_VOLUME", 50));
        binding.seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                        .putInt("MUSIC_VOLUME", seekBar.getProgress()).apply();
                workoutViewModel.setMusicVolume(myPreference.getMusicVolume());
            }
        });
        showState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Quay lại từ màn chọn nhạc: cập nhật tên bài.
        if (binding != null) showState();
    }

    private void showState() {
        boolean on = myPreference.isMusicOn();
        binding.tvMusicState.setText(on ? R.string.txt_on_music : R.string.txt_off_music);
        binding.tvSong.setText(myPreference.getMusicName().trim());
        binding.layoutSong.setAlpha(on ? 1f : 0.5f);
        binding.layoutSong.setEnabled(on);
        binding.seekVolume.setEnabled(on);
        binding.seekVolume.setAlpha(on ? 1f : 0.5f);
        binding.tvVolumeTitle.setAlpha(on ? 1f : 0.5f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
