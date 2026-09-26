package mint.woman.female.workout.activity.ui.preview;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.minfitness.decode.Decoder;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.adapter.ExerciseClickListener;
import mint.woman.female.workout.common.adapter.ReplaceAdapter;
import mint.woman.female.workout.custom.DialogEditExercise;
import mint.woman.female.workout.custom.EditExerciseListener;
import mint.woman.female.workout.databinding.ActivityReplaceBinding;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.Replace;
import mint.woman.female.workout.utils.FitnessConstUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplaceActivity extends AppCompatActivity implements ExerciseClickListener, EditExerciseListener {
    ActivityReplaceBinding binding;
//    @BindView(R.id.img_exercise)
//    ImageView imExercise;
//    @BindView(R.id.tv_exercise_name)
//    TextView mExerciseName;
//    @BindView(R.id.txt_exercise_time)
//    TextView mReps;
//
//    @BindView(R.id.rc_replace)
//    RecyclerView rcReplace;
//    @BindView(R.id.tv_replace_in)
//            TextView tvReplaceIn;
    ReplaceAdapter replaceAdapter;
    List<Replace> replaceList;

    Bundle mBundle;
    int actionId;
    int time;
    int classId =0;
    int version = 1003;
    ExerciseObject mCurrentExerciseObject;
    HashMap<Integer,ExerciseObject> hashMap;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        mBundle = getIntent().getExtras();
        if (mBundle!= null){
            actionId = mBundle.getInt("actionId");
            time = mBundle.getInt("time");
            classId = mBundle.getInt("group");
            version = mBundle.getInt("version",1003);

            hashMap = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(0,version);
            mCurrentExerciseObject = hashMap.get(actionId);
            binding.tvExerciseName.setText(mCurrentExerciseObject.getName());
            if (mCurrentExerciseObject.getUnit().contains("s")){
                binding.txtExerciseTime.setText(FitnessConstUtils.toTime(time));
            }else{
                binding.txtExerciseTime.setText("x"+time);
            }
           // byte[] encrypt = VideoEncryptor.decryptGif(getAssets(), "i/g/video_" +actionId,"ASDFGHJKLASDFGHJ");
            byte[] encrypt = Decoder.decryptFromAssets(getAssets(), "i/g/video_" +actionId);
            Glide.with(this).asGif().load(encrypt).into(binding.imgExercise);
            replaceAdapter = new ReplaceAdapter( this,hashMap);

            replaceList = getList();
            replaceAdapter.setReplaceList(replaceList);
            binding.rcReplace.setLayoutManager(new GridLayoutManager(this, 1));
            binding.rcReplace.setAdapter(replaceAdapter);
        }

        binding.btnReplace.setOnClickListener(this::onReplace);


    }
    List<Replace> getList(){
        List<Replace> list = new ArrayList<>();
        for(ExerciseObject exerciseObject: FitnessApplication.getApplication().mJsonHelper.getAllExercise(0,version)){
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



    void onReplace(View view){
        int position = replaceAdapter.getSelectPosition();
        if (position!=-1){
            Intent intent = getIntent();
            Bundle b = new Bundle();
            int id = replaceList.get(position).getId();
            int time = replaceList.get(position).getTime();
            String unit = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(0,version).get(id).getUnit();
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