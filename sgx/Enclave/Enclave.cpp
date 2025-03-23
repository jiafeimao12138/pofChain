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

#include "Enclave.h"
#include "Enclave_t.h" /* print_string */
#include "sgx_trts.h"
#include <stdarg.h>
#include <stdio.h> /* vsnprintf */
#include <string.h>

/* 
 * printf: 
 *   Invokes OCALL to display the enclave buffer to the terminal.
 */
int printf(const char* fmt, ...)
{
    char buf[BUFSIZ] = { '\0' };
    va_list ap;
    va_start(ap, fmt);
    vsnprintf(buf, BUFSIZ, fmt, ap);
    va_end(ap);
    ocall_print_string(buf);
    return (int)strnlen(buf, BUFSIZ - 1) + 1;
}

void ecall_process_data() {
    printf("sgx receive\n");
}


void start_fuzzing_timer() {

    uint64_t start_time, current_time;
    int afl_pid = -1;
    int target_pid = -1;
    int fuzz_worker_pid = -1;
    int isEqual = 0;

    printf("开始执行start_fuzzing_timer\n");


    while(1) {
       ocall_get_afl_pid(0, &afl_pid);
       // 获取 AFL 进程 ID
        if (afl_pid > 0) {
            break;
        }
    }

    ocall_get_target_pid(0, afl_pid, &target_pid);
    printf("target_pid=%d\n", target_pid);

    // 获取当前时间
    ocall_get_time(&start_time);

    // 窗口时间 1s
    uint64_t max_time = 1;

    printf("afl进程：%d\n", afl_pid);
    printf("forkserver进程：%d\n", target_pid);

    while (1) {
        ocall_get_time(&current_time);

        if (current_time - start_time >= max_time) {
            printf("时间到，暂停 Fuzzing 进程\n");
            printf("=========current=%d\n", current_time);
            
            // 获取 `fuzz_worker` 进程 ID
            ocall_get_fuzz_worker_pid(0, target_pid, &fuzz_worker_pid);
            printf("target program进程：%d\n", fuzz_worker_pid);

             ocall_pause_fuzzing(fuzz_worker_pid, target_pid);  // 暂停 fuzz_worker
             // 通知java程序计算hash
             ocall_notify_java();
             // 监控共享内存
             ocall_check_java(0, &isEqual);
             // 共享内存可存hash值，说明挖矿成功，则签名,签名完写入共享文件，立即恢复afl

             if(isEqual) {
                printf("继续afl\n");
                ocall_resume_fuzzing(fuzz_worker_pid, target_pid);
             }
             // 等待java程序计算完毕，恢复

            ocall_get_time(&start_time);
            
        }
    }
}
