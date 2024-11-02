#include <stdio.h>

#include <openssl/sha.h>


void compute_sha256_hash(const char *str, unsigned char *outputBuffer) {


    SHA256_CTX sha256;


    SHA256_Init(&sha256);


    SHA256_Update(&sha256, str, strlen(str));


    SHA256_Final(outputBuffer, &sha256);


}                                                          


void print_hash(unsigned char *hash, size_t length) {


    for (size_t i = 0; i < length; i++) {


        printf("%02x", hash[i]);


    }


    printf("n");


}


/* SHA Hashing functions
* Parameters:
*   Return: sgx_status_t  - SGX_SUCCESS or failure as defined sgx_error.h
*   Inputs: uint8_t *p_src - Pointer to input stream to be hashed
*           uint32_t src_len - Length of input stream to be hashed
*   Output: sgx_sha256_hash_t *p_hash - Resultant hash from operation */
void sgx_sha256_msg(char *p_hash)
{
   const char *src = "hello world!"; 

	    /* generates digest of p_src */
   SHA256(src, 12, p_hash);

}

void sha256(){
    unsigned char md[33];  
    SHA256((const unsigned char *)"hello,sgx!", strlen("hello,sgx!"), md);  
      
    int i = 0;  
    char buf[65] = {0};  
    char tmp[3] = {0};  
    for(i = 0; i < 32; i++ )  
    {  
        printf("%02x,",md[i]);
        // sprintf(tmp,"%02X", md[i]);  
        // strcat(buf, tmp);  
    }  
    printf("%s\n",buf);
}

void testarr(){
    int arr[32];
    for(int i=0; i<32; i++){
        printf("%d,", arr[0]);
    }
}


int main() {

    sha256();
    printf("==================\n");
    testarr();

    char* phash[32];
    sgx_sha256_msg(phash);
    printf("\n");
    for(int i=0; i<32; i++){
       printf("%02x",phash[i]); 
    }
    printf("\n");
    const char *data = "Hello, World!";


    unsigned char hash[SHA256_DIGEST_LENGTH];


    compute_sha256_hash(data, hash);


    // printf("SHA-256 hash of '%s': ", data);


    // print_hash(hash, SHA256_DIGEST_LENGTH);
    printf("\n");

    char* str = "hello";
    // printf("%c,", *str);
    // printf("%c,", *(str+1));
    // printf("%c,", *(str+2));
    // printf("%02x,", &str[2]);
    // for(int i=0; i<5; i++){
    //     printf("%c\n", str[i]);
    // }


    return 0;


}


