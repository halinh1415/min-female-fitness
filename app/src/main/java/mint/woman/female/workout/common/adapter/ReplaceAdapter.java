package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.Replace;
import com.minfitness.decode.Decoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReplaceAdapter extends RecyclerView.Adapter<ReplaceAdapter.ReplaceViewHolder> {

    List<Replace> replaceList;
    int selectPosition = -1;
    ExerciseClickListener exerciseClickListener;
    HashMap<Integer,ExerciseObject> hashMap;

    public ReplaceAdapter(ExerciseClickListener exerciseClickListener,HashMap<Integer,ExerciseObject> hashMap) {
        replaceList = new ArrayList<>();
        this.hashMap = hashMap;
        this.exerciseClickListener = exerciseClickListener;
    }


    public void setReplaceList(List<Replace> replaceList) {
        this.replaceList.clear();
        this.replaceList.addAll(replaceList);
        Log.d("HAHA", "size =" + replaceList.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReplaceAdapter.ReplaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.replace_item_layout, null);
        return new ReplaceAdapter.ReplaceViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final ReplaceAdapter.ReplaceViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Replace replace = replaceList.get(position);
        ExerciseObject exerciseObject = hashMap.get(replace.getId());

        holder.mExerciseName.setText(exerciseObject.getName());


        if (selectPosition == position) {
            holder.imgSelect.setImageResource(R.drawable.ic_selected);
        } else {
            holder.imgSelect.setImageResource(R.drawable.ic_unselected);
        }

        if (exerciseObject.getUnit().contains("s")) {
            int minutes = (replace.getMinValue()) / 60;
            int seconds = replace.getMinValue() % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            holder.mReps.setText(timeString);
        } else {
            holder.mReps.setText("x " + replace.getMinValue());
        }
        holder.container.setOnClickListener(view -> {
            selectPosition = position;
            notifyDataSetChanged();
            if (exerciseClickListener != null) {
                exerciseClickListener.onExerciseClick(replace);
            }
        });
        byte[] encrypt = Decoder.decryptFromAssets(holder.itemView.getContext().getAssets(), "i/g/video_" +replace.getId());
        Glide.with(holder.itemView).asGif().load(encrypt).into(holder.mDemoExercise);
       // Glide.with(holder.itemView).load(Uri.parse("file:///android_asset/exercises/" + AppEncrypt.getName("video_" + exerciseObject.getId()) + ".webp")).into(holder.mDemoExercise);

    }

    public int getSelectPosition() {
        return selectPosition;
    }

    @Override
    public int getItemCount() {
        return replaceList.size();
    }


    class ReplaceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_exercise)
        ImageView mDemoExercise;
        @BindView(R.id.tv_exercise_name)
        TextView mExerciseName;
        @BindView(R.id.txt_exercise_time)
        TextView mReps;

        @BindView(R.id.img_select)
        ImageView imgSelect;
        @BindView(R.id.container)
        View container;

        ReplaceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }
}