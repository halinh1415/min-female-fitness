package mint.woman.female.workout.activity.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ExploreDetailActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.common.adapter.SectionAdapter;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ExploreFragment extends Fragment implements ProgramClickListener {
    @BindView(R.id.rc_explore)
    RecyclerView mExploreRc;
    SectionAdapter mAdapter;
    HashMap<Integer, ProgramObject> hashMap;
    int[] mListChallenge = new int[]{1};
    int[] mListAbs = new int[]{4, 5, 6,7};
    int[] mListArm = new int[]{8, 9, 10,11};
    int[] mListButt = new int[]{12, 13, 14,15};

    int[] mListThigh = new int[]{16, 17, 18,19,20,21};
    int[] mListSplit = new int[]{22, 23, 24,25,26,27,28,29};
    boolean flagAds;

    public ExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private List<ProgramObject> getProgram(int[] list) {
        List<ProgramObject> programs = new ArrayList<>();
        for (int id : list
        ) {
            ProgramObject programObject = hashMap.get(id);
            if (programObject != null) programs.add(programObject);
        }
        return programs;
    }

    private void initData() {
        hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        List<Section> sections = new ArrayList<>();
        sections.add(new Section(getString(R.string.txt_challenge), getProgram(mListChallenge),R.layout.item_child_recent_challenge));
        sections.add(new Section(getString(R.string.txt_abs), getProgram(mListAbs),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_arm), getProgram(mListArm),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_butt), getProgram(mListButt),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_thigh), getProgram(mListThigh),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_split), getProgram(mListSplit),R.layout.item_child_recent_rect));
//        final GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
//        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(final int position) {
//                if (mAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER || mAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER || position <4) {
//                    return 2;
//                }
//                return 1;
//            }
//        });
     //   mExploreRc.setLayoutManager(glm);
        SectionAdapter sectionAdapter = new SectionAdapter(sections,this);
        mExploreRc.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
       // mExploreRc.setLayoutManager(new LinearLayoutManager(getContext()));
        mExploreRc.setAdapter(sectionAdapter);
       // rcSection.setAdapter(mSectionAdapter);
    }

    @Override
    public void onItemRootViewClicked(@NonNull ProgramObject mCurrentCategory) {
        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(getActivity(), ProgramActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (mCurrentCategory.getType() == 2) {
            Intent intent2 = new Intent(getActivity(), PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", mCurrentCategory.getId());
            bundle.putInt("DAY", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }

    @Override
    public void onFooterRootViewClicked(@NonNull String title, List<ProgramObject> programObjects) {
        Intent intent = new Intent(getActivity(), ExploreDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("PLAN_LIST_NAME", title);
     //   bundle.putParcelableArrayList("PLAN_MORE", (ArrayList<? extends Parcelable>) programObjects);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}