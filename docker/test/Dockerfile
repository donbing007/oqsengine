FROM mysql:5.7.31 as builder
RUN ["sed", "-i", "s/exec \"$@\"/echo \"not running $@\"/", "/usr/local/bin/docker-entrypoint.sh"]
ENV MYSQL_ROOT_PASSWORD=xplat
COPY oqsengine-init.sql /docker-entrypoint-initdb.d/

RUN ["/usr/local/bin/docker-entrypoint.sh", "mysqld", "--datadir", "/initialized-db"]
FROM mysql:5.7.31
ENV MYSQL_ROOT_PASSWORD=xplat
COPY --from=builder /initialized-db /var/lib/mysql
