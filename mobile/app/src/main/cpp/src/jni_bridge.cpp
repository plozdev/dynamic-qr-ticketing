#include <jni.h>
#include <string>
#include <stdexcept>
#include "crypto_engine.h"

static ticketing::crypto::CryptoEngine g_cryptoEngine;

static void throwJavaException(JNIEnv *env, const char *message) {
    jclass exClass = env->FindClass("java/lang/IllegalArgumentException");
    if (exClass != nullptr) {
        env->ThrowNew(exClass, message);
    }
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_generateDynamicTotpToken(
        JNIEnv *env,
        jobject /* this */,
        jstring ticketId,
        jstring secretKey,
        jlong epochSeconds,
        jint intervalSec) {
    if (ticketId == nullptr || secretKey == nullptr) {
        throwJavaException(env, "ticketId and secretKey must not be null");
        return nullptr;
    }

    const char *ticketIdChars = env->GetStringUTFChars(ticketId, nullptr);
    const char *secretKeyChars = env->GetStringUTFChars(secretKey, nullptr);

    if (!ticketIdChars || !secretKeyChars) {
        if (ticketIdChars) env->ReleaseStringUTFChars(ticketId, ticketIdChars);
        if (secretKeyChars) env->ReleaseStringUTFChars(secretKey, secretKeyChars);
        throwJavaException(env, "Out of memory getting UTF chars");
        return nullptr;
    }

    jstring result = nullptr;
    try {
        std::string token = g_cryptoEngine.generateTotpToken(
            ticketIdChars, secretKeyChars, epochSeconds, intervalSec
        );
        result = env->NewStringUTF(token.c_str());
    } catch (const std::exception &e) {
        throwJavaException(env, e.what());
    }

    env->ReleaseStringUTFChars(ticketId, ticketIdChars);
    env->ReleaseStringUTFChars(secretKey, secretKeyChars);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_verifyDynamicTotpToken(
        JNIEnv *env,
        jobject /* this */,
        jstring ticketId,
        jstring secretKey,
        jstring token,
        jlong epochSeconds,
        jint intervalSec,
        jint allowedDriftSteps) {
    if (ticketId == nullptr || secretKey == nullptr || token == nullptr) {
        return JNI_FALSE;
    }

    const char *ticketIdChars = env->GetStringUTFChars(ticketId, nullptr);
    const char *secretKeyChars = env->GetStringUTFChars(secretKey, nullptr);
    const char *tokenChars = env->GetStringUTFChars(token, nullptr);

    if (!ticketIdChars || !secretKeyChars || !tokenChars) {
        if (ticketIdChars) env->ReleaseStringUTFChars(ticketId, ticketIdChars);
        if (secretKeyChars) env->ReleaseStringUTFChars(secretKey, secretKeyChars);
        if (tokenChars) env->ReleaseStringUTFChars(token, tokenChars);
        return JNI_FALSE;
    }

    bool isValid = false;
    try {
        isValid = g_cryptoEngine.verifyTotpToken(
            ticketIdChars, secretKeyChars, tokenChars, epochSeconds, intervalSec, allowedDriftSteps
        );
    } catch (...) {
        isValid = false;
    }

    env->ReleaseStringUTFChars(ticketId, ticketIdChars);
    env->ReleaseStringUTFChars(secretKey, secretKeyChars);
    env->ReleaseStringUTFChars(token, tokenChars);

    return static_cast<jboolean>(isValid ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT jstring JNICALL
Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_getSecurityVersion(
        JNIEnv *env,
        jobject /* this */) {
    try {
        std::string version = g_cryptoEngine.getVersion();
        return env->NewStringUTF(version.c_str());
    } catch (...) {
        return env->NewStringUTF("unknown");
    }
}

} // extern "C"
