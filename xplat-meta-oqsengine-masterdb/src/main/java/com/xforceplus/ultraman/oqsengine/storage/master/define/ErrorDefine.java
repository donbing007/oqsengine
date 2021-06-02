package com.xforceplus.ultraman.oqsengine.storage.master.define;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ErrorDefine {
    private ErrorDefine() {
    }

    /**
     * 维护标示.
     */
    public static final String MAINTAIN_ID = "maintainid";

    /**
     * 数据标识.
     */
    public static final String ID = "id";

    /**
     * entityClassId.
     */
    public static final String ENTITY = "entity";

    /**
     * errors.
     */
    public static final String ERRORS = "errors";

    /**
     * executeTime.
     */
    public static final String EXECUTE_TIME = "executetime";

    /**
     * fixedTime.
     */
    public static final String FIXED_TIME = "fixedtime";

    /**
     * status.
     */
    public static final String STATUS = "status";

    public static final Long DEFAULT_LIMITS = 1000L;
}
