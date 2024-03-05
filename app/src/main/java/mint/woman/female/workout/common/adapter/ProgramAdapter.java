package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.CustomLayoutParams;
import mint.woman.female.workout.custom.CustomLayoutParams2;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramViewHolder> {
    private List<ProgramObject> programObjects;
    int layout;

    ProgramClickListener programClickListener;
    Context mContext;

    public ProgramAdapter(int layout, ProgramClickListener programClickListener) {
        this.layout = layout;
        this.programClickListener = programClickListener;
    }

// Constructor, getters, and setters

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        if (layout == R.layout.item_child_recent_challenge) {
            view.setLayoutParams(new CustomLayoutParams(parent.getContext()));
        }else {
            view.setLayoutParams(new CustomLayoutParams2(parent.getContext()));
        }
        return new ProgramViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        ProgramObject programObject = programObjects.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.optionalFitCenter();
        // Load image using Glide
        Glide.with(holder.mBackground.getContext())
                .load(Uri.parse("file:///android_asset/images/"+programObject.getBackground()))
                .error(Uri.parse("file:///android_asset/images/bg_1.png"))
                .centerCrop()
                .into(holder.mBackground);

        Glide.with(holder.mWorkout.getContext()).load(Uri.parse("file:///android_asset/images/"+programObject.getImage()))
                .into(holder.mWorkout);
        // Set programName and time
        MyPreference myPreference = new MyPreference(mContext);

        holder.mProgramName.setText(programObject.getName());
        if(programObject.getType()==1){
            holder.mSubTitle.setText(holder.mSubTitle.getContext().getString(R.string.txt_challenge_7x4));

            if (holder.mProgressBar!= null && holder.mProgressTxt!= null) {
                int progress = myPreference.getPlanProgress(programObject.getId());
                holder.mProgressBar.setMax(programObject.getTotal());
                holder.mProgressBar.setProgress(progress);
                holder.mProgressTxt.setText("" + (programObject.getTotal() - progress) + " " + mContext.getString(R.string.txt_day_left));
            }

        }else {
            holder.mSubTitle.setText(""+programObject.getTotal()+" "+holder.mSubTitle.getContext().getString(R.string.minutes));
        }
        holder.itemView.setOnClickListener(view -> {
            if (programClickListener!= null){
                programClickListener.onItemRootViewClicked(programObject);
            }
        });
        if (holder.mStartBtn != null){
            holder.mStartBtn.setOnClickListener(view -> {
                if (programClickListener!= null){
                    programClickListener.onItemRootViewClicked(programObject);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return programObjects.size();
    }

    public void setData(List<ProgramObject> data,int layout) {
        programObjects = data;

        this.layout = layout;
        notifyDataSetChanged();
    }


}