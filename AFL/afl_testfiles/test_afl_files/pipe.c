/*
    #include <unistd.h>
    int pipe(int pipefd[2]);
        功能：创建一个匿名管道，用来进程间通信
        参数：int pipefd[2] ,这个数组是传出参数。 
            pipefd[0] 对应管道的读端
            pipefd[1] 对应管道的写端
        返回值：
            成功  返回0
            失败  返回-1
    注意：匿名管道只能用于具有关系的进程之间的通信（父子进程，兄弟进程）
    
    管道默认是阻塞的，如果管道中没有数据，read阻塞，如果管道满了，write阻塞。
*/
 
#include <unistd.h>
#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include<signal.h>
#include<sys/time.h>
#include<time.h>
#include<sys/types.h>
#include<sys/wait.h>

struct itimerval timer;
pid_t pid;
pid_t child_pid;

static void handle_timeout(int sig){
    printf("child_pid=%d\n", child_pid);
    kill(child_pid, SIGSTOP);
    printf("NOW STOP THE PROCESS\n");
    sleep(5);
    kill(child_pid, SIGCONT);
    timer.it_value.tv_sec = 3;      // 首次触发前的延迟时间（秒）
    timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
    setitimer(ITIMER_REAL, &timer, NULL);
    
}

//子进程发送数据给父进程，父进程读取到数据输出
int main()
{
    //需要在 fork 之前创建管道 ， 这样子进程与父进程 对应相同的管道
    int pipefd[2];
    int ret = pipe(pipefd);
    if(ret == -1)
    {
        perror("pipe");
        exit(0);
    }
    signal(SIGALRM, handle_timeout);
 
    //创建子进程
    pid = fork();
 
    if(pid>0)
    {
        timer.it_value.tv_sec = 1;      // 首次触发前的延迟时间（秒）
        timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
        setitimer(ITIMER_REAL, &timer, NULL);
        printf("I am parent process,pid:%d\n",getpid());
        //父进程

        //关闭写端
        close(pipefd[1]);
        //从管道的读取端读取数据
        char buf[30];
        while(1)
        {
            //read会阻塞，进行读取数据
            int len = read(pipefd[0],&buf,sizeof(buf));
            printf("parent recv : %s ,pid:%d \n",buf,getpid());
 
            //向管道中写数据
            //char* str = "Hello ,I am parent";
            //write(pipefd[1],str,strlen(str));
            //sleep(1);
        }
        
 
    }else if(pid == 0)
    {
        setsid();
        int status;
        // printf("I am child process,pid:%d\n",getpid());
        //子进程 关闭读端
        close(pipefd[0]);
        while(1){
            child_pid = fork();
            // printf("child of child pid = %d\n", child_pid);
            if(child_pid > 0){
            waitpid(child_pid, &status, 0);
            
            
            char* str = "Hello, I am child process.";
            // snprintf(str, 26, "Hello ,my child pid is %d", child_pid);
            // 把子进程的子进程id传到管道中
            // printf(str);
            write(pipefd[1],str,strlen(str));
            sleep(1);
 
             //read会阻塞，进行读取数据
            //char buf[100];
            //int len = read(pipefd[0],&buf,sizeof(buf));
            //printf("child recv : %s ,pid:%d \n",buf,getpid());
           
        }else if (child_pid == 0)
        {
            // printf("I am child of child process, pid=%d....\n", getpid());
            printf("child of child process......\n");
            for(int i=0; i<100; i++){
                printf("%d,",i);
            }
            printf("\n");
            sleep(3);
            for(int i=100; i<200; i++){
                printf("%d,",i);
            }
            printf("\n");
            // sleep(10);
            exit(0);
        }
        }
       
    }
 
}