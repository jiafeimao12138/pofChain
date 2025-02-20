/*
 * Copyright (C) 2011-2021 Intel Corporation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Intel Corporation nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <time.h>
#include <signal.h>
#include <jni.h>
#include <sys/mman.h>
#include <fcntl.h>

#include <unistd.h>
#include <pwd.h>
#define MAX_PATH FILENAME_MAX
#define SHM_SIZE 128

#include "sgx_urts.h"
#include "App.h"
#include "Enclave_u.h"

/* Global EID shared by multiple threads */
sgx_enclave_id_t global_eid = 0;
char *shm_ptr = NULL;

typedef struct _sgx_errlist_t {
    sgx_status_t err;
    const char *msg;
    const char *sug; /* Suggestion */
} sgx_errlist_t;

/* Error code returned by sgx_create_enclave */
static sgx_errlist_t sgx_errlist[] = {
    {
        SGX_ERROR_UNEXPECTED,
        "Unexpected error occurred.",
        NULL
    },
    {
        SGX_ERROR_INVALID_PARAMETER,
        "Invalid parameter.",
        NULL
    },
    {
        SGX_ERROR_OUT_OF_MEMORY,
        "Out of memory.",
        NULL
    },
    {
        SGX_ERROR_ENCLAVE_LOST,
        "Power transition occurred.",
        "Please refer to the sample \"PowerTransition\" for details."
    },
    {
        SGX_ERROR_INVALID_ENCLAVE,
        "Invalid enclave image.",
        NULL
    },
    {
        SGX_ERROR_INVALID_ENCLAVE_ID,
        "Invalid enclave identification.",
        NULL
    },
    {
        SGX_ERROR_INVALID_SIGNATURE,
        "Invalid enclave signature.",
        NULL
    },
    {
        SGX_ERROR_OUT_OF_EPC,
        "Out of EPC memory.",
        NULL
    },
    {
        SGX_ERROR_NO_DEVICE,
        "Invalid SGX device.",
        "Please make sure SGX module is enabled in the BIOS, and install SGX driver afterwards."
    },
    {
        SGX_ERROR_MEMORY_MAP_CONFLICT,
        "Memory map conflicted.",
        NULL
    },
    {
        SGX_ERROR_INVALID_METADATA,
        "Invalid enclave metadata.",
        NULL
    },
    {
        SGX_ERROR_DEVICE_BUSY,
        "SGX device was busy.",
        NULL
    },
    {
        SGX_ERROR_INVALID_VERSION,
        "Enclave version was invalid.",
        NULL
    },
    {
        SGX_ERROR_INVALID_ATTRIBUTE,
        "Enclave was not authorized.",
        NULL
    },
    {
        SGX_ERROR_ENCLAVE_FILE_ACCESS,
        "Can't open enclave file.",
        NULL
    },
    {
        SGX_ERROR_MEMORY_MAP_FAILURE,
        "Failed to reserve memory for the enclave.",
        NULL
    },
};

/* Check error conditions for loading enclave */
void print_error_message(sgx_status_t ret)
{
    size_t idx = 0;
    size_t ttl = sizeof sgx_errlist/sizeof sgx_errlist[0];

    for (idx = 0; idx < ttl; idx++) {
        if(ret == sgx_errlist[idx].err) {
            if(NULL != sgx_errlist[idx].sug)
                printf("Info: %s\n", sgx_errlist[idx].sug);
            printf("Error: %s\n", sgx_errlist[idx].msg);
            break;
        }
    }
    
    if (idx == ttl)
    	printf("Error code is 0x%X. Please refer to the \"Intel SGX SDK Developer Reference\" for more details.\n", ret);
}

/* Initialize the enclave:
 *   Call sgx_create_enclave to initialize an enclave instance
 */
int initialize_enclave(void)
{
    sgx_status_t ret = SGX_ERROR_UNEXPECTED;
    
    /* Call sgx_create_enclave to initialize an enclave instance */
    /* Debug Support: set 2nd parameter to 1 */
    ret = sgx_create_enclave(ENCLAVE_FILENAME, SGX_DEBUG_FLAG, NULL, NULL, &global_eid, NULL);
    if (ret != SGX_SUCCESS) {
        print_error_message(ret);
        return -1;
    }

    return 0;
}

/* OCall functions */
void ocall_print_string(const char *str)
{
    /* Proxy/Bridge will check the length and null-terminate 
     * the input string to prevent buffer overflow. 
     */
    printf("%s", str);
}

// 获取 AFL 进程 ID
int ocall_get_afl_pid(int* pid) {
    FILE* pipe = popen("pgrep -o afl-fuzz", "r");
    if (!pipe) {
        printf("pipe=%d\n", 0);
        return 0;
    }

    char buffer[16];
    if (fgets(buffer, sizeof(buffer), pipe) != NULL) {
        *pid = atoi(buffer);
        pclose(pipe);
        return 1;
    } else{
        pclose(pipe);
        return 0;
    }
    
}

// 获取 `forkserver` 进程 ID
int ocall_get_target_pid(int afl_pid, int* target_pid) {
    char command[64];
    snprintf(command, sizeof(command), "pgrep -P %d", afl_pid);

    FILE* pipe = popen(command, "r");
    if (!pipe) return 0;

    char buffer[16];
    if (fgets(buffer, sizeof(buffer), pipe) != NULL) {
        *target_pid = atoi(buffer);
        pclose(pipe);
        return 1;
    }

    pclose(pipe);
    return 0;
}

// 获取 `target_program` 进程 ID
int ocall_get_fuzz_worker_pid(int target_pid, int* fuzz_worker_pid) {
    char command[64];
    snprintf(command, sizeof(command), "pgrep -P %d", target_pid);

    FILE* pipe = popen(command, "r");
    if (!pipe) return 0;

    char buffer[16];
    if (fgets(buffer, sizeof(buffer), pipe) != NULL) {
        *fuzz_worker_pid = atoi(buffer);
        pclose(pipe);
        return 1;
    }

    pclose(pipe);
    return 0;
}

// 暂停 target_program 进程
void ocall_pause_fuzzing(int target_pid, int forkserver_pid) {
    if (target_pid > 0) {
        kill(target_pid, SIGSTOP);
//        printf("暂停 Fuzzing 进程: %d\n", target_pid);
    } else {
        kill(forkserver_pid, SIGSTOP);
//        printf("暂停 forkserver 进程: %d\n", forkserver_pid);
    }
}

// 恢复 target_program 进程
void ocall_resume_fuzzing(int pid, int forkserver_pid) {
    if (pid > 0) {
        kill(pid, SIGCONT);
        printf("恢复 Fuzzing 进程: %d\n", pid);
    } else {
        kill(forkserver_pid, SIGCONT);
        printf("恢复 forkserver 进程: %d\n", pid);
    }
}

void ocall_get_time(uint64_t* time) {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    *time = ts.tv_sec;  // 返回当前时间（秒）
}

/**
 * 检查是否有 afl-fuzz 进程。如果 AFL 进程在运行，返回 1；否则返回 0。
 */
int ocall_check_afl() {
    FILE* pipe = popen("ps aux | grep afl-fuzz | grep -v grep", "r");
    if (!pipe) return 0;

    char buffer[128];
    int found = 0;

    while (fgets(buffer, sizeof(buffer), pipe) != NULL) {
        if (strstr(buffer, "afl-fuzz")) {
            found = 1;  // AFL 在运行
            break;
        }
    }

    pclose(pipe);
    return found;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_net_conf_EnclaveInterface_callEnclave(JNIEnv *env, jobject obj, jstring input) {
    const char *nativeInput = env->GetStringUTFChars(input, 0);
    char output[256] = {0};
    char* modifiedInput = strdup(nativeInput);

    // 调用 Enclave 里的 ecall 方法
    sgx_status_t status = ecall_process_data(global_eid);
    env->ReleaseStringUTFChars(input, nativeInput);

    if (status != SGX_SUCCESS) {
        return env->NewStringUTF("Enclave Error");
    }
    free(input);
    return env->NewStringUTF(output);
}

void ocall_notify_java() {
    // 写入信号文件，通知 Java 开始执行
    FILE *file = fopen("/home/wj/dockerAFLdemo/pofChain/start_java_signal.txt", "w");
    if (file != NULL) {
        fprintf(file, "start_java\n");
        fclose(file);
    }
}

int ocall_check_java(int* isEqual) {
    char buffer[16];

    while (true) {
        FILE *file = fopen("/home/wj/dockerAFLdemo/pofChain/start_java_signal.txt", "r");
        if (!file) {
            perror("Failed to open file");
            return 0; // 文件打开失败
        }

        // 读取文件内容
        if (fgets(buffer, sizeof(buffer), file) != NULL) {
            // 去掉换行符
            buffer[strcspn(buffer, "\n")] = 0;
            // 检查内容是否为 "resume"
            if (strcmp(buffer, "resume") == 0) {
                printf("equal\n");
                *isEqual = 1;
                fclose(file);
                return 1; // 内容匹配
            }
        }

        fclose(file);
    }
}

// 创建共享内存
void create_shared_memory(char **shm_ptr) {
    int shm_fd = shm_open("/my_shm5", O_CREAT | O_RDWR, 0666);
    ftruncate(shm_fd, SHM_SIZE);
    *shm_ptr = (char*)mmap(NULL, SHM_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    memset(*shm_ptr, 0, SHM_SIZE); // 初始化共享内存
}

// 写入共享内存
void ocall_write_shm() {
    const char *start_msg = "start_java";
    memcpy(shm_ptr, start_msg, strlen(start_msg) + 1);
}

/* Application entry */
int SGX_CDECL main(int argc, char *argv[])
{
    (void)(argc);
    (void)(argv);

    /* Initialize the enclave */
    if(initialize_enclave() < 0){
        printf("Enter a character before exit ...\n");
        getchar();
        return -1; 
    }
 
    /* Utilize edger8r attributes */
    edger8r_array_attributes();
    edger8r_pointer_attributes();
    edger8r_type_attributes();
    edger8r_function_attributes();
    
    /* Utilize trusted libraries */
    ecall_libc_functions();
    ecall_libcxx_functions();
    ecall_thread_functions();

    // 创建共享内存

//    create_shared_memory(&shm_ptr);
//    const char *start_msg = "start_javaewewq";
//    ocall_write_shm(start_msg);
//    printf("shm_ptr=%s\n", shm_ptr);
//    ocall_write_shm(start_msg);

    start_fuzzing_timer(global_eid);

    /* Destroy the enclave */
    sgx_destroy_enclave(global_eid);
    
    // printf("Info: SampleEnclave successfully returned.\n");

    // printf("Enter a character before exit ...\n");
    // getchar();
    
    return 0;
}

