package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.minfitness.decode.Decoder;
import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.data.CustomWorkoutDataProvider;
import mint.woman.female.workout.common.utils.DrawableUtils;
import mint.woman.female.workout.common.utils.ViewUtils;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.MyPreference;

@SuppressLint("SetTextI18n")
public class CustomWorkoutAdapter extends AbstractExpandableItemAdapter<CustomWorkoutAdapter.MyGroupViewHolder, CustomWorkoutAdapter.MyChildViewHolder>
        implements ExpandableDraggableItemAdapter<CustomWorkoutAdapter.MyGroupViewHolder, CustomWorkoutAdapter.MyChildViewHolder>, View.OnClickListener {

    private CustomWorkoutDataProvider mProvider;
    private RecyclerViewExpandableItemManager mExpandableItemManager;
    private Context mContext;
    private EventListener mEventListener;
    private boolean flagEdit;
    private MyPreference myPreference;

    public CustomWorkoutAdapter(Context mContext, CustomWorkoutDataProvider provider, RecyclerViewExpandableItemManager expdMgr, boolean flagEdit) {
        setHasStableIds(true);
        this.mProvider = provider;
        this.mExpandableItemManager = expdMgr;
        this.mContext = mContext;
        this.flagEdit = flagEdit;
        myPreference = MyPreference.newInstance(mContext);
    }

    public void setEventListener(EventListener mEventListener) {
        this.mEventListener = mEventListener;
    }

    public void setFlagEdit(boolean flagEdit) {
        this.flagEdit = flagEdit;
        notifyDataSetChanged();
    }

    @Override
    public boolean onCheckGroupCanStartDrag(MyGroupViewHolder holder, int groupPosition, int x, int y) {
        return false;
    }

    @Override
    public boolean onCheckChildCanStartDrag(MyChildViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        final View containerView = holder.containerView;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (containerView.getTranslationX() + 0.5f);
        final int offsetY = containerView.getTop() + (int) (containerView.getTranslationY() + 0.5f);

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetGroupItemDraggableRange(MyGroupViewHolder holder, int groupPosition) {
        return null;
    }

    @Override
    public ItemDraggableRange onGetChildItemDraggableRange(MyChildViewHolder holder, int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public void onMoveGroupItem(int fromGroupPosition, int toGroupPosition) {

    }

    @Override
    public void onMoveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        mProvider.moveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
    }

    @Override
    public boolean onCheckGroupCanDrop(int draggingGroupPosition, int dropGroupPosition) {
        return false;
    }

    @Override
    public boolean onCheckChildCanDrop(int draggingGroupPosition, int draggingChildPosition, int dropGroupPosition, int dropChildPosition) {
        return true;
    }

    @Override
    public void onGroupDragStarted(int groupPosition) {

    }

    @Override
    public void onChildDragStarted(int groupPosition, int childPosition) {
        notifyDataSetChanged();
    }

    @Override
    public void onGroupDragFinished(int fromGroupPosition, int toGroupPosition, boolean result) {

    }

    @Override
    public void onChildDragFinished(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mProvider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mProvider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition + 1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mProvider.getChildItem(groupPosition, childPosition).mId;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_day_item_layout, parent, false);
        MyGroupViewHolder vh = new MyGroupViewHolder(v);
        vh.mAdd.setOnClickListener(this);
        return vh;
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_exercise_item_layout, parent, false);
        MyChildViewHolder vh = new MyChildViewHolder(v);
        vh.mDetail.setOnClickListener(this);
        vh.mDelete.setOnClickListener(this);
        vh.containerView.setOnClickListener(this);
        return vh;
    }


    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
        if (groupPosition == RecyclerView.NO_POSITION) {
            return;
        }
        holder.mDayName.setText(mContext.getString(R.string.txt_day) + " " + (groupPosition + 1));
        if (!flagEdit) {
            holder.mAdd.setVisibility(View.VISIBLE);
        } else {
            holder.mAdd.setVisibility(View.GONE);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
        PlanObject.ActionObject actionObject = mProvider.getChildItem(groupPosition, childPosition);
        ExerciseObject exerciseObject = FitnessApplication.getApplication().mJsonHelper.getHashMapExercise(0,1003).get(actionObject.actionId);
        if (exerciseObject == null){
            Log.d("HAHA","not found id ="+actionObject.actionId);
        }
        holder.mExerciseName.setText(exerciseObject.getName());

        if (!actionObject.getUnit().isEmpty()) {
            int minutes = (actionObject.time) / 60;
            int seconds = actionObject.time % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            holder.mReps.setText(timeString);
        }else{
            holder.mReps.setText("x"+actionObject.time);
        }


        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop().transform(new RoundedCorners(30));
        byte[] encrypt = Decoder.decryptFromAssets(holder.itemView.getContext().getAssets(), "i/g/video_" +actionObject.actionId);
     //   byte[] encrypt = VideoEncryptor.decryptGif(holder.itemView.getContext().getAssets(), "i/g/video_" +actionObject.actionId,"ASDFGHJKLASDFGHJ");

        Glide.with(holder.itemView).asGif().load(encrypt).into(holder.mDemoExercise);

        final int dragState = holder.getDragStateFlags();
        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.z
                DrawableUtils.clearState(holder.containerView.getBackground());
            } else if (((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) &&
                    ((dragState & Draggable.STATE_FLAG_IS_IN_RANGE) != 0)) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else {
                //bgResId = R.drawable.bg_item_normal_state;
                bgResId = R.drawable.bg_item_list;
            }

            holder.containerView.setBackgroundResource(bgResId);
        }
        if (!flagEdit) {
            holder.mDragHandle.setVisibility(View.VISIBLE);
            holder.mDelete.setVisibility(View.VISIBLE);
        } else {
            holder.mDragHandle.setVisibility(View.GONE);
            holder.mDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // handles click event manually (to show Snackbar message)
        return false;
    }

    @Override
    public boolean getInitialGroupExpandedState(int groupPosition) {
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        RecyclerView rv = RecyclerViewAdapterUtils.getParentRecyclerView(view);
        RecyclerView.ViewHolder vh = rv.findContainingViewHolder(view);
        int rootPosition = vh.getAdapterPosition();
        if (rootPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.Adapter rootAdapter = rv.getAdapter();
        int localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter, this, rootPosition);

        long expandablePosition = mExpandableItemManager.getExpandablePosition(localFlatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
        switch (view.getId()) {
            case R.id.img_add:
                if (mEventListener != null) {
                    mEventListener.onAddExerciseInDay(groupPosition);
                }
                break;
            case R.id.img_detail:
                if (mEventListener != null) {
                    mEventListener.onExerciseDetail(mProvider.getChildItem(groupPosition, childPosition).actionId);
                }
                break;
            case R.id.img_delete:
                mProvider.removeChildItem(groupPosition, childPosition);
                mExpandableItemManager.notifyChildItemRemoved(groupPosition, childPosition);
                break;
            case R.id.container:
                if (!flagEdit) {
                    if (mEventListener != null) {
                        mEventListener.onUpdateChildItem(groupPosition, childPosition);
                    }
                }
                break;
        }
    }

    private interface Draggable extends DraggableItemConstants {
    }

    public interface EventListener {
        void onExerciseDetail(int actionId);

        void onAddExerciseInDay(int day);

        void onUpdateChildItem(int groupPosition, int childPosition);
    }

    static abstract class MyBaseViewHolder extends AbstractDraggableItemViewHolder implements ExpandableItemViewHolder {
        View containerView;
        private int mExpandStateFlags;

        MyBaseViewHolder(View itemView) {
            super(itemView);
            containerView = itemView.findViewById(R.id.container);
        }

        @Override
        public int getExpandStateFlags() {
            return mExpandStateFlags;
        }

        @Override
        public void setExpandStateFlags(int flag) {
            mExpandStateFlags = flag;
        }
    }

    static class MyGroupViewHolder extends MyBaseViewHolder {
        TextView mDayName;
        ImageView mAdd;

        public MyGroupViewHolder(View itemView) {
            super(itemView);
            mDayName = itemView.findViewById(R.id.txt_day);
            mAdd = itemView.findViewById(R.id.img_add);
        }

        @NonNull
        @Override
        public ExpandableItemState getExpandState() {
            return null;
        }
    }

    static class MyChildViewHolder extends MyBaseViewHolder {
        TextView mExerciseName, mReps;
        ImageView mDemoExercise, mDelete, mDetail;
        View mDragHandle;

        public MyChildViewHolder(View itemView) {
            super(itemView);
            mExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            mReps = itemView.findViewById(R.id.txt_exercise_time);
            mDemoExercise = itemView.findViewById(R.id.img_exercise);
            mDelete = itemView.findViewById(R.id.img_delete);
            mDetail = itemView.findViewById(R.id.img_detail);
            mDragHandle = itemView.findViewById(R.id.drag_handle);

        }

        @NonNull
        @Override
        public ExpandableItemState getExpandState() {
            return null;
        }
    }
}
