FROM registry.cn-hangzhou.aliyuncs.com/xforceplus/image-javabase:0.2.1
LABEL maintainer="wangzheng@xforceplus.com"
LABEL maintainer="dongbin@xxforceplus.com"

RUN mkdir /xplat
WORKDIR /xplat

COPY app.jar /xplat/xplat-meta-oqsengine.jar

# grpc
EXPOSE 8081
# web
EXPOSE 8086

ENTRYPOINT exec java $JAVA_OPTS -jar /xplat/xplat-meta-oqsengine.jar
