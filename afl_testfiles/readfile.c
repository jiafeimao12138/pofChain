#include <stdio.h>
 
int main() {
    FILE *file;
    file = fopen("files/testfile1", "rb"); // 以二进制读取模式打开文件
 
    if (file == NULL) {
        perror("Error opening file");
        return 1;
    }
 
    // 文件已成功打开，进行读取操作
 
    fclose(file); // 关闭文件
    return 0;
}