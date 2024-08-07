package mint.woman.female.workout.activity.ui.workout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;

import java.util.Locale;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.databinding.ActivityCategoryDetailBinding;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.LanguageHelper;

public class CategoryDetailActivity extends AppCompatActivity {
    ActivityCategoryDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();

    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        FitnessApplication.getApplication().getStorageCommon().category.observe(this, categoryObject -> {
            if (categoryObject == null) return;
            String baseUrl = "file:///android_asset/images/";
            if (categoryObject.getFirstProgramObject().getDiscover() > 0) {
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(CategoryDetailActivity.this)
                    .load(Uri.parse(baseUrl + categoryObject.getFirstProgramObject().getBackground()))
                    .error(Uri.parse(baseUrl + "bg_1.png"))
                    .centerCrop()
                    .into(binding.ivWorkoutBg);

            Glide.with(CategoryDetailActivity.this).load(Uri.parse(baseUrl + categoryObject.getFirstProgramObject().getImage()))
                    .into(binding.ivWorkout);
            binding.tvTitle.setText(categoryObject.getName());
            binding.tvWorkoutIntro.setText(categoryObject.getFirstProgramObject().getDescription());
            if (categoryObject.getProgramObjectList() != null && categoryObject.getProgramObjectList().size() == 3) {
                binding.tvDurationLevel1.setText("" + categoryObject.getProgramObjectList().get(0).getTotal() + " " + CategoryDetailActivity.this.getString(R.string.minutes));
                binding.tvDurationLevel2.setText("" + categoryObject.getProgramObjectList().get(1).getTotal() + " " + CategoryDetailActivity.this.getString(R.string.minutes));
                binding.tvDurationLevel3.setText("" + categoryObject.getProgramObjectList().get(2).getTotal() + " " + CategoryDetailActivity.this.getString(R.string.minutes));
                binding.llBeginner.setOnClickListener(view -> {
                            Intent intent2 = new Intent(CategoryDetailActivity.this, PreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("PROGRAM_ID", categoryObject.getProgramObjectList().get(0).getId());
                            bundle.putInt("DAY_NUMBER", 0);
                            intent2.putExtras(bundle);
                            startActivity(intent2);
                        }
                );
                binding.llIntermediate.setOnClickListener(view -> {
                            Intent intent2 = new Intent(CategoryDetailActivity.this, PreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("PROGRAM_ID", categoryObject.getProgramObjectList().get(1).getId());
                            bundle.putInt("DAY_NUMBER", 0);
                            intent2.putExtras(bundle);
                            startActivity(intent2);
                        }
                );
                binding.llAdvanced.setOnClickListener(view -> {
                            Intent intent2 = new Intent(CategoryDetailActivity.this, PreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("PROGRAM_ID", categoryObject.getProgramObjectList().get(2).getId());
                            bundle.putInt("DAY_NUMBER", 0);
                            intent2.putExtras(bundle);
                            startActivity(intent2);
                        }
                );
            }

        });


        binding.imBack.setOnClickListener(view -> {
            CategoryDetailActivity.this.finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
}