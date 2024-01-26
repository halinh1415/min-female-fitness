package mint.woman.female.workout.activity.ui.firstsetup.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import mint.woman.female.workout.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FocusAreaFragment extends Fragment {

    @BindView(R.id.btn_choose_arm)
    TextView mChooseArm;
    @BindView(R.id.btn_choose_abs)
    TextView mChooseAbs;
    @BindView(R.id.btn_choose_butt)
    TextView mChooseButt;
    @BindView(R.id.btn_choose_thigh)
    TextView mChooseThigh;
    @BindView(R.id.btn_unchoose_arm)
    TextView mUnChooseArm;
    @BindView(R.id.btn_unchoose_abs)
    TextView mUnChooseAbs;
    @BindView(R.id.btn_unchoose_butt)
    TextView mUnChooseButt;
    @BindView(R.id.btn_unchoose_thigh)
    TextView mUnChooseThigh;
    @BindView(R.id.btn_choose_full_body)
    TextView mChooseBody;
    @BindView(R.id.btn_unchoose_full_body)
    TextView mUnChooseBody;
    boolean isChooseAbs = false,isChooseArm = false, isChooseButt = false,isChooseThigh = false;
    boolean isFullBody = true;

    public FocusAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_area, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.btn_choose_abs, R.id.btn_unchoose_abs, R.id.btn_choose_arm, R.id.btn_unchoose_arm, R.id.btn_unchoose_butt, R.id.btn_choose_butt, R.id.btn_choose_thigh, R.id.btn_unchoose_thigh,R.id.btn_choose_full_body,R.id.btn_unchoose_full_body})
    void choose(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_abs:
                mChooseBody.setVisibility(View.GONE);
                mUnChooseBody.setVisibility(View.VISIBLE);
                mUnChooseAbs.setVisibility(View.VISIBLE);
                mChooseAbs.setVisibility(View.GONE);
                break;
            case R.id.btn_unchoose_abs:
                mUnChooseAbs.setVisibility(View.GONE);
                mChooseAbs.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_choose_arm:
                mChooseBody.setVisibility(View.GONE);
                mUnChooseBody.setVisibility(View.VISIBLE);
                mChooseArm.setVisibility(View.GONE);
                mUnChooseArm.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_arm:
                mChooseArm.setVisibility(View.VISIBLE);
                mUnChooseArm.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_butt:
                mChooseBody.setVisibility(View.GONE);
                mUnChooseBody.setVisibility(View.VISIBLE);
                mChooseButt.setVisibility(View.GONE);
                mUnChooseButt.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_butt:
                mChooseButt.setVisibility(View.VISIBLE);
                mUnChooseButt.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_thigh:
                mChooseBody.setVisibility(View.GONE);
                mUnChooseBody.setVisibility(View.VISIBLE);
                mChooseThigh.setVisibility(View.GONE);
                mUnChooseThigh.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_unchoose_thigh:
                mChooseThigh.setVisibility(View.VISIBLE);
                mUnChooseThigh.setVisibility(View.GONE);
                break;
            case R.id.btn_unchoose_full_body:
                mChooseBody.setVisibility(View.VISIBLE);
                mUnChooseBody.setVisibility(View.GONE);
                mChooseAbs.setVisibility(View.VISIBLE);
                mChooseButt.setVisibility(View.VISIBLE);
                mChooseThigh.setVisibility(View.VISIBLE);
                mChooseArm.setVisibility(View.VISIBLE);
                mUnChooseAbs.setVisibility(View.GONE);
                mUnChooseArm.setVisibility(View.GONE);
                mUnChooseThigh.setVisibility(View.GONE);
                mUnChooseButt.setVisibility(View.GONE);
                break;
            case R.id.btn_choose_full_body:
                mChooseBody.setVisibility(View.GONE);
                mUnChooseBody.setVisibility(View.VISIBLE);
                mChooseAbs.setVisibility(View.GONE);
                mChooseButt.setVisibility(View.GONE);
                mChooseThigh.setVisibility(View.GONE);
                mChooseArm.setVisibility(View.GONE);
                mUnChooseAbs.setVisibility(View.VISIBLE);
                mUnChooseArm.setVisibility(View.VISIBLE);
                mUnChooseThigh.setVisibility(View.VISIBLE);
                mUnChooseButt.setVisibility(View.VISIBLE);
                break;
        }
    }
}