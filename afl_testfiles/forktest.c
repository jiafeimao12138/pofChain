#include<stdio.h>
#include<unistd.h>

void hello(){
    __pid_t pid;
    pid = fork();
    if (pid == 0)     //子进程
    {
        
        printf("child: %d\n",getpid());
        while (1)
        {
            // printf("child process\n");
            // sleep(1);
        }
        
    }
    printf("common: %d\n", getpid());
}

void bye(){
    int a = 9;
    printf("bye,%d\n",a);
}
int main(int argc, char const *argv[])
{
    
    hello();
    printf("after\n");
    bye();
    return 0;
}
