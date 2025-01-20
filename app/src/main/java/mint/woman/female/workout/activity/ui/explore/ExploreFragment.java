package mint.woman.female.workout.activity.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.minfitness.decode.Decoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ExploreDetailActivity;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.CategoryDetailActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.common.adapter.SectionAdapter;
import mint.woman.female.workout.databinding.FragmentExploreBinding;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Section;

public class ExploreFragment extends Fragment implements ProgramClickListener {

    FragmentExploreBinding binding;
    SectionAdapter mAdapter;
    HashMap<Integer, ProgramObject> hashMap;
    int[] mListStretch = new int[]{18, 19,971, 16, 17, 959};
    int[] mList7Minute = new int[]{24, 41, 37, 32, 45,969};
    int[] mListSummer = new int[]{1110,1140,47,39,46,30,34,38};
    int[] mListFaceYoga = new int[]{10, 11};
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
        binding = FragmentExploreBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private List<Object> getProgram(int[] list) {
        List<Object> programs = new ArrayList<>();
        for (int id : list
        ) {
            ProgramObject programObject = hashMap.get(id);
            if (programObject != null) programs.add(programObject);
        }
        return programs;
    }
    private List<ProgramObject> getProgramObject(int[] list) {
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
        List<Object> fatHiitWorkouts = new ArrayList<>();
        if (Decoder.isSuccess) {
            fatHiitWorkouts.add(new CategoryObject(getProgramObject(new int[]{345, 346, 347})));
            sections.add(new Section(getString(R.string.explore_tag_hot), fatHiitWorkouts, 2));
            List<Object> randomWorkouts = new ArrayList<>();
            randomWorkouts.add(new CategoryObject(getProgramObject(new int[]{351, 352, 353})));
            randomWorkouts.add(new CategoryObject(getProgramObject(new int[]{348, 349, 350})));
            sections.add(new Section(getString(R.string.random_workout), randomWorkouts, 4));
            List<Object> trainingGoals = new ArrayList<>();
            trainingGoals.add(new CategoryObject(getProgramObject(new int[]{354, 355, 356})));
            trainingGoals.add(new CategoryObject(getProgramObject(new int[]{357, 358, 359})));
            trainingGoals.add(new CategoryObject(getProgramObject(new int[]{360, 361, 362})));
            trainingGoals.add(new CategoryObject(getProgramObject(new int[]{363, 365, 366})));
            sections.add(new Section(getString(R.string.training_goal), trainingGoals, 4));

            sections.add(new Section(getString(R.string.reach_goal_fast), getString(R.string.seven_min_get_fit_des), getProgram(mList7Minute), 8));
            sections.add(new Section(getString(R.string.at_home), getString(R.string.upper_body_stretching_short_des), getProgram(mListStretch), 6));
            sections.add(new Section(getString(R.string.for_summer), getProgram(mListSummer), 8));
            sections.add(new Section(getString(R.string.tip_pro_instruction_3), getProgram(mListFaceYoga), 7));
            sections.add(new Section(getString(R.string.pain_relief), getProgram(new int[]{883,200,201,1151,1150}), 7));
        }
        SectionAdapter sectionAdapter = new SectionAdapter(sections, this);
        binding.rcExplore.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        // mExploreRc.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcExplore.setAdapter(sectionAdapter);
        // rcSection.setAdapter(mSectionAdapter);
    }

    @Override
    public void onItemRootViewClicked(@NonNull ProgramObject mCurrentCategory) {
        if (mCurrentCategory.getType() == 1) {
            Intent intent = new Intent(getActivity(), ChallengeActivity.class);
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
    public void onGroupClickListener(CategoryObject categoryObject) {
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        FitnessApplication.getApplication().getStorageCommon().category.postValue(categoryObject);
        getActivity().startActivity(intent);
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