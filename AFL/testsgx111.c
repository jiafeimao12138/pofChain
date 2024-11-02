#include <stdio.h>
#include <stdlib.h>
#include <openssl/sha.h>

#define SHA_DIGEST_LENGTH 32

void computeopensslhash(){
   unsigned const char ibuf[] = "compute sha1";
    unsigned char obuf[20];
    printf("%d\n", strlen((const char * )ibuf));

    SHA1(ibuf, strlen((const char * )ibuf), obuf);
    int i;

    for(i = 0; i < 20; i++)
    {
        printf("%02x ", obuf[i]);

    }    
    printf("\n");

}



int main(int argc, char *argv[]) {

    computeopensslhash();
    const char *app = "./app ";
    

    // 使用 popen 打开管道以读取输出
    FILE *fp = popen(app, "r");
    if (fp == NULL) {
        perror("popen() failed");
        return 1;
    }

    char buffer[256];
    // 读取子程序的输出
    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
        printf("Output: %s\n", buffer);
    }

    // 关闭管道
    int status = pclose(fp);
    if (status == -1) {
        perror("pclose() failed");
        return 1;
    }

    // 检查子程序的返回状态
    if (WIFEXITED(status)) {
        int exit_status = WEXITSTATUS(status);
        printf("Child program exited with status: %d\n", exit_status);
    }
    return 0;
}