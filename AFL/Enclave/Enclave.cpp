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
#include <stdarg.h>
#include <stdio.h> /* vsnprintf */
#include <string.h>
#include <sgx_dh.h>
#include <sgx_tcrypto.h>

#define SGX_SHA_LENGTH 32


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

// An highlighted block
void enclave_increment_array(int array[5], int len) {
    for (int i = 0; i < len; i++) {
        printf("%d\n",array[i]);
    }
    for (int i = 0; i < len; i++) {
        //array[i] += 100;
        if(i==0){
           array[i]= array[i]+1;
        }else if(i==1){
           array[i]= array[i]*5;
        }else if(i==2){
           array[i]= array[i]+4; 
        }else if(i==3){
            array[i]= array[i]+3;
        }else if(i==4){
            array[i]= array[i]+10;
        }
    }
      printf("\n");
    for (int i = 0; i < len; i++) {
        printf("%d\n",array[i]);
    }
}



void printf_helloworld(){
    printf("Hello world !!!!!!!!!\n");
}

int ecall_return_res(int a, int b) {
    return a + b + 10;
}




void authenticationHash(unsigned char hash_output[33]) {
    const char input[] = "hello,sgx!";
    size_t len = strlen(input);

// sgx_status_t SGXAPI sgx_sha256_msg(const uint8_t *p_src, uint32_t src_len, sgx_sha256_hash_t *p_hash);
//    sgx_status_t status = sgx_sha256_msg((const uint8_t*)input, (uint32_t)len, hash);
   sgx_sha256_hash_t hash1;    // note: no asterisk
   int first = 1;
   sgx_sha256_msg( ( const uint8_t * ) &first, sizeof (first), &hash1 );
   for(int i=0; i<sizeof(hash1); i++){
   printf("%02x", hash1[i]);
   }
}



