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
import mint.woman.female.workout.common.adapter.ProgramClickListener;
import mint.woman.female.workout.common.adapter.SectionAdapter;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Section;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint({"NonConstantResourceId", "DefaultLocale"})
public class HomeFragment extends Fragment implements ProgramClickListener {

    @BindView(R.id.rc_my_workout)
    RecyclerView mPlanRc;
//    @BindView(R.id.txt_add_program)
//    TextView mAddProgram;

    private SectionAdapter mSectionAdapter;
    private OnHomeFragmentInteractionListener mListener;
    private MyPreference myPreference;
    private HashMap<Integer,ProgramObject> hashMap;
    int[] mListChallenge = new int[]{1,20,21,22,23};
    int[] mListAbs = new int[]{20,2, 3, 4};
    int[] mListArm = new int[]{22,5};
    int[] mListButt = new int[]{21,6,7,8};

    int[] mListThigh = new int[]{23,13,14,15};
    int[] mListSplit = new int[]{9,10,11, 12, 16,17,18,19};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, root);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myPreference = MyPreference.newInstance(getContext());

        // mPlanCalendarView.setSelectedDate(CalendarDay.today());
        mPlanRc.setNestedScrollingEnabled(false);
        mPlanRc.setLayoutManager(new LinearLayoutManager(getContext()));
        WeekGoalFragment goalFragment = (WeekGoalFragment) getFragmentManager().findFragmentById(R.id.a1);
        if (goalFragment != null){
            goalFragment.setIsWeek(true);
        }
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
        sections.add(new Section(getString(R.string.challenge), getProgram(mListChallenge),R.layout.item_child_recent_challenge));
        sections.add(new Section(getString(R.string.txt_abs), getProgram(mListAbs),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_arm), getProgram(mListArm),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_butt), getProgram(mListButt),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.txt_thigh), getProgram(mListThigh),R.layout.item_child_recent_rect));
        sections.add(new Section(getString(R.string.tab_discover), getProgram(mListSplit),R.layout.item_child_recent_rect));
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
        mPlanRc.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        // mExploreRc.setLayoutManager(new LinearLayoutManager(getContext()));
        mPlanRc.setAdapter(sectionAdapter);
        // rcSection.setAdapter(mSectionAdapter);
    }



    private List<ProgramObject> getMainProgram() {
        List<Integer> list = FitnessApplication.getApplication().mDatabase.getMainPlan();
        HashMap<Integer, ProgramObject> hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        List<ProgramObject> programs = new ArrayList<>();
        for (int id : list
        ) {
            ProgramObject programObject = hashMap.get(id);
            if (programObject != null) programs.add(programObject);
        }
        return programs;
    }


    @OnClick({R.id.ln_my_training})
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