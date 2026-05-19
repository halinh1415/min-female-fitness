package mint.woman.female.workout.activity.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.LanguageHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private TextView tvCancel;
    private TextView tvNoResult;
    private LinearLayout layoutResults;
    private LinearLayout layoutBrowse;

    private RecyclerView recyclerResults;

    private Chip chipFreeOnly;
    private Chip chipSeatedOnly;

    private ChipGroup chipGroupGoal;
    private ChipGroup chipGroupDuration;
    private ChipGroup chipGroupBody;
    private ChipGroup chipGroupLevel;

    private List<ProgramObject> allPrograms = new ArrayList<>();
    private  List<ProgramObject> searchResults = new ArrayList<>();

    private SearchProgramAdapter adapter;

    private boolean freeOnly = false;
    private boolean seatedOnly = false;

    private final List<FilterItem> goalItems = new ArrayList<>();
    private final List<FilterItem> durationItems = new ArrayList<>();
    private final List<FilterItem> bodyItems = new ArrayList<>();
    private final List<FilterItem> levelItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        bindViews();
        setupData();
        setupRecyclerView();
        setupSearch();
        setupTopFilters();
        setupBrowseChips();

        updateContentVisibility();
    }

    private void bindViews() {
        edtSearch = findViewById(R.id.edtSearch);
        tvCancel = findViewById(R.id.tvCancel);
        tvNoResult = findViewById(R.id.tvNoResult);

        layoutResults = findViewById(R.id.layoutResults);
        layoutBrowse = findViewById(R.id.layoutBrowse);

        recyclerResults = findViewById(R.id.recyclerResults);

        chipFreeOnly = findViewById(R.id.chipFreeOnly);
        chipSeatedOnly = findViewById(R.id.chipSeatedOnly);

        chipGroupGoal = findViewById(R.id.chipGroupGoal);
        chipGroupDuration = findViewById(R.id.chipGroupDuration);
        chipGroupBody = findViewById(R.id.chipGroupBody);
        chipGroupLevel = findViewById(R.id.chipGroupLevel);

        tvCancel.setOnClickListener(v -> finish());
    }

    private void setupData() {
        /*
         * Thay đoạn này bằng data thật của app bạn.
         * Tương đương Flutter:
         * _allPrograms.addAll(TTSHelper.mapProgramObject.values);
         */
        allPrograms = FitnessApplication.getApplication().mJsonHelper.getAllCategory();

        goalItems.add(new FilterItem(getString(R.string.for_summer), "for_summer",0));
        goalItems.add(new FilterItem(getString(R.string.search_7_min), "7 min",0));
        goalItems.add(new FilterItem(getString(R.string.search_routine), "routine", 0));
        goalItems.add(new FilterItem(getString(R.string.search_flexibility), "flexibility", 0));
        goalItems.add(new FilterItem(getString(R.string.search_face_yoga), "power", 0));
        goalItems.add(new FilterItem(getString(R.string.full_body), "body", 0));
        goalItems.add(new FilterItem(getString(R.string.pain_relief), "pain", 0));
        goalItems.add(new FilterItem(getString(R.string.yoga), "yoga", 0));

        durationItems.add(new FilterItem(getString(R.string.fast_workout), "fast_workout", 0));
        durationItems.add(new FilterItem(getString(R.string.less_than_7_minute), "less_than_7_minute", 0));
        durationItems.add(new FilterItem(getString(R.string.longer_than_7_minutes), "longer_than_7_minutes", 0));

        bodyItems.add(new FilterItem(getString(R.string.belly), "abs", 0));
        bodyItems.add(new FilterItem(getString(R.string.butt), "butt", 0));
        bodyItems.add(new FilterItem(getString(R.string.thigh), "thigh", 0));
        bodyItems.add(new FilterItem(getString(R.string.arm), "arm", 0));
        bodyItems.add(new FilterItem(getString(R.string.breast), "breast", 0));

        levelItems.add(new FilterItem(getString(R.string.beginner), "beginner", 0));
        levelItems.add(new FilterItem(getString(R.string.intermediate), "intermediate", 0));
        levelItems.add(new FilterItem(getString(R.string.advanced), "advanced", 0));
    }


    private void setupRecyclerView() {
        adapter = new SearchProgramAdapter(searchResults, this::openProgram);
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }

    private void setupTopFilters() {
        chipFreeOnly.setText("🆓 " + getString(R.string.search_free_only));
        chipSeatedOnly.setText("🪑 " + getString(R.string.search_seated_only));

        chipFreeOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            freeOnly = isChecked;
            applyFilters();
        });

        chipSeatedOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            seatedOnly = isChecked;
            applyFilters();
        });
    }

    private void setupBrowseChips() {
        addFilterChips(chipGroupGoal, goalItems, false);
        addDurationChips();
        addFilterChips(chipGroupBody, bodyItems, false);
        addFilterChips(chipGroupLevel, levelItems, false);
    }

    private void addFilterChips(ChipGroup group, List<FilterItem> items, boolean duration) {
        group.removeAllViews();

        for (FilterItem item : items) {
            Chip chip = createActionChip(item.title,item.iconRes);
            chip.setOnClickListener(v -> {
                if (duration) {
                    openDurationFilter(item.tag, item.title);
                } else {
                    openTagFilter(item.tag, item.title);
                }
            });
            group.addView(chip);
        }
    }

    private void addDurationChips() {
        chipGroupDuration.removeAllViews();

        Chip expressChip = createActionChip("⚡ " + getString(R.string.search_duration_express_10));
        expressChip.setOnClickListener(v ->
                openDurationFilter("duration_10", getString(R.string.search_duration_express_10))
        );
        chipGroupDuration.addView(expressChip);

        for (FilterItem item : durationItems) {
            Chip chip = createActionChip(item.title);
            chip.setOnClickListener(v -> openDurationFilter(item.tag, item.title));
            chipGroupDuration.addView(chip);
        }
    }

    // Convenience overload: create a chip without an icon.
    private Chip createActionChip(String text) {
        return createActionChip(text, 0);
    }

    // Create a chip and optionally set an icon resource (pass 0 for no icon).
    private Chip createActionChip(String text, int iconRes) {
        Chip chip = new Chip(this);
        chip.setText(text);
        // If an icon resource id is provided, set it and make the icon visible.
        if (iconRes != 0) {
            try {
                chip.setChipIconResource(iconRes);
                chip.setChipIconVisible(true);
            } catch (Exception ignored) {
                // In case the resource is invalid for some flavors, silently ignore.
            }
        } else {
            chip.setChipIconVisible(false);
        }
        chip.setClickable(true);
        chip.setCheckable(false);
//        chip.setTextSize(13f);
//        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setChipStrokeWidth(1f);
        chip.setChipStrokeColorResource(android.R.color.darker_gray);
        return chip;
    }

    private void applyFilters() {
        String query = edtSearch.getText() == null
                ? ""
                : edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT);

        searchResults.clear();

        for (ProgramObject program : allPrograms) {
            String searchableText = safe(program.getName()) + " "
                    + safe(program.getTags()) + " "
                    + safe(program.getDescription()) + " "
                    + safe(program.getFocus());

            searchableText = searchableText.toLowerCase(Locale.ROOT);

            boolean matchesQuery = query.isEmpty() || searchableText.contains(query);
            boolean matchesFree = !freeOnly || program.getPremium() == 0;
            boolean matchesSeated = !seatedOnly || isSeatedProgram(program);

            if (matchesQuery && matchesFree && matchesSeated) {
                searchResults.add(program);
            }
        }

        adapter.notifyDataSetChanged();
        updateContentVisibility();
    }

    private boolean isSeatedProgram(ProgramObject program) {
        String text = safe(program.getName()) + " "
                + safe(program.getTags()) + " "
                + safe(program.getDescription()) + " "
                + safe(program.getFocus());

        text = text.toLowerCase(Locale.ROOT);

        return text.contains("seated")
                || text.contains("chair")
                || text.contains("sit");
    }

    private boolean hasActiveFilters() {
        String query = edtSearch.getText() == null
                ? ""
                : edtSearch.getText().toString().trim();

        return !query.isEmpty() || freeOnly || seatedOnly;
    }

    private void updateContentVisibility() {
        if (hasActiveFilters()) {
            layoutResults.setVisibility(View.VISIBLE);
            layoutBrowse.setVisibility(View.GONE);

            if (searchResults.isEmpty()) {
                tvNoResult.setVisibility(View.VISIBLE);
                recyclerResults.setVisibility(View.GONE);
            } else {
                tvNoResult.setVisibility(View.GONE);
                recyclerResults.setVisibility(View.VISIBLE);
            }
        } else {
            layoutResults.setVisibility(View.GONE);
            layoutBrowse.setVisibility(View.VISIBLE);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void openTagFilter(String tag, String title) {
        Intent intent = new Intent(this, FilterProgramActivity.class);
        intent.putExtra("filter_type", 2);
        intent.putExtra("tag", tag);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private void openDurationFilter(String durationKey, String title) {
        Intent intent = new Intent(this, FilterProgramActivity.class);
        intent.putExtra("filter_type", 3);
        intent.putExtra("tag", durationKey);
        intent.putExtra("title", title);
        startActivity(intent);
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

