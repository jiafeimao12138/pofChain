#include<stdio.h>

int main(int argc, char const *argv[])
{
    int i = 0;
    while (1)
    {
        printf("%d\n", i++);
        sleep(1);
    }
    
    return 0;
}
