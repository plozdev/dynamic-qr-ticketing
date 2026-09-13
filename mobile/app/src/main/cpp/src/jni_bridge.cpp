#include <jni.h>
#include <string>
#include "crypto_engine.h"

static ticketing::crypto::CryptoEngine g_cryptoEngine;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_generateDynamicTotpToken(
        JNIEnv *env,
        jobject,
        jstring ticketId,
        jstring secretKey,
        jlong epochSeconds,
        jint intervalSec) {
    const char *ticketIdChars = env->GetStringUTFChars(ticketId, nullptr);
    const char *secretKeyChars = env->GetStringUTFChars(secretKey, nullptr);

    std::string token = g_cryptoEngine.generateTotpToken(
        ticketIdChars, secretKeyChars, epochSeconds, intervalSec
    );

    env->ReleaseStringUTFChars(ticketId, ticketIdChars);
    env->ReleaseStringUTFChars(secretKey, secretKeyChars);

    return env->NewStringUTF(token.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_verifyDynamicTotpToken(
        JNIEnv *env,
        jobject,
        jstring ticketId,
        jstring secretKey,
        jstring token,
        jlong epochSeconds,
        jint intervalSec,
        jint allowedDriftSteps) {
    const char *ticketIdChars = env->GetStringUTFChars(ticketId, nullptr);
    const char *secretKeyChars = env->GetStringUTFChars(secretKey, nullptr);
    const char *tokenChars = env->GetStringUTFChars(token, nullptr);

    bool isValid = g_cryptoEngine.verifyTotpToken(
        ticketIdChars, secretKeyChars, tokenChars, epochSeconds, intervalSec, allowedDriftSteps
    );

    env->ReleaseStringUTFChars(ticketId, ticketIdChars);
    env->ReleaseStringUTFChars(secretKey, secretKeyChars);
    env->ReleaseStringUTFChars(token, tokenChars);

    return static_cast<jboolean>(isValid ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT jstring JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_getSecurityVersion(
        JNIEnv *env,
        jobject) {
    std::string version = g_cryptoEngine.getVersion();
    return env->NewStringUTF(version.c_str());
}

} // extern "C"
