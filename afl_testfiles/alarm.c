#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <sys/wait.h>
 
int cnt = 0;
void handle_sigalrm(int sig) {
    printf("Catch signal %d\n", sig);
    __pid_t pid;
    pid = fork();
    if (pid > 0)
    {
        int status;
        waitpid(pid, &status, 0);
    }else if (pid == 0)
    {
        printf("do something\n");
        sleep(3);
    }
    
    // 重新设置定时器
    alarm(3);
}
 
int main() {
    // 设置SIGALRM信号处理函数
    if (signal(SIGALRM, handle_sigalrm) == SIG_ERR) {
        perror("signal");
        exit(EXIT_FAILURE);
    }
 
    // 首次触发定时器
    alarm(3);
 
    // 循环等待信号
    while(1) {
        // pause(); // 暂停进程直到信号到来
        printf("%d\n",cnt++);
    }
 
    return 0;
}