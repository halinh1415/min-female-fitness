# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.android.vending.billing.**
-keep public class * implements androidx.versionedparcelable.VersionedParcelable
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keepclassmembers class mint.woman.female.workout.model.HistoryItem{
    *;
    }

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# PlanSettings được lưu bằng Gson: giữ tên field, constructor mặc định và generic Signature
# (Set<PlanFocus>...). Không giữ thì R8 xoá Signature -> Gson đọc phần tử thành String
# -> ClassCastException ở PersonalPlanStore.program().
-keep class mint.woman.female.workout.activity.ui.plan.PlanSettings { *; }
-keep enum mint.woman.female.workout.activity.ui.plan.Plan* { *; }
