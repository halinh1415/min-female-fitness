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
import com.google.protobuf.Api;

import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.CustomLayoutParams;
import mint.woman.female.workout.custom.CustomLayoutParams2;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;
import java.util.Objects;

public class ProgramAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_PROGRAM_SECTION = 0;
    private static final int TYPE_CATEGORY_SECTION = 1;
    private List<Object> items;
    int layout;
    ProgramClickListener programClickListener;
    Context mContext;

    public ProgramAdapter(int layout, ProgramClickListener programClickListener) {
        this.layout = layout;
        this.programClickListener = programClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof CategoryObject)
            return TYPE_CATEGORY_SECTION;
        else return TYPE_PROGRAM_SECTION;
    }
    // Constructor, getters, and setters

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        switch (viewType){
            case TYPE_PROGRAM_SECTION:
                View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                if (layout == R.layout.item_child_recent_challenge) {
                    view.setLayoutParams(new CustomLayoutParams(parent.getContext()));
                }else if (layout == R.layout.item_child_recent_rect){
                    view.setLayoutParams(new CustomLayoutParams2(parent.getContext()));
                }else if (layout == R.layout.item_child_discover_7_rect){

                }else {

                }
                return new ProgramViewHolder(view);
            case TYPE_CATEGORY_SECTION:
                View viewGroup = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_discover_group_rect, parent, false);
                return new CategoryViewHolder(viewGroup);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ProgramViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProgramViewHolder) {
            ProgramObject programObject =  (ProgramObject) (items.get(position));

            ProgramViewHolder programViewHolder = (ProgramViewHolder) holder;
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.optionalFitCenter();
            // Load image using Glide
            String baseUrl = "file:///android_asset/images/";
            if (programObject.getDiscover() >0){
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(programViewHolder.mBackground.getContext())
                    .load(Uri.parse(baseUrl+programObject.getBackground()))
                    .error(Uri.parse(baseUrl+"bg_1.png"))
                    .centerCrop()
                    .into(programViewHolder.mBackground);

            Glide.with(programViewHolder.mWorkout.getContext()).load(Uri.parse(baseUrl+programObject.getImage()))
                    .into(programViewHolder.mWorkout);
            // Set programName and time
            MyPreference myPreference = new MyPreference(mContext);


            if(programObject.getType()==1){
                programViewHolder.mProgramName.setText(programObject.getName());
                programViewHolder.mSubTitle.setText(programViewHolder.mSubTitle.getContext().getString(R.string.txt_challenge_7x4));

                if (programViewHolder.mProgressBar!= null && programViewHolder.mProgressTxt!= null) {
                    int progress = myPreference.getPlanProgress(programObject.getId());
                    programViewHolder.mProgressBar.setMax(programObject.getTotal());
                    programViewHolder.mProgressBar.setProgress(progress);
                    programViewHolder.mProgressTxt.setText("" + (programObject.getTotal() - progress) + " " + mContext.getString(R.string.txt_day_left));
                }

            }else {
                programViewHolder.mProgramName.setText(programObject.getName()+" "+programObject.getSubtitle());
                programViewHolder.mSubTitle.setText(""+programObject.getTotal()+" "+programViewHolder.mSubTitle.getContext().getString(R.string.minutes));
            }
            holder.itemView.setOnClickListener(view -> {
                if (programClickListener!= null){
                    programClickListener.onItemRootViewClicked(programObject);
                }
            });
            if (programViewHolder.mStartBtn != null){
                programViewHolder.mStartBtn.setOnClickListener(view -> {
                    if (programClickListener!= null){
                        programClickListener.onItemRootViewClicked(programObject);
                    }
                });
            }
        }else{
            CategoryObject categoryObject =  (CategoryObject) items.get(position);
            CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
            String baseUrl = "file:///android_asset/images/";
            if (categoryObject.getFirstProgramObject().getDiscover() >0){
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(categoryViewHolder.mBackground.getContext())
                    .load(Uri.parse(baseUrl+categoryObject.getFirstProgramObject().getBackground()))
                    .error(Uri.parse(baseUrl+"bg_1.png"))
                    .centerCrop()
                    .into(categoryViewHolder.mBackground);

            Glide.with(categoryViewHolder.mWorkout.getContext()).load(Uri.parse(baseUrl+categoryObject.getFirstProgramObject().getImage()))
                    .into(categoryViewHolder.mWorkout);
            categoryViewHolder.mProgramName.setText(categoryObject.getFirstProgramObject().getName());
            categoryViewHolder.mSubTitle.setText(""+categoryObject.getFirstProgramObject().getTotal()+" "+holder.itemView.getContext().getString(R.string.minutes));
            holder.itemView.setOnClickListener(view -> programClickListener.onGroupClickListener(categoryObject));
        }



    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<Object> data) {
        items = data;
        notifyDataSetChanged();
    }


}