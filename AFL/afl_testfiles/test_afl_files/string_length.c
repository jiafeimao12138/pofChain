#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h> 
#include <signal.h> 
#include <time.h>
#include <sys/time.h>

int AFLTest(char *str)
{
    int len = strlen(str);
    if (len<3)
    {
        printf("len=1\n");
    }
    else if (len >= 3 && len < 6)
    {
        printf("2<=len<=5\n");
    }
    else
    {
        printf("len > 5\n");
    }

    if(str[0] == 'A' && len == 6)
     {
         raise(SIGSEGV);
     }
     else
     {
         printf("it is good!\n"); 
     }
     for (int i=0; i<len; i++){
        sleep(1);       
       
     }
    return 0;
}
// void get_time_str_ms(char *time_str, int len)
// {
// 	struct timeval tv;
// 	struct timezone tz;
// 	struct tm *p;
	
// 	memset(time_str, 0, len);
	
// 	gettimeofday(&tv,&tz);
 
// 	p = localtime(&tv.tv_sec);
 
// 	sprintf(time_str,"%d-%02d-%02d %02d:%02d:%02d.%03ld",
// 		(1900+p->tm_year),(1+p->tm_mon),p->tm_mday,(p->tm_hour),p->tm_min,p->tm_sec,tv.tv_usec/1000);
// }


int main(int argc, char *argv[])
{
    
    // char str[30];
    
    char buf[100]={0};
    gets(buf);
    // get_time_str_ms(str, sizeof(str));
    // printf("%s\n", str);
    
    printf(buf);
    AFLTest(buf);
    // get_time_str_ms(str, sizeof(str));
    // printf("%s\n", str);
    return 0;
}
