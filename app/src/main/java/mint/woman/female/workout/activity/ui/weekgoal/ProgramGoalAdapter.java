package mint.woman.female.workout.activity.ui.weekgoal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.ProgramClickListener;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.List;

public class ProgramGoalAdapter extends RecyclerView.Adapter<ProgramGoalAdapter.ProgramGoalViewHolder>{
    List<ProgramObject> programObjects;
    Context mContext;
    MyPreference myPreference;
    ProgramClickListener clickListener;

    public ProgramGoalAdapter(Context mContext,ProgramClickListener clickListener) {
        this.mContext = mContext;
        this.myPreference = new MyPreference(mContext);
        this.clickListener = clickListener;
        programObjects = new ArrayList<>();
    }
    public void setProgram(List<ProgramObject> list){
        this.programObjects.clear();
        this.programObjects.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProgramGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.small_program_goal,parent,false);
        return new ProgramGoalViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ProgramGoalViewHolder holder, int position) {
        ProgramObject programObject = programObjects.get(position);
        holder.mProgramName.setText(programObject.getName());
        int progress = myPreference.getPlanProgress(programObject.getId());

        if (programObject.getType() == 1) {
            float percent =  (progress*100f/programObject.getTotal());
            holder.mPercent.setText(String.format("%.0f",percent)+"%");
            holder.mDayLeft.setText("" + (programObject.getTotal() / 7) + " " + mContext.getString(R.string.weeks));
            holder.mProgress.setVisibility(View.VISIBLE);
            holder.mProgress.setProgress(progress);
            holder.mProgress.setMax(programObject.getTotal());
        }else {

            float percent = 100f* myPreference.getSaveProgressPercent(programObject.getId(),0);
            holder.mPercent.setText(String.format("%.0f",percent)+"%");
            holder.mDayLeft.setText("Level " + (programObject.getLevel()) );
            holder.mProgress.setVisibility(View.GONE);
        }

        String baseUrl = "file:///android_asset/images/";
        if (programObject.getDiscover() >0){
            baseUrl = ApiUtils.BASE_URL_IMAGES;
        }
        Glide.with(mContext).load(Uri.parse(baseUrl + programObject.getImage())).optionalFitCenter().into(holder.imgProgram);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null){
                    clickListener.onProgramClick(programObject);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return programObjects.size();
    }

    class ProgramGoalViewHolder extends RecyclerView.ViewHolder{
        ImageView imgProgram;
        TextView mProgramName,mPercent,mDayLeft;
        ProgressBar mProgress;

        public ProgramGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            mProgramName = itemView.findViewById(R.id.txt_program_name);
            mPercent = itemView.findViewById(R.id.txt_program_percent);
            mDayLeft = itemView.findViewById(R.id.txt_program_day_left);
            imgProgram = itemView.findViewById(R.id.img_program);
            mProgress = itemView.findViewById(R.id.progressProgram);
        }
    }
}
