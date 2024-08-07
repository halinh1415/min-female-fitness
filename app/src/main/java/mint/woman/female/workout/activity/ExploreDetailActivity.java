package mint.woman.female.workout.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.ProgramObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import mint.woman.female.workout.utils.LanguageHelper;

public class ExploreDetailActivity extends AppCompatActivity implements ProgramClickListener {

    @BindView(R.id.rc_detail_explore)
    RecyclerView mDetailExploreRc;
    ArrayList<ProgramObject> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_detail);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // list = bundle.getParcelableArrayList("PLAN_MORE");
            list = new ArrayList<>();
            setTitle(bundle.getString("PLAN_LIST_NAME"));
            mDetailExploreRc.setLayoutManager(new LinearLayoutManager(this));
        }

    }

    @Override
    public void onItemRootViewClicked(@NonNull ProgramObject mCurrentCategory) {

        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(ExploreDetailActivity.this, ProgramActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (mCurrentCategory.getType() == 2) {
            Intent intent2 = new Intent(ExploreDetailActivity.this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            bundle.putInt("DAY", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }

    @Override
    public void onGroupClickListener(CategoryObject categoryObject) {

    }

    @Override
    public void onFooterRootViewClicked(String title, List<ProgramObject> programObjects) {

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
