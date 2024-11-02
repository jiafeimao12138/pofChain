#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/times.h>
#include <sys/resource.h>
#include <aio.h>
#include <sys/wait.h>
#include<unistd.h>
#include<stdio.h>
#include<sys/types.h>
#include<fcntl.h>
 
FILE *file;
struct rlimit rlim;

void handle_sigxcpu(int sig, siginfo_t *info, void *context) {
    // struct tms tms;
    // times(&tms);
    // printf("Caught SIGXCPU. CPU time used: %ld\n", tms.tms_utime);
    // 可以在此处添加其他逻辑，例如保存日志，终止进程等
    // printf("func: %d\n", getpid());
    // kill(getpid(),SIGSTOP);
    printf("do something....\n");
    // kill(getpid(),SIGCONT);
    // exit(0); // 正常退出进程
    // pid_t pid;
    
    // pid = fork();
    // int status = -1;
    rlim.rlim_cur = 4;
    rlim.rlim_max = RLIM_INFINITY;
    setrlimit(RLIMIT_CPU, &rlim);
    
    // if(pid > 0){
    //     printf("parent: %d\n", getpid());
    //     wait(&status);
    //     printf("parent continue....\n");
    // }else if (pid == 0)
    // {
        // setrlimit(RLIMIT_CPU, &rlim);
    //     printf("child: %d\n", getpid());
    //     //处理事务
    //     printf("do something...\n");
    //     // int fd = open("file.txt", O_RDWR);
    //     // if (ftruncate(fd, 0) == -1)
    //     // {
    //     //     perror("ftruncate error");
    //     // }
        
        
    //     exit(0);
        
    // }
}
 

int main() {
    int count = 0;
    int fd = open("file.txt", O_RDWR | O_CREAT, 0644);
    
    rlim.rlim_cur = 4;
    rlim.rlim_max = RLIM_INFINITY;
    setrlimit(RLIMIT_CPU, &rlim);

    struct sigaction sa;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = handle_sigxcpu;
    
    if (sigaction(SIGXCPU, &sa, NULL) < 0) {
        perror("sigaction failed");
        return EXIT_FAILURE;
    }
    
    // 模拟耗时操作
    while(1){
        // write(fd, "hello", 5);
        // printf("%d\n",count);
        // count ++;
    }
    
 
    return 0;
}