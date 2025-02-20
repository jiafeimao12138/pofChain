package com.example.net.conf;

public class EnclaveInterface {
    static {
        System.loadLibrary("sgx_app");  // 加载 libsgx_bridge.so
    }

    // 声明 native 方法，由 JNI 实现
    public native String callEnclave(String input);


    public static void main(String[] args) {
        EnclaveInterface enclave = new EnclaveInterface();
        String s = enclave.callEnclave("resume");
        System.out.println(s);
//        System.out.println(System.getProperty("java.library.path"));
    }
}
