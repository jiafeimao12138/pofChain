#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "Enclave_u.h"  // SGX Enclave 头文件
#include "sgx_urts.h"   // SGX 运行时头文件
#include "com_example_net_conf_EnclaveInterface.h"

sgx_enclave_id_t enclave_id; // Enclave ID

// 初始化 Enclave
void initialize_enclave() {
    sgx_status_t ret = sgx_create_enclave("enclave.signed.so", SGX_DEBUG_FLAG, NULL, NULL, &enclave_id, NULL);
    printf("enclave initilaize\n");
    if (enclave_id == 0) {
        printf("Enclave not initialized.\n");
    }
    if (ret != SGX_SUCCESS) {
        printf("Failed to initialize Enclave.\n");
        return;
    }
}

// JNI 方法 - Java 调用 SGX Enclave
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_net_conf_EnclaveInterface_callEnclave(JNIEnv *env, jobject obj, jstring input) {
    initialize_enclave();
    const char *nativeInput = env->GetStringUTFChars(input, 0);
    char output[256] = {0};
    char* modifiedInput = strdup(nativeInput);

    // 调用 Enclave 里的 ecall 方法
    sgx_status_t status = ecall_process_data(enclave_id);
    env->ReleaseStringUTFChars(input, nativeInput);

    if (status != SGX_SUCCESS) {
        return env->NewStringUTF("Enclave Error");
    }
    free(input);
    return env->NewStringUTF(output);
}

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_net_conf_EnclaveInterface_callEnclave(JNIEnv *env, jobject obj, jstring input) {
//    return input;
//}
