package mint.woman.female.workout.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ProductObject;
import mint.woman.female.workout.utils.MyPreference;


public class ShoppingListFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    ProductObject standardProduction, vegetarianProduct;
    RadioButton mStandard, mVegetarian;
    RecyclerView lvProduct;
    MyPreference myPreference;
    ShoppingItemAdapter mShoppingItemAdapter;

    public ShoppingListFragment() {
        // Required empty public constructor
    }

    public static ShoppingListFragment newInstance(ProductObject standardProduct, ProductObject vegetarianProduct) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putParcelable("STANDARD_PRODUCT", standardProduct);
        args.putParcelable("VEGETARIAN_PRODUCT", vegetarianProduct);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            standardProduction = getArguments().getParcelable("STANDARD_PRODUCT");
            vegetarianProduct = getArguments().getParcelable("VEGETARIAN_PRODUCT");
        }
        myPreference = new MyPreference(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        lvProduct = view.findViewById(R.id.lv_products);
        mStandard = view.findViewById(R.id.rb_standard);
        mVegetarian = view.findViewById(R.id.rb_vegetarian);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (myPreference.isStandardDiet()) {
            mStandard.setChecked(true);
            mVegetarian.setChecked(false);
        } else {
            mStandard.setChecked(false);
            mVegetarian.setChecked(true);
        }
        mShoppingItemAdapter = new ShoppingItemAdapter(myPreference.isStandardDiet() ? standardProduction : vegetarianProduct);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        lvProduct.setLayoutManager(gridLayoutManager);
        lvProduct.setAdapter(mShoppingItemAdapter);

        mStandard.setOnCheckedChangeListener(this);
        mVegetarian.setOnCheckedChangeListener(this);


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.rb_standard:
                if (b) {
                    myPreference.setStandardDiet(true);
                    mShoppingItemAdapter.setProduct(standardProduction);
                } else {
                    myPreference.setStandardDiet(false);
                    mShoppingItemAdapter.setProduct(vegetarianProduct);
                }
                break;
            case R.id.rb_vegetarian:
                break;
        }
    }

    class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ShoppingItemViewHolder> {
        ProductObject productObject;

        ShoppingItemAdapter(ProductObject productObject) {
            this.productObject = productObject;
        }

        public void setProduct(ProductObject product) {
            if (product == null) return;
            this.productObject = new ProductObject(product);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.select_reminder_item, parent, false);

            return new ShoppingItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
            int itemId = productObject.id * 100 + position;
            holder.mShoppingItemCb.setChecked(myPreference.isShoppingOk(itemId));
            holder.mShoppingItemCb.setText(productObject.products.get(position));
        }

        @Override
        public int getItemCount() {
            return productObject.products.size();
        }

        class ShoppingItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CheckBox mShoppingItemCb;

            public ShoppingItemViewHolder(View itemView) {
                super(itemView);
                mShoppingItemCb = itemView.findViewById(R.id.cb_select_rm);
                mShoppingItemCb.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                    return;
                }
                if (view.getId() == R.id.cb_select_rm) {
                    int itemId = productObject.id * 100 + getAdapterPosition();
                    myPreference.setShoppingOk(itemId, !myPreference.isShoppingOk(itemId));
                    mShoppingItemCb.setChecked(myPreference.isShoppingOk(itemId));
                }
            }
        }
    }

}
