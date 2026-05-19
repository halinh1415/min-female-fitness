package mint.woman.female.workout.activity.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.LanguageHelper;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FilterProgramActivity extends AppCompatActivity {

    public static final String EXTRA_FILTER_TYPE = "filter_type";
    public static final String EXTRA_TAG = "tag";
    public static final String EXTRA_TITLE = "title";

    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private LinearLayout layoutContainer;

    private  List<ProgramObject> filter = new ArrayList<>();

    private int option;
    private String filterName;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_program);

        bindViews();
        readIntent();
        setupToolbar();
        applyFilter();
        renderPrograms();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        layoutContainer = findViewById(R.id.layoutContainer);
    }

    private void readIntent() {
        Intent intent = getIntent();

        option = intent.getIntExtra(EXTRA_FILTER_TYPE, 2);
        filterName = intent.getStringExtra(EXTRA_TAG);
        title = intent.getStringExtra(EXTRA_TITLE);

        if (filterName == null) filterName = "";
        if (title == null) title = "";
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        tvToolbarTitle.setText(title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void applyFilter() {
        filter.clear();


        Map<Integer, ProgramObject> mapProgramObject = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();

        if (option == 1) {
            filterByName(mapProgramObject);
        } else if (option == 3) {
            filterByDuration(mapProgramObject);
        } else {
            filterByLevelOrSearchableText(mapProgramObject);
        }

        Collections.sort(filter, Comparator.comparingInt(this::programDuration));
    }

    private void filterByName(Map<Integer, ProgramObject> mapProgramObject) {
        String key = filterName.toLowerCase(Locale.ROOT);

        for (ProgramObject program : mapProgramObject.values()) {
            if (safe(program.getName()).toLowerCase(Locale.ROOT).contains(key)) {
                filter.add(program);
            }
        }
    }

    private void filterByDuration(Map<Integer, ProgramObject> mapProgramObject) {
        for (ProgramObject program : mapProgramObject.values()) {
            if (matchesDuration(program, filterName)) {
                filter.add(program);
            }
        }
    }

    private void filterByLevelOrSearchableText(Map<Integer, ProgramObject> mapProgramObject) {
        String key = filterName.toLowerCase(Locale.ROOT);
        Integer detectedLevel = detectLevel(key);

        if (detectedLevel != null && detectedLevel > 0) {
            for (ProgramObject program : mapProgramObject.values()) {
                if (program.getLevel() == detectedLevel) {
                    filter.add(program);
                }
            }
            return;
        }

        for (ProgramObject program : mapProgramObject.values()) {
            String searchable = safe(program.getName()) + " "
                    + safe(program.getTags()) + " "
                    + safe(program.getFocus()) + " "
                    + safe(program.getBody()) + " "
                    + safe(program.getDescription());

            searchable = searchable.toLowerCase(Locale.ROOT);

            if (searchable.contains(key)) {
                filter.add(program);
            }
        }
    }

    private Integer detectLevel(String key) {
        if (key.matches("^\\d+$")) {
            try {
                return Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        String beginner = getString(R.string.beginner).toLowerCase(Locale.ROOT);
        String intermediate = getString(R.string.intermediate).toLowerCase(Locale.ROOT);
        String advanced = getString(R.string.advanced).toLowerCase(Locale.ROOT);

        if (key.contains(beginner)) return 1;
        if (key.contains(intermediate)) return 2;
        if (key.contains(advanced)) return 3;

        java.util.regex.Pattern levelPattern =
                java.util.regex.Pattern.compile("level\\D*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);

        java.util.regex.Pattern lvPattern =
                java.util.regex.Pattern.compile("lv\\D*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);

        java.util.regex.Matcher levelMatcher = levelPattern.matcher(key);
        if (levelMatcher.find()) {
            return parseIntOrNull(levelMatcher.group(1));
        }

        java.util.regex.Matcher lvMatcher = lvPattern.matcher(key);
        if (lvMatcher.find()) {
            return parseIntOrNull(lvMatcher.group(1));
        }

        return null;
    }

    private Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean matchesDuration(ProgramObject program, String durationKey) {
        int minutes = programDuration(program);

        switch (durationKey) {
            case "fast_workout":
                return minutes < 10;

            case "duration_15_20":
                return minutes >= 15 && minutes <= 20;

            case "duration_25_35":
                return minutes >= 25 && minutes <= 35;

            case "duration_45_plus":
                return minutes >= 45;

            default:
                return false;
        }
    }

    private int programDuration(ProgramObject program) {
        if (program.getDuration() > 0) {
            return program.getDuration();
        }
        return program.getTotal();
    }

    private void renderPrograms() {
        layoutContainer.removeAllViews();

        if (filter.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(getString(R.string.search_no_matching_programs));
            emptyView.setTextColor(0xFF888888);
            emptyView.setTextSize(14f);
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, dp(40), 0, dp(40));

            layoutContainer.addView(
                    emptyView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (ProgramObject program : filter) {
            View card = inflater.inflate(R.layout.item_child_discover_7_rect, layoutContainer, false);
            bindProgramCard(card, program);
            layoutContainer.addView(card);
        }
    }

    private void bindProgramCard(View cardView, ProgramObject program) {
        MaterialCardView cardProgram = cardView.findViewById(R.id.card_view);
        TextView tvProgramName = cardView.findViewById(R.id.tv_title);
     //   TextView tvLevel = cardView.findViewById(R.id.tvLevel);
        TextView tvDuration = cardView.findViewById(R.id.tv_sub_title);
        ImageView ivWorkout = cardView.findViewById(R.id.iv_workout);
        ImageView ivBackground = cardView.findViewById(R.id.iv_workout_bg);
      //  TextView tvDescription = cardView.findViewById(R.id.tvDescription);
        String baseUrl = "file:///android_asset/images/";
        if (program.getDiscover() >0){
            baseUrl = ApiUtils.BASE_URL_IMAGES;
        }
        Glide.with(FilterProgramActivity.this)
                .load(Uri.parse(baseUrl+program.getBackground()))
                .error(Uri.parse(baseUrl+"bg_1.png"))
                .centerCrop()
                .into(ivBackground);
        Glide.with(FilterProgramActivity.this).load(Uri.parse(baseUrl+program.getImage()))
                .into(ivWorkout);
        tvProgramName.setText(program.getName());
       // tvLevel.setText(getLevelLabel(program.getLevel()));
        tvDuration.setText(getString(R.string.x_minutes,programDuration(program) ));
   //     tvDescription.setText(getProgramDescription(program));

        cardProgram.setOnClickListener(v -> openProgram(program));
    }

    private String getProgramDescription(ProgramObject program) {
        if (!safe(program.getDescription()).trim().isEmpty()) {
            return program.getDescription();
        }



        return safe(program.getFocus());
    }

    private String getLevelLabel(int level) {
        switch (level) {
            case 1:
                return getString(R.string.beginner);

            case 2:
                return getString(R.string.intermediate);

            case 3:
                return getString(R.string.advanced);

            default:
                return "Lv " + level;
        }
    }

    private void openProgram(ProgramObject programObject) {
        if (programObject.getType() == 1) {
            Intent intent = new Intent(this, ChallengeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", programObject.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", programObject.getId());
            bundle.putInt("DAY", 0);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() && lang.length() > 1) {
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