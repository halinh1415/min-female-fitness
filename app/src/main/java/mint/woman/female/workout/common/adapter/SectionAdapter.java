package mint.woman.female.workout.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.Section;

import java.util.List;


public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SECTION_HEADER = 0;
    private static final int VIEW_TYPE_HORIZONTAL_LIST_CHALLENGE= 1;
    private static final int VIEW_TYPE_VERTICAL_LIST_CHALLENGE= 1;
    private static final int VIEW_TYPE_VERTICAL_LIST = 2;

    private List<Section> itemList;
    ProgramClickListener clickListener;

    // Constructor and other necessary methods

    public SectionAdapter(List<Section> itemList,ProgramClickListener clickListener) {
        this.clickListener = clickListener;
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        Section item = itemList.get(position);

       return item.viewType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 1:
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(parent.getContext(),LinearLayoutManager.HORIZONTAL,false);
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionHeaderView = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionHeaderView,clickListener,R.layout.item_child_recent_challenge,layoutManager);
            case 4:
                RecyclerView.LayoutManager layoutManagerRandom = new GridLayoutManager(parent.getContext(),2,LinearLayoutManager.VERTICAL,false);
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionRandom = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionRandom,clickListener,R.layout.item_child_discover_group_rect,layoutManagerRandom);

            case 8:
                RecyclerView.LayoutManager layoutManagerVertical1 = new LinearLayoutManager(parent.getContext(),LinearLayoutManager.VERTICAL,false);
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionVertical7 = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionVertical7,clickListener,R.layout.item_child_discover_7_rect,layoutManagerVertical1);
            case 5:
                RecyclerView.LayoutManager layoutManagerVertical = new LinearLayoutManager(parent.getContext(),LinearLayoutManager.VERTICAL,false);
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionVertical = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionVertical,clickListener,R.layout.item_child_recent_rect,layoutManagerVertical);
            default:
                RecyclerView.LayoutManager layoutManagerDefault = new LinearLayoutManager(parent.getContext(),LinearLayoutManager.HORIZONTAL,false);
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionDefault = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionDefault,clickListener,R.layout.item_child_recent_rect,layoutManagerDefault);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Section item = itemList.get(position);

        //((SectionHeaderViewHolder) holder).horizontalRecyclerView.setLayoutManager(item.layoutManager);
      ((SectionHeaderViewHolder) holder).bind( item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView horizontalRecyclerView;
        private TextView sectionTextView,sectionSubtitle;
        private ProgramAdapter programAdapter;

        public SectionHeaderViewHolder(@NonNull View itemView,ProgramClickListener clickListener,int layout,RecyclerView.LayoutManager layoutManager) {
            super(itemView);
            int position = getAdapterPosition();
            // Initialize views for the section header
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);
            sectionTextView = itemView.findViewById(R.id.tv_section_title);
            sectionSubtitle = itemView.findViewById(R.id.tv_section_subtitle);

        //    horizontalRecyclerView.setLayoutManager(section.layoutManager);
            // Initialize and set up the adapter for the horizontal list
           programAdapter = new ProgramAdapter(layout,clickListener);
           horizontalRecyclerView.setLayoutManager(layoutManager);
           horizontalRecyclerView.setAdapter(programAdapter);
        }

        public void bind(Section section) {
            sectionTextView.setText(section.sectionName);
            if (!section.subtitle.isEmpty()){
                sectionSubtitle.setVisibility(View.VISIBLE);
                sectionSubtitle.setText(section.subtitle);
            }else {
                sectionSubtitle.setVisibility(View.GONE);
            }
            programAdapter.setData(section.items);
            programAdapter.notifyDataSetChanged();
        }
    }
}