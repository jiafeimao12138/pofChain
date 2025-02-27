# 使用基础镜像
FROM ubuntu:22.04

# 设置环境变量以避免交互式提示
# ENV DEBIAN_FRONTEND=noninteractive

# 安装必要的工具和依赖
RUN apt-get update && apt-get install -y \
    build-essential \
    clang \
    llvm \
    openjdk-8-jdk \
    wget \
    iputils-ping \
    locales \
    cmake \
    libssl-dev \
    python3 \
    python3-pip \
    

# 下载并安装 Intel SGX SDK（适用于 Simulation Mode）
RUN wget https://download.01.org/intel-sgx/latest/linux-latest/distro/ubuntu22.04-server/sgx_linux_x64_sdk_2.25.100.3.bin && \
    chmod +x sgx_linux_x64_sdk_2.25.100.3.bin && \
    echo -e "no\n/opt/intel" | ./sgx_linux_x64_sdk_2.25.100.3.bin && \
    rm -f sgx_linux_x64_sdk_2.25.100.3.bin
    

# 设置 JAVA_HOME 环境变量
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64 
ENV PATH $JAVA_HOME/bin:$PATH 
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8
    
# 设置 SGX SDK 目录
ENV SGX_SDK=/opt/intel/sgx-sdk
ENV PATH=$PATH:$SGX_SDK/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SGX_SDK/lib64

# 启用 SGX SDK 环境
RUN echo "source $SGX_SDK/environment" >> ~/.bashrc

# 设置工作目录
WORKDIR /app


# 复制你的目标应用程序到容器中
COPY . ./pofChain
COPY core_pattern /proc/sys/kernel/core_pattern

RUN locale-gen zh_CN.UTF-8
RUN cd pofChain/AFL && \
    cp afl-fuzz /usr/local/bin/ && \
    cp afl-gcc /usr/local/bin/


# 设置容器启动时的默认命令
#CMD ["afl-fuzz", "-i", "input_dir", "-o", "output_dir", "./your_target_program"]
