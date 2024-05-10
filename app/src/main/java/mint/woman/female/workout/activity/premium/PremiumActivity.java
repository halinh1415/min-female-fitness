package mint.woman.female.workout.activity.premium;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.google.android.material.internal.EdgeToEdgeUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.text.Regex;
import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.LanguageHelper;

public class PremiumActivity extends AppCompatActivity {
    PremiumViewModel premiumViewModel;

    @BindView(R.id.ll_life_time)
    View llLifetime;
    @BindView(R.id.ll_year)
    View llYearly;
    @BindView(R.id.ll_month)
    View llMonthly;
    @BindView(R.id.rc_premium)
    RecyclerView rcPremium;
    @BindView(R.id.img_year0)
    ImageView imgYearChoose;
    @BindView(R.id.img_month0)
    ImageView imgMonthChoose;
    @BindView(R.id.img_life_time0)
    ImageView imgLifetimeChoose;
    @BindView(R.id.tv_year_price)
    TextView tvYearPrice;
    @BindView(R.id.tv_month_price)
    TextView tvMonthPrice;
    @BindView(R.id.tv_lifetime_price)
    TextView tvLifeTimePrice;
    @BindView(R.id.tv_off2)
    TextView tvOffYear;
    @BindView(R.id.tv_year_to_month)
    TextView tvYearToMonth;
    @BindView(R.id.tv_des_1)
    TextView tvDes1;
    @BindView(R.id.tv_des_2)
    TextView tvDes2;
    @BindView(R.id.cl_flower)
    View clFlower1;
    @BindView(R.id.cl_flower_2)
    View clFlower2;

    PremiumAdapter premiumAdapter;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeUtils.applyEdgeToEdge(getWindow(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        ButterKnife.bind(this);
        initView();

    }

    private void initView() {
        premiumViewModel = new ViewModelProvider(this).get(PremiumViewModel.class);
        premiumAdapter = new PremiumAdapter();

        premiumViewModel.selected.observe(this, selected -> {
            changeTextColorOfTextViews((ViewGroup) llLifetime, selected == 0);
            changeTextColorOfTextViews((ViewGroup) llYearly, selected == 1);
            changeTextColorOfTextViews((ViewGroup) llMonthly, selected == 2);
            if (selected == 0){
                tvDes1.setVisibility(View.INVISIBLE);
                tvDes2.setVisibility(View.INVISIBLE);
                clFlower2.setBackgroundResource(R.drawable.ic_discount_2);
                clFlower1.setBackgroundResource(R.drawable.ic_discount);
            }else  if (selected == 1){
                tvDes1.setVisibility(View.VISIBLE);
                tvDes2.setVisibility(View.VISIBLE);
                clFlower2.setBackgroundResource(R.drawable.ic_discount);
                clFlower1.setBackgroundResource(R.drawable.ic_discount_2);
            }else {
                clFlower2.setBackgroundResource(R.drawable.ic_discount);
                clFlower1.setBackgroundResource(R.drawable.ic_discount);
                tvDes1.setVisibility(View.INVISIBLE);
                tvDes2.setVisibility(View.VISIBLE);
            }
        });

        rcPremium.setAdapter(premiumAdapter);
        rcPremium.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        premiumViewModel.productSkuDetailsMap.observe(this,productDetailsMap -> {
            ProductDetails productDetailsMonthly = productDetailsMap.get(InAppKey.PREMIUM_MONTHLY);
            Log.d("HAHA", "skuDetailsYearly check");

            long yPrice ,mPrice =2;
            int discount = 0;
            if (productDetailsMonthly != null) {
                String monthPrice = productDetailsMonthly.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                mPrice = productDetailsMonthly.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                tvMonthPrice.setText(String.format(getString(R.string.abbr_x_mth_gpt), monthPrice));
                premiumAdapter.setMonthPrice(monthPrice);
            }
            else {
                Log.d("HAHA", "skuDetailsMonthly!= null");
                // return;
            }

            ProductDetails skuDetailsYearly = productDetailsMap.get(InAppKey.PREMIUM_YEARLY);
            if (skuDetailsYearly != null){
                yPrice = getAnnualPrice(skuDetailsYearly).getPriceAmountMicros();
                if (mPrice >0) {
                    discount = (int) ((12 * mPrice - yPrice) * 100 / 12 / mPrice);
                }
                String saleOff = ""+discount+"%";
                String yPriceString = getAnnualPrice(skuDetailsYearly).getFormattedPrice();
                tvOffYear.setText(String.format(getString(R.string.premium_price_save_2),saleOff));
                tvDes1.setText(String.format(getString(R.string.free_7_days_trial_then_xx_year),yPriceString));
                tvYearToMonth.setText(String.format(getString(R.string.iap_only_x_per_mth_gpt),getAnnualMonthlyPrice(skuDetailsYearly)));
                tvYearPrice.setText(String.format(getString(R.string.txt_x_year_gpt),yPriceString));
                premiumAdapter.setYearPrice(yPriceString);
            }
            else {
                Log.d("HAHA", "skuDetailsYearly!= null");
            }

        });
        premiumViewModel.productDetailInApps.observe(this,inAppProducts->{
            ProductDetails productLifetime = inAppProducts.get(InAppKey.PREMIUM_LIFETIME);
            if (productLifetime != null){
                tvLifeTimePrice.setText(productLifetime.getOneTimePurchaseOfferDetails().getFormattedPrice());
            }
        });

    }
    @SuppressLint("DefaultLocale")
    public String getAnnualMonthlyPrice(ProductDetails productDetails){
        Regex regex = new Regex("[0-9.,]");
        ProductDetails.PricingPhase pricingPhase = getAnnualPrice(productDetails);
        String currency = regex.replace(pricingPhase.getFormattedPrice(),"");
        float priceMicro = (float)(pricingPhase.getPriceAmountMicros()/12/1000000);
        return String.format("%s%.2f",currency,priceMicro);
    }
    public ProductDetails.PricingPhase getAnnualPrice(ProductDetails productDetails){

        List<ProductDetails.PricingPhase> list = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList();
        if (list.size() ==1){
            return list.get(0);
        }else{
            return  list.get(1);
        }
    }

    // Function to change text color of all TextViews within a layout
    private void changeTextColorOfTextViews(ViewGroup viewGroup, boolean isEnable) {
        if (viewGroup.getTag() != null && viewGroup.getTag().equals("LL_CHOICE")) {
            viewGroup.setBackgroundResource(isEnable ? R.drawable.background_pro_selected : R.drawable.background_pro_unselected);
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals("LL_TOP")) {
                child.setBackgroundResource(isEnable ? R.drawable.background_pro_title_top_bound : R.drawable.background_pro_title_top_bound_unselected);
            }
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(isEnable ? Color.BLACK : Color.GRAY);
            } else if (child instanceof ImageView) {
                ImageView im = (ImageView) child;
                if (im.getTag() != null) {
                    Log.d("HAHA", "im tag =" + im.getTag());
                }
                if (im.getTag() != null && im.getTag().equals("IMG_CHOICE")) {

                    im.setImageResource(isEnable ? R.drawable.ic_selected : R.drawable.ic_unselected);
                }
                if (im.getTag() != null && im.getTag().equals("IMG_GIFT")) {

                    im.setImageResource(isEnable ? R.drawable.ic_gift_box : R.drawable.ic_gift_box_disable);
                }

            } else if (child instanceof ViewGroup) {

                changeTextColorOfTextViews((ViewGroup) child, isEnable);
            }
        }

    }

    @OnClick({R.id.ll_month, R.id.ll_year, R.id.ll_life_time})
    void chosePlan(View view) {
        switch (view.getId()) {
            case R.id.ll_month:
                premiumViewModel.setSelected(2);
                break;
            case R.id.ll_year:
                premiumViewModel.setSelected(1);
                break;
            case R.id.ll_life_time:
                premiumViewModel.setSelected(0);
                break;

        }
    }

    @OnClick(R.id.btn_continue)
    void chargePremium() {
        if (premiumViewModel.getSelected().getValue() == 0) {
            premiumViewModel.launchBillingFlowInApp(this, InAppKey.PREMIUM_LIFETIME);
        } else if (premiumViewModel.getSelected().getValue() == 2) {
            premiumViewModel.launchBillingFlow(this, InAppKey.PREMIUM_MONTHLY);
        } else {
            premiumViewModel.launchBillingFlow(this, InAppKey.PREMIUM_YEARLY);
        }
    }

    @OnClick(R.id.iv_close)
    void close() {
        finish();
    }

    class PremiumAdapter extends RecyclerView.Adapter<PremiumAdapter.PremiumIntroViewHolder> {
        String yearPrice ="", monthPrice ="";


        public void setYearPrice(String yearPrice) {
            this.yearPrice = yearPrice;
            notifyDataSetChanged();
        }

        public void setMonthPrice(String monthPrice) {
            this.monthPrice = monthPrice;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PremiumIntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro_intro_1, parent, false);
                    break;
                case 1:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro_intro_2, parent, false);
                    break;
                case 2:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro_intro_3, parent, false);
                    break;
                case 3:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro_intro_9, parent, false);
                    break;
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pro_intro_10, parent, false);
            }
            return new PremiumIntroViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PremiumIntroViewHolder holder, int position) {

            if (holder.iapDetail!= null){
                holder.iapDetail.setText(String.format(getString(R.string.iap_detail_review),yearPrice,monthPrice));
            }
        }


        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        class PremiumIntroViewHolder extends RecyclerView.ViewHolder {
            TextView iapDetail;
            public PremiumIntroViewHolder(@NonNull View itemView) {
                super(itemView);
                iapDetail = itemView.findViewById(R.id.iap_detail);
            }
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() && lang.length() > 2) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//                        // Set the content to appear under the system bars so that the
//                        // content doesn't resize when the system bars hide and show.
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        // Hide the nav bar and status bar4
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }

}