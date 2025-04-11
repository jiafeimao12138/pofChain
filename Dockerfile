# 使用基础镜像
FROM ubuntu:22.04

# 设置环境变量以避免交互式提示
# ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

COPY . /app/pofChain

# 安装必要的工具和依赖
RUN apt-get update && apt-get install -y \
    build-essential \
    locales \
    openjdk-8-jdk \
    cmake


# 下载并安装 Intel SGX SDK（适用于 Simulation Mode）
RUN chmod +x pofChain/sgx_linux_x64_sdk_2.25.100.3.bin && \
    echo -e "no\n/opt/intel" | ./pofChain/sgx_linux_x64_sdk_2.25.100.3.bin && \
    rm -f pofChain/sgx_linux_x64_sdk_2.25.100.3.bin
    
#RUN chmod 777 sgx_linux_x64_sdk_2.25.100.3.bin \
#    && ./sgx_linux_x64_sdk_2.25.100.3.bin --prefix=/opt/intel --silent
    
# 设置 JAVA_HOME 环境变量
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 
ENV PATH=$JAVA_HOME/bin:$PATH 
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8
    
# 设置 SGX SDK 目录
ENV SGX_SDK=/opt/intel/sgx-sdk
ENV PATH=$PATH:$SGX_SDK/bin

# 启用 SGX SDK 环境
# RUN echo "source $SGX_SDK/environment" >> ~/.bashrc

RUN locale-gen zh_CN.UTF-8
RUN cd pofChain/AFL && \
    cp afl-fuzz /usr/local/bin/ && \
    cp afl-gcc /usr/local/bin/


# 设置容器启动时的默认命令
#CMD ["afl-fuzz", "-i", "input_dir", "-o", "output_dir", "./your_target_program"]
