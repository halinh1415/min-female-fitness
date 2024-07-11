package mint.woman.female.workout.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;

public class ExercisePreAdapter extends RecyclerView.Adapter<ExerciseViewHolder> {

    Context mContext;
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item_layout, null);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {

        holder.mExerciseName.setText(mContext.getString(R.string.txt_exercise)+" "+(position+1));

    }

    @Override
    public int getItemCount() {
        return 12;
    }

}
