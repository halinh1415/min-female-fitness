package mint.woman.female.workout.activity.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ExploreDetailActivity;
import mint.woman.female.workout.activity.ui.weekgoal.WeekGoalFragment;
import mint.woman.female.workout.activity.ui.workout.CategoryDetailActivity;
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.common.adapter.SectionAdapter;
import mint.woman.female.workout.databinding.FragmentHomeBinding;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Section;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint({"NonConstantResourceId", "DefaultLocale"})
public class HomeFragment extends Fragment implements ProgramClickListener {
    FragmentHomeBinding binding;
//    @BindView(R.id.txt_add_program)
//    TextView mAddProgram;

    private SectionAdapter mSectionAdapter;
    private OnHomeFragmentInteractionListener mListener;
    private MyPreference myPreference;
    private HashMap<Integer,ProgramObject> hashMap;
    int[] mListChallenge = new int[]{1,879,880,881,882,1178};
    int[] mListAbs = new int[]{879,2, 3, 4};
    int[] mListArm = new int[]{880,5};
    int[] mListButt = new int[]{881,6,7,8};

    int[] mListThigh = new int[]{882,13,14,15};
    int[] mListBreast = new int[]{1178,1179,1180, 1181};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myPreference = MyPreference.newInstance(getContext());

        // mPlanCalendarView.setSelectedDate(CalendarDay.today());
        binding.rcMyWorkout.setNestedScrollingEnabled(false);
        binding.rcMyWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        WeekGoalFragment goalFragment = (WeekGoalFragment) getFragmentManager().findFragmentById(R.id.a1);
        if (goalFragment != null){
            goalFragment.setIsWeek(true);
        }
        binding.lnMyTraining.setOnClickListener(this::goTraining);
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
    private void initData() {
        hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        List<Section> sections = new ArrayList<>();
        sections.add(new Section(getString(R.string.challenge), getProgram(mListChallenge),1));
        sections.add(new Section(getString(R.string.breast_training), getProgram(mListBreast),2));
        sections.add(new Section(getString(R.string.txt_abs), getProgram(mListAbs),2));
        sections.add(new Section(getString(R.string.txt_arm),getProgram(mListArm),2));
        sections.add(new Section(getString(R.string.txt_butt), getProgram(mListButt),2));
        sections.add(new Section(getString(R.string.txt_thigh), getProgram(mListThigh),2));
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
        binding.rcMyWorkout.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        // mExploreRc.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcMyWorkout.setAdapter(sectionAdapter);
        // rcSection.setAdapter(mSectionAdapter);
    }



    private List<Object> getMainProgram() {
        List<Integer> list = FitnessApplication.getApplication().mDatabase.getMainPlan();
        HashMap<Integer, ProgramObject> hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        List<Object> programs = new ArrayList<>();
        for (int id : list
        ) {
            ProgramObject programObject = hashMap.get(id);
            if (programObject != null) programs.add(programObject);
        }
        return programs;
    }



    void goTraining(View view){
        Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_my_workout);
    }


    @Override
    public void onItemRootViewClicked(ProgramObject programObject) {
        if (mListener != null) {
            mListener.onGotoPlan(programObject);
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
       // bundle.putParcelableArrayList("PLAN_MORE", (ArrayList<? extends Parcelable>) programObjects);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHomeFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnHomeFragmentInteractionListener {

        void onGotoPlan(ProgramObject programObject);

    }

}