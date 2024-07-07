package mint.woman.female.workout.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.utils.MyPreference;
import com.minfitness.decode.Decoder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.SelectViewHolder> {


    List<ExerciseObject> mAllExercise;
    List<Integer> status = new ArrayList<>();
    Context mContext;
    EventSelectExercise mEventSelectExercise;
    MyPreference myPreference;
    int ptype;

    public SelectAdapter(Context context, int type, EventSelectExercise eventSelectExercise) {
        mContext = context;
        mAllExercise = new ArrayList<>();
        mEventSelectExercise = eventSelectExercise;
        status = new ArrayList<>();
        myPreference = new MyPreference(context);
    }

    public void setData(List<ExerciseObject> list) {
        this.mAllExercise.clear();
        this.mAllExercise.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.custom_select_exericse_item_layout, parent, false);
        SelectViewHolder myChildViewHolder = new SelectViewHolder(v);

        return myChildViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectViewHolder holder, int position) {

        ExerciseObject exerciseObject = mAllExercise.get(position);

        holder.mExerciseName.setText(exerciseObject.getName());
        //String videoPath = "android.resource://" + getPackageName() + "/" + resourceId;
        byte[] encrypt = Decoder.decryptFromAssets(holder.itemView.getContext().getAssets(), "i/g/video_" +exerciseObject.getId());

        Glide.with(holder.itemView).asGif().load(encrypt).into(holder.mDemoExercise);
        if (ptype == 0) {
            holder.mCbExercise.setVisibility(View.VISIBLE);
        } else {
            holder.mCbExercise.setVisibility(View.GONE);
        }
        if (status.contains(exerciseObject.getId())){
            holder.mCbExercise.setChecked(true);
        }else {
            holder.mCbExercise.setChecked(false);
        }


        holder.lnPremium.setVisibility(View.GONE);
        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status.contains(exerciseObject.getId())){
                    if (mEventSelectExercise != null) {
                        mEventSelectExercise.onRemove(exerciseObject.getId());
                    }
                    holder.mCbExercise.setChecked(false);
                }else{
                    if (mEventSelectExercise != null) {
                        mEventSelectExercise.onAdd(exerciseObject.getId());
                    }
                    holder.mCbExercise.setChecked(true);
                }


            }
        });
        holder.imgDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventSelectExercise != null) {
                    mEventSelectExercise.onDetail(exerciseObject.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAllExercise.size();
    }

    public interface EventSelectExercise {
        void onAdd(int action);

        void onRemove(int action);

        void onDetail(int action);

        void onGotoPremium();
    }

    class SelectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_exercise)
        ImageView mDemoExercise;
        @BindView(R.id.tv_exercise_name)
        TextView mExerciseName;
        @BindView(R.id.tv_exercise_type)
        TextView mType;
        @BindView(R.id.cb_exercise)
        CheckBox mCbExercise;
        @BindView(R.id.ln_premium)
        View lnPremium;
        @BindView(R.id.container)
        View mContainer;
        @BindView(R.id.img_detail)
        View imgDetail;

        SelectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (ptype == 0) {
                mType.setVisibility(View.VISIBLE);
            } else {
                mType.setVisibility(View.GONE);
            }
        }

    }

}
