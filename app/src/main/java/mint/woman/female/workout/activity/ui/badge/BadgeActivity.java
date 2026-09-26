package mint.woman.female.workout.activity.ui.badge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;

import mint.woman.female.workout.R;
import mint.woman.female.workout.badge.BadgeBoard;
import mint.woman.female.workout.badge.BadgeRepository;
import mint.woman.female.workout.databinding.ActivityBadgeBinding;
import mint.woman.female.workout.utils.LanguageHelper;

import java.util.Locale;

/**
 * Man hinh danh sach huy hieu. Noi dung lay tu assets/badges/badges.json,
 * tien do tinh tu lich su tap.
 */
public class BadgeActivity extends AppCompatActivity {

    private static final int SPAN_COUNT = 3;

    private ActivityBadgeBinding binding;
    private BadgeAdapter adapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, BadgeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBadgeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.badge_screen_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        adapter = new BadgeAdapter(badge -> BadgeDetailDialog.show(this, badge));

        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT);
        layoutManager.setSpanSizeLookup(adapter.spanSizeLookup(SPAN_COUNT));
        binding.rvBadges.setLayoutManager(layoutManager);
        binding.rvBadges.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tinh lai moi lan quay lai man hinh de tien do luon khop voi lich su moi nhat.
        BadgeRepository.load(this, this::render);
    }

    private void render(BadgeBoard board) {
        if (isFinishing() || isDestroyed()) return;

        boolean hasBadges = board.getTotalCount() > 0;
        binding.tvBadgeEmpty.setVisibility(hasBadges ? View.GONE : View.VISIBLE);
        binding.rvBadges.setVisibility(hasBadges ? View.VISIBLE : View.GONE);

        binding.tvBadgeSummary.setText(getString(
                R.string.badge_summary, board.getUnlockedCount(), board.getTotalCount()));
        binding.progressBadgeOverall.setProgress(board.getPercent());

        adapter.submit(board);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
