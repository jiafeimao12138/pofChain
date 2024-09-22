#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
 
int cnt;
struct itimerval timer;
void handle_signal(int sig) {
    printf("Caught signal %d\n", sig);

    __pid_t pid;
    int status;
    pid = fork();
    printf("pid=%d\n",pid);
    
    if (pid > 0)
    {
        printf("parent: %d\n",getpid());
        waitpid(pid, &status, 0);
        timer.it_value.tv_sec = 5;      // 首次触发前的延迟时间（秒）
        timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
        setitimer(ITIMER_REAL, &timer, NULL);
    }
    else if (pid == 0)
    {
        printf("child: %d\n", getpid());
        sleep(4);
        exit(0);
        
    }
    

    
}
 
int main() {
    
    signal(SIGALRM, handle_signal); // 注册信号处理函数
 
    // 设置定时器
    timer.it_value.tv_sec = 3;      // 首次触发前的延迟时间（秒）
    timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
    
 
    // 调用setitimer设置定时器
    if (setitimer(ITIMER_REAL, &timer, NULL) < 0) {
        perror("setitimer");
        return 1;
    }
 
    // 执行其他任务或阻塞等待信号
    while(1) {
        printf("%d\n", cnt ++); // 阻塞等待信号
        sleep(1);
    }
 
    return 0;
}
