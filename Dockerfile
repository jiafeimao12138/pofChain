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
    sudo \
    vim \
    iputils-ping \
    locales


# 设置 JAVA_HOME 环境变量
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64 
ENV PATH $JAVA_HOME/bin:$PATH 
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8

# 设置工作目录
WORKDIR /app


# 复制你的目标应用程序到容器中
COPY pofChain ./pofChain
COPY core_pattern /proc/sys/kernel/core_pattern

RUN locale-gen zh_CN.UTF-8
RUN cd pofChain/AFL && \
    cp afl-fuzz /usr/local/bin/ && \
    cp afl-gcc /usr/local/bin/


# 设置容器启动时的默认命令
#CMD ["afl-fuzz", "-i", "input_dir", "-o", "output_dir", "./your_target_program"]
