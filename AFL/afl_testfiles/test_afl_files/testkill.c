#include<stdio.h>
#include<signal.h>
#include<sys/time.h>
#include<time.h>
#include<unistd.h>

struct itimerval timer;
__pid_t pid;

static void handle_timeout(int sig){
    kill(425331, SIGSTOP);
    printf("NOW STOP THE PROCESS\n");
    sleep(3);
    kill(425331, SIGCONT);
    timer.it_value.tv_sec = 3;      // 首次触发前的延迟时间（秒）
    timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
    setitimer(ITIMER_REAL, &timer, NULL);
    
}

int main(int argc, char const *argv[])
{
    // signal(SIGALRM, handle_timeout);
    
    int i=0;
    // pid = fork();

    // timer.it_value.tv_sec = 3;      // 首次触发前的延迟时间（秒）
    // timer.it_value.tv_usec = 0;     // 首次触发前的延迟时间（微秒）
    // setitimer(ITIMER_REAL, &timer, NULL);
    while (i<7)
    {
        i++;
        printf("%d\n",i);
        sleep(1);
    }
    
    // return 0;
    kill(425331, SIGKILL);
}
