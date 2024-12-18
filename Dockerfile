#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ARG java_image_tag=17-jammy

FROM eclipse-temurin:${java_image_tag}

ARG spark_uid=185

# Before building the docker image, first build and make a Spark distribution following
# the instructions in https://spark.apache.org/docs/latest/building-spark.html.
# If this docker file is being used in the context of building your images from a Spark
# distribution, the docker build command should be invoked from the top level directory
# of the Spark distribution. E.g.:
# docker build -t spark:latest -f kubernetes/dockerfiles/spark/Dockerfile .

RUN set -ex && \
    apt-get update && \
    ln -s /lib /lib64 && \
    apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps net-tools && \
    mkdir -p /opt/spark && \
    mkdir -p /opt/spark/examples && \
    mkdir -p /opt/spark/work-dir && \
    touch /opt/spark/RELEASE && \
    rm /bin/sh && \
    ln -sv /bin/bash /bin/sh && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/*

WORKDIR /opt

# 下载 obsutil 并解压
RUN wget https://obs-community.obs.cn-north-1.myhuaweicloud.com/obsutil/current/obsutil_linux_amd64.tar.gz && \
    tar -xzvf obsutil_linux_amd64.tar.gz
RUN rm ./obsutil_linux_amd64.tar.gz

# 将 obsutil 所在目录添加到 PATH 环境变量

RUN chmod +rx /opt/obsutil_linux_amd64_*/
RUN mv /opt/obsutil_linux_amd64_* /opt/obsutil_linux_amd64
RUN chmod 755 /opt/obsutil_linux_amd64/obsutil
ENV PATH "/opt/obsutil_linux_amd64:${PATH}"
RUN obsutil config -i=2DO4D8KEXYPY72KXENP5 -k=mmnHKOhKQQlmuOWe8tLzdUzQM0K8lVT00WQgoBSX -e=obs.cn-south-1.myhuaweicloud.com

RUN obsutil cp obs://donson-mip-data-warehouse/dev/liudu/jar/hadoop-3.1.1.tar.gz ./ && \
    tar -xzvf hadoop-3.1.1.tar.gz
RUN rm ./hadoop-3.1.1.tar.gz
ENV HADOOP_HOME=/opt/hadoop-3.1.1
ENV HADOOP_CLASSPATH=$HADOOP_HOME/etc/hadoop:$HADOOP_HOME/share/hadoop/common/lib/*:$HADOOP_HOME/share/hadoop/common/*:$HADOOP_HOME/share/hadoop/hdfs:$HADOOP_HOME/share/hadoop/hdfs/lib/*:$HADOOP_HOME/share/hadoop/hdfs/*:$HADOOP_HOME/share/hadoop/mapreduce/lib/*:$HADOOP_HOME/share/hadoop/mapreduce/*:$HADOOP_HOME/share/hadoop/yarn:$HADOOP_HOME/share/hadoop/yarn/lib/*:$HADOOP_HOME/share/hadoop/yarn/*
ENV PATH=$HADOOP_HOME/bin:$PATH
RUN obsutil cp obs://donson-mip-data-warehouse/dev/liudu/jar/hadoop-huaweicloud-3.1.1-hw-45.jar $HADOOP_HOME/share/hadoop/tools/lib/
RUN obsutil cp obs://donson-mip-data-warehouse/dev/liudu/jar/hadoop-huaweicloud-3.1.1-hw-45.jar $HADOOP_HOME/share/hadoop/common/lib/

COPY kubernetes/dockerfiles/spark/core-site.xml $HADOOP_HOME/etc/hadoop/

COPY jars /opt/spark/jars
# Copy RELEASE file if exists
COPY RELEAS[E] /opt/spark/RELEASE
COPY bin /opt/spark/bin
COPY sbin /opt/spark/sbin
COPY kubernetes/dockerfiles/spark/entrypoint.sh /opt/
COPY kubernetes/dockerfiles/spark/decom.sh /opt/
COPY examples /opt/spark/examples
COPY kubernetes/tests /opt/spark/tests
COPY data /opt/spark/data

ENV SPARK_HOME /opt/spark
ENV PATH=$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH
ENV SPARK_DIST_CLASSPATH=$HADOOP_CLASSPATH

WORKDIR /opt/spark/work-dir
RUN chmod g+w /opt/spark/work-dir
RUN chmod a+x /opt/decom.sh

ENTRYPOINT [ "/opt/entrypoint.sh" ]

# Specify the User that the actual main process will run as
USER ${spark_uid}
