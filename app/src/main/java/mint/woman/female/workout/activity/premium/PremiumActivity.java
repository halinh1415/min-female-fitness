package mint.woman.female.workout.activity.premium;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;
import java.util.Locale;

import kotlin.text.Regex;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.firstsetup.FirstSetupActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.databinding.ActivityPremiumBinding;
import mint.woman.female.workout.databinding.ItemGoPro1Binding;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

public class PremiumActivity extends AppCompatActivity {
    PremiumViewModel premiumViewModel;
    ActivityPremiumBinding  binding;
    ItemGoPro1Binding goPro1Binding;


    MyPreference myPreference;
    PremiumAdapter premiumAdapter;
    boolean isFirstSetup = false;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeUtils.applyEdgeToEdge(getWindow(), true);
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        goPro1Binding = binding.itemGo;
        setContentView(binding.getRoot());
        initView();

    }

    private void initView() {
        isFirstSetup = getIntent().getBooleanExtra("IS_FIRST",false);
        myPreference = new MyPreference(this);
        premiumViewModel = new ViewModelProvider(this).get(PremiumViewModel.class);
        premiumAdapter = new PremiumAdapter();
        binding.ivClose.setOnClickListener(view -> {
            finishPremium();
        });

        premiumViewModel.selected.observe(this, selected -> {
            changeTextColorOfTextViews((ViewGroup) goPro1Binding.llLifeTime, selected == 0);
            changeTextColorOfTextViews((ViewGroup) goPro1Binding.llYear, selected == 1);
            changeTextColorOfTextViews((ViewGroup) goPro1Binding.llMonth, selected == 2);
            try {
                if (selected == 0) {
                    goPro1Binding.tvDes1.setVisibility(View.INVISIBLE);
                    goPro1Binding.tvDes2.setVisibility(View.INVISIBLE);
                    goPro1Binding.clFlower2.setBackgroundResource(R.drawable.ic_discount_2);
                    goPro1Binding.clFlower.setBackgroundResource(R.drawable.ic_discount);
                } else if (selected == 1) {
                    if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_TRIAL)) {
                        goPro1Binding.tvDes1.setVisibility(View.VISIBLE);
                    } else {
                        goPro1Binding.tvDes1.setVisibility(View.INVISIBLE);
                    }

                    goPro1Binding.tvDes2.setVisibility(View.VISIBLE);
                    goPro1Binding.clFlower2.setBackgroundResource(R.drawable.ic_discount);
                    goPro1Binding.clFlower.setBackgroundResource(R.drawable.ic_discount_2);
                } else {
                    goPro1Binding.clFlower2.setBackgroundResource(R.drawable.ic_discount);
                    goPro1Binding.clFlower.setBackgroundResource(R.drawable.ic_discount);
                    goPro1Binding.tvDes1.setVisibility(View.INVISIBLE);
                    goPro1Binding.tvDes2.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_TRIAL)){
            goPro1Binding.tvYear2.setText(getString(R.string.txt_day_free_trial));
        }else {
            goPro1Binding.tvYear2.setText(getString(R.string.txt_most_popular));
        }

        binding.rcPremium.setAdapter(premiumAdapter);
        binding.rcPremium.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        premiumViewModel.productSkuDetailsMap.observe(this, productDetailsMap -> {
            ProductDetails productDetailsMonthly = productDetailsMap.get(InAppKey.PREMIUM_MONTHLY);
            Log.d("HAHA", "skuDetailsYearly check");

            long yPrice, mPrice = 2;
            int discount = 0;
            if (productDetailsMonthly != null) {
                String monthPrice = productDetailsMonthly.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                mPrice = productDetailsMonthly.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                goPro1Binding.tvMonthPrice.setText(String.format(getString(R.string.abbr_x_mth_gpt), monthPrice));
                premiumAdapter.setMonthPrice(monthPrice);
            } else {
                Log.d("HAHA", "skuDetailsMonthly!= null");
                // return;
            }

            ProductDetails skuDetailsYearly = productDetailsMap.get(InAppKey.PREMIUM_YEARLY);
            if (skuDetailsYearly != null) {
                yPrice = getAnnualPrice(skuDetailsYearly).getPriceAmountMicros();
                if (mPrice > 0) {
                    discount = (int) ((12 * mPrice - yPrice) * 100 / 12 / mPrice);
                }
                String saleOff = "" + discount + "%";
                String yPriceString = getAnnualPrice(skuDetailsYearly).getFormattedPrice();
                goPro1Binding.tvOff2.setText(String.format(getString(R.string.premium_price_save_2), saleOff));
                goPro1Binding.tvDes1.setText(String.format(getString(R.string.free_7_days_trial_then_xx_year), yPriceString));
                goPro1Binding.tvYearToMonth.setText(String.format(getString(R.string.iap_only_x_per_mth_gpt), getAnnualMonthlyPrice(skuDetailsYearly)));
                goPro1Binding.tvYearPrice.setText(String.format(getString(R.string.txt_x_year_gpt), yPriceString));
                premiumAdapter.setYearPrice(yPriceString);
            } else {
                Log.d("HAHA", "skuDetailsYearly!= null");
            }
        });
        premiumViewModel.isPremium.observe(this, isPremium -> {
            if (isPremium) {
                myPreference.setPremiumMember(true);
            } else {
                myPreference.setPremiumMember(false);
            }
        });
        premiumViewModel.productDetailInApps.observe(this, inAppProducts -> {
            ProductDetails productLifetime = inAppProducts.get(InAppKey.PREMIUM_LIFETIME);
            if (productLifetime != null) {
                goPro1Binding.tvLifetimePrice.setText(productLifetime.getOneTimePurchaseOfferDetails().getFormattedPrice());
            }
        });
        goPro1Binding.btnContinue.setOnClickListener(v->{
            if (premiumViewModel.getSelected().getValue() == 0) {
                premiumViewModel.launchBillingFlowInApp(this, InAppKey.PREMIUM_LIFETIME);
            } else if (premiumViewModel.getSelected().getValue() == 2) {
                premiumViewModel.launchBillingFlow(this, InAppKey.PREMIUM_MONTHLY);
            } else {
                premiumViewModel.launchBillingFlowYearly(this, InAppKey.PREMIUM_YEARLY);
            }
        });
        goPro1Binding.llMonth.setOnClickListener(this::chosePlan);
        goPro1Binding.llYear.setOnClickListener(this::chosePlan);
        goPro1Binding.llLifeTime.setOnClickListener(this::chosePlan);

    }

    void finishPremium(){
        if (isFirstSetup && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_FIRST_OPEN) && MaticAd.getInstance().isAvailableInter()){
            MaticAd.getInstance().showPriorityInterstitialAd(PremiumActivity.this,new MaticAdCallBack(){
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    PremiumActivity.this.finish();
                }
            });
        }else{
            PremiumActivity.this.finish();
        }
    }

    @SuppressLint("DefaultLocale")
    public String getAnnualMonthlyPrice(ProductDetails productDetails) {
        Regex regex = new Regex("[0-9.,]");
        ProductDetails.PricingPhase pricingPhase = getAnnualPrice(productDetails);
        String currency = regex.replace(pricingPhase.getFormattedPrice(), "");
        float priceMicro = (float) (pricingPhase.getPriceAmountMicros() / 12000000.0f);
        return String.format("%s%.2f", currency, priceMicro);
    }

    public ProductDetails.PricingPhase getAnnualPrice(ProductDetails productDetails) {

        List<ProductDetails.PricingPhase> list = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList();
        if (list.size() == 1) {
            return list.get(0);
        } else {
            return list.get(1);
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




    void close() {
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        Bundle b = new Bundle();
        b.putBoolean("PREMIUM_MEMBER", myPreference.isPremiumMember());
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    class PremiumAdapter extends RecyclerView.Adapter<PremiumAdapter.PremiumIntroViewHolder> {
        String yearPrice = "", monthPrice = "";


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

//            if (holder.iapDetail!= null){
//                holder.iapDetail.setText(String.format(getString(R.string.iap_detail_review),yearPrice,monthPrice));
//            }
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
        if (!lang.isEmpty() ) {
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