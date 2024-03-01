package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.data.DemoDataProvider;
import mint.woman.female.workout.custom.ItemTouchHelperAdapter;
import mint.woman.female.workout.custom.OnStartDragListener;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.VideoEncryptor;

public class PreviewAdapter extends RecyclerView.Adapter<ExerciseViewHolder> implements ItemTouchHelperAdapter {
    private final OnStartDragListener mDragStartListener;

    private EventDemo mEventDemo;
    private DemoDataProvider mProvider;
    private Context mContext;
    private int mDiscover;
    private boolean flagEdit;

    public PreviewAdapter(Context context, OnStartDragListener dragStartListener, EventDemo eventDemo, DemoDataProvider mProvider, int discover) {
        this.mProvider = mProvider;
        this.mDragStartListener = dragStartListener;
        this.mContext = context;
        this.mDiscover = discover;
        this.flagEdit = false;
        this.mEventDemo = eventDemo;
    }

    public void setFlagEdit(boolean flagEdit) {
        this.flagEdit = flagEdit;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_item_layout, null);
        return new ExerciseViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final ExerciseViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PlanObject.ActionObject actionObject = mProvider.getItem(position);
        ExerciseObject exerciseObject = mProvider.getExerciseItem(actionObject.actionId);
        if (exerciseObject == null)
            return;
        holder.mExerciseName.setText(exerciseObject.getName());

        if (actionObject.unit.contains("s")){
            holder.mReps.setText(FitnessConstUtils.toTime(actionObject.time));
        }else {
            holder.mReps.setText("x "+actionObject.time);
        }
        byte[] encrypt;
        if (mDiscover == 0){
            encrypt = VideoEncryptor.decryptGif(holder.itemView.getContext().getAssets(), "i/g/video_" +actionObject.actionId,"ASDFGHJKLASDFGHJ");
        }else{
            encrypt = VideoEncryptor.decryptInput( mContext.getFilesDir().getPath()+"/discover/"+mDiscover+"/g/video_" +actionObject.actionId,"ASDFGHJKLASDFGHJ");
        }


        Glide.with(holder.itemView).asGif().load(encrypt).into(holder.mDemoExercise);

        holder.mDragHandle.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder);
            }
            return false;
        });
        if (flagEdit) {
            holder.mDragHandle.setVisibility(View.VISIBLE);
            holder.mDelete.setVisibility(View.GONE);
            holder.imgReplace.setVisibility(View.VISIBLE);
        } else {
            holder.mDragHandle.setVisibility(View.GONE);
            holder.mDelete.setVisibility(View.GONE);
            holder.imgReplace.setVisibility(View.GONE);
        }
        holder.imgReplace.setOnClickListener(v->{
            if (mEventDemo != null) {
                mEventDemo.onReplace(position,actionObject);
            }
        });
        holder.mDelete.setOnClickListener(v->{
            if (mProvider.getItemCount() > 1) {
                mProvider.removeItem(position);
                notifyItemRemoved(position);
            } else {
                Toast.makeText(mContext, "Need at least 1 exercise", Toast.LENGTH_SHORT).show();
            }
        });
        holder.container.setOnClickListener(v->{
            if (flagEdit) {
                if (mEventDemo != null) {
                    mEventDemo.onRepsEdit(position, mProvider.getItem(position));
                }
            }else{
                if (mEventDemo != null) {
                    mEventDemo.onDetail(actionObject.actionId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProvider.getItemCount();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        mProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public interface EventDemo {
        void onRepsEdit(int position, PlanObject.ActionObject actionObject);
        void onReplace(int position, PlanObject.ActionObject actionObject);
        void onDetail(int actionId);
    }

}