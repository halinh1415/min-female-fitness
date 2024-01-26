package mint.woman.female.workout.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.model.ProgramObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
            list = bundle.getParcelableArrayList("PLAN_MORE");
            setTitle(bundle.getString("PLAN_LIST_NAME"));
            mDetailExploreRc.setLayoutManager(new LinearLayoutManager(this));
        }

    }

    @Override
    public void onItemRootViewClicked(@NonNull ProgramObject mCurrentCategory) {

        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(ExploreDetailActivity.this, ProgramActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("PLAN", mCurrentCategory);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (mCurrentCategory.getType() == 2) {
            Intent intent2 = new Intent(ExploreDetailActivity.this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("PLAN", mCurrentCategory);
            bundle.putInt("DAY", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }

    @Override
    public void onFooterRootViewClicked(String title, List<ProgramObject> programObjects) {

    }


}
