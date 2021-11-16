package com.xforceplus.ultraman.oqsengine.testcontainer.enums;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public enum ContainerSupport {
    UNKNOWN(0),
    REDIS(1),
    MYSQL(2),
    MANTICORE(3),
    CANAL(4);

    int category;

    ContainerSupport(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }
}
