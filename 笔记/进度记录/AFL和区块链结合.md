## AFL和区块链结合

![image-20240830172256224](C:\Users\21874\AppData\Roaming\Typora\typora-user-images\image-20240830172256224.png)

计划：把handle_timeout中的代码放到enclave中，不需要入参（？？？），只涉及到对testfile1的处理

难题：是把AFL的全部代码都放入SGX的untrusted application部分吗？还是AFL调用SGX的application，然后再调ECALL

切割执行窗口实现方法：每次执行窗口结束，处理完本次的路径后，把testfile1清空

想法：执行窗口功能是否可以不在afl内部，而是放到区块链程序中进行监管，这样的话就不会影响afl原先的定时任务。

有可能会遭受怎样的攻击？
保证矿工一定会执行挂起的操作，也就是执行窗口结束后计算hash的部分，防止并没有被挂起

两个方式：（先执行方法一）
方法一：执行窗口由afl把控，即由afl中的定时器处理定时，计算每个窗口的hash值，通过shell脚本传给区块链
afl中直接调用enclave编译好的app试试呢？

方法二：由区块链把控afl执行窗口的时间，afl向区块链传递子进程id，运行到while()那边时，向区块链发送信号，开始计时。执行窗口时间到后，区块链立即暂停afl子进程，并发送自定义信号给afl进程，让afl计算每个窗口的hash
窗口的hash值（这块放到enclave中）
pro：java怎么调enclave？通过JNI

进程间通信方式：管道、信号、消息队列、共享内存。
如下图，区块链调用afl的shell脚本，再进行afl-fuzz的工作。区块链暂停的是forkserver的进程，afl主进程并不会被暂停。接着，区块链向
afl主进程发送自定义信号，afl接受到后调用enclave中的函数，计算hash值并签名。

![](C:\Users\21874\AppData\Roaming\Typora\typora-user-images\image-20240912211924001.png)





