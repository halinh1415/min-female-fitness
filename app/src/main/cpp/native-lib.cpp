#include <jni.h>
#include <string>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "security", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "security", __VA_ARGS__))
#define XOR_KEY 0xAB


static const char *SIGN_DEBUG = "308202a730820265a003020102020450057c42300b06072a8648ce38040305003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567301e170d3132303731373134353235305a170d3232303731353134353235305a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567308201b73082012c06072a8648ce3804013082011f02818100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a038184000281806ad11bd7d566d27af439c02e4168acfd45b4be85bc998c7b9b8e1c7754693f8c0d428aa4fce110848138424fa68cd130524eeff6f1373863822fa637298bfe4d46a0b86665eef041173901035b1c806aa318180d303aa8cc9e5923e06a6fabfa75683c453bb207777cf2fde7cfb19b14303814aa1df7b43d5b222b5706b48b94300b06072a8648ce3804030500032f00302c021409d2d1b0470229b5bed2902661d112f270c5e61d021467500206a78050ba78aec7174f16047f84eaa2f7";
static const char *SIGN = "308203833082026ba00302010202040da09a0a300d06092a864886f70d01010b05003071310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b31193017060355040a13104d696e204669746e657373205465616d310b3009060355040b13024e59311430120603550403130b4d696e204669746e6573733020170d3231303730363130303035375a180f32303731303632343130303035375a3071310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b31193017060355040a13104d696e204669746e657373205465616d310b3009060355040b13024e59311430120603550403130b4d696e204669746e65737330820122300d06092a864886f70d01010105000382010f003082010a0282010100a3266d5fbef5558d23d568a6ae7998dd4751679ba1658f9df8bbfb6d5b3800ac8d872885fbb7a8ad557c6fabd7b7d0158781a3effc7533a5e87c3bce15d00c057e49092944bab26aca1a06774ef969de83530244b37691e35f3366f506113198dc9743bf4e8bcc1596301dc9575f0fd18881fbe4d69920d6536188fb991ad21a26ede4fd279e58b68032c6a735c25984424881138e8588c0d00101c18820930dbcdc661702b56ea40a12cb6acce6e345ca7151c6f560f01e5c268e9f75f5dfd762ee6de409fd64e29cb1993e6a683f44269ddba76c781280483e14cba6e9058e65ce46fc6dc430a9183b68f5118a2ae94830f3c48702b9d7d68b2fd21fbd76950203010001a321301f301d0603551d0e04160414f7327c481db94ee6b853c6738f44c418c7c12b1c300d06092a864886f70d01010b050003820101006d364c7deff20bc6660918ccaefa4655de2cf990a4c1ce3ea4cfcacbc15a4eaa049283e67052b831e38aea46a35c67725e7e68e7899bcaf8512672655396c34dfcbff606cb904232bce0ae9056238b57f6d601a6d2adfbeaf3a972b9123037d36222467e71e136f433d13ac1575ac3656948b2be17d0810cf3cf455ae605eb224cdcf19a11b3cf5f9eebb3866994819e240e5ef82b011cfb89984a282653d67524a3c45e4a74738b9f5d92db557e8a8506f69ea6d8f29dcc19277f92abcd53509d58be90cec378335f5335e0fc5740e37abc7b0f3cb056effb32cd1643f5e3adea1eabb6839aa86959c39551e33cb13245d5257da54012ba9082ef165623f67f";

static const char *ERROR_CODE = "dm thang ngu";
extern "C" JNIEXPORT jstring JNICALL
Java_weightloss_loseweight_female_workout_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    __android_log_print(ANDROID_LOG_DEBUG, "MyLib", "ECB decrypt: ");
    return env->NewStringUTF(hello.c_str());
}


jobject getApp(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = env->FindClass("android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID currentApplication = env->GetStaticMethodID(
                activity_thread_clz, "currentApplication", "()Landroid/app/Application;");
        if (currentApplication != NULL) {
            application = env->CallStaticObjectMethod(activity_thread_clz, currentApplication);
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, "HAHA",
                                "Cannot find method: currentApplication() in ActivityThread.");
        }
        env->DeleteLocalRef(activity_thread_clz);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "HAHA",
                            "Cannot find class: android.app.ActivityThread");
    }

    return application;
}

int vfF101211(JNIEnv *env) {
    // Application object
    jobject application = getApp(env);
    if (application == NULL) {
        return JNI_ERR;
    }
    // Context(ContextWrapper) class
    jclass context_clz = env->GetObjectClass(application);
    // getPackageManager()
    jmethodID getPackageManager = env->GetMethodID(context_clz, "getPackageManager",
                                                   "()Landroid/content/pm/PackageManager;");
    // android.content.pm.PackageManager object
    jobject package_manager = env->CallObjectMethod(application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = env->GetObjectClass(package_manager);
    // getPackageInfo()
    jmethodID getPackageInfo = env->GetMethodID(package_manager_clz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = env->GetMethodID(context_clz, "getPackageName",
                                                "()Ljava/lang/String;");
    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) (env->CallObjectMethod(application, getPackageName));
    // PackageInfo object
    jobject package_info = env->CallObjectMethod(package_manager, getPackageInfo, package_name, 64);
    // class PackageInfo
    jclass package_info_clz = env->GetObjectClass(package_info);
    // field signatures
    jfieldID signatures_field = env->GetFieldID(package_info_clz, "signatures",
                                                "[Landroid/content/pm/Signature;");
    jobject signatures = env->GetObjectField(package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = env->GetObjectArrayElement(signatures_array, 0);
    jclass signature_clz = env->GetObjectClass(signature0);

    jmethodID toCharsString = env->GetMethodID(signature_clz, "toCharsString",
                                               "()Ljava/lang/String;");
    // call toCharsString()
    jstring signature_str = (jstring) (env->CallObjectMethod(signature0, toCharsString));

    // release
    env->DeleteLocalRef(application);
    env->DeleteLocalRef(context_clz);
    env->DeleteLocalRef(package_manager);
    env->DeleteLocalRef(package_manager_clz);
    env->DeleteLocalRef(package_name);
    env->DeleteLocalRef(package_info);
    env->DeleteLocalRef(package_info_clz);
    env->DeleteLocalRef(signatures);
    env->DeleteLocalRef(signature0);
    env->DeleteLocalRef(signature_clz);

    const char *sign = env->GetStringUTFChars(signature_str, NULL);
    if (sign == NULL) {
        LOGE("Failed to allocate memory");
        return JNI_ERR;
    }

    // LOGI("The signature read in the application is %s", sign);
    // LOGI("native The preset signature is：%s", SIGN);
    int result = strcmp(sign, SIGN);
    int result2 = strcmp(sign,SIGN_DEBUG);
    LOGE("HAHA: String value: %s\n", sign);
    // 使用之后要释放这段内存
    env->ReleaseStringUTFChars(signature_str, sign);
    env->DeleteLocalRef(signature_str);
    if (result == 0 || result2 ==0) {
        return JNI_OK;
    }


    return JNI_ERR;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("com/minfitness/decode/Decoder");

    if (c == nullptr) return JNI_ERR;
//    if (vfF101211(env) == JNI_OK) {
//        __android_log_print(ANDROID_LOG_DEBUG, "HAHA", "workout lib SUCCESS!\n");
//        return JNI_VERSION_1_6;
//    }
    __android_log_print(ANDROID_LOG_DEBUG, "HAHA", "workout lib  ERROR!\n");
    // Register your class' native methods.
//    static const JNINativeMethod methods[] = {
//            {"nativeFoo", "()V", reinterpret_cast<void*>(nativeFoo)},
//            //{"nativeBar", "(Ljava/lang/String;I)Z", reinterpret_cast<void*>(nativeBar)},
//    };
//    int rc = env->RegisterNatives(c, methods, sizeof(methods)/sizeof(JNINativeMethod));
//    if (rc != JNI_OK) {
//        __android_log_print(ANDROID_LOG_DEBUG, "HAHA", "fail!\n");
//        return rc;
//    }
    return JNI_VERSION_1_6;
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_minfitness_decode_Decoder_encryptFromAssets(JNIEnv *env,
                                                     jclass clazz,
                                                     jobject assetManager, jstring filename) {
    jboolean isCopy;
    // Convert Java filename to C string
    const char *c_filename = (*env).GetStringUTFChars(filename, &isCopy);
    if (c_filename == NULL) {
        return NULL; // Error handling
    }

    // Open asset file
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    AAsset *asset = AAssetManager_open(mgr, c_filename, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        env->ReleaseStringUTFChars(filename, c_filename);
        return NULL; // Error handling
    }

    // Read file contents into memory
    off_t fileSize = AAsset_getLength(asset);
    unsigned char *fileData = (unsigned char *) malloc(fileSize);
    AAsset_read(asset, fileData, fileSize);

    char table[256];
    for (int i = 0; i < 256; i++) {
        table[i] = (char) ((i + 13) % 256); // Example: rotate by 13
    }


    for (int i = 0; i < fileSize; ++i) {
        //dataBytes[i] =dataBytes[i] -1;
        fileData[i] = (jbyte) table[(unsigned char) fileData[i]];
        fileData[i] ^= XOR_KEY; // Simple XOR decryption
    }


    // Close asset file
    AAsset_close(asset);
    env->ReleaseStringUTFChars(filename, c_filename);

    // Convert encrypted data to Java byte array
    jbyteArray encryptedData = env->NewByteArray(fileSize);
    env->SetByteArrayRegion(encryptedData, 0, fileSize, (jbyte *) fileData);

    // Free allocated memory
    free(fileData);

    return encryptedData;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_minfitness_decode_Decoder_decryptFromAssets(JNIEnv *env, jclass thiz,
                                                     jobject assetManager, jstring filename) {
    // Convert Java filename to C string
    jboolean isCopy;
    const char *c_filename = env->GetStringUTFChars(filename, &isCopy);
    if (c_filename == NULL) {
        return NULL; // Error handling
    }

//    if (vfF101211(env) != JNI_OK) {
//        return NULL;
//    }
    // Open asset file
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    AAsset *asset = AAssetManager_open(mgr, c_filename, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        env->ReleaseStringUTFChars(filename, c_filename);
        return NULL; // Error handling
    }

    // Read file contents into memory
    off_t fileSize = AAsset_getLength(asset);
    unsigned char *fileData = (unsigned char *) malloc(fileSize);
    AAsset_read(asset, fileData, fileSize);
    char table[256];
    for (int i = 0; i < 256; i++) {
        table[i] = (char) ((i - 13) % 256); // Example: rotate by 13
    }
    // Decrypt file data
    for (int i = 0; i < fileSize; i++) {
        fileData[i] = (jbyte) table[(unsigned char) fileData[i]];
        fileData[i] ^= XOR_KEY; // Simple XOR decryption
    }

    // Close asset file
    AAsset_close(asset);
    env->ReleaseStringUTFChars(filename, c_filename);

    // Convert decrypted data to Java byte array
    jbyteArray decryptedData = env->NewByteArray(fileSize);
    env->SetByteArrayRegion(decryptedData, 0, fileSize, (jbyte *) fileData);

    // Free allocated memory
    free(fileData);


    return decryptedData;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_minfitness_decode_Decoder_decrypt(JNIEnv *env, jclass clazz, jbyteArray data) {


//    if (vfF101211(env) != JNI_OK) {
//        int str_length = strlen(ERROR_CODE);
//        jbyteArray result = env->NewByteArray(str_length);
//        env->SetByteArrayRegion(result, 0, str_length, (jbyte *) ERROR_CODE);
//        return result;
//    }
    jsize length = env->GetArrayLength(data);
    jbyte *dataBytes = env->GetByteArrayElements(data, 0);
    jbyteArray result = env->NewByteArray(length);
    char table[256];
    for (int i = 0; i < 256; i++) {
        table[i] = (char) ((i - 13 + 256) % 256); // Example: reverse rotation by 13
    }
    jbyte *decryptedBytes = env->GetByteArrayElements(result, 0);
    for (int i = 0; i < length; i++) {
        decryptedBytes[i] = (jbyte) table[(unsigned char) dataBytes[i]];
        decryptedBytes[i] ^= XOR_KEY; // Simple XOR decryption
    }


    env->ReleaseByteArrayElements(data, dataBytes, 0);
    env->ReleaseByteArrayElements(result, decryptedBytes, 0);
    return result;

}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_minfitness_decode_Decoder_encrypt(JNIEnv *env, jclass clazz, jbyteArray data) {
//    if (vfF101211(env) != JNI_OK) {
//        return NULL; // don't need in debug
//    }

    jsize length = env->GetArrayLength(data);
    jbyte *dataBytes = env->GetByteArrayElements(data, 0);
    jbyteArray result = env->NewByteArray(length);

    jbyte *encryptedBytes = env->GetByteArrayElements(result, 0);
    char table[256];
    for (int i = 0; i < 256; i++) {
        table[i] = (char) ((i + 13) % 256); // Example: rotate by 13
    }


    for (int i = 0; i < length; ++i) {
        //dataBytes[i] =dataBytes[i] -1;
        dataBytes[i] ^= XOR_KEY; // Simple XOR decryption
        encryptedBytes[i] = (jbyte) table[(unsigned char) dataBytes[i]];

    }

    env->ReleaseByteArrayElements(data, dataBytes, 0);
    env->ReleaseByteArrayElements(result, encryptedBytes, 0);
    return result;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_minfitness_decode_Decoder_getAllData(JNIEnv *env, jclass clazz, jobject asset_manager,
                                              jstring file_name) {
    jboolean isCopy;
    const char *c_filename = env->GetStringUTFChars(file_name, &isCopy);
    if (c_filename == NULL) {
        return env->NewStringUTF(""); // Error handling
    }

    if (vfF101211(env) != JNI_OK) {
        return env->NewStringUTF("hack cai lon");
    }
    // Open asset file
    AAssetManager *mgr = AAssetManager_fromJava(env, asset_manager);
    AAsset *asset = AAssetManager_open(mgr, c_filename, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        env->ReleaseStringUTFChars(file_name, c_filename);
        return NULL; // Error handling
    }

    // Read file contents into memory
    off_t fileSize = AAsset_getLength(asset);
    unsigned char *fileData = (unsigned char *) malloc(fileSize);

    AAsset_read(asset, fileData, fileSize);
    char table[256];
    for (int i = 0; i < 256; i++) {
        table[i] = (char) ((i - 13) % 256); // Example: rotate by 13
    }
//    jbyteArray result = env->NewByteArray(fileSize);
//
//    jbyte *encryptedBytes = env->GetByteArrayElements(result, 0);
    // Decrypt file data
    for (int i = 0; i < fileSize; i++) {
        //  fileData[i] ^= XOR_KEY;
        fileData[i] = (jbyte) table[(unsigned char) fileData[i]];
        fileData[i] ^= XOR_KEY; // Simple XOR decryption

    }

    // Close asset file
    AAsset_close(asset);
    env->ReleaseStringUTFChars(file_name, c_filename);

    // Convert decrypted data to Java byte array
    jbyteArray decryptedData = env->NewByteArray(fileSize);
    env->SetByteArrayRegion(decryptedData, 0, fileSize, (jbyte *) fileData);
    // env->ReleaseByteArrayElements(result, encryptedBytes, 0);
    jstring encrypted_content = env->NewStringUTF((char *) fileData);
    // Free allocated memory
    free(fileData);
    return encrypted_content;

}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_minfitness_decode_Decoder_getKey(JNIEnv *env, jclass clazz) {
    // Application object
    jobject application = getApp(env);
    if (application == NULL) {
        return NULL;
    }
    // Context(ContextWrapper) class
    jclass context_clz = env->GetObjectClass(application);
    // getPackageManager()
    jmethodID getPackageManager = env->GetMethodID(context_clz, "getPackageManager",
                                                   "()Landroid/content/pm/PackageManager;");
    // android.content.pm.PackageManager object
    jobject package_manager = env->CallObjectMethod(application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = env->GetObjectClass(package_manager);
    // getPackageInfo()
    jmethodID getPackageInfo = env->GetMethodID(package_manager_clz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = env->GetMethodID(context_clz, "getPackageName",
                                                "()Ljava/lang/String;");
    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) (env->CallObjectMethod(application, getPackageName));
    // PackageInfo object
    jobject package_info = env->CallObjectMethod(package_manager, getPackageInfo, package_name, 64);
    // class PackageInfo
    jclass package_info_clz = env->GetObjectClass(package_info);
    // field signatures
    jfieldID signatures_field = env->GetFieldID(package_info_clz, "signatures",
                                                "[Landroid/content/pm/Signature;");
    jobject signatures = env->GetObjectField(package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = env->GetObjectArrayElement(signatures_array, 0);
    jclass signature_clz = env->GetObjectClass(signature0);

    jmethodID toCharsString = env->GetMethodID(signature_clz, "toCharsString",
                                               "()Ljava/lang/String;");
    // call toCharsString()
    jstring signature_str = (jstring) (env->CallObjectMethod(signature0, toCharsString));

    // release
    env->DeleteLocalRef(application);
    env->DeleteLocalRef(context_clz);
    env->DeleteLocalRef(package_manager);
    env->DeleteLocalRef(package_manager_clz);
    env->DeleteLocalRef(package_name);
    env->DeleteLocalRef(package_info);
    env->DeleteLocalRef(package_info_clz);
    env->DeleteLocalRef(signatures);
    env->DeleteLocalRef(signature0);
    env->DeleteLocalRef(signature_clz);

    return signature_str;
}