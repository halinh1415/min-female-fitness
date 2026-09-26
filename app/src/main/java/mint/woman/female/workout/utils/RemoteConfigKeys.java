package mint.woman.female.workout.utils;

public class RemoteConfigKeys {
    public static final String INTER_SPLASH ="inter_splash" ;
    public static final String APP_OPEN_RESUME = "app_resume" ;
    public static final String PRO_VIDEO_REWARD = "pro_video_reward";
    public static final String GO_BEFORE = "inter_go_before";
    public static final String ENABLE_TRIAL = "enable_trial";
    public static final String ENABLE_SYNC = "enable_sync";
    public static final String BACKGROUND_MUSIC = "background_music";
    public static final String ENABLE_LOCK_WIDGET = "enable_lock";
    public static final String ENABLE_LANGUAGE_NEXT = "enable_language_next";
    public static final String NATIVE_LAYOUT_STYLE = "native_layout_style";
    public static String NATIVE_LANGUAGE = "native_language";
    public static String NATIVE_SETUP = "native_setup";
    public static String NATIVE_ONBOARD = "native_onboard";
    public static String INTER_FIRST_OPEN = "inter_first_open";
    public static String INTER_START = "inter_start";
    public static String BANNER = "banner";

    // Onboarding v2
    /** true: new quiz onboarding (OnboardingActivity), false: legacy FirstSetupActivity. */
    public static final String ONBOARDING_V2 = "onboarding_v2";
    /** Experiment arm: "control_46" | "short_28" | "adaptive". */
    public static final String ONBOARDING_EXPERIMENT_VARIANT = "onboarding_experiment_variant";

    // Paywall
    /** Show the paywall right after onboarding. */
    public static final String PAYWALL_AFTER_ONBOARDING = "paywall_after_onboarding";
    /** Seconds before the close button appears (0 = immediately). */
    public static final String PAYWALL_CLOSE_DELAY_SEC = "paywall_close_delay_sec";
    /** Pre-selected plan: "yearly" | "monthly" | "lifetime". */
    public static final String PAYWALL_DEFAULT_PLAN = "paywall_default_plan";
    public static final String PAYWALL_SHOW_LIFETIME = "paywall_show_lifetime";
    public static final String PAYWALL_SHOW_MONTHLY = "paywall_show_monthly";
    /** Show the free-trial timeline when the selected plan has a trial. */
    public static final String PAYWALL_SHOW_TIMELINE = "paywall_show_timeline";
    /** Show a discounted offer when the user closes the paywall. */
    public static final String PAYWALL_EXIT_OFFER_ENABLED = "paywall_exit_offer_enabled";
    /** Offer id (Play Console) of the yearly discounted offer used for the exit offer. */
    public static final String PAYWALL_EXIT_OFFER_ID = "paywall_exit_offer_id";
}
