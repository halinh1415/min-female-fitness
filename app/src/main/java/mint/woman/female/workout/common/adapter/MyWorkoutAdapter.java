package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.List;

public class MyWorkoutAdapter extends RecyclerView.Adapter<MyWorkoutAdapter.MyWorkoutViewHolder> {
    Context mContext;
    private List<ProgramObject> mItems;
    private MyPreference myPreference;
    private EventListener mEventListener;

    public MyWorkoutAdapter() {
        this.mItems = new ArrayList<>();
    }

    public void setData(List<ProgramObject> list) {
        this.mItems.clear();
        this.mItems.addAll(list);
        this.notifyDataSetChanged();
    }

    public void setEventListener(EventListener eventListener) {
        this.mEventListener = eventListener;
    }

    @NonNull
    @Override
    public MyWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        myPreference = new MyPreference(mContext);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_workout_item_layout, null);
        return new MyWorkoutViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyWorkoutViewHolder holder, int position) {
        ProgramObject programObject = mItems.get(position);
        int progress = myPreference.getPlanProgress(programObject.getId());
        holder.mWorkoutName.setText(programObject.getName());
        holder.mProgress.setMax(programObject.getTotal());
        holder.mProgress.setProgress(progress);
        holder.mProgressCount.setText(mContext.getString(R.string.txt_day_left) + " " + (programObject.getTotal() - progress));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface EventListener {
        void onEdit(ProgramObject myWorkoutObject);

        void onGotoWorkout(ProgramObject myWorkoutObject);

        void onDeleteWorkout(ProgramObject myWorkoutObject);
    }

    @SuppressLint("NonConstantResourceId")
    class MyWorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mWorkoutName;
        TextView mProgressCount;
        ProgressBar mProgress;

        MyWorkoutViewHolder(View itemView) {
            super(itemView);
            mWorkoutName = itemView.findViewById(R.id.tv_exercise_name);
            mProgressCount = itemView.findViewById(R.id.tv_program_progress);
            mProgress = itemView.findViewById(R.id.progressBar);
            itemView.findViewById(R.id.img_edit).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.img_delete).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.container).setOnClickListener(this::onClick);
        }


        public void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            ProgramObject myWorkoutObject = mItems.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.container:
                    if (mEventListener != null) {
                        mEventListener.onGotoWorkout(myWorkoutObject);
                    }
                    break;
                case R.id.img_edit:
                    if (mEventListener != null) {
                        mEventListener.onEdit(myWorkoutObject);
                    }
                    break;
                case R.id.img_delete:
                    if (mEventListener != null) {
                        mEventListener.onDeleteWorkout(myWorkoutObject);
                    }
                    break;
            }
        }
    }


}
