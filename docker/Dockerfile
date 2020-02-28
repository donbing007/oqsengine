FROM registry.cn-hangzhou.aliyuncs.com/xforceplus/image-javabase:0.2.1
LABEL maintainer="wangzheng<wangzheng@xforceplus.com>"
COPY app.jar /xplat-meta-oqsengine-boot.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /xplat-meta-oqsengine-boot.jar
