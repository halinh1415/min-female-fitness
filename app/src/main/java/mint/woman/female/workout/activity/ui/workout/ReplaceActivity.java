package mint.woman.female.workout.activity.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.adapter.ExerciseClickListener;
import mint.woman.female.workout.common.adapter.ReplaceAdapter;
import mint.woman.female.workout.custom.DialogEditExercise;
import mint.woman.female.workout.custom.EditExerciseListener;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.Replace;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.VideoEncryptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReplaceActivity extends AppCompatActivity implements ExerciseClickListener, EditExerciseListener {
    @BindView(R.id.img_exercise)
    ImageView imExercise;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.txt_exercise_time)
    TextView mReps;

    @BindView(R.id.rc_replace)
    RecyclerView rcReplace;
    @BindView(R.id.tv_replace_in)
            TextView tvReplaceIn;
    ReplaceAdapter replaceAdapter;
    List<Replace> replaceList;

    Bundle mBundle;
    int actionId;
    int time;
    int classId =0;
    ExerciseObject mCurrentExerciseObject;
    HashMap<Integer,ExerciseObject> hashMap;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mBundle = getIntent().getExtras();
        if (mBundle!= null){
            actionId = mBundle.getInt("actionId");
            time = mBundle.getInt("time");
            classId = mBundle.getInt("group");
            hashMap = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(0);
            mCurrentExerciseObject = hashMap.get(actionId);
            mExerciseName.setText(mCurrentExerciseObject.getName());
            if (mCurrentExerciseObject.getUnit().contains("s")){
                mReps.setText(FitnessConstUtils.toTime(time));
            }else{
                mReps.setText("x"+time);
            }
            byte[] encrypt = VideoEncryptor.decryptGif(getAssets(), "i/g/video_" +actionId,"ASDFGHJKLASDFGHJ");
            Glide.with(this).asGif().load(encrypt).into(imExercise);
            replaceAdapter = new ReplaceAdapter( this,hashMap);

            replaceList = getList();
            replaceAdapter.setReplaceList(replaceList);
            rcReplace.setLayoutManager(new GridLayoutManager(this, 1));
            rcReplace.setAdapter(replaceAdapter);
        }



    }
    List<Replace> getList(){
        List<Replace> list = new ArrayList<>();
        for(ExerciseObject exerciseObject: FitnessApplication.getApplication().mJsonHelper.getAllExercise(0)){
            if (exerciseObject.getClassid() == classId){
                if (exerciseObject.getUnit().isEmpty()){
                    list.add(new Replace(exerciseObject.getId(),40,12));
                }else {
                    list.add(new Replace(exerciseObject.getId(),120,30));
                }

            }
        }
        return list;
    }

    @Override
    public void onExerciseClick(Replace replace) {
        DialogEditExercise dialogEditWorkout = DialogEditExercise.newInstance(replace.getId(), replace.getTime(),1,200);
        dialogEditWorkout.setEditExerciseListener(this);
        dialogEditWorkout.show(getSupportFragmentManager(), "Edit");
    }


    @OnClick(R.id.btn_replace)
    void onReplace(){
        int position = replaceAdapter.getSelectPosition();
        if (position!=-1){
            Intent intent = getIntent();
            Bundle b = new Bundle();
            int id = replaceList.get(position).getId();
            int time = replaceList.get(position).getTime();
            String unit = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(0).get(id).getUnit();
            b.putInt("actionId",id);
            b.putInt("time",time);
            b.putString("unit",unit);
            intent.putExtras(b);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChange(int action, int time, String unit) {
        Intent intent = getIntent();
        Bundle b = new Bundle();
        b.putInt("actionId",action);
        b.putInt("time",time);
        b.putString("unit",unit);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }
}